import React, { useState, useEffect } from 'react';
import { authService } from './services/authService';
import { inspectionService } from './services/inspectionService';
import { COMUNAS_DATA } from './data/mockData';
import { LoginScreen } from './components/LoginScreen';
import { Header } from './components/Header';
import { MapView } from './components/MapView';
import { ContainerCard } from './components/ContainerCard';
import { InspectionModal } from './components/InspectionModal';

export function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [selectedComunaId, setSelectedComunaId] = useState(COMUNAS_DATA[0].id);
  const [inspeccionSemanal, setInspeccionSemanal] = useState(null);
  const [activeModalContenedor, setActiveModalContenedor] = useState(null);

  // Inicializar autenticación
  useEffect(() => {
    const user = authService.getCurrentUser();
    if (user) {
      setCurrentUser(user);
    }
  }, []);

  // Cargar registro de inspección semanal cuando cambie la comuna o usuario
  useEffect(() => {
    if (currentUser) {
      const record = inspectionService.getInspeccionSemanal(selectedComunaId, currentUser.id);
      setInspeccionSemanal(record);
    }
  }, [selectedComunaId, currentUser]);

  if (!currentUser) {
    return <LoginScreen onLoginSuccess={(user) => setCurrentUser(user)} />;
  }

  const selectedComuna = COMUNAS_DATA.find((c) => c.id === selectedComunaId) || COMUNAS_DATA[0];
  const detallesMap = inspeccionSemanal?.detalles || {};

  // Estadísticas de la ruta semanal
  const totalContenedores = selectedComuna.contenedores.length;
  const visitadosCount = Object.values(detallesMap).filter((d) => d.visitado).length;
  const pendientesCount = totalContenedores - visitadosCount;

  const totalKilos = Object.values(detallesMap)
    .filter((d) => d.visitado)
    .reduce((sum, d) => sum + (d.kilosCalculados || 0), 0);

  const handleSaveInspection = (contenedorId, inspectionData, isEditing) => {
    const updatedRecord = inspectionService.saveDetalleInspeccion(
      selectedComunaId,
      currentUser.id,
      contenedorId,
      inspectionData,
      isEditing
    );
    setInspeccionSemanal({ ...updatedRecord });
    setActiveModalContenedor(null);
  };

  const handleFinalizarRuta = () => {
    if (pendientesCount > 0) {
      if (!confirm(`⚠️ Aún quedan ${pendientesCount} contenedores pendientes en ${selectedComuna.nombre}. ¿Deseas marcar la ruta como completada de todas formas?`)) {
        return;
      }
    }
    const updatedRecord = inspectionService.finalizarRutaSemanal(selectedComunaId, currentUser.id);
    setInspeccionSemanal({ ...updatedRecord });
    alert('✅ ¡Ruta semanal finalizada exitosamente!');
  };

  return (
    <div className="app-main-layout">
      <Header
        user={currentUser}
        comunas={COMUNAS_DATA}
        selectedComunaId={selectedComunaId}
        onSelectComuna={(id) => setSelectedComunaId(id)}
        onLogout={() => {
          authService.logout();
          setCurrentUser(null);
        }}
      />

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
          contenedores={selectedComuna.contenedores}
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
            {selectedComuna.contenedores.map((contenedor) => (
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
