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

  const fillTestCredentials = () => {
    setEmail('inspector@reciclajelitoral.cl');
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
          <p className="test-title">🧪 Credenciales de Prueba POC:</p>
          <div className="credentials-code">
            <span><strong>Usuario:</strong> inspector@reciclajelitoral.cl</span>
            <span><strong>Clave:</strong> Password123!</span>
          </div>
          <button onClick={fillTestCredentials} className="auto-fill-btn">
            ⚡ Cargar Credenciales de Prueba
          </button>
        </div>
      </div>
    </div>
  );
};
