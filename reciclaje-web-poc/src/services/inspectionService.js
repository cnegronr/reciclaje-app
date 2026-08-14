import { API_BASE_URL, getAuthHeaders } from './apiConfig';

const INSPECTIONS_STORAGE_KEY = 'reciclaje_inspecciones_semanales_v2';

export const getDeadlineCurrentWeek = () => {
  const now = new Date();
  const day = now.getDay();
  const diff = now.getDate() - day + (day === 0 ? -2 : 5); // Próximo viernes a las 23:59
  const friday = new Date(now.setDate(diff));
  friday.setHours(23, 59, 59, 999);
  return friday;
};

export const getWeekNumber = (d) => {
  const date = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
  date.setUTCDate(date.getUTCDate() + 4 - (date.getUTCDay() || 7));
  const yearStart = new Date(Date.UTC(date.getUTCFullYear(), 0, 1));
  return Math.ceil(((date - yearStart) / 86400000 + 1) / 7);
};

export const inspectionService = {
  parseBackendComunaId: (comunaId) => {
    if (typeof comunaId === 'number') return comunaId;
    if (!comunaId) return 1;
    const lower = String(comunaId).toLowerCase();
    if (lower.includes('quisco')) return 1;
    if (lower.includes('algarrobo')) return 2;
    if (lower.includes('tabo')) return 3;
    if (lower.includes('cartagena') || lower.includes('carthagena')) return 4;
    if (lower.includes('san-antonio') || lower.includes('antonio')) return 5;
    if (lower.includes('santo-domingo') || lower.includes('domingo')) return 6;
    const digitsOnly = lower.replace(/[^\d]/g, '');
    const parsed = parseInt(digitsOnly, 10);
    return isNaN(parsed) || parsed <= 0 ? 1 : parsed;
  },

  parseBackendContenedorId: (contenedorId) => {
    if (typeof contenedorId === 'number') return contenedorId;
    if (!contenedorId) return 1;
    const digitsOnly = String(contenedorId).replace(/[^\d]/g, '');
    const parsed = parseInt(digitsOnly, 10);
    return isNaN(parsed) || parsed <= 0 ? 1 : parsed;
  },

  getInspeccionSemanal: async (comunaId, inspectorId, backendComunaId = null) => {
    const today = new Date();
    const weekNum = getWeekNumber(today);
    const year = today.getFullYear();

    const key = `inspeccion_${comunaId}_u${inspectorId || 1}_w${weekNum}_${year}`;
    let backendRecord = null;

    // 1. Intentar obtener datos actualizados desde la API REST de Spring Boot / PostgreSQL
    try {
      const comunaIdQuery = backendComunaId 
        ? inspectionService.parseBackendComunaId(backendComunaId) 
        : inspectionService.parseBackendComunaId(comunaId);

      const res = await fetch(`${API_BASE_URL}/inspecciones/comuna/${comunaIdQuery}?inspectorId=${inspectorId || 1}`, {
        headers: getAuthHeaders()
      });
      if (res.ok) {
        backendRecord = await res.json();
      } else {
        console.warn('GET Inspección backend retornó status:', res.status);
      }
    } catch (err) {
      console.warn('Backend API no disponible para consultar inspección, usando caché local:', err);
    }

    // 2. Si el backend responde, estructurar los detalles de inspección recibidos de PostgreSQL/S3
    if (backendRecord) {
      const detallesMap = {};
      (backendRecord.detalles || []).forEach((d) => {
        const contId = String(d.contenedorId);
        
        const fotosInicialesAntes = (d.fotos || [])
          .filter(f => f.momento === 'INICIAL_ANTES')
          .map(f => ({ id: f.id, url: f.urlFoto, creadoEn: f.creadoEn }));

        const fotosInicialesDespues = (d.fotos || [])
          .filter(f => f.momento === 'INICIAL_DESPUES')
          .map(f => ({ id: f.id, url: f.urlFoto, creadoEn: f.creadoEn }));

        // Agrupar fotos de actualización por momento y timestamp de creación (creadoEn)
        const updatePhotos = (d.fotos || []).filter(f =>
          f.momento === 'ACTUALIZACION_ANTES' || f.momento === 'ACTUALIZACION_DESPUES'
        );

        const updatesByTimestamp = {};
        updatePhotos.forEach(f => {
          const tKey = f.creadoEn || d.fechaHoraActualizacion || 'sin-fecha';
          if (!updatesByTimestamp[tKey]) {
            updatesByTimestamp[tKey] = { fechaHora: tKey, fotosAntes: [], fotosDespues: [] };
          }
          if (f.momento === 'ACTUALIZACION_ANTES') {
            updatesByTimestamp[tKey].fotosAntes.push({ id: f.id, url: f.urlFoto, usuarioNombre: f.usuarioNombre });
          } else if (f.momento === 'ACTUALIZACION_DESPUES') {
            updatesByTimestamp[tKey].fotosDespues.push({ id: f.id, url: f.urlFoto, usuarioNombre: f.usuarioNombre });
          }
        });

        const actualizacionesHistorial = Object.values(updatesByTimestamp).sort(
          (a, b) => new Date(a.fechaHora) - new Date(b.fechaHora)
        );

        const ultimaAct = actualizacionesHistorial.length > 0
          ? actualizacionesHistorial[actualizacionesHistorial.length - 1]
          : null;

        detallesMap[contId] = {
          contenedorId: contId,
          creadoPorUsuarioId: d.creadoPorUsuarioId,
          creadoPorUsuarioNombre: d.creadoPorUsuarioNombre,
          actualizadoPorUsuarioId: d.actualizadoPorUsuarioId,
          actualizadoPorUsuarioNombre: d.actualizadoPorUsuarioNombre,
          porcentajeEstimado: Number(d.porcentajeEstimado || 0),
          kilosCalculados: Number(d.kilosCalculados || 0),
          visitado: Boolean(d.visitado),
          fechaHoraInicial: d.fechaHoraInicial,
          fechaHoraActualizacion: d.fechaHoraActualizacion,
          observaciones: d.observaciones || '',
          fotosInicialesAntes,
          fotosInicialesDespues,
          actualizacionesHistorial,
          fotosActualizacionAntes: ultimaAct ? ultimaAct.fotosAntes : [],
          fotosActualizacionDespues: ultimaAct ? ultimaAct.fotosDespues : []
        };
      });

      const formattedRecord = {
        id: key,
        backendId: backendRecord.id,
        comunaId,
        inspectorId,
        inspectorAsociadoId: backendRecord.inspectorAsociadoId,
        inspectorAsociadoNombre: backendRecord.inspectorAsociadoNombre,
        rolUsuario: backendRecord.rolUsuario,
        semana: backendRecord.semanaNumero,
        anio: backendRecord.anio,
        fechaLimite: backendRecord.fechaLimite || getDeadlineCurrentWeek().toISOString(),
        estado: backendRecord.estado,
        detalles: detallesMap
      };

      // Sincronizar en caché local
      const raw = localStorage.getItem(INSPECTIONS_STORAGE_KEY);
      const inspections = raw ? JSON.parse(raw) : {};
      inspections[key] = formattedRecord;
      localStorage.setItem(INSPECTIONS_STORAGE_KEY, JSON.stringify(inspections));

      return formattedRecord;
    }

    // 3. Fallback a caché local si la API REST no respondió
    const raw = localStorage.getItem(INSPECTIONS_STORAGE_KEY);
    const inspections = raw ? JSON.parse(raw) : {};

    if (!inspections[key]) {
      inspections[key] = {
        id: key,
        backendId: null,
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

    const fotosAntesList = isUpdateMode ? inspectionData.fotosAntesActualizacion : inspectionData.fotosAntes;
    const fotosDespuesList = isUpdateMode ? inspectionData.fotosDespuesActualizacion : inspectionData.fotosDespues;

    const backendContenedorId = inspectionService.parseBackendContenedorId(contenedorId);

    // 1. Enviar registro y fotos (Base64/URLs) a la API REST de Spring Boot para subida a S3 y PostgreSQL
    try {
      const payload = {
        contenedorId: backendContenedorId,
        porcentajeEstimado: inspectionData.porcentajeEstimado,
        observaciones: inspectionData.observaciones || '',
        fotosAntesUrls: (fotosAntesList || []).map(f => f.url),
        fotosDespuesUrls: (fotosDespuesList || []).map(f => f.url),
        esActualizacion: isUpdateMode,
        usuarioId: inspectorId
      };

      const res = await fetch(`${API_BASE_URL}/inspecciones/${record.backendId || 1}/registrar`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        backendSuccess = true;
      } else {
        const errTxt = await res.text().catch(() => '');
        console.error(`Error backend status ${res.status} al guardar inspección:`, errTxt);
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
        actualizacionesHistorial: [],
        fotosActualizacionAntes: [],
        fotosActualizacionDespues: [],
        visitado: true
      };
    } else {
      const hasNewPhotos = (inspectionData.fotosAntesActualizacion && inspectionData.fotosAntesActualizacion.length > 0) ||
                           (inspectionData.fotosDespuesActualizacion && inspectionData.fotosDespuesActualizacion.length > 0);

      let nuevoHistorial = currentDetalle.actualizacionesHistorial || [];
      let fotosActAntes = currentDetalle.fotosActualizacionAntes || [];
      let fotosActDespues = currentDetalle.fotosActualizacionDespues || [];

      if (hasNewPhotos) {
        const updateEntry = {
          fechaHora: nowIso,
          fotosAntes: inspectionData.fotosAntesActualizacion || [],
          fotosDespues: inspectionData.fotosDespuesActualizacion || []
        };
        nuevoHistorial = [...nuevoHistorial, updateEntry];
        fotosActAntes = updateEntry.fotosAntes;
        fotosActDespues = updateEntry.fotosDespues;
      }

      record.detalles[contenedorId] = {
        ...currentDetalle,
        porcentajeEstimado: inspectionData.porcentajeEstimado,
        kilosCalculados: inspectionData.kilosCalculados,
        observaciones: inspectionData.observaciones || currentDetalle.observaciones,
        fechaHoraActualizacion: nowIso,
        actualizacionesHistorial: nuevoHistorial,
        fotosActualizacionAntes: fotosActAntes,
        fotosActualizacionDespues: fotosActDespues
      };
    }

    record.estado = 'EN_PROGRESO';
    inspections[record.id] = record;
    localStorage.setItem(INSPECTIONS_STORAGE_KEY, JSON.stringify(inspections));
    
    // Si la API del backend respondió, volver a consultar para obtener las URLs finales de S3 y orden de historial
    if (backendSuccess) {
      return await inspectionService.getInspeccionSemanal(comunaId, inspectorId, backendComunaId);
    }

    return record;
  },

  finalizarRutaSemanal: async (comunaId, inspectorId, backendComunaId = null) => {
    const record = await inspectionService.getInspeccionSemanal(comunaId, inspectorId, backendComunaId);

    try {
      const backendIdQuery = record.backendId || 1;
      await fetch(`${API_BASE_URL}/inspecciones/${backendIdQuery}/finalizar`, {
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
