// Configuración centralizada de API Base URL para peticiones HTTP al backend Spring Boot
export const API_BASE_URL = import.meta.env.VITE_API_URL || 
  (typeof window !== 'undefined' && window.location.port === '5173' ? 'http://localhost:8080/api' : '/api');

export const getAuthHeaders = () => {
  const token = localStorage.getItem('reciclaje_auth_token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
};

export const checkAuthResponse = (res) => {
  if (res && (res.status === 401 || res.status === 403)) {
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('reciclaje:auth-error', {
        detail: { status: res.status }
      }));
    }
  }
  return res;
};

// Interceptor global para peticiones fetch: notifica al instante ante errores 401 o 403
if (typeof window !== 'undefined' && !window.__reciclaje_fetch_intercepted__) {
  window.__reciclaje_fetch_intercepted__ = true;
  const originalFetch = window.fetch;
  window.fetch = async (...args) => {
    const response = await originalFetch(...args);
    const url = typeof args[0] === 'string' ? args[0] : (args[0]?.url || '');
    if ((response.status === 401 || response.status === 403) && !url.includes('/auth/')) {
      window.dispatchEvent(new CustomEvent('reciclaje:auth-error', {
        detail: { status: response.status, url }
      }));
    }
    return response;
  };
}

