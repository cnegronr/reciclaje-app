import React, { useState, useEffect } from 'react';
import { authService } from './services/authService';
import { comunaService } from './services/comunaService';
import { inspectionService } from './services/inspectionService';
import { LoginScreen } from './components/LoginScreen';
import { Header } from './components/Header';
import { MapView } from './components/MapView';
import { ContainerCard } from './components/ContainerCard';
import { InspectionModal } from './components/InspectionModal';
import TraspasoVisitasModal from './components/admin/TraspasoVisitasModal';

export function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [comunas, setComunas] = useState([]);
  const [selectedComunaId, setSelectedComunaId] = useState('');
  const [inspeccionSemanal, setInspeccionSemanal] = useState(null);
  const [activeModalContenedor, setActiveModalContenedor] = useState(null);
  const [loadingComunas, setLoadingComunas] = useState(true);
  const [activeView, setActiveView] = useState('inspection'); // 'inspection' | 'admin'

  // Estados para Traspaso de Visitas y Limpieza con Respaldo
  const [isTraspasoModalOpen, setIsTraspasoModalOpen] = useState(false);
  const [traspasoPreviewData, setTraspasoPreviewData] = useState(null);
  const [loadingTraspaso, setLoadingTraspaso] = useState(false);
  const [loadingLimpieza, setLoadingLimpieza] = useState(false);

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
  const reloadInspeccion = async () => {
    if (currentUser && selectedComunaId) {
      const selectedComuna = comunas.find((c) => c.id === selectedComunaId);
      const backendComunaId = selectedComuna?.backendId || null;
      const record = await inspectionService.getInspeccionSemanal(
        selectedComunaId,
        currentUser.id,
        backendComunaId
      );
      setInspeccionSemanal(record);
    }
  };

  useEffect(() => {
    reloadInspeccion();
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

  // Agrupar contenedores por sector manteniendo el orden
  const groupedContenedores = (selectedComuna.contenedores || []).reduce((acc, contenedor) => {
    const secName = contenedor.sector || 'Sin Sector';
    if (!acc[secName]) {
      acc[secName] = [];
    }
    acc[secName].push(contenedor);
    return acc;
  }, {});

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

  const handleAbrirTraspasoModal = async () => {
    try {
      setLoadingTraspaso(true);
      const preview = await inspectionService.getPreviewTraspaso(
        selectedComunaId,
        currentUser.id,
        selectedComuna.backendId
      );
      setTraspasoPreviewData(preview);
      setIsTraspasoModalOpen(true);
    } catch (err) {
      alert(err.message || 'Error al obtener resumen de traspaso');
    } finally {
      setLoadingTraspaso(false);
    }
  };

  const handleConfirmarTraspaso = async () => {
    try {
      setLoadingTraspaso(true);
      await inspectionService.aplicarTraspaso(
        selectedComunaId,
        currentUser.id,
        selectedComuna.backendId
      );
      await reloadInspeccion();
      setIsTraspasoModalOpen(false);
      alert('✅ Inspecciones de la semana previa traspasadas exitosamente.');
    } catch (err) {
      alert(err.message || 'Error al traspasar inspecciones');
    } finally {
      setLoadingTraspaso(false);
    }
  };

  const handleLimpiarSemanaActual = async () => {
    if (!window.confirm(`⚠️ ¿Estás seguro de que deseas limpiar todas las inspecciones de la semana actual en ${selectedComuna.nombre}?\n\nSe creará un respaldo automático que podrás revertir en cualquier momento.`)) {
      return;
    }
    try {
      setLoadingLimpieza(true);
      await inspectionService.limpiarSemanaActual(
        selectedComunaId,
        currentUser.id,
        selectedComuna.backendId
      );
      await reloadInspeccion();
      alert('🧹 Semana actual limpiada exitosamente. Se guardó un respaldo para revertir si lo requieres.');
    } catch (err) {
      alert(err.message || 'Error al limpiar la semana actual');
    } finally {
      setLoadingLimpieza(false);
    }
  };

  const handleRevertirLimpieza = async () => {
    if (!window.confirm(`⏪ ¿Deseas deshacer la última limpieza realizada y restaurar el estado anterior de las inspecciones y fotos?`)) {
      return;
    }
    try {
      setLoadingLimpieza(true);
      await inspectionService.revertirLimpieza(
        selectedComunaId,
        currentUser.id,
        selectedComuna.backendId
      );
      await reloadInspeccion();
      alert('⏪ Estado de la semana restaurado exitosamente desde el respaldo.');
    } catch (err) {
      alert(err.message || 'Error al revertir la limpieza');
    } finally {
      setLoadingLimpieza(false);
    }
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
        activeView={activeView}
        onChangeView={(view) => setActiveView(view)}
      />

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
            <div className="section-header-bar flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 className="section-title">📦 Contenedores en {selectedComuna.nombre}</h2>
                <p className="section-subtitle">Categorías: EMPRESA (Máx 500kg) | MUNICIPAL (Máx 1000kg)</p>
              </div>

              <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', alignItems: 'center' }}>
                <button
                  type="button"
                  onClick={handleAbrirTraspasoModal}
                  disabled={loadingTraspaso || loadingLimpieza}
                  className="action-btn action-btn-edit"
                  style={{ background: 'rgba(20, 184, 166, 0.15)', color: '#2dd4bf', border: '1px solid rgba(45, 212, 191, 0.3)' }}
                >
                  📋 Traspasar Visitas Previas
                </button>

                <button
                  type="button"
                  onClick={handleLimpiarSemanaActual}
                  disabled={loadingLimpieza}
                  className="action-btn action-btn-delete"
                >
                  🧹 Limpiar Semana Actual
                </button>

                {inspeccionSemanal?.tieneRespaldoLimpieza && (
                  <button
                    type="button"
                    onClick={handleRevertirLimpieza}
                    disabled={loadingLimpieza}
                    className="action-btn action-btn-primary"
                    style={{ background: 'rgba(168, 85, 247, 0.2)', color: '#c084fc', border: '1px solid rgba(192, 132, 252, 0.4)' }}
                  >
                    ⏪ Revertir Limpieza
                  </button>
                )}

                {inspeccionSemanal?.estado !== 'FINALIZADO' ? (
                  <button onClick={handleFinalizarRuta} className="finish-route-btn">
                    🏁 Confirmar y Finalizar Ruta Semanal
                  </button>
                ) : (
                  <span className="route-completed-badge">🔒 Ruta Confirmada</span>
                )}
              </div>
            </div>

            {Object.entries(groupedContenedores).map(([sectorName, items]) => (
              <div key={sectorName} className="sector-group-block" style={{ marginBottom: '2.5rem' }}>
                <div
                  className="sector-header-banner"
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '0.75rem 1.25rem',
                    margin: '1.25rem 0 1rem 0',
                    borderRadius: '8px',
                    background: 'rgba(59, 130, 246, 0.1)',
                    borderLeft: '4px solid #3b82f6',
                    color: 'var(--text-main, #1e293b)'
                  }}
                >
                  <h3 style={{ margin: 0, fontSize: '1.15rem', fontWeight: '600' }}>
                    📍 Sector: {sectorName}
                  </h3>
                  <span style={{ fontSize: '0.9rem', fontWeight: '500', opacity: 0.85 }}>
                    {items.length} {items.length === 1 ? 'contenedor' : 'contenedores'}
                  </span>
                </div>

                <div className="containers-grid">
                  {items.map((contenedor) => (
                    <ContainerCard
                      key={contenedor.id}
                      contenedor={contenedor}
                      detalleInspeccion={detallesMap[contenedor.id]}
                      onInspect={(c) => setActiveModalContenedor(c)}
                    />
                  ))}
                </div>
              </div>
            ))}
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

      {/* MODAL DE TRASPASO DE VISITAS DE SEMANA PREVIA */}
      <TraspasoVisitasModal
        isOpen={isTraspasoModalOpen}
        onClose={() => setIsTraspasoModalOpen(false)}
        previewData={traspasoPreviewData}
        loading={loadingTraspaso}
        onConfirmTraspaso={handleConfirmarTraspaso}
      />
    </div>
  );
}

export default App;
