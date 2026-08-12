import { API_BASE_URL, getAuthHeaders } from './apiConfig';

const INSPECTIONS_STORAGE_KEY = 'reciclaje_inspecciones_semanales_v1';

export const getDeadlineCurrentWeek = () => {
  const now = new Date();
  const dayOfWeek = now.getDay();
  const distanceToSunday = (0 - dayOfWeek + 7) % 7;
  
  const sunday = new Date(now);
  sunday.setDate(now.getDate() + distanceToSunday);
  sunday.setHours(20, 0, 0, 0);

  if (now > sunday) {
    sunday.setDate(sunday.getDate() + 7);
  }

  return sunday;
};

export const getISOWeekNumber = (d = new Date()) => {
  const date = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
  const dayNum = date.getUTCDay() || 7;
  date.setUTCDate(date.getUTCDate() + 4 - dayNum);
  const yearStart = new Date(Date.UTC(date.getUTCFullYear(), 0, 1));
  return Math.ceil((((date - yearStart) / 86400000) + 1) / 7);
};

export const inspectionService = {
  getInspeccionSemanal: (comunaId, inspectorId) => {
    const raw = localStorage.getItem(INSPECTIONS_STORAGE_KEY);
    const inspections = raw ? JSON.parse(raw) : {};
    const weekNum = getISOWeekNumber();
    const year = new Date().getFullYear();
    const key = `${inspectorId}_${comunaId}_${year}_W${weekNum}`;

    if (!inspections[key]) {
      return {
        id: key,
        backendId: 1, // ID por defecto de la inspección en el backend
        comunaId,
        inspectorId,
        semana: weekNum,
        anio: year,
        fechaLimite: getDeadlineCurrentWeek().toISOString(),
        estado: 'PENDIENTE',
        detalles: {}
      };
    }
    return inspections[key];
  },

  saveDetalleInspeccion: async (comunaId, inspectorId, contenedorId, inspectionData, isUpdateMode = false) => {
    const record = inspectionService.getInspeccionSemanal(comunaId, inspectorId);
    const nowIso = new Date().toISOString();

    // 1. Intentar enviar el registro al Backend Spring Boot vía REST API
    try {
      const backendContenedorId = typeof contenedorId === 'number' ? contenedorId : parseInt(contenedorId, 10) || 1;
      
      const payload = {
        contenedorId: backendContenedorId,
        porcentajeEstimado: inspectionData.porcentajeEstimado,
        observaciones: inspectionData.observaciones || '',
        fotosAntesUrls: (inspectionData.fotosAntes || []).map(f => f.url),
        fotosDespuesUrls: (inspectionData.fotosDespues || []).map(f => f.url),
        esActualizacion: isUpdateMode
      };

      await fetch(`${API_BASE_URL}/inspecciones/${record.backendId || 1}/registrar`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload)
      });
    } catch (err) {
      console.warn('Backend API no disponible para guardar inspección, registrando localmente:', err);
    }

    // 2. Persistir localmente en LocalStorage para garantizar sincronización offline/online
    const raw = localStorage.getItem(INSPECTIONS_STORAGE_KEY);
    const inspections = raw ? JSON.parse(raw) : {};
    const currentDetalle = record.detalles[contenedorId] || {};

    if (!isUpdateMode) {
      record.detalles[contenedorId] = {
        contenedorId,
        porcentajeEstimado: inspectionData.porcentajeEstimado,
        kilosCalculados: inspectionData.kilosCalculados,
        observaciones: inspectionData.observaciones || '',
        fotosInicialesAntes: inspectionData.fotosAntes || [],
        fotosInicialesDespues: inspectionData.fotosDespues || [],
        fechaHoraInicial: nowIso,
        fechaHoraActualizacion: null,
        fotosActualizacionAntes: [],
        fotosActualizacionDespues: [],
        visitado: true
      };
    } else {
      record.detalles[contenedorId] = {
        ...currentDetalle,
        porcentajeEstimado: inspectionData.porcentajeEstimado,
        kilosCalculados: inspectionData.kilosCalculados,
        observaciones: inspectionData.observaciones || currentDetalle.observaciones,
        fechaHoraActualizacion: nowIso,
        fotosActualizacionAntes: [
          ...(currentDetalle.fotosActualizacionAntes || []),
          ...(inspectionData.fotosAntesActualizacion || [])
        ],
        fotosActualizacionDespues: [
          ...(currentDetalle.fotosActualizacionDespues || []),
          ...(inspectionData.fotosDespuesActualizacion || [])
        ]
      };
    }

    record.estado = 'EN_PROGRESO';
    inspections[record.id] = record;
    localStorage.setItem(INSPECTIONS_STORAGE_KEY, JSON.stringify(inspections));
    return record;
  },

  finalizarRutaSemanal: (comunaId, inspectorId) => {
    const raw = localStorage.getItem(INSPECTIONS_STORAGE_KEY);
    const inspections = raw ? JSON.parse(raw) : {};
    const record = inspectionService.getInspeccionSemanal(comunaId, inspectorId);
    record.estado = 'FINALIZADO';
    inspections[record.id] = record;
    localStorage.setItem(INSPECTIONS_STORAGE_KEY, JSON.stringify(inspections));
    return record;
  }
};
