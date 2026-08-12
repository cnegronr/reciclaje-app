import { API_BASE_URL } from './apiConfig';

const AUTH_KEY = 'reciclaje_auth_token';
const USER_KEY = 'reciclaje_user_data';

export const authService = {
  login: async (email, password) => {
    try {
      // Autenticación obligatoria vía API REST con backend Spring Boot / PostgreSQL
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
          message: errorData.message || 'Credenciales inválidas en el servidor backend.' 
        };
      }
    } catch (err) {
      console.error('Error al conectar con la API REST del backend Spring Boot:', err);
      return { 
        success: false, 
        message: 'No se pudo establecer conexión con el servidor backend (Spring Boot/PostgreSQL en http://localhost:8080). Verifica los contenedores con docker compose.' 
      };
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
