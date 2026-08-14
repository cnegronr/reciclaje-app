import React, { useState, useEffect } from 'react';
import { authService } from './services/authService';
import { comunaService } from './services/comunaService';
import { inspectionService } from './services/inspectionService';
import { LoginScreen } from './components/LoginScreen';
import { Header } from './components/Header';
import { MapView } from './components/MapView';
import { ContainerCard } from './components/ContainerCard';
import { InspectionModal } from './components/InspectionModal';

export function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [comunas, setComunas] = useState([]);
  const [selectedComunaId, setSelectedComunaId] = useState('');
  const [inspeccionSemanal, setInspeccionSemanal] = useState(null);
  const [activeModalContenedor, setActiveModalContenedor] = useState(null);
  const [loadingComunas, setLoadingComunas] = useState(true);
  const [activeView, setActiveView] = useState('inspection'); // 'inspection' | 'admin'

  // Inicializar autenticación y cargar comunas desde el backend Spring Boot
  useEffect(() => {
    const user = authService.getCurrentUser();
    if (user) {
      setCurrentUser(user);
    }

    const cargarComunasData = async () => {
      setLoadingComunas(true);
      const dataComunas = await comunaService.obtenerComunas();
      setComunas(dataComunas);
      if (dataComunas && dataComunas.length > 0) {
        setSelectedComunaId(dataComunas[0].id);
      }
      setLoadingComunas(false);
    };

    cargarComunasData();
  }, []);

  // Cargar registro de inspección semanal desde PostgreSQL cuando cambie la comuna o usuario
  useEffect(() => {
    if (currentUser && selectedComunaId) {
      const selectedComuna = comunas.find((c) => c.id === selectedComunaId);
      const backendComunaId = selectedComuna?.backendId || null;

      const cargarInspeccion = async () => {
        const record = await inspectionService.getInspeccionSemanal(
          selectedComunaId,
          currentUser.id,
          backendComunaId
        );
        setInspeccionSemanal(record);
      };

      cargarInspeccion();
    }
  }, [selectedComunaId, currentUser, comunas]);

  if (!currentUser) {
    return <LoginScreen onLoginSuccess={(user) => setCurrentUser(user)} />;
  }

  const selectedComuna = comunas.find((c) => c.id === selectedComunaId) || comunas[0] || null;

  if (loadingComunas || !selectedComuna) {
    return (
      <div className="app-main-layout">
        <Header
          user={currentUser}
          comunas={comunas}
          selectedComunaId={selectedComunaId}
          onSelectComuna={(id) => setSelectedComunaId(id)}
          onLogout={() => {
            authService.logout();
            setCurrentUser(null);
          }}
        />
        <main className="main-content-container" style={{ textAlign: 'center', padding: '4rem 1rem' }}>
          <h2>⏳ Cargando comunas y puntos de reciclaje desde PostgreSQL...</h2>
        </main>
      </div>
    );
  }

  const detallesMap = inspeccionSemanal?.detalles || {};

  // Estadísticas de la ruta semanal
  const totalContenedores = selectedComuna.contenedores ? selectedComuna.contenedores.length : 0;
  const visitadosCount = Object.values(detallesMap).filter((d) => d.visitado).length;
  const pendientesCount = totalContenedores - visitadosCount;

  const totalKilos = Object.values(detallesMap)
    .filter((d) => d.visitado)
    .reduce((sum, d) => sum + (d.kilosCalculados || 0), 0);

  const handleSaveInspection = async (contenedorId, inspectionData, isEditing) => {
    const backendContenedorId = activeModalContenedor?.backendId || contenedorId;

    const updatedRecord = await inspectionService.saveDetalleInspeccion(
      selectedComunaId,
      currentUser.id,
      backendContenedorId,
      inspectionData,
      isEditing,
      selectedComuna.backendId
    );
    setInspeccionSemanal({ ...updatedRecord });
    setActiveModalContenedor(null);
  };

  const handleFinalizarRuta = async () => {
    if (pendientesCount > 0) {
      if (!confirm(`⚠️ Aún quedan ${pendientesCount} contenedores pendientes en ${selectedComuna.nombre}. ¿Deseas marcar la ruta como completada de todas formas?`)) {
        return;
      }
    }
    const updatedRecord = await inspectionService.finalizarRutaSemanal(
      selectedComunaId,
      currentUser.id,
      selectedComuna.backendId
    );
    setInspeccionSemanal({ ...updatedRecord });
    alert('✅ ¡Ruta semanal finalizada exitosamente!');
  };

  return (
    <div className="app-main-layout">
      <Header
        user={currentUser}
        comunas={comunas}
        selectedComunaId={selectedComunaId}
        onSelectComuna={(id) => setSelectedComunaId(id)}
        onLogout={() => {
          authService.logout();
          setCurrentUser(null);
        }}
      />

      {currentUser?.rol === 'ADMIN' && (
        <div style={{ background: '#eef2f6', borderBottom: '1px solid #cfd8dc', padding: '0.5rem 1rem', display: 'flex', gap: '8px', justifyContent: 'center' }}>
          <button
            className={`btn ${activeView === 'inspection' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveView('inspection')}
          >
            📋 Módulo Inspección (Vista Inspector)
          </button>
          <button
            className={`btn ${activeView === 'admin' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveView('admin')}
          >
            ⚙️ Panel de Administración (ADMIN)
          </button>
        </div>
      )}

      {activeView === 'admin' && currentUser?.rol === 'ADMIN' ? (
        <React.Suspense fallback={<div className="p-4 text-center">Cargando Panel Admin...</div>}>
          {React.createElement(React.lazy(() => import('./components/admin/AdminPanelScreen')))}
        </React.Suspense>
      ) : (
        <main className="main-content-container">
          {/* BARRA DE ESTADÍSTICAS E INDICADORES DE RUTA */}
          <section className="stats-dashboard">
            <div className="stat-card blue">
              <span className="stat-icon">📍</span>
              <div>
                <span className="stat-value">{totalContenedores}</span>
                <span className="stat-label">Puntos Totales ({selectedComuna.nombre})</span>
              </div>
            </div>

            <div className="stat-card green">
              <span className="stat-icon">✅</span>
              <div>
                <span className="stat-value">{visitadosCount} / {totalContenedores}</span>
                <span className="stat-label">Visitas Finalizadas</span>
              </div>
            </div>

            <div className="stat-card orange">
              <span className="stat-icon">⏳</span>
              <div>
                <span className="stat-value">{pendientesCount}</span>
                <span className="stat-label">Pendientes Esta Semana</span>
              </div>
            </div>

            <div className="stat-card purple">
              <span className="stat-icon">⚖️</span>
              <div>
                <span className="stat-value">{totalKilos.toFixed(1)} kg</span>
                <span className="stat-label">Carga Recolectada Estimada</span>
              </div>
            </div>
          </section>

          {/* MAPA E INDICACIONES DE GEORREFERENCIACIÓN */}
          <MapView
            contenedores={selectedComuna.contenedores || []}
            selectedContenedorId={activeModalContenedor?.id}
            onSelectContenedor={(c) => setActiveModalContenedor(c)}
          />

          {/* LISTADO DE CONTENEDORES DE LA COMUNA */}
          <section className="containers-section">
            <div className="section-header-bar">
              <div>
                <h2 className="section-title">📦 Contenedores en {selectedComuna.nombre}</h2>
                <p className="section-subtitle">Categorías: EMPRESA (Máx 500kg) | MUNICIPAL (Máx 1000kg)</p>
              </div>

              {inspeccionSemanal?.estado !== 'FINALIZADO' ? (
                <button onClick={handleFinalizarRuta} className="finish-route-btn">
                  🏁 Confirmar y Finalizar Ruta Semanal
                </button>
              ) : (
                <span className="route-completed-badge">🔒 Ruta Confirmada (Editable en modo actualización)</span>
              )}
            </div>

            <div className="containers-grid">
              {(selectedComuna.contenedores || []).map((contenedor) => (
                <ContainerCard
                  key={contenedor.id}
                  contenedor={contenedor}
                  detalleInspeccion={detallesMap[contenedor.id]}
                  onInspect={(c) => setActiveModalContenedor(c)}
                />
              ))}
            </div>
          </section>
        </main>
      )}

      {/* MODAL DE INSPECCIÓN / EDICIÓN */}
      {activeModalContenedor && (
        <InspectionModal
          contenedor={activeModalContenedor}
          detalleActual={detallesMap[activeModalContenedor.id]}
          onClose={() => setActiveModalContenedor(null)}
          onSave={handleSaveInspection}
        />
      )}
    </div>
  );
}

export default App;
