import React, { createContext, useContext, useState, useCallback, useRef } from 'react';
import { createPortal } from 'react-dom';

const FeedbackContext = createContext(null);

export const FeedbackProvider = ({ children }) => {
  // Toasts
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const showToast = useCallback((message, type = 'info', duration = 4000) => {
    const id = Date.now() + Math.random().toString(36).substring(2, 9);
    setToasts((prev) => [...prev, { id, message, type }]);

    if (duration > 0) {
      setTimeout(() => {
        removeToast(id);
      }, duration);
    }
    return id;
  }, [removeToast]);

  const showSuccess = useCallback((msg, duration) => showToast(msg, 'success', duration), [showToast]);
  const showError = useCallback((msg, duration) => showToast(msg, 'error', duration || 5000), [showToast]);
  const showWarning = useCallback((msg, duration) => showToast(msg, 'warning', duration), [showToast]);
  const showInfo = useCallback((msg, duration) => showToast(msg, 'info', duration), [showToast]);

  // Confirm Modal
  const [confirmState, setConfirmState] = useState(null);
  const confirmResolveRef = useRef(null);

  const confirm = useCallback(({
    title = '¿Estás seguro?',
    message,
    confirmText = 'Confirmar',
    cancelText = 'Cancelar',
    type = 'primary' // 'primary' | 'danger' | 'warning'
  }) => {
    return new Promise((resolve) => {
      confirmResolveRef.current = resolve;
      setConfirmState({
        title,
        message,
        confirmText,
        cancelText,
        type
      });
    });
  }, []);

  const handleConfirmClose = useCallback((result) => {
    if (confirmResolveRef.current) {
      confirmResolveRef.current(result);
      confirmResolveRef.current = null;
    }
    setConfirmState(null);
  }, []);

  const value = {
    showToast,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    confirm
  };

  return (
    <FeedbackContext.Provider value={value}>
      {children}

      {/* Toast Container */}
      {typeof document !== 'undefined' && createPortal(
        <div className="feedback-toast-container" style={{
          position: 'fixed',
          top: '1.25rem',
          right: '1.25rem',
          zIndex: 99999,
          display: 'flex',
          flexDirection: 'column',
          gap: '0.75rem',
          maxWidth: '420px',
          width: 'calc(100% - 2.5rem)',
          pointerEvents: 'none'
        }}>
          {toasts.map((t) => {
            const isSuccess = t.type === 'success';
            const isError = t.type === 'error';
            const isWarning = t.type === 'warning';
            
            const border = isSuccess ? 'rgba(16, 185, 129, 0.5)' :
                           isError ? 'rgba(239, 68, 68, 0.5)' :
                           isWarning ? 'rgba(245, 158, 11, 0.5)' :
                           'rgba(59, 130, 246, 0.5)';
            const icon = isSuccess ? '✅' :
                         isError ? '❌' :
                         isWarning ? '⚠️' :
                         'ℹ️';

            return (
              <div
                key={t.id}
                className="feedback-toast-item"
                style={{
                  pointerEvents: 'auto',
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '0.75rem',
                  padding: '0.85rem 1.1rem',
                  borderRadius: '10px',
                  backgroundColor: '#0f172a',
                  border: `1px solid ${border}`,
                  boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.6), 0 8px 10px -6px rgba(0, 0, 0, 0.6)',
                  backdropFilter: 'blur(12px)',
                  animation: 'toastSlideIn 0.25s cubic-bezier(0.16, 1, 0.3, 1)'
                }}
              >
                <span style={{ fontSize: '1.2rem', lineHeight: 1 }}>{icon}</span>
                <div style={{ flex: 1, fontSize: '0.875rem', lineHeight: 1.45, color: '#f1f5f9', whiteSpace: 'pre-wrap' }}>
                  {t.message}
                </div>
                <button
                  type="button"
                  onClick={() => removeToast(t.id)}
                  style={{
                    background: 'transparent',
                    border: 'none',
                    color: '#94a3b8',
                    cursor: 'pointer',
                    fontSize: '1rem',
                    lineHeight: 1,
                    padding: '0.1rem',
                    marginLeft: '0.25rem'
                  }}
                  title="Cerrar"
                >
                  ✕
                </button>
              </div>
            );
          })}
        </div>,
        document.body
      )}

      {/* Confirm Modal */}
      {confirmState && typeof document !== 'undefined' && createPortal(
        <div
          className="modal-backdrop"
          onClick={() => handleConfirmClose(false)}
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.75)',
            backdropFilter: 'blur(6px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 99998,
            padding: '1rem',
            animation: 'fadeIn 0.15s ease-out'
          }}
        >
          <div
            className="glass-panel"
            onClick={(e) => e.stopPropagation()}
            style={{
              backgroundColor: '#1e293b',
              border: confirmState.type === 'danger'
                ? '1px solid rgba(239, 68, 68, 0.4)'
                : confirmState.type === 'warning'
                  ? '1px solid rgba(245, 158, 11, 0.4)'
                  : '1px solid rgba(59, 130, 246, 0.4)',
              borderRadius: '12px',
              maxWidth: '480px',
              width: '100%',
              padding: '1.5rem',
              boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.5)',
              color: '#f8fafc',
              animation: 'scaleIn 0.2s cubic-bezier(0.16, 1, 0.3, 1)'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.75rem' }}>
              <span style={{ fontSize: '1.5rem' }}>
                {confirmState.type === 'danger' ? '⚠️' : confirmState.type === 'warning' ? '⚠️' : '❓'}
              </span>
              <h3 style={{ margin: 0, fontSize: '1.15rem', fontWeight: 600, color: '#f8fafc' }}>
                {confirmState.title}
              </h3>
            </div>
            <div style={{ fontSize: '0.9rem', color: '#cbd5e1', lineHeight: 1.5, marginBottom: '1.5rem', whiteSpace: 'pre-wrap' }}>
              {confirmState.message}
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                type="button"
                onClick={() => handleConfirmClose(false)}
                style={{
                  padding: '0.55rem 1rem',
                  borderRadius: '6px',
                  border: '1px solid #475569',
                  backgroundColor: '#334155',
                  color: '#f1f5f9',
                  fontWeight: 500,
                  cursor: 'pointer',
                  fontSize: '0.875rem'
                }}
              >
                {confirmState.cancelText}
              </button>
              <button
                type="button"
                onClick={() => handleConfirmClose(true)}
                style={{
                  padding: '0.55rem 1.15rem',
                  borderRadius: '6px',
                  border: 'none',
                  backgroundColor: confirmState.type === 'danger'
                    ? '#dc2626'
                    : confirmState.type === 'warning'
                      ? '#d97706'
                      : '#2563eb',
                  color: '#ffffff',
                  fontWeight: 600,
                  cursor: 'pointer',
                  fontSize: '0.875rem'
                }}
              >
                {confirmState.confirmText}
              </button>
            </div>
          </div>
        </div>,
        document.body
      )}
    </FeedbackContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(FeedbackContext);
  if (!context) {
    throw new Error('useToast must be used within a FeedbackProvider');
  }
  return {
    showToast: context.showToast,
    showSuccess: context.showSuccess,
    showError: context.showError,
    showWarning: context.showWarning,
    showInfo: context.showInfo
  };
};

export const useConfirm = () => {
  const context = useContext(FeedbackContext);
  if (!context) {
    throw new Error('useConfirm must be used within a FeedbackProvider');
  }
  return context.confirm;
};

export const useFeedback = () => {
  const context = useContext(FeedbackContext);
  if (!context) {
    throw new Error('useFeedback must be used within a FeedbackProvider');
  }
  return context;
};
