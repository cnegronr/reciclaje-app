export const COMUNAS_DATA = [
  {
    id: "el-quisco",
    nombre: "El Quisco",
    region: "Litoral Central",
    contenedores: [
      {
        id: "eq-1",
        nombrePunto: "EL TOTORAL",
        ubicacion: "FRENTE AL COLEGIO EL TOTORAL (CAMPANA MUNICIPAL)",
        categoria: "MUNICIPAL",
        maxKilos: 1000,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.4203812%2C-71.6253086&z=17&hl=es",
        lat: -33.4203812,
        lng: -71.6253086
      },
      {
        id: "eq-2",
        nombrePunto: "ISLA NEGRA - LA PERLA",
        ubicacion: "AV. CENTRAL CON LA PERLA (CAMPANA MUNICIPAL)",
        categoria: "MUNICIPAL",
        maxKilos: 1000,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.4393117%2C-71.6779972&z=17&hl=es",
        lat: -33.4393117,
        lng: -71.6779972
      },
      {
        id: "eq-3",
        nombrePunto: "LOMA LINDA",
        ubicacion: "JUNTA DE VECINOS LOMA LINDA CON SANTA ROSA (CAMPANA MUNICIPAL)",
        categoria: "MUNICIPAL",
        maxKilos: 1000,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.4373254%2C-71.6797372&z=17&hl=es",
        lat: -33.4373254,
        lng: -71.6797372
      },
      {
        id: "eq-4",
        nombrePunto: "CENTINELA - ACOPIO",
        ubicacion: "CAMINO ANTIGUO HACIA ALGARROBO (EMPRESA)",
        categoria: "EMPRESA",
        maxKilos: 500,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.3987617%2C-71.6819992&z=17&hl=es",
        lat: -33.3987617,
        lng: -71.6819992
      }
    ]
  },
  {
    id: "algarrobo",
    nombre: "Algarrobo",
    region: "Litoral Central",
    contenedores: [
      {
        id: "alg-1",
        nombrePunto: "CANCHA DE FUTBOL SAN JOSE",
        ubicacion: "LOS CLAVELES / CANCHA SAN JOSE (CAMPANA MUNICIPAL)",
        categoria: "MUNICIPAL",
        maxKilos: 1000,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.364215%2C-71.668541&z=17&hl=es",
        lat: -33.364215,
        lng: -71.668541
      },
      {
        id: "alg-2",
        nombrePunto: "CONDOMINIO BAHIA DE ALGARROBO",
        ubicacion: "ACCESO PRINCIPAL CONDOMINIO (EMPRESA)",
        categoria: "EMPRESA",
        maxKilos: 500,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.371102%2C-71.672901&z=17&hl=es",
        lat: -33.371102,
        lng: -71.672901
      }
    ]
  },
  {
    id: "san-antonio",
    nombre: "San Antonio",
    region: "Litoral Central",
    contenedores: [
      {
        id: "sa-1",
        nombrePunto: "CALETA PESCADORES BOCA RIO MAIPO",
        ubicacion: "BOCA RIO MAIPO C / L CABRERA TEJAS VERDES (EMPRESA)",
        categoria: "EMPRESA",
        maxKilos: 500,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.6191329%2C-71.6223373&z=17&hl=es",
        lat: -33.6191329,
        lng: -71.6223373
      },
      {
        id: "sa-2",
        nombrePunto: "PUNTO LIMPIO CODELCO",
        ubicacion: "REGIMIENTO TEJAS VERDES (EMPRESA)",
        categoria: "EMPRESA",
        maxKilos: 500,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.6191062%2C-71.6189346&z=17&hl=es",
        lat: -33.6191062,
        lng: -71.6189346
      },
      {
        id: "sa-3",
        nombrePunto: "ROTONDA PLAZA LA ESTRELLA",
        ubicacion: "PLAZA LA ESTRELLA (CAMPANA MUNICIPAL)",
        categoria: "MUNICIPAL",
        maxKilos: 1000,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.6136932%2C-71.6152877&z=17&hl=es",
        lat: -33.6136932,
        lng: -71.6152877
      }
    ]
  },
  {
    id: "cartagena",
    nombre: "Cartagena",
    region: "Litoral Central",
    contenedores: [
      {
        id: "car-1",
        nombrePunto: "LOS ALMENDROS / ECHAURREN",
        ubicacion: "VISTA HERMOSA (EMPRESA)",
        categoria: "EMPRESA",
        maxKilos: 500,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.5537452%2C-71.6197433&z=17&hl=es",
        lat: -33.5537452,
        lng: -71.6197433
      },
      {
        id: "car-2",
        nombrePunto: "PLAYA GRANDE - GIMNASIO",
        ubicacion: "AFUERA GIMNASIO MUNICIPAL (CAMPANA MUNICIPAL)",
        categoria: "MUNICIPAL",
        maxKilos: 1000,
        urlGoogleMaps: "https://maps.google.com/maps?q=-33.550100%2C-71.608900&z=17&hl=es",
        lat: -33.550100,
        lng: -71.608900
      }
    ]
  }
];

export const TEST_USER = {
  id: "insp-001",
  nombre: "Carlos Valenzuela",
  email: "inspector@reciclajelitoral.cl",
  rol: "INSPECTOR",
  comunasAsignadas: ["el-quisco", "algarrobo", "san-antonio", "cartagena"]
};
