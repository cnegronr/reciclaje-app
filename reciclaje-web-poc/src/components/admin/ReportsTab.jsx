import React, { useState, useEffect } from 'react';
import { adminService } from '../../services/adminService';
import { comunaService } from '../../services/comunaService';

export default function ReportsTab() {
  const [comunas, setComunas] = useState([]);
  const [users, setUsers] = useState([]);
  const [selectedComuna, setSelectedComuna] = useState('');
  const [selectedUser, setSelectedUser] = useState('');
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
      const [cData, uData] = await Promise.all([
        comunaService.getComunas().catch(() => []),
        adminService.getUsers().catch(() => [])
      ]);
      setComunas(cData || []);
      setUsers(uData || []);
    } catch (err) {
      console.error('Error al cargar opciones:', err);
    }
  };

  const handleDownloadExcelZip = async () => {
    try {
      setDownloadingExcel(true);
      const params = {};
      if (selectedComuna) params.comunaId = selectedComuna;
      if (selectedUser) params.usuarioId = selectedUser;

      const blob = await adminService.downloadExcelZipReport(params);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'Reporte_Consolidado_Reciclaje.zip';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      alert(err.message || 'Error al descargar el archivo ZIP de Excel.');
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
        <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>📁 Reportes y Respaldo de Base de Datos</h3>
        <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          Generación de reportes de inspecciones y exportación/importación completa de la base de datos en formato **SQL Dump**.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
        {/* Card 1: Reportes de Inspección */}
        <div className="calculation-card">
          <h4 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
            📊 Reportes Consolidados
          </h4>

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
              <option value="">Todos los Usuarios</option>
              {users.map(u => (
                <option key={u.id} value={u.id}>{u.nombre} ({u.rol})</option>
              ))}
            </select>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <button
              className="action-btn action-btn-primary"
              style={{ width: '100%', padding: '0.85rem', fontSize: '0.9rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
              onClick={handleDownloadExcelZip}
              disabled={downloadingExcel || downloadingPdf}
            >
              {downloadingExcel ? '⏳ Generando Excel ZIP y Fotos...' : '📊 Descargar Reporte Excel (ZIP con Fotos)'}
            </button>

            <button
              className="action-btn action-btn-edit"
              style={{ width: '100%', padding: '0.85rem', fontSize: '0.9rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', background: 'rgba(239, 68, 68, 0.2)', color: '#f87171', borderColor: 'rgba(239, 68, 68, 0.4)' }}
              onClick={handleDownloadPdf}
              disabled={downloadingExcel || downloadingPdf}
            >
              {downloadingPdf ? '⏳ Generando Documento PDF...' : '📄 Descargar Reporte PDF Consolidado'}
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
