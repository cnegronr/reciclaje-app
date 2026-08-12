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
  getInspeccionSemanal: async (comunaId, inspectorId, backendComunaId = null) => {
    const raw = localStorage.getItem(INSPECTIONS_STORAGE_KEY);
    const inspections = raw ? JSON.parse(raw) : {};
    const weekNum = getISOWeekNumber();
    const year = new Date().getFullYear();
    const key = `${inspectorId}_${comunaId}_${year}_W${weekNum}`;

    let backendRecord = null;

    // 1. Intentar obtener o crear la inspección en la base de datos PostgreSQL vía REST API
    try {
      const comId = backendComunaId || (typeof comunaId === 'number' ? comunaId : 1);
      const response = await fetch(`${API_BASE_URL}/inspecciones/comuna/${comId}?inspectorId=${inspectorId || 1}`, {
        headers: getAuthHeaders()
      });

      if (response.ok) {
        backendRecord = await response.json();
      }
    } catch (err) {
      console.warn('Backend API no disponible para consultar inspección, usando caché local:', err);
    }

    // 2. Si el backend responde, estructurar los detalles de inspección recibidos de PostgreSQL/S3
    if (backendRecord) {
      const detallesMap = {};
      (backendRecord.detalles || []).forEach((d) => {
        const contId = String(d.contenedorId);
        
        const fotosInicialesAntes = (d.fotos || []).filter(f => f.momento === 'INICIAL_ANTES').map(f => ({ id: f.id, url: f.urlFoto }));
        const fotosInicialesDespues = (d.fotos || []).filter(f => f.momento === 'INICIAL_DESPUES').map(f => ({ id: f.id, url: f.urlFoto }));
        const fotosActualizacionAntes = (d.fotos || []).filter(f => f.momento === 'ACTUALIZACION_ANTES').map(f => ({ id: f.id, url: f.urlFoto }));
        const fotosActualizacionDespues = (d.fotos || []).filter(f => f.momento === 'ACTUALIZACION_DESPUES').map(f => ({ id: f.id, url: f.urlFoto }));

        detallesMap[contId] = {
          contenedorId: contId,
          porcentajeEstimado: Number(d.porcentajeEstimado || 0),
          kilosCalculados: Number(d.kilosCalculados || 0),
          visitado: Boolean(d.visitado),
          fechaHoraInicial: d.fechaHoraInicial,
          fechaHoraActualizacion: d.fechaHoraActualizacion,
          observaciones: d.observaciones || '',
          fotosInicialesAntes,
          fotosInicialesDespues,
          fotosActualizacionAntes,
          fotosActualizacionDespues
        };
      });

      const formattedRecord = {
        id: key,
        backendId: backendRecord.id,
        comunaId,
        inspectorId,
        semana: backendRecord.semanaNumero,
        anio: backendRecord.anio,
        fechaLimite: backendRecord.fechaLimite || getDeadlineCurrentWeek().toISOString(),
        estado: backendRecord.estado,
        detalles: detallesMap
      };

      inspections[key] = formattedRecord;
      localStorage.setItem(INSPECTIONS_STORAGE_KEY, JSON.stringify(inspections));
      return formattedRecord;
    }

    // Fallback a almacenamiento local si la API está inaccesible
    if (!inspections[key]) {
      return {
        id: key,
        backendId: 1,
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

  saveDetalleInspeccion: async (comunaId, inspectorId, contenedorId, inspectionData, isUpdateMode = false, backendComunaId = null) => {
    const record = await inspectionService.getInspeccionSemanal(comunaId, inspectorId, backendComunaId);
    const nowIso = new Date().toISOString();

    let backendSuccess = false;

    // 1. Enviar registro y fotos (Base64/URLs) a la API REST de Spring Boot para subida a S3 y PostgreSQL
    try {
      const backendContenedorId = typeof contenedorId === 'number' ? contenedorId : parseInt(contenedorId, 10) || 1;
      
      const payload = {
        contenedorId: backendContenedorId,
        porcentajeEstimado: inspectionData.porcentajeEstimado,
        observaciones: inspectionData.observaciones || '',
        fotosAntesUrls: (inspectionData.fotosAntes || inspectionData.fotosAntesActualizacion || []).map(f => f.url),
        fotosDespuesUrls: (inspectionData.fotosDespues || inspectionData.fotosDespuesActualizacion || []).map(f => f.url),
        esActualizacion: isUpdateMode
      };

      const res = await fetch(`${API_BASE_URL}/inspecciones/${record.backendId || 1}/registrar`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        backendSuccess = true;
      }
    } catch (err) {
      console.warn('Error al guardar en el backend Spring Boot / S3, usando respaldo local:', err);
    }

    // 2. Actualizar estado local
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
    
    // Si la API del backend respondió, volver a consultar para obtener las URLs finales de S3
    if (backendSuccess) {
      return await inspectionService.getInspeccionSemanal(comunaId, inspectorId, backendComunaId);
    }

    return record;
  },

  finalizarRutaSemanal: async (comunaId, inspectorId, backendComunaId = null) => {
    const record = await inspectionService.getInspeccionSemanal(comunaId, inspectorId, backendComunaId);

    try {
      await fetch(`${API_BASE_URL}/inspecciones/${record.backendId || 1}/finalizar`, {
        method: 'POST',
        headers: getAuthHeaders()
      });
    } catch (err) {
      console.warn('Error al finalizar ruta en backend:', err);
    }

    const raw = localStorage.getItem(INSPECTIONS_STORAGE_KEY);
    const inspections = raw ? JSON.parse(raw) : {};
    record.estado = 'FINALIZADO';
    inspections[record.id] = record;
    localStorage.setItem(INSPECTIONS_STORAGE_KEY, JSON.stringify(inspections));
    return record;
  }
};
