import React, { useState } from 'react';
import { authService } from '../services/authService';

export const LoginScreen = ({ onLoginSuccess }) => {
  const [email, setEmail] = useState('inspector@reciclajelitoral.cl');
  const [password, setPassword] = useState('Password123!');
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

  const fillInspectorCredentials = () => {
    setEmail('inspector@reciclajelitoral.cl');
    setPassword('Password123!');
  };

  const fillChofer1Credentials = () => {
    setEmail('chofer@reciclajelitoral.cl');
    setPassword('Password123!');
  };

  const fillChofer2Credentials = () => {
    setEmail('chofer2@reciclajelitoral.cl');
    setPassword('Password123!');
  };

  const fillAdminCredentials = () => {
    setEmail('admin@reciclajelitoral.cl');
    setPassword('Password123!');
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
              placeholder="inspector@reciclajelitoral.cl"
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

        <div className="test-credentials-box">
          <p className="test-title">🧪 Credenciales de Prueba (Inspector, Admin, Choferes):</p>
          <div className="credentials-code">
            <span><strong>Admin:</strong> admin@reciclajelitoral.cl</span>
            <span><strong>Inspector:</strong> inspector@reciclajelitoral.cl</span>
            <span><strong>Chofer 1:</strong> chofer@reciclajelitoral.cl (Pedro)</span>
            <span><strong>Chofer 2:</strong> chofer2@reciclajelitoral.cl (Juan)</span>
            <span><strong>Clave:</strong> Password123!</span>
          </div>
          <div style={{ display: 'flex', gap: '0.4rem', marginTop: '0.5rem', flexWrap: 'wrap' }}>
            <button onClick={fillAdminCredentials} className="auto-fill-btn" style={{ flex: 1, minWidth: '90px', background: '#ffebee', color: '#c62828', borderColor: '#ef9a9a' }}>
              🛡️ Admin
            </button>
            <button onClick={fillInspectorCredentials} className="auto-fill-btn" style={{ flex: 1, minWidth: '90px' }}>
              📋 Inspector
            </button>
            <button onClick={fillChofer1Credentials} className="auto-fill-btn" style={{ flex: 1, minWidth: '90px' }}>
              🚛 Chofer 1 (Pedro)
            </button>
            <button onClick={fillChofer2Credentials} className="auto-fill-btn" style={{ flex: 1, minWidth: '90px' }}>
              🚛 Chofer 2 (Juan)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
