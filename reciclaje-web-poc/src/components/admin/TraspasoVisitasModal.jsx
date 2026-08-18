import React from 'react';

export default function TraspasoVisitasModal({
  isOpen,
  onClose,
  previewData,
  loading,
  onConfirmTraspaso
}) {
  if (!isOpen) return null;

  const permitido = previewData?.permitidoTraspaso ?? false;
  const items = previewData?.detallesVisitados || [];
  const semanaOrigen = previewData?.semanaOrigen || '-';
  const anioOrigen = previewData?.anioOrigen || '-';
  const semanaDestino = previewData?.semanaDestino || '-';
  const anioDestino = previewData?.anioDestino || '-';

  return (
    <div className="modal-backdrop">
      <div className="modal-window" style={{ maxWidth: '42rem', width: '100%' }}>
        {/* Modal Header */}
        <div className="modal-header">
          <div>
            <h3 className="modal-title">📋 Traspasar Visitas de Semana Anterior</h3>
            <p className="modal-subtitle">
              Copia las inspecciones visitadas (visitado = true) de la semana previa a la semana actual.
            </p>
          </div>
          <button onClick={onClose} className="close-modal-btn">✕</button>
        </div>

        {/* Modal Content */}
        <div style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', gap: '1.25rem', maxHeight: '70vh', overflowY: 'auto' }}>
          {/* Week Transition Indicator */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1.5rem', background: 'var(--bg-secondary)', padding: '0.85rem', borderRadius: '10px', border: '1px solid var(--border-color)' }}>
            <div style={{ textAlign: 'center' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', fontWeight: 600 }}>Semana Previa</span>
              <span style={{ fontSize: '0.9rem', fontWeight: 700 }}>Semana {semanaOrigen} ({anioOrigen})</span>
            </div>
            <span style={{ fontSize: '1.4rem', color: '#2dd4bf' }}>➔</span>
            <div style={{ textAlign: 'center' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', textTransform: 'uppercase', fontWeight: 600 }}>Semana Actual</span>
              <span style={{ fontSize: '0.9rem', fontWeight: 700, color: '#2dd4bf' }}>Semana {semanaDestino} ({anioDestino})</span>
            </div>
          </div>

          {/* Validation Banner */}
          {permitido ? (
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem', padding: '1rem', borderRadius: '10px', background: 'rgba(16, 185, 129, 0.12)', border: '1px solid rgba(16, 185, 129, 0.3)', color: '#34d399' }}>
              <span style={{ fontSize: '1.2rem' }}>✅</span>
              <div>
                <h4 style={{ fontSize: '0.85rem', fontWeight: 700, margin: 0 }}>Traspaso Permitido</h4>
                <p style={{ fontSize: '0.75rem', marginTop: '0.2rem', margin: 0, opacity: 0.9 }}>
                  La semana actual no tiene inspecciones ingresadas. Se traspasarán {items.length} visita(s) realizada(s).
                </p>
              </div>
            </div>
          ) : (
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem', padding: '1rem', borderRadius: '10px', background: 'rgba(239, 68, 68, 0.12)', border: '1px solid rgba(239, 68, 68, 0.3)', color: '#f87171' }}>
              <span style={{ fontSize: '1.2rem' }}>⚠️</span>
              <div>
                <h4 style={{ fontSize: '0.85rem', fontWeight: 700, margin: 0 }}>Traspaso No Permitido</h4>
                <p style={{ fontSize: '0.75rem', marginTop: '0.2rem', margin: 0, opacity: 0.9 }}>
                  {previewData?.mensajeValidacion || 'La semana actual ya tiene inspecciones registradas. Debe limpiar la semana actual primero si desea traspasar desde la semana previa.'}
                </p>
              </div>
            </div>
          )}

          {/* Details Table Preview */}
          <div>
            <div style={{ marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', letterSpacing: '0.05em' }}>
                Resumen de Inspecciones a Traspasar ({items.length})
              </span>
            </div>

            {items.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '2rem 1rem', color: 'var(--text-muted)', background: 'var(--bg-secondary)', borderRadius: '10px', border: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                No se encontraron visitas realizadas en la semana anterior para traspasar.
              </div>
            ) : (
              <div className="admin-table-wrapper" style={{ maxHeight: '240px', overflowY: 'auto' }}>
                <table className="admin-table" style={{ fontSize: '0.8rem' }}>
                  <thead>
                    <tr>
                      <th>Punto Limpio</th>
                      <th>Categoría</th>
                      <th style={{ textAlign: 'right' }}>% Llenado</th>
                      <th style={{ textAlign: 'right' }}>Kilos Est.</th>
                      <th>Inspector</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.map((item) => (
                      <tr key={item.detalleId || item.contenedorId}>
                        <td style={{ fontWeight: 600 }}>{item.contenedorNombre}</td>
                        <td>
                          <span className={`category-badge ${item.categoria ? item.categoria.toLowerCase() : 'municipal'}`}>
                            {item.categoria}
                          </span>
                        </td>
                        <td style={{ textAlign: 'right', fontWeight: 700, color: '#2dd4bf' }}>
                          {item.porcentajeEstimado}%
                        </td>
                        <td style={{ textAlign: 'right', fontWeight: 600 }}>
                          {item.kilosCalculados} kg
                        </td>
                        <td style={{ color: 'var(--text-muted)' }}>
                          {item.inspectorNombre}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {/* Modal Footer */}
        <div className="modal-footer" style={{ justifyContent: 'space-between', padding: '1rem 1.25rem' }}>
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="cancel-btn"
          >
            Cancelar
          </button>

          <button
            type="button"
            onClick={onConfirmTraspaso}
            disabled={!permitido || items.length === 0 || loading}
            className="confirm-btn"
            style={{
              opacity: !permitido || items.length === 0 || loading ? 0.5 : 1,
              cursor: !permitido || items.length === 0 || loading ? 'not-allowed' : 'pointer'
            }}
          >
            {loading ? '⏳ Traspasando...' : `🛡️ Confirmar y Traspasar ${items.length} Visitas`}
          </button>
        </div>
      </div>
    </div>
  );
}
