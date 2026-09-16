import React, { useState } from 'react';
import { authService } from '../services/authService';

export const LoginScreen = ({ onLoginSuccess, logoutMessage, onClearLogoutMessage }) => {
  // Las credenciales de prueba NUNCA se compilan ni muestran en producción (vite build -> import.meta.env.DEV = false).
  // Solo se habilitan en desarrollo local si import.meta.env.DEV es true y VITE_SHOW_TEST_CREDENTIALS === 'true'.
  // Al compilar con Vite en producción, el Dead Code Elimination elimina por completo
  // el contenedor .test-credentials-box y las funciones de auto-llenado del bundle JavaScript final.
  const showTestCredentials = Boolean(
    import.meta.env.DEV && import.meta.env.VITE_SHOW_TEST_CREDENTIALS === 'true'
  );
  const defaultAdminEmail = showTestCredentials ? (import.meta.env.VITE_ADMIN_INITIAL_EMAIL || 'admin@reciclajelitoral.cl') : '';
  const defaultAdminPassword = showTestCredentials ? (import.meta.env.VITE_ADMIN_INITIAL_PASSWORD || '') : '';

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
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

        {logoutMessage && (
          <div
            style={{
              padding: '0.85rem 1rem',
              marginBottom: '1.25rem',
              borderRadius: '8px',
              backgroundColor: 'rgba(239, 68, 68, 0.15)',
              border: '1px solid rgba(239, 68, 68, 0.4)',
              color: '#fca5a5',
              fontSize: '0.875rem',
              lineHeight: 1.45,
              display: 'flex',
              alignItems: 'flex-start',
              gap: '0.6rem'
            }}
          >
            <span style={{ fontSize: '1.2rem', lineHeight: 1 }}>🚫</span>
            <div style={{ flex: 1, fontWeight: 500 }}>{logoutMessage}</div>
            {onClearLogoutMessage && (
              <button
                type="button"
                onClick={onClearLogoutMessage}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: '#fca5a5',
                  cursor: 'pointer',
                  fontSize: '1rem',
                  lineHeight: 1,
                  padding: '0 0.2rem'
                }}
                title="Cerrar aviso"
              >
                ✕
              </button>
            )}
          </div>
        )}

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
              placeholder="usuario@ejemplo.cl"
            />
          </div>

          <div className="form-group">
            <label className="field-label">Contraseña:</label>
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="text-input"
                placeholder="••••••••"
                style={{ paddingRight: '2.5rem', width: '100%' }}
              />
              <button
                type="button"
                onClick={() => setShowPassword((prev) => !prev)}
                aria-label={showPassword ? 'Ocultar contraseña' : 'Ver contraseña'}
                title={showPassword ? 'Ocultar contraseña' : 'Ver contraseña'}
                style={{
                  position: 'absolute',
                  right: '0.6rem',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '1.1rem',
                  lineHeight: 1,
                  padding: '0.2rem',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#94a3b8',
                  userSelect: 'none'
                }}
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
            </div>
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
