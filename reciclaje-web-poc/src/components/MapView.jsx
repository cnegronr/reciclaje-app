import React, { useState } from 'react';

export const MapView = ({ contenedores, selectedContenedorId }) => {
  const [isOpen, setIsOpen] = useState(false);

  if (!contenedores || contenedores.length === 0) return null;

  // Centro aproximado de los contenedores
  const avgLat = contenedores.reduce((acc, c) => acc + c.lat, 0) / contenedores.length;
  const avgLng = contenedores.reduce((acc, c) => acc + c.lng, 0) / contenedores.length;

  const handleDriveTo = (c) => {
    const navUrl = `https://www.google.com/maps/dir/?api=1&destination=${c.lat},${c.lng}&travelmode=driving`;
    window.open(navUrl, '_blank', 'noopener,noreferrer');
  };

  return (
    <div className="map-view-card">
      <div 
        className="map-card-header" 
        onClick={() => setIsOpen(!isOpen)}
        style={{ cursor: 'pointer', userSelect: 'none' }}
        title="Haz clic para desplegar o plegar el mapa"
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <h3 className="map-card-title">🗺️ Mapa de Georreferenciación</h3>
          <span className="toggle-icon" style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            {isOpen ? '▲ Plegar' : '▼ Desplegar'}
          </span>
        </div>
        <span className="map-subtitle">{contenedores.length} Contenedores Registrados</span>
      </div>

      {isOpen && (
        <div className="map-card-body" style={{ marginTop: '1rem' }}>
          <div className="leaflet-container-embed">
            <iframe
              title="Mapa de contenedores"
              width="100%"
              height="280"
              frameBorder="0"
              scrolling="no"
              marginHeight="0"
              marginWidth="0"
              src={`https://www.openstreetmap.org/export/embed.html?bbox=${avgLng - 0.05}%2C${avgLat - 0.05}%2C${avgLng + 0.05}%2C${avgLat + 0.05}&layer=mapnik&marker=${avgLat}%2C${avgLng}`}
              style={{ borderRadius: '12px', border: '1px solid var(--border-color)' }}
            ></iframe>
          </div>

          <div className="quick-pins-list">
            {contenedores.map((c) => (
              <button
                key={c.id}
                onClick={() => handleDriveTo(c)}
                className={`pin-chip ${selectedContenedorId === c.id ? 'active' : ''} ${c.categoria.toLowerCase()}`}
                title="🚘 Manejar hacia ubicación en Google Maps"
              >
                <span className="pin-icon">{c.categoria === 'EMPRESA' ? '🏢' : '🏛️'}</span>
                <span className="pin-name">{c.nombrePunto}</span>
                <span className="pin-badge">🚘 Manejar hacia ubicación</span>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
