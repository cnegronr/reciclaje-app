import React, { useState } from 'react';
import { authService } from '../services/authService';

export const LoginScreen = ({ onLoginSuccess }) => {
  const showTestCredentials = import.meta.env.VITE_SHOW_TEST_CREDENTIALS === 'true';
  const defaultAdminEmail = import.meta.env.VITE_ADMIN_INITIAL_EMAIL || 'admin@reciclajelitoral.cl';
  const defaultAdminPassword = import.meta.env.VITE_ADMIN_INITIAL_PASSWORD || '';

  const [email, setEmail] = useState(showTestCredentials ? defaultAdminEmail : '');
  const [password, setPassword] = useState(showTestCredentials ? defaultAdminPassword : '');
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setLoading(true);

    const res = await authService.login(email, password);
    setLoading(false);

    if (res.success) {
      onLoginSuccess(res.user);
    } else {
      setErrorMsg(res.message);
    }
  };

  const fillAdminCredentials = () => {
    setEmail(defaultAdminEmail);
    setPassword(defaultAdminPassword);
  };

  return (
    <div className="login-wrapper">
      <div className="login-card glass-panel">
        <div className="login-header">
          <div className="logo-badge">♻️</div>
          <h2>Reciclaje Litoral</h2>
          <p className="login-subtitle">Sistema de Monitoreo & Inspección Semanal</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          {errorMsg && <div className="error-box">⚠️ {errorMsg}</div>}

          <div className="form-group">
            <label className="field-label">Correo Electrónico:</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="text-input"
              placeholder="admin@reciclajelitoral.cl"
            />
          </div>

          <div className="form-group">
            <label className="field-label">Contraseña:</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="text-input"
              placeholder="••••••••"
            />
          </div>

          <button type="submit" disabled={loading} className="login-btn">
            {loading ? 'Autenticando...' : '🔑 Ingresar al Sistema'}
          </button>
        </form>

        {showTestCredentials && (
          <div className="test-credentials-box">
            <p className="test-title">🧪 Credenciales de Prueba (Administrador):</p>
            <div className="credentials-code">
              <span><strong>Usuario Admin:</strong> {defaultAdminEmail}</span>
            </div>
            <div style={{ display: 'flex', gap: '0.4rem', marginTop: '0.5rem' }}>
              <button
                type="button"
                onClick={fillAdminCredentials}
                className="auto-fill-btn"
                style={{ flex: 1, background: '#e8f5e9', color: '#2e7d32', borderColor: '#a5d6a7' }}
              >
                🛡️ Cargar Credenciales Admin
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
