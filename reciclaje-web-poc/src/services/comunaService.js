import { API_BASE_URL, getAuthHeaders } from './apiConfig';

export const comunaService = {
  obtenerComunas: async (usuarioId) => {
    try {
      const url = usuarioId ? `${API_BASE_URL}/comunas?usuarioId=${usuarioId}` : `${API_BASE_URL}/comunas`;
      const response = await fetch(url, {
        headers: getAuthHeaders()
      });

      if (response.ok) {
        const comunasAPI = await response.json();
        if (comunasAPI && comunasAPI.length > 0) {
          // Mapear respuesta DTO del backend al formato del frontend
          return comunasAPI.map((c) => ({
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
              maxKilos: Number(cont.kilosMaximos),
              urlGoogleMaps: cont.urlGoogleMaps,
              lat: Number(cont.latitud),
              lng: Number(cont.longitud)
            }))
          }));
        }
      }
    } catch (err) {
      console.error('Error al cargar comunas desde la API REST del backend:', err);
    }
    return [];
  },
  getComunas: async function (usuarioId) {
    return this.obtenerComunas(usuarioId);
  }
};
