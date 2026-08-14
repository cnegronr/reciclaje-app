import React, { useState, useEffect } from 'react';
import { adminService } from '../../services/adminService';
import { comunaService } from '../../services/comunaService';

export default function ReportsTab() {
  const [comunas, setComunas] = useState([]);
  const [users, setUsers] = useState([]);
  const [selectedComuna, setSelectedComuna] = useState('');
  const [selectedUser, setSelectedUser] = useState('');
  const [downloading, setDownloading] = useState(false);

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
      setDownloading(true);
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
      setDownloading(false);
    }
  };

  return (
    <div className="reports-tab">
      <div style={{ marginBottom: '1.25rem' }}>
        <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>📁 Generación de Reportes Consolidados</h3>
        <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          Descargue el consolidado en formato <strong>ZIP con Excel POI e imágenes organizadas en subcarpetas</strong>. El reporte en Excel incluye hipervínculos funcionales y metadatos JPEG/EXIF estándar.
        </p>
      </div>

      <div className="calculation-card" style={{ maxWidth: '520px' }}>
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

        <button
          className="action-btn action-btn-primary"
          style={{ width: '100%', padding: '0.85rem', fontSize: '0.95rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
          onClick={handleDownloadExcelZip}
          disabled={downloading}
        >
          {downloading ? '⏳ Generando Excel ZIP y empaquetando fotos...' : '📊 Descargar Reporte Consolidado Excel (ZIP con Fotos)'}
        </button>
      </div>
    </div>
  );
}
