import React, { useState, useEffect } from 'react';
import { adminService } from '../../services/adminService';
import { comunaService } from '../../services/comunaService';

export default function MetricsDashboardTab() {
  const [metrics, setMetrics] = useState(null);
  const [users, setUsers] = useState([]);
  const [comunas, setComunas] = useState([]);
  const [loading, setLoading] = useState(true);

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

  return (
    <div className="metrics-dashboard">
      <div style={{ marginBottom: '1.25rem' }}>
        <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>📈 Dashboard de Métricas Configurables</h3>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Filtre métricas consolidadas por periodo de tiempo, comuna, tipo de usuario o región.</p>
      </div>

      {/* Filtros de Consolidado y Período */}
      <div className="filter-grid" style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '1.25rem' }}>
        <div>
          <label className="field-label" style={{ marginBottom: '0.4rem', display: 'block' }}>Agrupación / Scope:</label>
          <select
            className="select-control"
            value={filters.scope}
            onChange={e => setFilters({ ...filters, scope: e.target.value })}
          >
            <option value="ALL">Todos los Usuarios</option>
            <option value="INDIVIDUAL">Usuario Individual</option>
            <option value="COMUNA">Por Comuna</option>
            <option value="ROLE">Por Tipo (ROL)</option>
            <option value="REGION">Por Región</option>
          </select>
        </div>

        <div>
          <label className="field-label" style={{ marginBottom: '0.4rem', display: 'block' }}>Período de Tiempo:</label>
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

        {(filters.scope === 'INDIVIDUAL' || filters.scope === 'ALL') && (
          <div>
            <label className="field-label" style={{ marginBottom: '0.4rem', display: 'block' }}>Usuario / Inspector:</label>
            <select
              className="select-control"
              value={filters.userId}
              onChange={e => {
                const uVal = e.target.value;
                setFilters({
                  ...filters,
                  userId: uVal,
                  scope: uVal ? 'INDIVIDUAL' : 'ALL'
                });
              }}
            >
              <option value="">Todos los Usuarios</option>
              {users.map(u => (
                <option key={u.id} value={u.id}>{u.nombre} ({u.rol})</option>
              ))}
            </select>
          </div>
        )}

        {filters.scope === 'COMUNA' && (
          <div>
            <label className="field-label" style={{ marginBottom: '0.4rem', display: 'block' }}>Comuna:</label>
            <select
              className="select-control"
              value={filters.comunaId}
              onChange={e => setFilters({ ...filters, comunaId: e.target.value })}
            >
              <option value="">Todas las Comunas</option>
              {comunas.map(c => (
                <option key={c.id} value={c.backendId || c.id}>{c.nombre}</option>
              ))}
            </select>
          </div>
        )}

        {filters.scope === 'ROLE' && (
          <div>
            <label className="field-label" style={{ marginBottom: '0.4rem', display: 'block' }}>Rol:</label>
            <select
              className="select-control"
              value={filters.role}
              onChange={e => setFilters({ ...filters, role: e.target.value })}
            >
              <option value="">Todos los Roles</option>
              <option value="INSPECTOR">INSPECTOR</option>
              <option value="CHOFER">CHOFER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>
        )}
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
              <div>
                <span className="stat-value">{metrics.totalKilosCalculados} kg</span>
                <span className="stat-label">Recolección Total</span>
              </div>
            </div>

            <div className="stat-card blue">
              <span className="stat-icon">📊</span>
              <div>
                <span className="stat-value">{metrics.promedioPorcentajeLlenado}%</span>
                <span className="stat-label">Llenado Promedio</span>
              </div>
            </div>
          </div>

          {/* Tabla de Desglose por Comuna */}
          {metrics.comunaMetrics && metrics.comunaMetrics.length > 0 && (
            <div>
              <h4 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '0.85rem' }}>Consolidado por Comuna</h4>
              <div className="admin-table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Comuna</th>
                      <th>Región</th>
                      <th>Contenedores</th>
                      <th>Inspecciones</th>
                      <th>Kilos Acumulados</th>
                    </tr>
                  </thead>
                  <tbody>
                    {metrics.comunaMetrics.map(cm => (
                      <tr key={cm.comunaId}>
                        <td style={{ fontWeight: 'bold' }}>{cm.comunaNombre}</td>
                        <td>Región {cm.codigoRegion}</td>
                        <td>{cm.totalContenedores}</td>
                        <td>{cm.inspeccionesCompletadas}</td>
                        <td style={{ fontWeight: 'bold', color: '#34d399' }}>{cm.kilosRecolectados} kg</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      ) : null}
    </div>
  );
}
