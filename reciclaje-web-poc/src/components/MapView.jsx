import React from 'react';

export const MapView = ({ contenedores, selectedContenedorId, onSelectContenedor }) => {
  if (!contenedores || contenedores.length === 0) return null;

  // Centro aproximado de los contenedores
  const avgLat = contenedores.reduce((acc, c) => acc + c.lat, 0) / contenedores.length;
  const avgLng = contenedores.reduce((acc, c) => acc + c.lng, 0) / contenedores.length;

  return (
    <div className="map-view-card">
      <div className="map-card-header">
        <h3 className="map-card-title">🗺️ Mapa de Georreferenciación (OpenStreetMap - 100% Gratuito)</h3>
        <span className="map-subtitle">{contenedores.length} Contenedores Registrados</span>
      </div>

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
            onClick={() => onSelectContenedor(c)}
            className={`pin-chip ${selectedContenedorId === c.id ? 'active' : ''} ${c.categoria.toLowerCase()}`}
          >
            <span className="pin-icon">{c.categoria === 'EMPRESA' ? '🏢' : '🏛️'}</span>
            <span className="pin-name">{c.nombrePunto}</span>
            <span className="pin-badge">{c.maxKilos}kg</span>
          </button>
        ))}
      </div>
    </div>
  );
};
