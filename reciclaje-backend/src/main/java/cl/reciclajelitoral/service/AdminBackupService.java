package cl.reciclajelitoral.service;

import cl.reciclajelitoral.entity.*;
import cl.reciclajelitoral.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminBackupService {

    private final UsuarioRepository usuarioRepository;
    private final ComunaRepository comunaRepository;
    private final ContenedorRepository contenedorRepository;
    private final AsignacionInspectorRepository asignacionInspectorRepository;
    private final InspeccionSemanalRepository inspeccionSemanalRepository;
    private final DetalleInspeccionRepository detalleInspeccionRepository;
    private final ActualizacionDetalleRepository actualizacionDetalleRepository;
    private final FotoInspeccionRepository fotoInspeccionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public byte[] generateSqlDump() {
        StringBuilder sql = new StringBuilder();
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sql.append("-- ==========================================================================\n");
        sql.append("-- RESPALDO DE BASE DE DATOS RECLICLAJE LITORAL\n");
        sql.append("-- Generado el: ").append(nowStr).append("\n");
        sql.append("-- ==========================================================================\n\n");
        sql.append("SET statement_timeout = 0;\n");
        sql.append("SET client_encoding = 'UTF8';\n\n");

        // 1. Usuarios
        List<Usuario> usuarios = usuarioRepository.findAll();
        sql.append("-- 1. Tabla: usuarios (").append(usuarios.size()).append(" registros)\n");
        for (Usuario u : usuarios) {
            sql.append(String.format(
                    "INSERT INTO usuarios (id, nombre, email, password_hash, rol, activo, creado_en) VALUES (%d, %s, %s, %s, %s, %b, %s) ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre, email = EXCLUDED.email, password_hash = EXCLUDED.password_hash, rol = EXCLUDED.rol, activo = EXCLUDED.activo;\n",
                    u.getId(),
                    escapeSql(u.getNombre()),
                    escapeSql(u.getEmail()),
                    escapeSql(u.getPasswordHash()),
                    escapeSql(u.getRol() != null ? u.getRol().name() : "INSPECTOR"),
                    u.getActivo() != null ? u.getActivo() : true,
                    escapeTimestamp(u.getCreadoEn())
            ));
        }
        sql.append("\n");

        // 2. Comunas
        List<Comuna> comunas = comunaRepository.findAll();
        sql.append("-- 2. Tabla: comunas (").append(comunas.size()).append(" registros)\n");
        for (Comuna c : comunas) {
            sql.append(String.format(
                    "INSERT INTO comunas (id, nombre, codigo_region) VALUES (%d, %s, %s) ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre, codigo_region = EXCLUDED.codigo_region;\n",
                    c.getId(),
                    escapeSql(c.getNombre()),
                    escapeSql(c.getCodigoRegion())
            ));
        }
        sql.append("\n");

        // 3. Contenedores
        List<Contenedor> contenedores = contenedorRepository.findAll();
        sql.append("-- 3. Tabla: contenedores (").append(contenedores.size()).append(" registros)\n");
        for (Contenedor cont : contenedores) {
            sql.append(String.format(
                    "INSERT INTO contenedores (id, comuna_id, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud, activo) VALUES (%d, %d, %s, %s, %s, %s, %s, %s, %s, %b) ON CONFLICT (id) DO UPDATE SET comuna_id = EXCLUDED.comuna_id, nombre_punto = EXCLUDED.nombre_punto, ubicacion_descripcion = EXCLUDED.ubicacion_descripcion, categoria = EXCLUDED.categoria, kilos_maximos = EXCLUDED.kilos_maximos, url_google_maps = EXCLUDED.url_google_maps, latitud = EXCLUDED.latitud, longitud = EXCLUDED.longitud, activo = EXCLUDED.activo;\n",
                    cont.getId(),
                    cont.getComuna() != null ? cont.getComuna().getId() : null,
                    escapeSql(cont.getNombrePunto()),
                    escapeSql(cont.getUbicacionDescripcion()),
                    escapeSql(cont.getCategoria() != null ? cont.getCategoria().name() : "MUNICIPAL"),
                    cont.getKilosMaximos() != null ? cont.getKilosMaximos().toString() : "1000",
                    escapeSql(cont.getUrlGoogleMaps()),
                    cont.getLatitud() != null ? cont.getLatitud().toString() : "NULL",
                    cont.getLongitud() != null ? cont.getLongitud().toString() : "NULL",
                    cont.getActivo() != null ? cont.getActivo() : true
            ));
        }
        sql.append("\n");

        // 4. Asignaciones Inspector
        List<AsignacionInspector> asignaciones = asignacionInspectorRepository.findAll();
        sql.append("-- 4. Tabla: asignaciones_inspector (").append(asignaciones.size()).append(" registros)\n");
        for (AsignacionInspector a : asignaciones) {
            sql.append(String.format(
                    "INSERT INTO asignaciones_inspector (id, inspector_id, comuna_id) VALUES (%d, %d, %d) ON CONFLICT (id) DO NOTHING;\n",
                    a.getId(),
                    a.getInspector() != null ? a.getInspector().getId() : null,
                    a.getComuna() != null ? a.getComuna().getId() : null
            ));
        }
        sql.append("\n");

        // 5. Inspecciones Semanales
        List<InspeccionSemanal> inspecciones = inspeccionSemanalRepository.findAll();
        sql.append("-- 5. Tabla: inspecciones_semanales (").append(inspecciones.size()).append(" registros)\n");
        for (InspeccionSemanal ins : inspecciones) {
            sql.append(String.format(
                    "INSERT INTO inspecciones_semanales (id, comuna_id, inspector_id, inspector_asociado_id, tipo_ruta, semana_numero, anio, fecha_limite, estado, creado_en) VALUES (%d, %s, %s, %s, %s, %d, %d, %s, %s, %s) ON CONFLICT (id) DO NOTHING;\n",
                    ins.getId(),
                    ins.getComuna() != null ? ins.getComuna().getId() : "NULL",
                    ins.getInspector() != null ? ins.getInspector().getId() : "NULL",
                    ins.getInspectorAsociado() != null ? ins.getInspectorAsociado().getId() : "NULL",
                    escapeSql(ins.getTipoRuta() != null ? ins.getTipoRuta().name() : "INSPECTOR"),
                    ins.getSemanaNumero(),
                    ins.getAnio(),
                    escapeTimestamp(ins.getFechaLimite()),
                    escapeSql(ins.getEstado() != null ? ins.getEstado().name() : "EN_PROGRESO"),
                    escapeTimestamp(ins.getCreadoEn())
            ));
        }
        sql.append("\n");

        // 6. Detalle Inspecciones
        List<DetalleInspeccion> detalles = detalleInspeccionRepository.findAll();
        sql.append("-- 6. Tabla: detalle_inspecciones (").append(detalles.size()).append(" registros)\n");
        for (DetalleInspeccion d : detalles) {
            sql.append(String.format(
                    "INSERT INTO detalle_inspecciones (id, inspeccion_semanal_id, contenedor_id, creado_por_usuario_id, actualizado_por_usuario_id, porcentaje_estimado, kilos_calculados, porcentaje_estimado_inicial, kilos_calculados_inicial, visitado, fecha_hora_inicial, fecha_hora_actualizacion, observaciones, observaciones_inicial) VALUES (%d, %s, %s, %s, %s, %s, %s, %s, %s, %b, %s, %s, %s, %s) ON CONFLICT (id) DO NOTHING;\n",
                    d.getId(),
                    d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getId() : "NULL",
                    d.getContenedor() != null ? d.getContenedor().getId() : "NULL",
                    d.getCreadoPorUsuario() != null ? d.getCreadoPorUsuario().getId() : "NULL",
                    d.getActualizadoPorUsuario() != null ? d.getActualizadoPorUsuario().getId() : "NULL",
                    d.getPorcentajeEstimado() != null ? d.getPorcentajeEstimado().toString() : "NULL",
                    d.getKilosCalculados() != null ? d.getKilosCalculados().toString() : "0.00",
                    d.getPorcentajeEstimadoInicial() != null ? d.getPorcentajeEstimadoInicial().toString() : "NULL",
                    d.getKilosCalculadosInicial() != null ? d.getKilosCalculadosInicial().toString() : "NULL",
                    d.getVisitado() != null ? d.getVisitado() : false,
                    escapeTimestamp(d.getFechaHoraInicial()),
                    escapeTimestamp(d.getFechaHoraActualizacion()),
                    escapeSql(d.getObservaciones()),
                    escapeSql(d.getObservacionesInicial())
            ));
        }
        sql.append("\n");

        // 7. Actualizaciones Detalle
        List<ActualizacionDetalle> actualizaciones = actualizacionDetalleRepository.findAll();
        sql.append("-- 7. Tabla: actualizaciones_detalle (").append(actualizaciones.size()).append(" registros)\n");
        for (ActualizacionDetalle act : actualizaciones) {
            sql.append(String.format(
                    "INSERT INTO actualizaciones_detalle (id, detalle_inspeccion_id, usuario_id, porcentaje_estimado, kilos_calculados, observaciones, fecha_hora) VALUES (%d, %s, %s, %s, %s, %s, %s) ON CONFLICT (id) DO NOTHING;\n",
                    act.getId(),
                    act.getDetalleInspeccion() != null ? act.getDetalleInspeccion().getId() : "NULL",
                    act.getUsuario() != null ? act.getUsuario().getId() : "NULL",
                    act.getPorcentajeEstimado() != null ? act.getPorcentajeEstimado().toString() : "NULL",
                    act.getKilosCalculados() != null ? act.getKilosCalculados().toString() : "0.00",
                    escapeSql(act.getObservaciones()),
                    escapeTimestamp(act.getFechaHora())
            ));
        }
        sql.append("\n");

        // 8. Fotos Inspección
        List<FotoInspeccion> fotos = fotoInspeccionRepository.findAll();
        sql.append("-- 8. Tabla: fotos_inspeccion (").append(fotos.size()).append(" registros)\n");
        for (FotoInspeccion f : fotos) {
            sql.append(String.format(
                    "INSERT INTO fotos_inspeccion (id, detalle_inspeccion_id, actualizacion_detalle_id, usuario_id, momento, url_foto, creado_en) VALUES (%d, %s, %s, %s, %s, %s, %s) ON CONFLICT (id) DO NOTHING;\n",
                    f.getId(),
                    f.getDetalleInspeccion() != null ? f.getDetalleInspeccion().getId() : "NULL",
                    f.getActualizacionDetalle() != null ? f.getActualizacionDetalle().getId() : "NULL",
                    f.getUsuario() != null ? f.getUsuario().getId() : "NULL",
                    escapeSql(f.getMomento() != null ? f.getMomento().name() : "INICIAL_ANTES"),
                    escapeSql(f.getUrlFoto()),
                    escapeTimestamp(f.getCreadoEn())
            ));
        }
        sql.append("\n");

        // 9. Outbox Events
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        sql.append("-- 9. Tabla: outbox_events (").append(outboxEvents.size()).append(" registros)\n");
        for (OutboxEvent o : outboxEvents) {
            sql.append(String.format(
                    "INSERT INTO outbox_events (id, aggregate_type, aggregate_id, event_type, payload, status, created_at) VALUES (%d, %s, %s, %s, %s, %s, %s) ON CONFLICT (id) DO NOTHING;\n",
                    o.getId(),
                    escapeSql(o.getAggregateType()),
                    escapeSql(o.getAggregateId()),
                    escapeSql(o.getEventType()),
                    escapeSql(o.getPayload()),
                    escapeSql(o.getStatus() != null ? o.getStatus() : "PENDING"),
                    escapeTimestamp(o.getCreatedAt())
            ));
        }
        sql.append("\n");

        // Reset de Secuencias PostgreSQL
        sql.append("-- 10. Actualización de Secuencias PostgreSQL\n");
        sql.append("SELECT setval('usuarios_id_seq', COALESCE((SELECT MAX(id) FROM usuarios), 1));\n");
        sql.append("SELECT setval('comunas_id_seq', COALESCE((SELECT MAX(id) FROM comunas), 1));\n");
        sql.append("SELECT setval('contenedores_id_seq', COALESCE((SELECT MAX(id) FROM contenedores), 1));\n");
        sql.append("SELECT setval('asignaciones_inspector_id_seq', COALESCE((SELECT MAX(id) FROM asignaciones_inspector), 1));\n");
        sql.append("SELECT setval('inspecciones_semanales_id_seq', COALESCE((SELECT MAX(id) FROM inspecciones_semanales), 1));\n");
        sql.append("SELECT setval('detalle_inspecciones_id_seq', COALESCE((SELECT MAX(id) FROM detalle_inspecciones), 1));\n");
        sql.append("SELECT setval('actualizaciones_detalle_id_seq', COALESCE((SELECT MAX(id) FROM actualizaciones_detalle), 1));\n");
        sql.append("SELECT setval('fotos_inspeccion_id_seq', COALESCE((SELECT MAX(id) FROM fotos_inspeccion), 1));\n");
        sql.append("SELECT setval('outbox_events_id_seq', COALESCE((SELECT MAX(id) FROM outbox_events), 1));\n");

        return sql.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public void restoreSqlDump(byte[] sqlBytes) {
        if (sqlBytes == null || sqlBytes.length == 0) {
            throw new IllegalArgumentException("El archivo SQL de respaldo está vacío");
        }

        String sqlScript = new String(sqlBytes, StandardCharsets.UTF_8);
        String[] statements = sqlScript.split(";");

        for (String stmt : statements) {
            String trimmed = stmt.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                try {
                    jdbcTemplate.execute(trimmed);
                } catch (Exception e) {
                    System.err.println("Aviso al ejecutar sentencia de restauración: " + e.getMessage());
                }
            }
        }
    }

    private String escapeSql(String val) {
        if (val == null) return "NULL";
        return "'" + val.replace("'", "''") + "'";
    }

    private String escapeTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) return "NULL";
        return "'" + dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "'";
    }
}
