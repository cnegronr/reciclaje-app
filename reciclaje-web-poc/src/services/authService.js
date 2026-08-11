import { TEST_USER } from '../data/mockData';

const AUTH_KEY = 'reciclaje_auth_token';
const USER_KEY = 'reciclaje_user_data';

export const authService = {
  login: async (email, password) => {
    // Simulación de respuesta de backend Spring Boot / JWT
    if (email === TEST_USER.email && password === 'Password123!') {
      const token = `jwt_header.${btoa(JSON.stringify(TEST_USER))}.signature_hash_reciclaje_2026`;
      localStorage.setItem(AUTH_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(TEST_USER));
      return { success: true, user: TEST_USER, token };
    } else if (email === TEST_USER.email) {
      return { success: false, message: 'Contraseña incorrecta. Prueba con: Password123!' };
    }
    return { success: false, message: 'Usuario no registrado. Usa: inspector@reciclajelitoral.cl' };
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
