import React, { useState, useEffect } from 'react';
import { adminService } from '../../services/adminService';
import { comunaService } from '../../services/comunaService';

export default function UserManagementTab() {
  const [users, setUsers] = useState([]);
  const [comunas, setComunas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingUser, setEditingUser] = useState(null);

  const [formData, setFormData] = useState({
    nombre: '',
    email: '',
    password: '',
    rol: 'INSPECTOR',
    activo: true,
    comunaIds: []
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const [uData, cData] = await Promise.all([
        adminService.getUsers().catch(() => []),
        comunaService.getComunas().catch(() => [])
      ]);
      setUsers(uData || []);
      setComunas(cData || []);
    } catch (err) {
      setError(err.message || 'Error al cargar datos');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (user = null) => {
    if (user) {
      setEditingUser(user);
      setFormData({
        nombre: user.nombre || '',
        email: user.email || '',
        password: '',
        rol: user.rol || 'INSPECTOR',
        activo: user.activo ?? true,
        comunaIds: user.comunaIds || []
      });
    } else {
      setEditingUser(null);
      setFormData({
        nombre: '',
        email: '',
        password: '',
        rol: 'INSPECTOR',
        activo: true,
        comunaIds: []
      });
    }
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingUser) {
        await adminService.updateUser(editingUser.id, formData);
      } else {
        await adminService.createUser(formData);
      }
      setShowModal(false);
      loadData();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Desactivar este usuario?')) {
      try {
        await adminService.deleteUser(id);
        loadData();
      } catch (err) {
        alert(err.message);
      }
    }
  };

  const handleHardDelete = async (id, nombre) => {
    if (window.confirm(`⚠️ ¿Deseas eliminar DEFINITIVAMENTE al usuario "${nombre}" de la base de datos?\n\nLos registros históricos de inspección se mantendrán intactos. Esta acción no se puede deshacer.`)) {
      try {
        await adminService.hardDeleteUser(id);
        loadData();
      } catch (err) {
        alert(err.message);
      }
    }
  };

  const toggleComuna = (cId) => {
    const isAdding = !formData.comunaIds.includes(cId);

    if (isAdding) {
      // Buscar si la comuna ya está asignada a otro inspector
      const assignedUser = users.find(u =>
        u.id !== editingUser?.id &&
        u.comunaIds &&
        u.comunaIds.includes(cId)
      );

      if (assignedUser) {
        const comunaObj = comunas.find(c => (c.backendId || c.id) === cId);
        const comunaNombre = comunaObj ? comunaObj.nombre : 'esta comuna';
        const confirmReassign = window.confirm(
          `⚠️ La comuna "${comunaNombre}" actualmente está asignada a ${assignedUser.nombre}.\n\n` +
          `Al asignarla a este usuario, ${assignedUser.nombre} perderá la asignación de dicha comuna.\n\n` +
          `¿Deseas continuar con la reasignación?`
        );

        if (!confirmReassign) {
          return;
        }
      }
    }

    setFormData(prev => {
      const exists = prev.comunaIds.includes(cId);
      return {
        ...prev,
        comunaIds: exists ? prev.comunaIds.filter(id => id !== cId) : [...prev.comunaIds, cId]
      };
    });
  };

  if (loading) return <div className="p-4 text-center">⏳ Cargando lista de usuarios...</div>;

  return (
    <div className="user-management">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
        <div>
          <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>👥 Gestión de Usuarios</h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Administración de permisos, roles (INSPECTOR, ADMIN, CHOFER) y comunas asignadas.</p>
        </div>
        <button className="action-btn action-btn-primary" onClick={() => handleOpenModal()}>
          + Nuevo Usuario
        </button>
      </div>

      {error && <div className="error-box mb-3">{error}</div>}

      <div className="admin-table-wrapper">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Email</th>
              <th>Rol</th>
              <th>Estado</th>
              <th>Comunas Asignadas</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 ? (
              <tr>
                <td colSpan="7" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem' }}>
                  No se encontraron usuarios registrados.
                </td>
              </tr>
            ) : (
              users.map(u => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td style={{ fontWeight: 'bold' }}>{u.nombre}</td>
                  <td style={{ color: 'var(--text-muted)' }}>{u.email}</td>
                  <td>
                    <span className={`badge-role ${u.rol === 'ADMIN' ? 'badge-admin' : u.rol === 'CHOFER' ? 'badge-chofer' : 'badge-inspector'}`}>
                      {u.rol}
                    </span>
                  </td>
                  <td>
                    <span className={`badge-role ${u.activo ? 'badge-active' : 'badge-inactive'}`}>
                      {u.activo ? '🟢 Activo' : '🔴 Inactivo'}
                    </span>
                  </td>
                  <td>
                    {u.comunaNombres && u.comunaNombres.length > 0 ? u.comunaNombres.join(', ') : 'Sin asignación'}
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.4rem' }}>
                      <button className="action-btn action-btn-edit" onClick={() => handleOpenModal(u)}>
                        ✏️ Editar
                      </button>
                      {u.activo ? (
                        <button className="action-btn action-btn-delete" onClick={() => handleDelete(u.id)}>
                          🚫 Desactivar
                        </button>
                      ) : (
                        <button className="action-btn action-btn-delete" style={{ background: 'rgba(239, 68, 68, 0.25)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.4)' }} onClick={() => handleHardDelete(u.id, u.nombre)}>
                          🗑️ Eliminar Definitivamente
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
                <h3 className="modal-title">{editingUser ? 'Editar Usuario' : 'Nuevo Usuario'}</h3>
                <p className="modal-subtitle">Ingrese los datos del perfil y comunas a supervisar</p>
              </div>
              <button className="close-modal-btn" onClick={() => setShowModal(false)}>✕</button>
            </div>

            <form onSubmit={handleSubmit} className="modal-form">
              <div>
                <label className="field-label">Nombre Completo:</label>
                <input
                  type="text"
                  className="input-control"
                  required
                  value={formData.nombre}
                  onChange={e => setFormData({ ...formData, nombre: e.target.value })}
                />
              </div>

              <div>
                <label className="field-label">Correo Electrónico:</label>
                <input
                  type="email"
                  className="input-control"
                  required
                  value={formData.email}
                  onChange={e => setFormData({ ...formData, email: e.target.value })}
                />
              </div>

              <div>
                <label className="field-label">Contraseña {editingUser && '(Dejar vacío para mantener actual)'}:</label>
                <input
                  type="password"
                  className="input-control"
                  required={!editingUser}
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={e => setFormData({ ...formData, password: e.target.value })}
                />
              </div>

              <div>
                <label className="field-label">Rol del Usuario:</label>
                <select
                  className="select-control"
                  value={formData.rol}
                  onChange={e => setFormData({ ...formData, rol: e.target.value })}
                >
                  <option value="INSPECTOR">INSPECTOR (Inspección Terreno)</option>
                  <option value="ADMIN">ADMIN (Acceso Total)</option>
                  <option value="CHOFER">CHOFER (Retiro y Logística)</option>
                </select>
              </div>

              {editingUser && (
                <div>
                  <label className="field-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={formData.activo}
                      onChange={e => setFormData({ ...formData, activo: e.target.checked })}
                    />
                    Usuario Activo en el Sistema
                  </label>
                </div>
              )}

              <div>
                <label className="field-label">Comunas Asignadas:</label>
                <div style={{ maxHeight: '140px', overflowY: 'auto', background: '#0f172a', border: '1px solid var(--border-color)', padding: '0.75rem', borderRadius: 'var(--radius-md)', display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                  {comunas.length === 0 ? (
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Cargando comunas...</span>
                  ) : (
                    comunas.map(c => {
                      const cId = c.backendId || c.id;
                      const assignedUser = users.find(u => u.id !== editingUser?.id && u.comunaIds && u.comunaIds.includes(cId));
                      return (
                        <label key={c.id} style={{ fontSize: '0.85rem', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '0.5rem' }}>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <input
                              type="checkbox"
                              checked={formData.comunaIds.includes(cId)}
                              onChange={() => toggleComuna(cId)}
                            />
                            📍 {c.nombre}
                          </span>
                          {assignedUser && (
                            <span style={{ fontSize: '0.75rem', color: '#f59e0b', fontWeight: 600 }}>
                              (Asignada a: {assignedUser.nombre})
                            </span>
                          )}
                        </label>
                      );
                    })
                  )}
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>Cancelar</button>
                <button type="submit" className="confirm-btn">Guardar Usuario</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
