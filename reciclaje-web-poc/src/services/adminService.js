import { API_BASE_URL, getAuthHeaders } from './apiConfig';

export const adminService = {
  // Manejo de usuarios
  async getUsers() {
    const res = await fetch(`${API_BASE_URL}/admin/users`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Error al cargar usuarios');
    return res.json();
  },

  async createUser(userData) {
    const res = await fetch(`${API_BASE_URL}/admin/users`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(userData)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Error al crear usuario');
    }
    return res.json();
  },

  async updateUser(id, userData) {
    const res = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(userData)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Error al actualizar usuario');
    }
    return res.json();
  },

  async deleteUser(id) {
    const res = await fetch(`${API_BASE_URL}/admin/users/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Error al desactivar usuario');
  },

  // Manejo de contenedores
  async getContainers() {
    const res = await fetch(`${API_BASE_URL}/admin/containers`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Error al cargar contenedores');
    return res.json();
  },

  async createContainer(data) {
    const res = await fetch(`${API_BASE_URL}/admin/containers`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Error al crear contenedor');
    }
    return res.json();
  },

  async updateContainer(id, data) {
    const res = await fetch(`${API_BASE_URL}/admin/containers/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(data)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Error al actualizar contenedor');
    }
    return res.json();
  },

  async deleteContainer(id) {
    const res = await fetch(`${API_BASE_URL}/admin/containers/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Error al desactivar contenedor');
  },

  // Métricas del Dashboard
  async getMetrics(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await fetch(`${API_BASE_URL}/admin/dashboard?${query}`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Error al cargar métricas');
    return res.json();
  },

  // Reportes Excel en formato ZIP
  async downloadExcelZipReport(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await fetch(`${API_BASE_URL}/admin/reports/excel-zip?${query}`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Error al descargar reporte Excel ZIP');
    return res.blob();
  },

  // Reportes en formato PDF
  async downloadPdfReport(params = {}) {
    const query = new URLSearchParams(params).toString();
    const res = await fetch(`${API_BASE_URL}/admin/reports/pdf?${query}`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Error al descargar reporte PDF');
    return res.blob();
  },

  // Respaldo de Base de Datos Completa (SQL Dump)
  async downloadDatabaseBackup() {
    const res = await fetch(`${API_BASE_URL}/admin/reports/db-backup/export`, {
      headers: getAuthHeaders()
    });
    if (!res.ok) throw new Error('Error al exportar el respaldo de base de datos SQL');
    return res.blob();
  },

  // Restauración de Base de Datos Completa (SQL Dump)
  async restoreDatabaseBackup(file) {
    const formData = new FormData();
    formData.append('file', file);

    const headers = getAuthHeaders();
    delete headers['Content-Type']; // Permite que el navegador establezca multipart boundary automáticamente

    const res = await fetch(`${API_BASE_URL}/admin/reports/db-backup/import`, {
      method: 'POST',
      headers,
      body: formData
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Error al restaurar el respaldo de base de datos');
    }
    return res.json();
  }
};
