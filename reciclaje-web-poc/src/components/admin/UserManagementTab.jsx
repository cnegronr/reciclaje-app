import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
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

  const currentUser = JSON.parse(localStorage.getItem('reciclaje_user_data') || '{}');
  const isCurrentAdmin = currentUser?.rol === 'ADMIN';

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
        comunaIds: user.rol === 'INSPECTOR' ? (user.comunaIds || []) : []
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

  const isSelfUser = (u) => {
    if (!u || !currentUser) return false;
    if (currentUser.id != null && u.id != null && String(currentUser.id) === String(u.id)) {
      return true;
    }
    if (currentUser.email && u.email && currentUser.email.toLowerCase() === u.email.toLowerCase()) {
      return true;
    }
    return false;
  };

  const isEditingSelf = editingUser && isSelfUser(editingUser);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const isEditingSelf = editingUser && isSelfUser(editingUser);
      const payload = {
        ...formData,
        rol: isEditingSelf ? editingUser.rol : formData.rol,
        activo: isEditingSelf ? true : formData.activo,
        comunaIds: (isEditingSelf ? editingUser.rol : formData.rol) === 'INSPECTOR' ? formData.comunaIds : []
      };
      if (editingUser) {
        await adminService.updateUser(editingUser.id, payload);
      } else {
        await adminService.createUser(payload);
      }
      setShowModal(false);
      loadData();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleDelete = async (id) => {
    if (currentUser?.id && String(id) === String(currentUser.id)) {
      alert('No puedes desactivar tu propio usuario.');
      return;
    }
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
    if (currentUser?.id && String(id) === String(currentUser.id)) {
      alert('No puedes eliminar tu propio usuario.');
      return;
    }
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
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Administración de permisos, roles (INSPECTOR, ADMIN, CHOFER, REPORTERIA) y comunas asignadas.</p>
        </div>
        <button className="action-btn action-btn-primary" onClick={() => handleOpenModal(null)}>
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
                    <span className={`badge-role ${u.rol === 'ADMIN' ? 'badge-admin' : u.rol === 'CHOFER' ? 'badge-chofer' : u.rol === 'REPORTERIA' ? 'badge-reporteria' : 'badge-inspector'}`}>
                      {u.rol}
                    </span>
                  </td>
                  <td>
                    <span className={`badge-role ${u.activo ? 'badge-active' : 'badge-inactive'}`}>
                      {u.activo ? '🟢 Activo' : '🔴 Inactivo'}
                    </span>
                  </td>
                  <td>
                    {u.rol === 'ADMIN' || u.rol === 'CHOFER' || u.rol === 'REPORTERIA'
                      ? 'Todas'
                      : (u.comunaNombres && u.comunaNombres.length > 0 ? u.comunaNombres.join(', ') : 'Sin asignación')}
                  </td>
                  <td>
                    {u.rol === 'ADMIN' && !isCurrentAdmin ? (
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>Protegido</span>
                    ) : (
                      <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center' }}>
                        <button className="action-btn action-btn-edit" onClick={() => handleOpenModal(u)}>
                          ✏️ Editar
                        </button>
                        {!isSelfUser(u) && (
                          u.activo ? (
                            <button className="action-btn action-btn-delete" onClick={() => handleDelete(u.id)}>
                              🚫 Desactivar
                            </button>
                          ) : (
                            <button className="action-btn action-btn-delete" style={{ background: 'rgba(239, 68, 68, 0.25)', color: '#ef4444', border: '1px solid rgba(239, 68, 68, 0.4)' }} onClick={() => handleHardDelete(u.id, u.nombre)}>
                              🗑️ Eliminar Definitivamente
                            </button>
                          )
                        )}
                        {isSelfUser(u) && (
                          <span style={{ fontSize: '0.75rem', color: '#60a5fa', fontWeight: 600, padding: '0.2rem 0.5rem', background: 'rgba(59, 130, 246, 0.1)', borderRadius: '4px', border: '1px solid rgba(59, 130, 246, 0.2)' }}>
                            Tu usuario
                          </span>
                        )}
                      </div>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showModal && createPortal(
        <div className="modal-backdrop">
          <div className="modal-window">
            <div className="modal-header">
              <div>
                <h3 className="modal-title">{editingUser ? 'Editar Usuario' : 'Nuevo Usuario'}</h3>
                <p className="modal-subtitle">
                  {formData.rol === 'INSPECTOR'
                    ? 'Ingrese los datos del perfil y comunas a supervisar'
                    : 'Ingrese los datos del perfil de usuario (acceso a todas las comunas)'}
                </p>
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
                <label className="field-label" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <span>Rol del Usuario:</span>
                  {isEditingSelf && (
                    <span style={{ fontSize: '0.75rem', color: '#f59e0b', fontWeight: 500 }}>
                      🔒 No puedes cambiar tu propio rol
                    </span>
                  )}
                </label>
                <select
                  className="select-control"
                  value={formData.rol}
                  disabled={isEditingSelf}
                  style={isEditingSelf ? { opacity: 0.7, cursor: 'not-allowed', background: 'rgba(15, 23, 42, 0.6)' } : {}}
                  onChange={e => {
                    if (isEditingSelf) return;
                    const newRol = e.target.value;
                    setFormData({
                      ...formData,
                      rol: newRol,
                      comunaIds: newRol === 'INSPECTOR' ? formData.comunaIds : []
                    });
                  }}
                >
                  {isCurrentAdmin && (
                    <option value="ADMIN">ADMIN (Acceso Total)</option>
                  )}
                  <option value="INSPECTOR">INSPECTOR (Inspección Terreno)</option>
                  <option value="CHOFER">CHOFER (Retiro y Logística)</option>
                  <option value="REPORTERIA">REPORTERIA (Monitoreo y Reportes)</option>
                </select>
              </div>

              {editingUser && (
                <div>
                  <label
                    className="field-label"
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem',
                      cursor: isEditingSelf ? 'not-allowed' : 'pointer',
                      opacity: isEditingSelf ? 0.8 : 1
                    }}
                  >
                    <input
                      type="checkbox"
                      checked={formData.activo}
                      disabled={isEditingSelf}
                      onChange={e => {
                        if (isEditingSelf) return;
                        setFormData({ ...formData, activo: e.target.checked });
                      }}
                    />
                    <span>Usuario Activo en el Sistema</span>
                    {isEditingSelf && (
                      <span style={{ fontSize: '0.75rem', color: '#f59e0b', fontWeight: 500, marginLeft: 'auto' }}>
                        🔒 No puedes desactivar tu propia cuenta
                      </span>
                    )}
                  </label>
                </div>
              )}

              {formData.rol === 'INSPECTOR' ? (
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
              ) : (
                <div style={{ padding: '0.75rem 0.85rem', background: 'rgba(59, 130, 246, 0.08)', border: '1px solid rgba(59, 130, 246, 0.25)', borderRadius: 'var(--radius-md)', fontSize: '0.82rem', color: '#93c5fd', lineHeight: '1.4' }}>
                  ℹ️ <strong>Acceso a Todas las Comunas:</strong> Por regla de negocio, los usuarios con perfil <strong>{formData.rol}</strong> tienen asignadas todas las comunas. La asignación individual de comunas aplica exclusivamente para el perfil <strong>INSPECTOR</strong>.
                </div>
              )}

              <div className="modal-footer">
                <button type="button" className="cancel-btn" onClick={() => setShowModal(false)}>Cancelar</button>
                <button type="submit" className="confirm-btn">Guardar Usuario</button>
              </div>
            </form>
          </div>
        </div>,
        document.body
      )}
    </div>
  );
}
