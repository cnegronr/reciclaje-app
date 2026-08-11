import React, { useState, useEffect } from 'react';

export const InspectionModal = ({ contenedor, detalleActual, onClose, onSave }) => {
  const isEditing = !!detalleActual?.visitado;

  // Estado del formulario
  const [porcentaje, setPorcentaje] = useState(detalleActual ? detalleActual.porcentajeEstimado : 50);
  const [observaciones, setObservaciones] = useState(detalleActual ? detalleActual.observaciones || '' : '');
  
  // Fotos para inspección inicial (o previsualización)
  const [fotosAntes, setFotosAntes] = useState(
    detalleActual ? detalleActual.fotosInicialesAntes || [] : []
  );
  const [fotosDespues, setFotosDespues] = useState(
    detalleActual ? detalleActual.fotosInicialesDespues || [] : []
  );

  // Fotos para flujo de ACTUALIZACIÓN (Edición)
  const [fotosAntesActualizacion, setFotosAntesActualizacion] = useState([]);
  const [fotosDespuesActualizacion, setFotosDespuesActualizacion] = useState([]);

  // Estado para modal de Resumen / Confirmación previo a guardar
  const [showingSummary, setShowingSummary] = useState(false);

  // Cálculo automático de Kilos
  const maxKilos = contenedor.categoria === 'EMPRESA' ? 500 : 1000;
  const kilosCalculados = ((porcentaje / 100.0) * maxKilos).toFixed(1);

  // Helper para convertir archivo a Data URL previsualizable
  const handleFileUpload = (e, setPhotoState) => {
    const files = Array.from(e.target.files);
    files.forEach((file) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPhotoState((prev) => [
          ...prev,
          { id: Date.now() + Math.random(), url: reader.result, name: file.name }
        ]);
      };
      reader.readAsDataURL(file);
    });
  };

  const handleRemovePhoto = (photoId, setPhotoState) => {
    setPhotoState((prev) => prev.filter((p) => p.id !== photoId));
  };

  const handleOpenSummary = (e) => {
    e.preventDefault();
    if (!isEditing && (fotosAntes.length === 0 || fotosDespues.length === 0)) {
      alert('⚠️ Por favor adjunta al menos 1 foto del estado ANTES y 1 foto del estado DESPUÉS de la inspección inicial.');
      return;
    }
    setShowingSummary(true);
  };

  const handleConfirmSave = () => {
    const dataToSave = {
      porcentajeEstimado: Number(porcentaje),
      kilosCalculados: Number(kilosCalculados),
      observaciones,
      fotosAntes,
      fotosDespues,
      fotosAntesActualizacion,
      fotosDespuesActualizacion
    };

    onSave(contenedor.id, dataToSave, isEditing);
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-window">
        <div className="modal-header">
          <div>
            <h2 className="modal-title">
              {isEditing ? '✏️ Modo Actualización de Inspección' : '📋 Inspección Inicial de Contenedor'}
            </h2>
            <p className="modal-subtitle">{contenedor.nombrePunto} • {contenedor.ubicacion}</p>
          </div>
          <button onClick={onClose} className="close-modal-btn">✖</button>
        </div>

        {!showingSummary ? (
          <form onSubmit={handleOpenSummary} className="modal-form">
            <div className="modal-info-bar">
              <span className={`category-tag ${contenedor.categoria.toLowerCase()}`}>
                {contenedor.categoria === 'EMPRESA' ? '🏢 EMPRESA (Máx 500 kg)' : '🏛️ MUNICIPAL (Máx 1000 kg)'}
              </span>
              <span className="location-tag">📍 Lat: {contenedor.lat}, Lng: {contenedor.lng}</span>
            </div>

            {/* SI ES EDICIÓN: MOSTRAR CONSERVACIÓN DE FOTOS INICIALES */}
            {isEditing && (
              <div className="initial-preserved-section">
                <h4>🔒 Fotos Conservadas de la Inspección Inicial ({new Date(detalleActual.fechaHoraInicial).toLocaleString('es-CL')})</h4>
                <div className="photo-grid-readonly">
                  <div>
                    <span className="photo-sublabel">Fotos Iniciales ANTES:</span>
                    <div className="photo-thumbnails">
                      {detalleActual.fotosInicialesAntes?.map((f, i) => (
                        <img key={i} src={f.url} alt="Antes Inicial" className="thumb-img" />
                      ))}
                    </div>
                  </div>
                  <div>
                    <span className="photo-sublabel">Fotos Iniciales DESPUÉS:</span>
                    <div className="photo-thumbnails">
                      {detalleActual.fotosInicialesDespues?.map((f, i) => (
                        <img key={i} src={f.url} alt="Después Inicial" className="thumb-img" />
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* SECCIÓN 1: PORCENTAJE ESTIMADO Y CÁLCULO DE KILOS */}
            <div className="form-group calculation-card">
              <label className="field-label">
                📊 Porcentaje Estimado Utilizado: <strong className="percentage-display">{porcentaje}%</strong>
              </label>
              <input
                type="range"
                min="0"
                max="100"
                step="5"
                value={porcentaje}
                onChange={(e) => setPorcentaje(e.target.value)}
                className="percentage-slider"
              />
              <div className="kilo-calculation-box">
                <span>Fórmula de Kilos: ({porcentaje}% × {maxKilos}kg) =</span>
                <span className="kilos-total">{kilosCalculados} kg</span>
              </div>
            </div>

            {/* SECCIÓN 2: FOTOS ANTES Y DESPUÉS (INICIALES O DE ACTUALIZACIÓN) */}
            <div className="form-section">
              <h3 className="section-title">
                {isEditing ? '📸 Nuevas Fotos para Registro de Actualización' : '📸 Fotos de Inspección Actual'}
              </h3>

              <div className="photos-upload-grid">
                {/* FOTOS ANTES */}
                <div className="upload-box">
                  <label className="upload-label">
                    <span>📷 Estado ANTES de la inspección</span>
                    <input
                      type="file"
                      accept="image/*"
                      multiple
                      onChange={(e) => handleFileUpload(e, isEditing ? setFotosAntesActualizacion : setFotosAntes)}
                      className="file-input-hidden"
                    />
                    <span className="upload-btn-styled">➕ Subir Fotos ANTES</span>
                  </label>

                  <div className="photo-preview-list">
                    {(isEditing ? fotosAntesActualizacion : fotosAntes).map((photo) => (
                      <div key={photo.id} className="photo-preview-item">
                        <img src={photo.url} alt="Antes preview" />
                        <button
                          type="button"
                          onClick={() => handleRemovePhoto(photo.id, isEditing ? setFotosAntesActualizacion : setFotosAntes)}
                          className="remove-photo-btn"
                          title="Eliminar foto"
                        >
                          🗑️
                        </button>
                      </div>
                    ))}
                  </div>
                </div>

                {/* FOTOS DESPUÉS */}
                <div className="upload-box">
                  <label className="upload-label">
                    <span>📸 Estado DESPUÉS de la inspección</span>
                    <input
                      type="file"
                      accept="image/*"
                      multiple
                      onChange={(e) => handleFileUpload(e, isEditing ? setFotosDespuesActualizacion : setFotosDespues)}
                      className="file-input-hidden"
                    />
                    <span className="upload-btn-styled">➕ Subir Fotos DESPUÉS</span>
                  </label>

                  <div className="photo-preview-list">
                    {(isEditing ? fotosDespuesActualizacion : fotosDespues).map((photo) => (
                      <div key={photo.id} className="photo-preview-item">
                        <img src={photo.url} alt="Después preview" />
                        <button
                          type="button"
                          onClick={() => handleRemovePhoto(photo.id, isEditing ? setFotosDespuesActualizacion : setFotosDespues)}
                          className="remove-photo-btn"
                          title="Eliminar foto"
                        >
                          🗑️
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            {/* SECCIÓN 3: OBSERVACIONES */}
            <div className="form-group">
              <label className="field-label">💬 Observaciones de la Visita:</label>
              <textarea
                value={observaciones}
                onChange={(e) => setObservaciones(e.target.value)}
                placeholder="Ej. Contenedor con leve desgaste, acceso despejado..."
                className="text-input"
                rows="3"
              ></textarea>
            </div>

            <div className="modal-footer">
              <button type="button" onClick={onClose} className="cancel-btn">
                Cancelar
              </button>
              <button type="submit" className="confirm-btn">
                🔍 Revisar Resumen de Cambios →
              </button>
            </div>
          </form>
        ) : (
          /* VISTA DE RESUMEN Y CONFIRMACIÓN DE CAMBIOS */
          <div className="summary-view">
            <div className="summary-alert">
              ⚠️ <strong>Confirmación de Inspección:</strong> Una vez guardados los cambios, el registro solo podrá modificarse mediante el flujo de actualización.
            </div>

            <div className="summary-card">
              <h4>📊 Resumen de Registro:</h4>
              <ul>
                <li><strong>Contenedor:</strong> {contenedor.nombrePunto} ({contenedor.categoria})</li>
                <li><strong>Capacidad Máxima:</strong> {maxKilos} kg</li>
                <li><strong>Porcentaje Ingresado:</strong> {porcentaje}%</li>
                <li><strong>Kilos Calculados:</strong> <span className="highlight">{kilosCalculados} kg</span></li>
                <li><strong>Tipo de Registro:</strong> {isEditing ? 'Actualización / Edición' : 'Inspección Inicial'}</li>
                <li><strong>Marca Temporal:</strong> {new Date().toLocaleString('es-CL')}</li>
              </ul>
            </div>

            <div className="summary-photos-preview">
              <h4>📸 Fotos a Confirmar:</h4>
              <p>
                - Antes: {(isEditing ? fotosAntesActualizacion : fotosAntes).length} foto(s)
                <br />
                - Después: {(isEditing ? fotosDespuesActualizacion : fotosDespues).length} foto(s)
              </p>
            </div>

            <div className="modal-footer">
              <button onClick={() => setShowingSummary(false)} className="cancel-btn">
                ← Volver a Modificar
              </button>
              <button onClick={handleConfirmSave} className="save-final-btn">
                ✅ Confirmar y Guardar Registro
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
