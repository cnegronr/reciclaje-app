import React, { useState, useEffect } from 'react';
import { adminService } from '../../services/adminService';
import { comunaService } from '../../services/comunaService';

// Helper para obtener el número de semana ISO actual
const getCurrentISOWeek = () => {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  d.setDate(d.getDate() + 4 - (d.getDay() || 7));
  const yearStart = new Date(d.getFullYear(), 0, 1);
  return Math.ceil((((d - yearStart) / 86400000) + 1) / 7);
};

export default function ReportsTab() {
  const currentWeekNumber = getCurrentISOWeek();
  const currentYearNumber = new Date().getFullYear();

  const [comunas, setComunas] = useState([]);
  const [users, setUsers] = useState([]);
  const [availableYears, setAvailableYears] = useState([currentYearNumber]);

  const [selectedComuna, setSelectedComuna] = useState('');
  const [selectedUser, setSelectedUser] = useState('');
  const [filterByWeek, setFilterByWeek] = useState(true); // Checkbox para reporte semanal opcional (marcado por defecto)
  const [selectedSemana, setSelectedSemana] = useState(currentWeekNumber.toString());
  const [selectedAnio, setSelectedAnio] = useState(currentYearNumber.toString());

  const [downloadingExcel, setDownloadingExcel] = useState(false);
  const [downloadingPdf, setDownloadingPdf] = useState(false);
  const [downloadingDb, setDownloadingDb] = useState(false);
  const [restoringDb, setRestoringDb] = useState(false);
  const [restoreMessage, setRestoreMessage] = useState('');

  useEffect(() => {
    loadOptions();
  }, []);

  const loadOptions = async () => {
    try {
      const [cData, uData, yData] = await Promise.all([
        comunaService.getComunas().catch(() => []),
        adminService.getUsers().catch(() => []),
        adminService.getReportYears().catch(() => [currentYearNumber])
      ]);
      setComunas(cData || []);
      setUsers(uData || []);
      if (yData && yData.length > 0) {
        setAvailableYears(yData);
      }
    } catch (err) {
      console.error('Error al cargar opciones de reportes:', err);
    }
  };

  const handleDownloadExcel = async () => {
    try {
      setDownloadingExcel(true);
      const params = {};
      if (selectedComuna) params.comunaId = selectedComuna;
      if (selectedUser) params.usuarioId = selectedUser;
      if (filterByWeek) {
        if (selectedSemana) params.semanaNumero = selectedSemana;
        if (selectedAnio) params.anio = selectedAnio;
      }

      const blob = await adminService.downloadExcelReport(params);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'Reporte_Consolidado_Reciclaje.xlsx';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      alert(err.message || 'Error al descargar el reporte Excel (.xlsx).');
    } finally {
      setDownloadingExcel(false);
    }
  };

  const handleDownloadPdf = async () => {
    try {
      setDownloadingPdf(true);
      const params = {};
      if (selectedComuna) params.comunaId = selectedComuna;
      if (selectedUser) params.usuarioId = selectedUser;
      if (filterByWeek) {
        if (selectedSemana) params.semanaNumero = selectedSemana;
        if (selectedAnio) params.anio = selectedAnio;
      }

      const blob = await adminService.downloadPdfReport(params);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'Reporte_Consolidado_Reciclaje.pdf';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      alert(err.message || 'Error al descargar el archivo PDF.');
    } finally {
      setDownloadingPdf(false);
    }
  };

  const handleDownloadDbBackup = async () => {
    try {
      setDownloadingDb(true);
      const blob = await adminService.downloadDatabaseBackup();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      const now = new Date().toISOString().slice(0, 10).replace(/-/g, '');
      a.download = `reciclaje_db_backup_${now}.sql`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      alert(err.message || 'Error al exportar el respaldo SQL de la base de datos.');
    } finally {
      setDownloadingDb(false);
    }
  };

  const handleRestoreDbBackup = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (!window.confirm(`⚠️ ADVERTENCIA: ¿Desea restaurar la base de datos con el archivo "${file.name}"? Esto insertará o actualizará los registros de producción.`)) {
      e.target.value = null;
      return;
    }

    try {
      setRestoringDb(true);
      setRestoreMessage('');
      const res = await adminService.restoreDatabaseBackup(file);
      setRestoreMessage(`✅ ${res.message || 'Restauración completada con éxito'}`);
      loadOptions();
    } catch (err) {
      alert(err.message || 'Error al restaurar el respaldo SQL.');
    } finally {
      setRestoringDb(false);
      e.target.value = null;
    }
  };

  return (
    <div className="reports-tab">
      <div style={{ marginBottom: '1.5rem' }}>
        <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>📁 Reportes Consolidados y Respaldo de BD</h3>
        <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          Generación directa de reportes Excel (.xlsx) con fotos incrustadas e hipervínculos S3, reportes PDF y exportación/importación completa en formato SQL Dump.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
        {/* Card 1: Reportes de Inspección */}
        <div className="calculation-card">
          <h4 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
            📊 Reportes Consolidados de Inspección
          </h4>

          {/* Checkbox y Filtros de Semana y Año */}
          <div style={{ marginBottom: '1.25rem', padding: '0.85rem', background: 'rgba(255, 255, 255, 0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer', fontWeight: 600, fontSize: '0.9rem', color: '#f3f4f6', marginBottom: filterByWeek ? '0.75rem' : '0' }}>
              <input
                type="checkbox"
                checked={filterByWeek}
                onChange={e => setFilterByWeek(e.target.checked)}
                style={{ width: '17px', height: '17px', accentColor: '#10b981', cursor: 'pointer' }}
              />
              <span>📅 Filtrar por Semana Específica</span>
            </label>

            {filterByWeek && (
              <div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                  <div>
                    <label className="field-label" style={{ marginBottom: '0.4rem', display: 'block' }}>Semana:</label>
                    <select
                      className="select-control"
                      value={selectedSemana}
                      onChange={e => setSelectedSemana(e.target.value)}
                    >
                      <option value="">Todas las Semanas</option>
                      {Array.from({ length: 52 }, (_, i) => i + 1).map(num => (
                        <option key={num} value={num}>
                          Semana {num} {num === currentWeekNumber ? ' (Semana Actual ⭐)' : ''}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="field-label" style={{ marginBottom: '0.4rem', display: 'block' }}>Año:</label>
                    <select
                      className="select-control"
                      value={selectedAnio}
                      onChange={e => setSelectedAnio(e.target.value)}
                    >
                      <option value="">Todos los Años</option>
                      {availableYears.map(yr => (
                        <option key={yr} value={yr}>
                          {yr} {yr === currentYearNumber ? ' (Año Actual ⭐)' : ''}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                {selectedSemana === currentWeekNumber.toString() && (
                  <div style={{ fontSize: '0.78rem', color: '#34d399', display: 'flex', alignItems: 'center', gap: '5px', marginTop: '0.4rem', fontStyle: 'italic' }}>
                    <span>💡</span>
                    <span>Semana {currentWeekNumber} ({currentYearNumber}) seleccionada por defecto (Semana Actual)</span>
                  </div>
                )}
              </div>
            )}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label className="field-label" style={{ marginBottom: '0.4rem', display: 'block' }}>Filtrar por Comuna (Opcional):</label>
            <select
              className="select-control"
              value={selectedComuna}
              onChange={e => setSelectedComuna(e.target.value)}
            >
              <option value="">Todas las Comunas</option>
              {comunas.map(c => (
                <option key={c.id} value={c.backendId || c.id}>{c.nombre}</option>
              ))}
            </select>
          </div>

          <div style={{ marginBottom: '1.25rem' }}>
            <label className="field-label" style={{ marginBottom: '0.4rem', display: 'block' }}>Filtrar por Inspector / Usuario (Opcional):</label>
            <select
              className="select-control"
              value={selectedUser}
              onChange={e => setSelectedUser(e.target.value)}
            >
              <option value="">Todos los Usuarios Activos</option>
              {users.filter(u => u.activo).map(u => (
                <option key={u.id} value={u.id}>{u.nombre} ({u.rol})</option>
              ))}
            </select>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <button
              className="action-btn action-btn-primary"
              style={{
                width: '100%',
                padding: '0.85rem',
                fontSize: '0.9rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px',
                opacity: downloadingExcel ? 0.75 : 1,
                cursor: downloadingExcel || downloadingPdf ? 'not-allowed' : 'pointer'
              }}
              onClick={handleDownloadExcel}
              disabled={downloadingExcel || downloadingPdf}
            >
              {downloadingExcel ? (
                <>
                  <span className="spinner-icon" style={{ fontSize: '1.1rem' }}>🌀</span>
                  <span>Generando Excel .xlsx...</span>
                </>
              ) : (
                <>
                  <span>📊</span>
                  <span>Descargar Reporte Excel (.xlsx Directo)</span>
                </>
              )}
            </button>

            <button
              className="action-btn action-btn-edit"
              style={{
                width: '100%',
                padding: '0.85rem',
                fontSize: '0.9rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px',
                background: 'rgba(239, 68, 68, 0.2)',
                color: '#f87171',
                borderColor: 'rgba(239, 68, 68, 0.4)',
                opacity: downloadingPdf ? 0.75 : 1,
                cursor: downloadingExcel || downloadingPdf ? 'not-allowed' : 'pointer'
              }}
              onClick={handleDownloadPdf}
              disabled={downloadingExcel || downloadingPdf}
            >
              {downloadingPdf ? (
                <>
                  <span className="spinner-icon" style={{ fontSize: '1.1rem' }}>🌀</span>
                  <span>Generando Documento PDF...</span>
                </>
              ) : (
                <>
                  <span>📄</span>
                  <span>Descargar Reporte PDF Consolidado</span>
                </>
              )}
            </button>
          </div>
        </div>

        {/* Card 2: Respaldo y Restauración de Base de Datos */}
        <div className="calculation-card" style={{ borderColor: 'rgba(59, 130, 246, 0.4)', background: 'rgba(15, 23, 42, 0.8)' }}>
          <h4 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '8px', color: '#60a5fa' }}>
            🗄️ Respaldo y Restauración de BD (SQL Dump)
          </h4>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '1.25rem' }}>
            Exporte un respaldo completo `.sql` con todos los usuarios, puntos limpios, inspecciones y fotos para probar datos reales en local o restaurar tras un redespliegue en AWS.
          </p>

          {restoreMessage && (
            <div className="alert alert-success" style={{ background: 'rgba(16, 185, 129, 0.2)', color: '#34d399', padding: '0.75rem', borderRadius: '6px', fontSize: '0.85rem', marginBottom: '1rem' }}>
              {restoreMessage}
            </div>
          )}

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <button
              className="action-btn action-btn-primary"
              style={{ width: '100%', padding: '0.85rem', fontSize: '0.9rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', background: 'linear-gradient(135deg, #3b82f6, #1d4ed8)' }}
              onClick={handleDownloadDbBackup}
              disabled={downloadingDb || restoringDb}
            >
              {downloadingDb ? '⏳ Generando Dump SQL...' : '💾 Exportar Respaldo Completo (.sql)'}
            </button>

            <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
              <label className="field-label" style={{ marginBottom: '0.5rem', display: 'block' }}>Restaurar BD desde Archivo (.sql):</label>
              <input
                type="file"
                accept=".sql"
                onChange={handleRestoreDbBackup}
                disabled={downloadingDb || restoringDb}
                className="input-control"
                style={{ cursor: 'pointer' }}
              />
              {restoringDb && <span style={{ fontSize: '0.8rem', color: '#60a5fa', marginTop: '0.5rem', display: 'block' }}>⏳ Ejecutando restauración de base de datos...</span>}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
