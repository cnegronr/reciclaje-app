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
import { useToast, useConfirm } from './context/FeedbackContext';

const AdminPanelScreen = React.lazy(() => import('./components/admin/AdminPanelScreen'));

export function App() {
  const { showSuccess, showError, showWarning } = useToast();
  const confirm = useConfirm();
  const [currentUser, setCurrentUser] = useState(null);
  const [comunas, setComunas] = useState([]);
  const [selectedComunaId, setSelectedComunaId] = useState('');
  const [inspeccionSemanal, setInspeccionSemanal] = useState(null);
  const [activeModalContenedor, setActiveModalContenedor] = useState(null);
  const [loadingComunas, setLoadingComunas] = useState(true);
  const [activeView, setActiveView] = useState('inspection'); // 'inspection' | 'admin'
  const [logoutMessage, setLogoutMessage] = useState('');

  const handleLogout = () => {
    authService.logout();
    setCurrentUser(null);
    setActiveView('inspection');
  };

  useEffect(() => {
    const handleForceLogout = () => {
      handleLogout();
    };
    window.addEventListener('reciclaje:force-logout', handleForceLogout);
    return () => window.removeEventListener('reciclaje:force-logout', handleForceLogout);
  }, []);

  // Monitoreo proactivo del estado de la sesión (para invalidar inmediatamente si un administrador actualizó el email, contraseña o desactivó el usuario)
  useEffect(() => {
    if (!currentUser) return;

    let isHandled = false;

    const verifySession = async () => {
      if (isHandled) return;
      const status = await authService.checkSessionStatus();
      if (!status.active && !isHandled) {
        isHandled = true;
        let bannerText = '';
        if (status.emailUpdated && status.passwordUpdated) {
          bannerText = 'Tu correo electrónico y contraseña han sido actualizados por un administrador. Por favor, inicia sesión con tus nuevas credenciales.';
        } else if (status.passwordUpdated) {
          bannerText = 'Tu contraseña ha sido actualizada por un administrador. Por favor, inicia sesión con tu nueva contraseña.';
        } else if (status.emailUpdated) {
          bannerText = 'Tu correo electrónico ha sido actualizado por un administrador. Por favor, inicia sesión con tu nuevo correo electrónico.';
        } else if (status.deactivated) {
          bannerText = 'Tu cuenta ha sido desactivada por un administrador. Para reactivar tu acceso, contacta a la administración del sistema.';
        } else if (status.message) {
          bannerText = status.message;
        }
        if (bannerText) {
          setLogoutMessage(bannerText);
          showWarning(bannerText);
        }
        handleLogout();
      }
    };

    // 1. Verificar inmediatamente ante errores de autorización (401 / 403) en cualquier llamada HTTP
    const handleAuthError = () => {
      verifySession();
    };
    window.addEventListener('reciclaje:auth-error', handleAuthError);

    // 2. Verificar periódicamente cada 3 segundos
    const intervalId = setInterval(verifySession, 3000);

    // 3. Verificar inmediatamente al cambiar de pestaña o volver a enfocar la ventana
    const handleVisibilityOrFocus = () => {
      if (document.visibilityState === 'visible') {
        verifySession();
      }
    };
    window.addEventListener('visibilitychange', handleVisibilityOrFocus);
    window.addEventListener('focus', handleVisibilityOrFocus);

    return () => {
      clearInterval(intervalId);
      window.removeEventListener('reciclaje:auth-error', handleAuthError);
      window.removeEventListener('visibilitychange', handleVisibilityOrFocus);
      window.removeEventListener('focus', handleVisibilityOrFocus);
    };
  }, [currentUser]);

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

    const cargarComunasData = async (uId) => {
      setLoadingComunas(true);
      const dataComunas = await comunaService.obtenerComunas(uId);
      setComunas(dataComunas);
      if (dataComunas && dataComunas.length > 0) {
        setSelectedComunaId(dataComunas[0].id);
      }
      setLoadingComunas(false);
    };

    if (currentUser) {
      cargarComunasData(currentUser.id);
    } else {
      cargarComunasData(null);
    }
  }, [currentUser?.id]);

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
    return (
      <LoginScreen
        onLoginSuccess={(user) => {
          setLogoutMessage('');
          setCurrentUser(user);
        }}
        logoutMessage={logoutMessage}
        onClearLogoutMessage={() => setLogoutMessage('')}
      />
    );
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
          onLogout={handleLogout}
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
      const ok = await confirm({
        title: 'Contenedores Pendientes',
        message: `Aún quedan ${pendientesCount} contenedores pendientes en ${selectedComuna.nombre}. ¿Deseas marcar la ruta como completada de todas formas?`,
        confirmText: 'Completar Ruta',
        type: 'warning'
      });
      if (!ok) return;
    }
    try {
      const updatedRecord = await inspectionService.finalizarRutaSemanal(
        selectedComunaId,
        currentUser.id,
        selectedComuna.backendId
      );
      setInspeccionSemanal({ ...updatedRecord });
      showSuccess('¡Ruta semanal finalizada exitosamente!');
    } catch (err) {
      showError(err.message || 'Error al finalizar ruta semanal');
    }
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
      showError(err.message || 'Error al obtener resumen de traspaso');
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
      showSuccess('Inspecciones de la semana previa traspasadas exitosamente.');
    } catch (err) {
      showError(err.message || 'Error al traspasar inspecciones');
    } finally {
      setLoadingTraspaso(false);
    }
  };

  const handleLimpiarSemanaActual = async () => {
    const ok = await confirm({
      title: 'Limpiar Semana Actual',
      message: `¿Estás seguro de que deseas limpiar todas las inspecciones de la semana actual en ${selectedComuna.nombre}?\n\nSe creará un respaldo automático que podrás revertir en cualquier momento.`,
      confirmText: 'Limpiar Semana',
      type: 'danger'
    });
    if (!ok) return;

    try {
      setLoadingLimpieza(true);
      await inspectionService.limpiarSemanaActual(
        selectedComunaId,
        currentUser.id,
        selectedComuna.backendId
      );
      await reloadInspeccion();
      showSuccess('Semana actual limpiada exitosamente. Se guardó un respaldo para revertir si lo requieres.');
    } catch (err) {
      showError(err.message || 'Error al limpiar la semana actual');
    } finally {
      setLoadingLimpieza(false);
    }
  };

  const handleRevertirLimpieza = async () => {
    const ok = await confirm({
      title: 'Restaurar Estado Anterior',
      message: '¿Deseas deshacer la última acción (limpieza o traspaso) y restaurar el estado anterior de las inspecciones y fotos?',
      confirmText: 'Restaurar Respaldo',
      type: 'warning'
    });
    if (!ok) return;

    try {
      setLoadingLimpieza(true);
      await inspectionService.revertirLimpieza(
        selectedComunaId,
        currentUser.id,
        selectedComuna.backendId
      );
      await reloadInspeccion();
      showSuccess('Estado de la semana restaurado exitosamente desde el respaldo.');
    } catch (err) {
      showError(err.message || 'Error al revertir la última acción');
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
        onLogout={handleLogout}
        activeView={activeView}
        onChangeView={(view) => setActiveView(view)}
      />

      {activeView === 'admin' && (currentUser?.rol === 'ADMIN' || currentUser?.rol === 'REPORTERIA') ? (
        <React.Suspense fallback={<div className="p-4 text-center">Cargando Panel Admin...</div>}>
          <AdminPanelScreen onLogout={handleLogout} />
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
                {currentUser?.rol === 'ADMIN' && (
                  <button
                    type="button"
                    onClick={handleAbrirTraspasoModal}
                    disabled={loadingTraspaso || loadingLimpieza}
                    className="action-btn action-btn-edit"
                    style={{ background: 'rgba(20, 184, 166, 0.15)', color: '#2dd4bf', border: '1px solid rgba(45, 212, 191, 0.3)' }}
                  >
                    📋 Traspasar Visitas Previas
                  </button>
                )}

                <button
                  type="button"
                  onClick={handleLimpiarSemanaActual}
                  disabled={loadingLimpieza || visitadosCount === 0}
                  className="action-btn action-btn-delete"
                  style={{ opacity: loadingLimpieza || visitadosCount === 0 ? 0.5 : 1 }}
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
                    ⏪ Deshacer ÚLTIMA Acción
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
