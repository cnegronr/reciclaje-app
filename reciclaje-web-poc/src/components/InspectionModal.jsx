import React, { useState } from 'react';

export const InspectionModal = ({ contenedor, detalleActual, onClose, onSave }) => {
  const isEditing = !!detalleActual?.visitado;

  // Valores iniciales para detección de cambios (por defecto 0% en vez de 50%)
  const initialPorcentaje = (detalleActual && (detalleActual.visitado || detalleActual.porcentajeEstimado !== undefined))
    ? Number(detalleActual.porcentajeEstimado)
    : 0;
  const initialObservaciones = detalleActual ? (detalleActual.observaciones || '') : '';

  // Estado del formulario
  const [porcentaje, setPorcentaje] = useState(initialPorcentaje);
  const [observaciones, setObservaciones] = useState(initialObservaciones);
  
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

  // Estado para modal de Resumen / Confirmación previo a guardar y spinner de carga
  const [showingSummary, setShowingSummary] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  // Cálculo automático de Kilos
  const maxKilos = contenedor.categoria === 'EMPRESA' ? 500 : 1000;
  const kilosCalculados = ((porcentaje / 100.0) * maxKilos).toFixed(1);

  // Detección de cambios introducidos en el registro
  const porcentajeChanged = Number(porcentaje) !== initialPorcentaje;
  const observacionesChanged = observaciones.trim() !== initialObservaciones.trim();
  const newPhotosUploaded = fotosAntesActualizacion.length > 0 || fotosDespuesActualizacion.length > 0;

  const hasChanges = isEditing
    ? (porcentajeChanged || observacionesChanged || newPhotosUploaded)
    : (fotosAntes.length > 0 && fotosDespues.length > 0);

  // Helper para convertir archivo a Data URL previsualizable y comprime imagen client-side
  const handleFileUpload = (e, setPhotoState) => {
    const files = Array.from(e.target.files);
    files.forEach((file) => {
      const reader = new FileReader();
      reader.onload = (event) => {
        const img = new Image();
        img.onload = () => {
          const maxDim = 1280;
          let width = img.width;
          let height = img.height;

          if (width > maxDim || height > maxDim) {
            if (width >= height) {
              height = Math.round((height * maxDim) / width);
              width = maxDim;
            } else {
              width = Math.round((width * maxDim) / height);
              height = maxDim;
            }
          }

          const canvas = document.createElement('canvas');
          canvas.width = width;
          canvas.height = height;
          const ctx = canvas.getContext('2d');
          ctx.drawImage(img, 0, 0, width, height);

          // Compresión JPEG a 75% de calidad
          const compressedDataUrl = canvas.toDataURL('image/jpeg', 0.75);

          setPhotoState((prev) => [
            ...prev,
            { id: Date.now() + Math.random(), url: compressedDataUrl, name: file.name }
          ]);
        };
        img.src = event.target.result;
      };
      reader.readAsDataURL(file);
    });
  };

  const handleRemovePhoto = (photoId, setPhotoState) => {
    setPhotoState((prev) => prev.filter((p) => p.id !== photoId));
  };

  const handleOpenSummary = (e) => {
    e.preventDefault();
    if (!hasChanges) {
      return;
    }
    if (!isEditing && (fotosAntes.length === 0 || fotosDespues.length === 0)) {
      alert('⚠️ Para registrar la inspección inicial, debe proporcionar al menos 1 imagen del estado ANTES y al menos 1 imagen del estado DESPUÉS.');
      return;
    }
    if (isEditing && newPhotosUploaded) {
      if (fotosAntesActualizacion.length === 0 || fotosDespuesActualizacion.length === 0) {
        alert('⚠️ Al actualizar imágenes, debe proporcionar al menos 1 imagen del estado ANTES y al menos 1 imagen del estado DESPUÉS.');
        return;
      }
    }
    setShowingSummary(true);
  };

  const handleConfirmSave = async () => {
    if (isSaving || !hasChanges) return;
    setIsSaving(true);
    try {
      let finalObservaciones = observaciones.trim();

      if (!isEditing) {
        // Registro inicial
        finalObservaciones = finalObservaciones ? finalObservaciones : "Registro inicial";
      } else {
        // Actualización
        if (newPhotosUploaded) {
          // Si no se modificó el comentario en esta visita o viene vacuo/automático previo -> "Actualización de fotos"
          if (!observacionesChanged || !finalObservaciones || finalObservaciones === "Actualización de porcentaje" || finalObservaciones === "Registro inicial") {
            finalObservaciones = "Actualización de fotos";
          }
        } else {
          // Actualización sin imágenes
          if (!observacionesChanged) {
            finalObservaciones = "Actualización de porcentaje";
          } else {
            if (porcentajeChanged) {
              finalObservaciones = `Actualización de porcentaje: ${finalObservaciones}`;
            } else {
              finalObservaciones = `Comentario actualizado: ${finalObservaciones}`;
            }
          }
        }
      }

      const dataToSave = {
        porcentajeEstimado: Number(porcentaje),
        kilosCalculados: Number(kilosCalculados),
        observaciones: finalObservaciones,
        fotosAntes,
        fotosDespues,
        fotosAntesActualizacion,
        fotosDespuesActualizacion
      };

      await onSave(contenedor.id, dataToSave, isEditing);
    } catch (err) {
      console.error('Error al guardar registro de inspección:', err);
    } finally {
      setIsSaving(false);
    }
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
          <button onClick={onClose} className="close-modal-btn" disabled={isSaving}>✖</button>
        </div>

        {!showingSummary ? (
          <form onSubmit={handleOpenSummary} className="modal-form">
            <div className="modal-info-bar">
              <span className={`category-tag ${contenedor.categoria.toLowerCase()}`}>
                {contenedor.categoria === 'EMPRESA' ? '🏢 EMPRESA (Máx 500 kg)' : '🏛️ MUNICIPAL (Máx 1000 kg)'}
              </span>
              <span className="location-tag">📍 Lat: {contenedor.lat}, Lng: {contenedor.lng}</span>
            </div>

            {/* SI ES EDICIÓN: MOSTRAR HISTÓRICO DE FOTOS (INICIALES Y CADA ACTUALIZACIÓN POR SEPARADO) */}
            {isEditing && (
              <div className="initial-preserved-section">
                <h4>
                  🔒 Inspección Inicial ({detalleActual.fechaHoraInicial ? new Date(detalleActual.fechaHoraInicial).toLocaleString('es-CL') : ''})
                  {detalleActual.creadoPorUsuarioNombre && (
                    <span style={{ fontSize: '0.8rem', color: '#38bdf8', marginLeft: '8px', fontWeight: 'bold' }}>
                      👤 Registrado por: {detalleActual.creadoPorUsuarioNombre}
                    </span>
                  )}
                </h4>
                <div className="update-data-summary" style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', margin: '8px 0 10px 0', fontSize: '0.85rem', background: 'rgba(0, 0, 0, 0.2)', padding: '8px 12px', borderRadius: '6px' }}>
                  <span><strong>📊 Llenado Inicial Registrado:</strong> <span style={{ color: '#38bdf8', fontWeight: 'bold' }}>{detalleActual.porcentajeEstimadoInicial ?? detalleActual.porcentajeEstimado}%</span> ({detalleActual.kilosCalculadosInicial ?? detalleActual.kilosCalculados} kg)</span>
                  {(detalleActual.observacionesInicial || detalleActual.observaciones) && (
                    <span><strong>💬 Comentarios Iniciales:</strong> <span style={{ color: '#e2e8f0', fontStyle: 'italic' }}>"{detalleActual.observacionesInicial || detalleActual.observaciones}"</span></span>
                  )}
                </div>
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

                {/* RENDERIZAR CADA ACTUALIZACIÓN POR SEPARADO CON SUS FOTOS ESPECÍFICAS O FOTOS VIGENTES CONSERVADAS */}
                {detalleActual.actualizacionesHistorial && detalleActual.actualizacionesHistorial.length > 0 ? (
                  detalleActual.actualizacionesHistorial.map((upd, idx) => {
                    const authorName = upd.usuarioNombre || upd.fotosAntes?.[0]?.usuarioNombre || upd.fotosDespues?.[0]?.usuarioNombre || detalleActual.actualizadoPorUsuarioNombre;
                    const hasNewPhotosInUpdate = (upd.fotosAntes && upd.fotosAntes.length > 0) || (upd.fotosDespues && upd.fotosDespues.length > 0);

                    // Helper para obtener fotos vigentes conservadas cuando la actualización no cargó fotos nuevas
                    const getVigentePhotos = (historial, currentIdx) => {
                      for (let i = currentIdx - 1; i >= 0; i--) {
                        const prevUpd = historial[i];
                        const pAntes = prevUpd.fotosAntes || [];
                        const pDespues = prevUpd.fotosDespues || [];
                        if (pAntes.length > 0 || pDespues.length > 0) {
                          return {
                            fotosAntes: pAntes,
                            fotosDespues: pDespues,
                            origen: `Actualización #${i + 1}`
                          };
                        }
                      }
                      return {
                        fotosAntes: detalleActual.fotosInicialesAntes || [],
                        fotosDespues: detalleActual.fotosInicialesDespues || [],
                        origen: 'Inspección Inicial'
                      };
                    };

                    const vigenteInfo = hasNewPhotosInUpdate ? null : getVigentePhotos(detalleActual.actualizacionesHistorial, idx);
                    const displayAntes = hasNewPhotosInUpdate ? (upd.fotosAntes || []) : vigenteInfo.fotosAntes;
                    const displayDespues = hasNewPhotosInUpdate ? (upd.fotosDespues || []) : vigenteInfo.fotosDespues;

                    return (
                      <div key={idx} className="update-preserved-subsection" style={{ marginTop: '14px', paddingTop: '12px', borderTop: '1px dashed rgba(255,255,255,0.15)' }}>
                        <h4>
                          🔄 Actualización #{idx + 1} ({upd.fechaHora ? new Date(upd.fechaHora).toLocaleString('es-CL') : ''})
                          {authorName && (
                            <span style={{ fontSize: '0.8rem', color: '#f59e0b', marginLeft: '8px', fontWeight: 'bold' }}>
                              👤 Actualizado por: {authorName}
                            </span>
                          )}
                        </h4>
                        <div className="update-data-summary" style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', margin: '8px 0 10px 0', fontSize: '0.85rem', background: 'rgba(0, 0, 0, 0.2)', padding: '8px 12px', borderRadius: '6px' }}>
                          <span><strong>📊 Llenado Registrado:</strong> <span style={{ color: '#38bdf8', fontWeight: 'bold' }}>{upd.porcentajeEstimado}%</span> ({upd.kilosCalculados} kg)</span>
                          {upd.observaciones && (
                            <span><strong>💬 Comentarios:</strong> <span style={{ color: '#e2e8f0', fontStyle: 'italic' }}>"{upd.observaciones}"</span></span>
                          )}
                        </div>

                        {!hasNewPhotosInUpdate && (
                          <div style={{ fontSize: '0.78rem', color: '#38bdf8', margin: '4px 0 8px 0', fontStyle: 'italic', display: 'flex', alignItems: 'center', gap: '4px' }}>
                            ℹ️ Esta actualización no incluyó nuevas imágenes. Se mantienen las fotografías vigentes conservadas de {vigenteInfo.origen}:
                          </div>
                        )}

                        <div className="photo-grid-readonly">
                          <div>
                            <span className="photo-sublabel">
                              {hasNewPhotosInUpdate ? 'Fotos ANTES cargadas en esta visita:' : `Fotos ANTES vigentes (${vigenteInfo.origen}):`}
                            </span>
                            <div className="photo-thumbnails">
                              {displayAntes && displayAntes.length > 0 ? (
                                displayAntes.map((f, i) => (
                                  <img key={i} src={f.url} alt={`Antes Actualización ${idx + 1}`} className="thumb-img" />
                                ))
                              ) : (
                                <span style={{ fontSize: '0.75rem', color: '#94a3b8', fontStyle: 'italic' }}>Sin fotos antes</span>
                              )}
                            </div>
                          </div>
                          <div>
                            <span className="photo-sublabel">
                              {hasNewPhotosInUpdate ? 'Fotos DESPUÉS cargadas en esta visita:' : `Fotos DESPUÉS vigentes (${vigenteInfo.origen}):`}
                            </span>
                            <div className="photo-thumbnails">
                              {displayDespues && displayDespues.length > 0 ? (
                                displayDespues.map((f, i) => (
                                  <img key={i} src={f.url} alt={`Después Actualización ${idx + 1}`} className="thumb-img" />
                                ))
                              ) : (
                                <span style={{ fontSize: '0.75rem', color: '#94a3b8', fontStyle: 'italic' }}>Sin fotos después</span>
                              )}
                            </div>
                          </div>
                        </div>
                      </div>
                    );
                  })
                ) : (
                  (detalleActual.fotosActualizacionAntes?.length > 0 || detalleActual.fotosActualizacionDespues?.length > 0) && (
                    <div className="update-preserved-subsection" style={{ marginTop: '14px', paddingTop: '12px', borderTop: '1px dashed rgba(255,255,255,0.15)' }}>
                      <h4>
                        🔄 Última Actualización ({detalleActual.fechaHoraActualizacion ? new Date(detalleActual.fechaHoraActualizacion).toLocaleString('es-CL') : ''})
                        {detalleActual.actualizadoPorUsuarioNombre && (
                          <span style={{ fontSize: '0.8rem', color: '#f59e0b', marginLeft: '8px', fontWeight: 'bold' }}>
                            👤 Actualizado por: {detalleActual.actualizadoPorUsuarioNombre}
                          </span>
                        )}
                      </h4>
                      <div className="photo-grid-readonly">
                        <div>
                          <span className="photo-sublabel">Fotos ANTES:</span>
                          <div className="photo-thumbnails">
                            {detalleActual.fotosActualizacionAntes?.map((f, i) => (
                              <img key={i} src={f.url} alt="Antes Actualización" className="thumb-img" />
                            ))}
                          </div>
                        </div>
                        <div>
                          <span className="photo-sublabel">Fotos DESPUÉS:</span>
                          <div className="photo-thumbnails">
                            {detalleActual.fotosActualizacionDespues?.map((f, i) => (
                              <img key={i} src={f.url} alt="Después Actualización" className="thumb-img" />
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                  )
                )}
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
                {isEditing ? '📸 Nuevas Fotos Opcionales para esta Actualización' : '📸 Fotos de Inspección Actual'}
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
              <button
                type="submit"
                disabled={!hasChanges}
                className={`confirm-btn ${!hasChanges ? 'disabled' : ''}`}
                title={!hasChanges ? 'Modifica el porcentaje, comentarios o añade fotos para habilitar este botón' : ''}
              >
                🔍 Revisar Resumen de Cambios →
              </button>
            </div>
          </form>
        ) : (
          /* VISTA DE RESUMEN Y CONFIRMACIÓN DE CAMBIOS */
          <div className="summary-view">
            {isEditing && !newPhotosUploaded && (
              <div className="summary-alert warning-no-photos" style={{ background: 'rgba(234, 179, 8, 0.15)', border: '1px solid #eab308', padding: '12px 16px', borderRadius: '8px', marginBottom: '16px', color: '#fef08a' }}>
                ⚠️ <strong>Atención (Actualización sin imágenes):</strong> La actualización no contiene nuevas imágenes. ¿Está seguro de continuar sin adjuntar fotografías?
              </div>
            )}

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
              <h4>📸 Fotos a Confirmar en esta Petición:</h4>
              <p>
                - Antes: {(isEditing ? fotosAntesActualizacion : fotosAntes).length} foto(s)
                <br />
                - Después: {(isEditing ? fotosDespuesActualizacion : fotosDespues).length} foto(s)
              </p>
            </div>

            <div className="modal-footer">
              <button onClick={() => setShowingSummary(false)} className="cancel-btn" disabled={isSaving}>
                ← Volver a Modificar
              </button>
              <button onClick={handleConfirmSave} disabled={isSaving || !hasChanges} className="save-final-btn">
                {isSaving ? (
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
                    <span className="spinner-icon" style={{ display: 'inline-block', animation: 'spin 1s linear infinite' }}>⏳</span> Guardando e subiendo fotos...
                  </span>
                ) : (
                  '✅ Confirmar y Guardar Registro'
                )}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
