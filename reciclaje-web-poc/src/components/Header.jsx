import React, { useState, useEffect } from 'react';
import { getDeadlineCurrentWeek } from '../services/inspectionService';

export const Header = ({ user, comunas, selectedComunaId, onSelectComuna, onLogout, activeView, onChangeView }) => {
  const [timeLeft, setTimeLeft] = useState('');

  useEffect(() => {
    const updateCountdown = () => {
      const deadline = getDeadlineCurrentWeek();
      const now = new Date();
      const diff = deadline - now;

      if (diff <= 0) {
        setTimeLeft('¡PLAZO FINALIZADO!');
        return;
      }

      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
      const minutes = Math.floor((diff / (1000 * 60)) % 60);

      setTimeLeft(`${days}d ${hours}h ${minutes}m (Dom 20:00)`);
    };

    updateCountdown();
    const interval = setInterval(updateCountdown, 60000);
    return () => clearInterval(interval);
  }, []);

  const formatShortName = (name) => {
    if (!name) return '';
    const parts = name.trim().split(/\s+/);
    if (parts.length <= 1) return name;
    return `${parts[0].charAt(0).toUpperCase()}. ${parts.slice(1).join(' ')}`;
  };

  return (
    <header className="header-glass">
      <div className="header-container">
        <div className="brand-section">
          <div className="logo-icon">♻️</div>
          <div>
            <h1 className="brand-title">Reciclaje Litoral</h1>
            <p className="brand-subtitle">Gestión & Monitoreo de Vidrio Comunal</p>
          </div>
        </div>

        <div className="controls-section">
          <div className="comuna-selector-wrapper">
            <label htmlFor="comuna-select" className="select-label">Comuna Asignada:</label>
            <select
              id="comuna-select"
              value={selectedComunaId}
              onChange={(e) => onSelectComuna(e.target.value)}
              className="comuna-select"
            >
              {comunas.map((c) => (
                <option key={c.id} value={c.id}>
                  📍 {c.nombre} ({c.contenedores.length} Puntos)
                </option>
              ))}
            </select>
          </div>

          <div className="deadline-badge">
            <span className="deadline-icon">⏱️</span>
            <div>
              <span className="deadline-title">Límite Semanal:</span>
              <span className="deadline-timer">{timeLeft}</span>
            </div>
          </div>

          <div className="user-profile">
            <div className="avatar">👤</div>
            <div className="user-info">
              <span className="user-name">{formatShortName(user?.nombre)}</span>
              <span className="user-role">{user?.rol}</span>
            </div>

            {user?.rol === 'ADMIN' && (
              <div className="admin-mode-pill-toggle">
                <button
                  className={`mode-toggle-btn ${activeView === 'inspection' ? 'active' : ''}`}
                  onClick={() => onChangeView('inspection')}
                  title="Módulo Inspección (Vista Inspector)"
                >
                  📋 Inspección
                </button>
                <button
                  className={`mode-toggle-btn ${activeView === 'admin' ? 'active' : ''}`}
                  onClick={() => onChangeView('admin')}
                  title="Panel de Administración (ADMIN)"
                >
                  ⚙️ Admin
                </button>
              </div>
            )}

            <button onClick={onLogout} className="logout-btn" title="Cerrar Sesión">
              🚪 Salir
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
