// Servicio de gestión de inspecciones semanales y persistencia en LocalStorage / API

const INSPECTIONS_STORAGE_KEY = 'reciclaje_inspecciones_semanales_v1';

// Función helper para calcular el próximo Domingo a las 20:00 hrs de la semana en curso
export const getDeadlineCurrentWeek = () => {
  const now = new Date();
  const dayOfWeek = now.getDay(); // 0 = Domingo, 1 = Lunes, ...
  const distanceToSunday = (0 - dayOfWeek + 7) % 7;
  
  const sunday = new Date(now);
  sunday.setDate(now.getDate() + distanceToSunday);
  sunday.setHours(20, 0, 0, 0);

  // Si hoy es domingo después de las 20:00 hrs, pasa al domingo siguiente
  if (now > sunday) {
    sunday.setDate(sunday.getDate() + 7);
  }

  return sunday;
};

// Función para obtener número de semana ISO
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
        comunaId,
        inspectorId,
        semana: weekNum,
        anio: year,
        fechaLimite: getDeadlineCurrentWeek().toISOString(),
        estado: 'PENDIENTE', // PENDIENTE | EN_PROGRESO | FINALIZADO
        detalles: {} // key: contenedorId -> inspectionRecord
      };
    }
    return inspections[key];
  },

  saveDetalleInspeccion: (comunaId, inspectorId, contenedorId, inspectionData, isUpdateMode = false) => {
    const raw = localStorage.getItem(INSPECTIONS_STORAGE_KEY);
    const inspections = raw ? JSON.parse(raw) : {};
    const record = inspectionService.getInspeccionSemanal(comunaId, inspectorId);

    const nowIso = new Date().toISOString();
    const currentDetalle = record.detalles[contenedorId] || {};

    if (!isUpdateMode) {
      // Inspección inicial
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
      // Edición / Actualización de contenedor visitado
      // CONSERVA fotos iniciales y agrega fotos de actualización
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
