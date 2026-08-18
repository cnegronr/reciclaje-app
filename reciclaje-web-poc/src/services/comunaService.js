import { API_BASE_URL, getAuthHeaders } from './apiConfig';

export const comunaService = {
  obtenerComunas: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/comunas`, {
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
            contenedores: (c.contenedores || []).map((cont) => ({
              id: String(cont.id),
              backendId: cont.id,
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
  getComunas: async function () {
    return this.obtenerComunas();
  }
};
