import React from 'react';

export const ContainerCard = ({ contenedor, detalleInspeccion, onInspect }) => {
  const isVisited = detalleInspeccion?.visitado;

  // Generar Deep Link de navegación GPS directa a Google Maps app
  const googleMapsNavUrl = `https://www.google.com/maps/dir/?api=1&destination=${contenedor.lat},${contenedor.lng}&travelmode=driving`;

  return (
    <div className={`container-card ${isVisited ? 'visited' : 'pending'}`}>
      <div className="card-top-bar">
        <span className={`category-badge ${contenedor.categoria.toLowerCase()}`}>
          {contenedor.categoria === 'EMPRESA' ? '🏢 EMPRESA (Máx 500kg)' : '🏛️ MUNICIPAL (Máx 1000kg)'}
        </span>

        <span className={`status-badge ${isVisited ? 'visited' : 'pending'}`}>
          {isVisited ? '✅ VISITADO' : '⏳ PENDIENTE'}
        </span>
      </div>

      <div className="card-body">
        <h3 className="container-title">{contenedor.nombrePunto}</h3>
        <p className="container-address">📍 {contenedor.ubicacion}</p>

        {isVisited && (
          <div className="inspection-result-box">
            <div className="result-metric">
              <span className="metric-label">Porcentaje Utilizado:</span>
              <span className="metric-value font-bold">{detalleInspeccion.porcentajeEstimado}%</span>
            </div>
            <div className="result-metric">
              <span className="metric-label">Kilos Calculados:</span>
              <span className="metric-value highlight">{detalleInspeccion.kilosCalculados} kg</span>
            </div>

            <div className="timestamps-history">
              <div className="timestamp-item">
                🕒 <strong>Inicial:</strong> {new Date(detalleInspeccion.fechaHoraInicial).toLocaleString('es-CL')}
              </div>
              {detalleInspeccion.fechaHoraActualizacion && (
                <div className="timestamp-item updated">
                  ✏️ <strong>Actualizado:</strong> {new Date(detalleInspeccion.fechaHoraActualizacion).toLocaleString('es-CL')}
                </div>
              )}
            </div>

            <div className="photo-count-pills">
              <span className="photo-pill">
                📸 Inicial: {detalleInspeccion.fotosInicialesAntes?.length || 0} antes / {detalleInspeccion.fotosInicialesDespues?.length || 0} después
              </span>
              {detalleInspeccion.fechaHoraActualizacion && (
                <span className="photo-pill update">
                  🔄 Actualización: {detalleInspeccion.fotosActualizacionAntes?.length || 0} antes / {detalleInspeccion.fotosActualizacionDespues?.length || 0} después
                </span>
              )}
            </div>
          </div>
        )}
      </div>

      <div className="card-actions">
        <a
          href={googleMapsNavUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="nav-btn"
          title="Navegar hacia el contenedor en Google Maps"
        >
          🚘 Manejar hacia ubicación
        </a>

        <button
          onClick={() => onInspect(contenedor)}
          className={`inspect-btn ${isVisited ? 'edit-btn' : 'start-btn'}`}
        >
          {isVisited ? '✏️ Editar Inspección' : '📋 Registrar Inspección'}
        </button>
      </div>
    </div>
  );
};
