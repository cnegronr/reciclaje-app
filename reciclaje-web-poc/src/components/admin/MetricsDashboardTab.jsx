import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { adminService } from '../../services/adminService';
import { comunaService } from '../../services/comunaService';

export default function MetricsDashboardTab() {
  const [metrics, setMetrics] = useState(null);
  const [users, setUsers] = useState([]);
  const [comunas, setComunas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [inspectionsModal, setInspectionsModal] = useState(null);

  const [filters, setFilters] = useState({
    scope: 'ALL',
    period: 'WEEK',
    userId: '',
    comunaId: '',
    role: '',
    region: ''
  });

  useEffect(() => {
    loadFiltersData();
  }, []);

  useEffect(() => {
    fetchMetrics();
  }, [filters]);

  const loadFiltersData = async () => {
    try {
      const [uData, cData] = await Promise.all([
        adminService.getUsers().catch(() => []),
        comunaService.getComunas().catch(() => [])
      ]);
      setUsers(uData || []);
      setComunas(cData || []);
    } catch (err) {
      console.error('Error cargando filtros:', err);
    }
  };

  const fetchMetrics = async () => {
    try {
      setLoading(true);
      const cleanParams = {};
      Object.keys(filters).forEach(k => {
        if (filters[k]) cleanParams[k] = filters[k];
      });
      const data = await adminService.getMetrics(cleanParams);
      setMetrics(data);
    } catch (err) {
      alert(err.message || 'Error al obtener métricas');
    } finally {
      setLoading(false);
    }
  };

  const selectedUser = users.find(u => String(u.id) === String(filters.userId));
  const isFilterInspector = filters.role === 'INSPECTOR' || selectedUser?.rol === 'INSPECTOR';
  const isFilterChofer = filters.role === 'CHOFER' || selectedUser?.rol === 'CHOFER';
  const showInspectorSection = (!filters.userId && !filters.role) || isFilterInspector;
  const showChoferSection = (!filters.userId && !filters.role) || isFilterChofer;

  return (
    <div className="metrics-dashboard">
      <div style={{ marginBottom: '1.25rem' }}>
        <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>📈 Dashboard de Métricas</h3>
        <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Resumen consolidado de inspecciones y volumen recolectado por periodo e inspector.</p>
      </div>

      {/* Controles de Filtro Simplificados */}
      <div className="filter-grid" style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '1rem', display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div style={{ flex: '1', minWidth: '180px' }}>
          <label className="field-label" style={{ marginBottom: '0.35rem', display: 'block', fontSize: '0.8rem' }}>🗓️ Período de Tiempo:</label>
          <select
            className="select-control"
            value={filters.period}
            onChange={e => setFilters({ ...filters, period: e.target.value })}
          >
            <option value="WEEK">Esta Semana</option>
            <option value="PAST_WEEK">Semana Anterior</option>
            <option value="DAY">Hoy (Día)</option>
            <option value="MONTH">Este Mes</option>
            <option value="YEAR">Este Año</option>
            <option value="HISTORIC">Histórico Total</option>
          </select>
        </div>

        <div style={{ flex: '1', minWidth: '180px' }}>
          <label className="field-label" style={{ marginBottom: '0.35rem', display: 'block', fontSize: '0.8rem' }}>👤 Inspector / Chofer:</label>
          <select
            className="select-control"
            value={filters.role ? ('ROLE_' + filters.role) : (filters.userId || '')}
            onChange={e => {
              const uVal = e.target.value;
              if (uVal === 'ROLE_INSPECTOR') {
                setFilters({
                  ...filters,
                  role: 'INSPECTOR',
                  userId: '',
                  scope: filters.comunaId ? 'COMUNA' : 'ALL'
                });
              } else if (uVal === 'ROLE_CHOFER') {
                setFilters({
                  ...filters,
                  role: 'CHOFER',
                  userId: '',
                  scope: filters.comunaId ? 'COMUNA' : 'ALL'
                });
              } else if (uVal) {
                setFilters({
                  ...filters,
                  role: '',
                  userId: uVal,
                  scope: 'INDIVIDUAL'
                });
              } else {
                setFilters({
                  ...filters,
                  role: '',
                  userId: '',
                  scope: filters.comunaId ? 'COMUNA' : 'ALL'
                });
              }
            }}
          >
            <option value="">Todos los Usuarios Activos (Inspectores y Choferes)</option>
            <option value="ROLE_INSPECTOR">Todos los Inspectores Activos</option>
            <option value="ROLE_CHOFER">Todos los Choferes Activos</option>
            {users.filter(u => u.activo && u.rol === 'INSPECTOR').length > 0 && (
              <optgroup label="Inspectores">
                {users.filter(u => u.activo && u.rol === 'INSPECTOR').map(u => (
                  <option key={u.id} value={u.id}>{u.nombre}</option>
                ))}
              </optgroup>
            )}
            {users.filter(u => u.activo && u.rol === 'CHOFER').length > 0 && (
              <optgroup label="Choferes">
                {users.filter(u => u.activo && u.rol === 'CHOFER').map(u => (
                  <option key={u.id} value={u.id}>{u.nombre}</option>
                ))}
              </optgroup>
            )}
          </select>
        </div>

        <div style={{ flex: '1', minWidth: '180px' }}>
          <label className="field-label" style={{ marginBottom: '0.35rem', display: 'block', fontSize: '0.8rem' }}>📍 Comuna:</label>
          <select
            className="select-control"
            value={filters.comunaId}
            onChange={e => {
              const cVal = e.target.value;
              setFilters({
                ...filters,
                comunaId: cVal,
                scope: cVal ? 'COMUNA' : filters.userId ? 'INDIVIDUAL' : 'ALL'
              });
            }}
          >
            <option value="">Todas las Comunas</option>
            {comunas.map(c => (
              <option key={c.id} value={c.backendId || c.id}>{c.nombre}</option>
            ))}
          </select>
        </div>
      </div>

      {loading ? (
        <div className="p-4 text-center">⏳ Calculando métricas en tiempo real...</div>
      ) : metrics ? (
        <div>
          {/* Tarjetas de Métricas Clave */}
          <div className="stats-dashboard" style={{ marginBottom: '1.5rem' }}>
            <div className="stat-card blue">
              <span className="stat-icon">👥</span>
              <div>
                <span className="stat-value">{metrics.totalUsuarios}</span>
                <span className="stat-label">Total Usuarios</span>
              </div>
            </div>

            <div className="stat-card green">
              <span className="stat-icon">📦</span>
              <div>
                <span className="stat-value">{metrics.totalContenedores}</span>
                <span className="stat-label">Total Contenedores</span>
              </div>
            </div>

            <div className="stat-card orange">
              <span className="stat-icon">📋</span>
              <div>
                <span className="stat-value">{metrics.totalInspecciones}</span>
                <span className="stat-label">Inspecciones</span>
              </div>
            </div>

            <div className="stat-card purple">
              <span className="stat-icon">⚖️</span>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', minWidth: 0 }}>
                {showInspectorSection && (
                  <div>
                    <span className="stat-value" style={{ fontSize: '1.15rem', lineHeight: 1.1 }}>
                      {metrics.totalKilosAcumulados != null ? metrics.totalKilosAcumulados : 0} kg
                    </span>
                    <span className="stat-label">Total Acumulados</span>
                  </div>
                )}
                {showChoferSection && (
                  <div style={showInspectorSection ? { borderTop: '1px solid rgba(255, 255, 255, 0.1)', paddingTop: '0.3rem' } : {}}>
                    <span className="stat-value" style={{ fontSize: '1.15rem', lineHeight: 1.1 }}>
                      {metrics.totalKilosRetirados != null ? metrics.totalKilosRetirados : 0} kg
                    </span>
                    <span className="stat-label">Total Retirados</span>
                  </div>
                )}
              </div>
            </div>

            <div className="stat-card blue">
              <span className="stat-icon">📊</span>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem', minWidth: 0 }}>
                {showInspectorSection && (
                  <div>
                    <span className="stat-value" style={{ fontSize: '1.15rem', lineHeight: 1.1 }}>
                      {metrics.promedioPorcentajeAcumulados != null ? metrics.promedioPorcentajeAcumulados : 0}%
                    </span>
                    <span className="stat-label">Promedio Acumulados</span>
                  </div>
                )}
                {showChoferSection && (
                  <div style={showInspectorSection ? { borderTop: '1px solid rgba(255, 255, 255, 0.1)', paddingTop: '0.3rem' } : {}}>
                    <span className="stat-value" style={{ fontSize: '1.15rem', lineHeight: 1.1 }}>
                      {metrics.promedioPorcentajeRetirados != null ? metrics.promedioPorcentajeRetirados : 0}%
                    </span>
                    <span className="stat-label">Promedio Retirados</span>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Desglose Consolidado por Comuna - Sección INSPECTORES */}
          {showInspectorSection && (
            <div style={{ marginBottom: '2rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.85rem' }}>
                <h4 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0, color: '#60a5fa' }}>
                  🏛️ Consolidado por Comuna - Inspectores
                </h4>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Inspecciones periódicas de ruta
                </span>
              </div>
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Comuna</th>
                      <th>Región</th>
                      <th>Inspector Asignado</th>
                      <th style={{ textAlign: 'center' }}>Contenedores</th>
                      <th style={{ textAlign: 'center' }}>Inspecciones</th>
                      <th style={{ textAlign: 'right' }}>Kilos Acumulados</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(!metrics.inspectorComunaMetrics || metrics.inspectorComunaMetrics.length === 0) ? (
                      <tr>
                        <td colSpan="6" style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>
                          No hay registros de inspecciones de inspectores para los filtros seleccionados.
                        </td>
                      </tr>
                    ) : (
                      metrics.inspectorComunaMetrics.map(im => (
                        <tr key={im.comunaId}>
                          <td style={{ fontWeight: 'bold' }}>{im.comunaNombre}</td>
                          <td>Región {im.codigoRegion}</td>
                          <td>
                            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem', fontWeight: 600 }}>
                              👤 {im.inspectorNombre || 'Sin Asignar'}
                            </span>
                          </td>
                          <td style={{ textAlign: 'center' }}>{im.totalContenedores}</td>
                          <td style={{ textAlign: 'center' }}>
                            {im.inspeccionesCompletadas > 0 ? (
                              <button
                                type="button"
                                onClick={() => setInspectionsModal({
                                  type: 'INSPECTOR',
                                  comunaNombre: im.comunaNombre,
                                  inspectorNombre: im.inspectorNombre,
                                  totalContenedores: im.totalContenedores,
                                  inspeccionesCompletadas: im.inspeccionesCompletadas,
                                  kilos: im.kilosCalculados,
                                  porcentajeLlenadoPromedio: im.porcentajeLlenadoPromedio,
                                  contenedores: im.contenedoresInspeccionados || []
                                })}
                                style={{
                                  background: 'rgba(56, 189, 248, 0.12)',
                                  color: '#38bdf8',
                                  border: '1px solid rgba(56, 189, 248, 0.3)',
                                  borderRadius: '6px',
                                  padding: '0.25rem 0.6rem',
                                  cursor: 'pointer',
                                  fontWeight: 'bold',
                                  fontSize: '0.85rem',
                                  display: 'inline-flex',
                                  alignItems: 'center',
                                  gap: '0.3rem',
                                  transition: 'all 0.2s'
                                }}
                                title="Ver detalle de contenedores inspeccionados"
                              >
                                🔍 {im.inspeccionesCompletadas} ver detalle
                              </button>
                            ) : (
                              <span style={{ color: 'var(--text-muted)' }}>0</span>
                            )}
                          </td>
                          <td style={{ textAlign: 'right', fontWeight: 'bold', color: '#34d399' }}>
                            {im.kilosCalculados} kg
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Desglose Consolidado por Comuna - Sección CHOFERES */}
          {showChoferSection && (
            <div style={{ marginBottom: '2rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.85rem' }}>
                <h4 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0, color: '#f59e0b' }}>
                  🚚 Consolidado por Comuna - Choferes
                </h4>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Retiros efectivos de material
                </span>
              </div>
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Comuna</th>
                      <th>Región</th>
                      <th style={{ textAlign: 'center' }}>Contenedores</th>
                      <th style={{ textAlign: 'center' }}>Inspecciones</th>
                      <th style={{ textAlign: 'right' }}>Kilos Retirados</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(!metrics.choferComunaMetrics || metrics.choferComunaMetrics.length === 0) ? (
                      <tr>
                        <td colSpan="5" style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}>
                          No hay registros de retiros de choferes para los filtros seleccionados.
                        </td>
                      </tr>
                    ) : (
                      metrics.choferComunaMetrics.map(cm => (
                        <tr key={cm.comunaId}>
                          <td style={{ fontWeight: 'bold' }}>{cm.comunaNombre}</td>
                          <td>Región {cm.codigoRegion}</td>
                          <td style={{ textAlign: 'center' }}>{cm.totalContenedores}</td>
                          <td style={{ textAlign: 'center' }}>
                            {cm.inspeccionesCompletadas > 0 ? (
                              <button
                                type="button"
                                onClick={() => setInspectionsModal({
                                  type: 'CHOFER',
                                  comunaNombre: cm.comunaNombre,
                                  totalContenedores: cm.totalContenedores,
                                  inspeccionesCompletadas: cm.inspeccionesCompletadas,
                                  kilos: cm.kilosRetirados,
                                  porcentajeLlenadoPromedio: cm.porcentajeLlenadoPromedio,
                                  contenedores: cm.contenedoresInspeccionados || []
                                })}
                                style={{
                                  background: 'rgba(245, 158, 11, 0.12)',
                                  color: '#fbbf24',
                                  border: '1px solid rgba(245, 158, 11, 0.3)',
                                  borderRadius: '6px',
                                  padding: '0.25rem 0.6rem',
                                  cursor: 'pointer',
                                  fontWeight: 'bold',
                                  fontSize: '0.85rem',
                                  display: 'inline-flex',
                                  alignItems: 'center',
                                  gap: '0.3rem',
                                  transition: 'all 0.2s'
                                }}
                                title="Ver detalle de contenedores retirados"
                              >
                                🔍 {cm.inspeccionesCompletadas} ver detalle
                              </button>
                            ) : (
                              <span style={{ color: 'var(--text-muted)' }}>0</span>
                            )}
                          </td>
                          <td style={{ textAlign: 'right', fontWeight: 'bold', color: '#fbbf24' }}>
                            {cm.kilosRetirados} kg
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      ) : null}

      {/* Modal Flotante de Detalle de Contenedores Inspeccionados */}
      {inspectionsModal && createPortal(
        <div className="modal-backdrop" onClick={() => setInspectionsModal(null)}>
          <div
            className="modal-window"
            style={{ maxWidth: '920px', width: '95%' }}
            onClick={e => e.stopPropagation()}
          >
            <div className="modal-header">
              <div>
                <h2 className="modal-title">
                  {inspectionsModal.type === 'INSPECTOR'
                    ? `📋 Detalle de Inspecciones Inspector - ${inspectionsModal.comunaNombre}`
                    : `🚚 Detalle de Retiros Choferes - ${inspectionsModal.comunaNombre}`}
                </h2>
                <p className="modal-subtitle" style={{ margin: '0.25rem 0 0 0', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  {inspectionsModal.type === 'INSPECTOR'
                    ? `Inspector Asignado: ${inspectionsModal.inspectorNombre || 'Sin Asignar'}`
                    : 'Detalle de contenedores visitados y chofer más reciente'}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setInspectionsModal(null)}
                className="close-modal-btn"
              >
                ✖
              </button>
            </div>

            {/* Barra Resumen dentro del modal */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
              gap: '0.75rem',
              background: 'rgba(15, 23, 42, 0.7)',
              padding: '0.75rem 1rem',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-color)'
            }}>
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>
                  Contenedores Inspeccionados
                </span>
                <strong style={{ fontSize: '1.1rem', color: '#38bdf8' }}>
                  {inspectionsModal.inspeccionesCompletadas} / {inspectionsModal.totalContenedores}
                </strong>
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>
                  {inspectionsModal.type === 'CHOFER' ? 'Kilos Retirados Totales' : 'Kilos Acumulados Totales'}
                </span>
                <strong style={{ fontSize: '1.1rem', color: inspectionsModal.type === 'CHOFER' ? '#fbbf24' : '#34d399' }}>
                  {inspectionsModal.kilos} kg
                </strong>
              </div>
              <div>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>
                  Llenado Promedio
                </span>
                <strong style={{ fontSize: '1.1rem', color: '#e2e8f0' }}>
                  {inspectionsModal.porcentajeLlenadoPromedio}%
                </strong>
              </div>
            </div>

            {/* Tabla de Contenedores Inspeccionados */}
            <div className="admin-table-wrapper" style={{ maxHeight: '420px', overflowY: 'auto' }}>
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Punto Limpio</th>
                    <th>Sector</th>
                    <th>Categoría</th>
                    <th style={{ textAlign: 'center' }}>% Llenado</th>
                    <th style={{ textAlign: 'right' }}>
                      {inspectionsModal.type === 'CHOFER' ? 'Kilos Retirados' : 'Kilos Calculados'}
                    </th>
                    {inspectionsModal.type === 'CHOFER' && <th>Chofer Más Reciente</th>}
                    <th>Fecha Inspección</th>
                  </tr>
                </thead>
                <tbody>
                  {inspectionsModal.contenedores.length === 0 ? (
                    <tr>
                      <td
                        colSpan={inspectionsModal.type === 'CHOFER' ? 8 : 7}
                        style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)' }}
                      >
                        No hay contenedores inspeccionados para este período.
                      </td>
                    </tr>
                  ) : (
                    inspectionsModal.contenedores.map((c, idx) => (
                      <tr key={c.contenedorId || idx}>
                        <td>{idx + 1}</td>
                        <td style={{ fontWeight: 'bold' }}>{c.nombrePunto}</td>
                        <td>{c.sector || '-'}</td>
                        <td>
                          <span className={`category-badge ${c.categoria ? c.categoria.toLowerCase() : ''}`}>
                            {c.categoria || 'EMPRESA'}
                          </span>
                        </td>
                        <td style={{ textAlign: 'center', fontWeight: 'bold', color: '#38bdf8' }}>
                          {c.porcentaje != null ? `${c.porcentaje}%` : '-'}
                        </td>
                        <td style={{
                          textAlign: 'right',
                          fontWeight: 'bold',
                          color: inspectionsModal.type === 'CHOFER' ? '#fbbf24' : '#34d399'
                        }}>
                          {inspectionsModal.type === 'CHOFER'
                            ? `${c.kilosRetirados != null ? c.kilosRetirados : c.kilos} kg`
                            : `${c.kilos} kg`}
                        </td>
                        {inspectionsModal.type === 'CHOFER' && (
                          <td>
                            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontWeight: 600 }}>
                              🚚 {c.choferNombre || 'No registrado'}
                            </span>
                          </td>
                        )}
                        <td style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                          {c.fechaInspeccion
                            ? new Date(c.fechaInspeccion).toLocaleString('es-CL')
                            : '-'}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', paddingTop: '0.5rem', borderTop: '1px solid var(--border-color)' }}>
              <button
                type="button"
                onClick={() => setInspectionsModal(null)}
                className="btn-secondary"
                style={{ padding: '0.5rem 1.25rem' }}
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
}
