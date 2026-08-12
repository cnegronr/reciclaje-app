import { API_BASE_URL, getAuthHeaders } from './apiConfig';
import { TEST_USER } from '../data/mockData';

const AUTH_KEY = 'reciclaje_auth_token';
const USER_KEY = 'reciclaje_user_data';

export const authService = {
  login: async (email, password) => {
    try {
      // Intentar autenticación vía API REST con backend Spring Boot
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem(AUTH_KEY, data.token);
        localStorage.setItem(USER_KEY, JSON.stringify(data));
        return { success: true, user: data, token: data.token };
      } else {
        const errorData = await response.json().catch(() => ({}));
        return { 
          success: false, 
          message: errorData.message || 'Credenciales inválidas en el servidor.' 
        };
      }
    } catch (err) {
      console.warn('Backend no disponible, ejecutando en modo simulación local POC:', err);
      // Fallback a modo simulación local POC
      if (email === TEST_USER.email && password === 'Password123!') {
        const token = `jwt_mock_${btoa(JSON.stringify(TEST_USER))}`;
        localStorage.setItem(AUTH_KEY, token);
        localStorage.setItem(USER_KEY, JSON.stringify(TEST_USER));
        return { success: true, user: TEST_USER, token };
      }
      return { success: false, message: 'Usuario no registrado. Prueba con: inspector@reciclajelitoral.cl' };
    }
  },

  logout: () => {
    localStorage.removeItem(AUTH_KEY);
    localStorage.removeItem(USER_KEY);
  },

  getCurrentUser: () => {
    const userData = localStorage.getItem(USER_KEY);
    return userData ? JSON.parse(userData) : null;
  },

  getToken: () => {
    return localStorage.getItem(AUTH_KEY);
  },

  isAuthenticated: () => {
    return !!localStorage.getItem(AUTH_KEY);
  }
};
