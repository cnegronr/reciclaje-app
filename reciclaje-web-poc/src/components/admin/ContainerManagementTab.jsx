import React, { useState, useEffect } from 'react';
import { adminService } from '../../services/adminService';
import { comunaService } from '../../services/comunaService';

export default function ContainerManagementTab() {
  const [containers, setContainers] = useState([]);
  const [comunas, setComunas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingContainer, setEditingContainer] = useState(null);

  const [formData, setFormData] = useState({
    comunaId: '',
    nombrePunto: '',
    ubicacionDescripcion: '',
    categoria: 'MUNICIPAL',
    kilosMaximos: 1000,
    urlGoogleMaps: '',
    latitud: '',
    longitud: '',
    activo: true
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [cData, comData] = await Promise.all([
        adminService.getContainers().catch(() => []),
        comunaService.getComunas().catch(() => [])
      ]);
      setContainers(cData || []);
      setComunas(comData || []);
    } catch (err) {
      alert(err.message || 'Error al cargar contenedores');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (cont = null) => {
    if (cont) {
      setEditingContainer(cont);
      setFormData({
        comunaId: cont.comunaId || (comunas[0] ? (comunas[0].backendId || comunas[0].id) : ''),
        nombrePunto: cont.nombrePunto || '',
        ubicacionDescripcion: cont.ubicacionDescripcion || '',
        categoria: cont.categoria || 'MUNICIPAL',
        kilosMaximos: cont.kilosMaximos || (cont.categoria === 'EMPRESA' ? 500 : 1000),
        urlGoogleMaps: cont.urlGoogleMaps || '',
        latitud: cont.latitud || '',
        longitud: cont.longitud || '',
        activo: cont.activo ?? true
      });
    } else {
      setEditingContainer(null);
      setFormData({
        comunaId: comunas[0] ? (comunas[0].backendId || comunas[0].id) : '',
        nombrePunto: '',
        ubicacionDescripcion: '',
        categoria: 'MUNICIPAL',
        kilosMaximos: 1000,
        urlGoogleMaps: '',
        latitud: '',
        longitud: '',
        activo: true
      });
    }
    setShowModal(true);
  };

  const handleCategoryChange = (cat) => {
    const defaultKilos = cat === 'EMPRESA' ? 500 : 1000;
    setFormData(prev => ({
      ...prev,
      categoria: cat,
      kilosMaximos: defaultKilos
    }));
  };

  const handleUseCurrentLocation = () => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const lat = pos.coords.latitude.toFixed(7);
          const lng = pos.coords.longitude.toFixed(7);
          setFormData(prev => ({
            ...prev,
            latitud: lat,
            longitud: lng,
            urlGoogleMaps: `https://maps.google.com/?q=${lat},${lng}`
          }));
        },
        (err) => alert('Error al obtener ubicación GPS: ' + err.message)
      );
    } else {
      alert('Geolocalización no soportada en el navegador');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...formData,
        comunaId: Number(formData.comunaId),
        kilosMaximos: Number(formData.kilosMaximos),
        latitud: formData.latitud ? Number(formData.latitud) : null,
        longitud: formData.longitud ? Number(formData.longitud) : null
      };

      if (editingContainer) {
        await adminService.updateContainer(editingContainer.id, payload);
      } else {
        await adminService.createContainer(payload);
      }
      setShowModal(false);
      loadData();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Desactivar este contenedor?')) {
      try {
        await adminService.deleteContainer(id);
        loadData();
      } catch (err) {
        alert(err.message);
      }
    }
  };

  if (loading) return <div className="p-4 text-center">⏳ Cargando contenedores...</div>;

  return (
    <div className="container-management">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <div>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>📦 Gestión de Contenedores y Puntos Limpios</h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Configuración de capacidades, categorías (MUNICIPAL / EMPRESA) y geolocalización.</p>
        </div>
        <button className="action-btn action-btn-primary" onClick={() => handleOpenModal()}>
          + Nuevo Contenedor
        </button>
      </div>

      <div className="admin-table-wrapper">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Comuna</th>
              <th>Nombre Punto</th>
              <th>Categoría</th>
              <th>Capacidad</th>
              <th>Mapa / GPS</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {containers.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem' }}>
                  No se encontraron contenedores registrados.
                </td>
              </tr>
            ) : (
              containers.map(c => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td style={{ fontWeight: 'bold' }}>{c.comunaNombre || 'N/A'}</td>
                  <td>{c.nombrePunto}</td>
                  <td>
                    <span className={`category-badge ${c.categoria ? c.categoria.toLowerCase() : 'municipal'}`}>
                      {c.categoria}
                    </span>
                  </td>
                  <td>{c.kilosMaximos} kg</td>
                  <td>
                    {c.urlGoogleMaps ? (
                      <a href={c.urlGoogleMaps} target="_blank" rel="noreferrer" className="nav-btn" style={{ display: 'inline-block', padding: '0.3rem 0.6rem' }}>
                        📍 Mapa
                      </a>
                    ) : 'Sin Mapa'}
                  </td>
                  <td>
                    <span className={`badge-role ${c.activo ? 'badge-active' : 'badge-inactive'}`}>
                      {c.activo ? '🟢 Activo' : '🔴 Inactivo'}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.4rem' }}>
                      <button className="action-btn action-btn-edit" onClick={() => handleOpenModal(c)}>
                        ✏️ Editar
                      </button>
                      {c.activo && (
                        <button className="action-btn action-btn-delete" onClick={() => handleDelete(c.id)}>
                          🚫 Desactivar
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-window">
            <div className="modal-header">
              <div>
                <h3 className="modal-title">{editingContainer ? 'Editar Contenedor' : 'Nuevo Contenedor'}</h3>
                <p className="modal-subtitle">Especifique categoría, comuna y enlace a mapa de ubicación</p>
              </div>
              <button className="close-modal-btn" onClick={() => setShowModal(false)}>✕</button>
            </div>

            <form onSubmit={handleSubmit} className="modal-form">
              <div>
                <label className="field-label">Comuna:</label>
                <select
                  className="select-control"
                  required
                  value={formData.comunaId}
                  onChange={e => setFormData({ ...formData, comunaId: e.target.value })}
                >
                  <option value="">Seleccione Comuna</option>
                  {comunas.map(cm => (
                    <option key={cm.id} value={cm.backendId || cm.id}>{cm.nombre}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="field-label">Nombre del Punto Limpio:</label>
                <input
                  type="text"
                  className="input-control"
                  required
                  value={formData.nombrePunto}
                  onChange={e => setFormData({ ...formData, nombrePunto: e.target.value })}
                />
              </div>

              <div>
                <label className="field-label">Descripción de Ubicación:</label>
                <textarea
                  className="input-control"
                  rows="2"
                  value={formData.ubicacionDescripcion}
                  onChange={e => setFormData({ ...formData, ubicacionDescripcion: e.target.value })}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label className="field-label">Categoría:</label>
                  <select
                    className="select-control"
                    value={formData.categoria}
                    onChange={e => handleCategoryChange(e.target.value)}
                  >
                    <option value="MUNICIPAL">MUNICIPAL (1000 kg)</option>
                    <option value="EMPRESA">EMPRESA (500 kg)</option>
                  </select>
                </div>
                <div>
                  <label className="field-label">Capacidad Máxima (kg):</label>
                  <input
                    type="number"
                    className="input-control"
                    required
                    value={formData.kilosMaximos}
                    onChange={e => setFormData({ ...formData, kilosMaximos: e.target.value })}
                  />
                </div>
              </div>

              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                  <label className="field-label">URL Google Maps / Ubicación GPS:</label>
                  <button type="button" className="action-btn action-btn-edit" onClick={handleUseCurrentLocation}>
                    🎯 Usar Mi Ubicación Actual
                  </button>
                </div>
                <input
                  type="url"
                  className="input-control"
                  placeholder="https://maps.google.com/?q=-33.4,-71.6"
                  value={formData.urlGoogleMaps}
                  onChange={e => setFormData({ ...formData, urlGoogleMaps: e.target.value })}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label className="field-label">Latitud:</label>
                  <input
                    type="number"
                    step="any"
                    className="input-control"
                    value={formData.latitud}
                    onChange={e => setFormData({ ...formData, latitud: e.target.value })}
                  />
                </div>
                <div>
                  <label className="field-label">Longitud:</label>
                  <input
                    type="number"
                    step="any"
                    className="input-control"
                    value={formData.longitud}
                    onChange={e => setFormData({ ...formData, longitud: e.target.value })}
                  />
                </div>
              </div>

              {editingContainer && (
                <div>
                  <label className="field-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={formData.activo}
                      onChange={e => setFormData({ ...formData, activo: e.target.checked })}
                    />
                    Contenedor Activo
                  </label>
                </div>
              )}

              <div className="modal-footer">
                <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>Cancelar</button>
                <button type="submit" className="confirm-btn">Guardar Contenedor</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
