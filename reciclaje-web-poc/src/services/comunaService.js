import { API_BASE_URL, getAuthHeaders } from './apiConfig';

// Cache en memoria liviano con TTL y deduplicación de peticiones concurrentes en vuelo
const cache = new Map();
const inFlightRequests = new Map();
const CACHE_TTL_MS = 30000; // 30 segundos

export const comunaService = {
  obtenerComunas: async (usuarioId) => {
    const cacheKey = usuarioId ? `comunas_${usuarioId}` : 'comunas_all';

    // 1. Devolver desde caché si aún es válido
    const cached = cache.get(cacheKey);
    if (cached && Date.now() - cached.timestamp < CACHE_TTL_MS) {
      return cached.data;
    }

    // 2. Si ya hay una petición idéntica en vuelo, reutilizar la misma promesa
    if (inFlightRequests.has(cacheKey)) {
      return inFlightRequests.get(cacheKey);
    }

    const fetchPromise = (async () => {
      try {
        const url = usuarioId ? `${API_BASE_URL}/comunas?usuarioId=${usuarioId}` : `${API_BASE_URL}/comunas`;
        const response = await fetch(url, {
          headers: getAuthHeaders()
        });

        if (response.ok) {
          const comunasAPI = await response.json();
          if (comunasAPI && comunasAPI.length > 0) {
            // Mapear respuesta DTO del backend al formato del frontend
            const mapped = comunasAPI.map((c) => ({
              id: c.id ? String(c.id) : c.nombre.toLowerCase().replace(/\s+/g, '-'),
              backendId: c.id,
              nombre: c.nombre,
              region: c.codigoRegion === 'V' ? 'Litoral Central' : c.codigoRegion,
              inspectorAsociadoId: c.inspectorAsociadoId,
              inspectorAsociadoNombre: c.inspectorAsociadoNombre,
              contenedores: (c.contenedores || []).map((cont) => ({
                id: String(cont.id),
                backendId: cont.id,
                inspectorAsociadoNombre: c.inspectorAsociadoNombre,
                sector: cont.sector,
                nombrePunto: cont.nombrePunto,
                ubicacion: cont.ubicacionDescripcion,
                categoria: cont.categoria,
                urlGoogleMaps: cont.urlGoogleMaps || '',
                lat: cont.latitud != null ? Number(cont.latitud) : null,
                lng: cont.longitud != null ? Number(cont.longitud) : null
              }))
            }));

            cache.set(cacheKey, { data: mapped, timestamp: Date.now() });
            return mapped;
          }
        }
      } catch (err) {
        console.error('Error al cargar comunas desde la API REST del backend:', err);
      } finally {
        inFlightRequests.delete(cacheKey);
      }
      return [];
    })();

    inFlightRequests.set(cacheKey, fetchPromise);
    return fetchPromise;
  },

  getComunas: async function (usuarioId) {
    return this.obtenerComunas(usuarioId);
  },

  invalidarCache: () => {
    cache.clear();
    inFlightRequests.clear();
  }
};
