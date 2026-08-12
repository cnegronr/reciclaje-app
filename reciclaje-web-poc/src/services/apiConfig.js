// Configuración centralizada de API Base URL para peticiones HTTP al backend Spring Boot
export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

export const getAuthHeaders = () => {
  const token = localStorage.getItem('reciclaje_auth_token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
};
