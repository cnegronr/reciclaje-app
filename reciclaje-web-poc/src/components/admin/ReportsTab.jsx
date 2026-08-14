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

  return (
    <div className="reports-tab">
      <div style={{ marginBottom: '1.25rem' }}>
        <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>📁 Generación de Reportes Consolidados</h3>
        <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          Descargue el reporte consolidado de inspecciones en formato <strong>Excel ZIP (con imágenes)</strong> o en formato <strong>PDF oficial</strong>.
        </p>
      </div>

      <div className="calculation-card" style={{ maxWidth: '560px' }}>
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
    </div>
  );
}
