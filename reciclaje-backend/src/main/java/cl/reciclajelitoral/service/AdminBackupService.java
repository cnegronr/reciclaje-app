package cl.reciclajelitoral.service;

import cl.reciclajelitoral.entity.*;
import cl.reciclajelitoral.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public byte[] generateSqlDump() {
        StringBuilder sql = new StringBuilder();
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sql.append("""
                -- ==========================================================================
                -- RESPALDO DE BASE DE DATOS RECICLAJE LITORAL
                -- Generado el: %s
                -- ==========================================================================

                SET statement_timeout = 0;
                SET client_encoding = 'UTF8';

                """.formatted(nowStr));

        // 1. Usuarios
        List<Usuario> usuarios = usuarioRepository.findAll();
        sql.append("-- 1. Tabla: usuarios (%d registros)\n".formatted(usuarios.size()));
        for (Usuario u : usuarios) {
            sql.append("""
                    INSERT INTO usuarios (id, nombre, email, password_hash, rol, activo, creado_en) VALUES (%d, %s, %s, %s, %s, %b, %s) ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre, email = EXCLUDED.email, password_hash = EXCLUDED.password_hash, rol = EXCLUDED.rol, activo = EXCLUDED.activo;
                    """.formatted(
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
        sql.append("-- 2. Tabla: comunas (%d registros)\n".formatted(comunas.size()));
        for (Comuna c : comunas) {
            sql.append("""
                    INSERT INTO comunas (id, nombre, codigo_region) VALUES (%d, %s, %s) ON CONFLICT (id) DO UPDATE SET nombre = EXCLUDED.nombre, codigo_region = EXCLUDED.codigo_region;
                    """.formatted(
                    c.getId(),
                    escapeSql(c.getNombre()),
                    escapeSql(c.getCodigoRegion())
            ));
        }
        sql.append("\n");

        // 3. Contenedores
        List<Contenedor> contenedores = contenedorRepository.findAll();
        sql.append("-- 3. Tabla: contenedores (%d registros)\n".formatted(contenedores.size()));
        for (Contenedor cont : contenedores) {
            sql.append("""
                    INSERT INTO contenedores (id, comuna_id, nombre_punto, ubicacion_descripcion, categoria, kilos_maximos, url_google_maps, latitud, longitud, activo) VALUES (%d, %s, %s, %s, %s, %s, %s, %s, %s, %b) ON CONFLICT (id) DO UPDATE SET comuna_id = EXCLUDED.comuna_id, nombre_punto = EXCLUDED.nombre_punto, ubicacion_descripcion = EXCLUDED.ubicacion_descripcion, categoria = EXCLUDED.categoria, kilos_maximos = EXCLUDED.kilos_maximos, url_google_maps = EXCLUDED.url_google_maps, latitud = EXCLUDED.latitud, longitud = EXCLUDED.longitud, activo = EXCLUDED.activo;
                    """.formatted(
                    cont.getId(),
                    cont.getComuna() != null ? cont.getComuna().getId().toString() : "NULL",
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
        sql.append("-- 4. Tabla: asignaciones_inspector (%d registros)\n".formatted(asignaciones.size()));
        for (AsignacionInspector a : asignaciones) {
            sql.append("""
                    INSERT INTO asignaciones_inspector (id, inspector_id, comuna_id) VALUES (%d, %s, %s) ON CONFLICT (id) DO UPDATE SET inspector_id = EXCLUDED.inspector_id, comuna_id = EXCLUDED.comuna_id;
                    """.formatted(
                    a.getId(),
                    a.getInspector() != null ? a.getInspector().getId().toString() : "NULL",
                    a.getComuna() != null ? a.getComuna().getId().toString() : "NULL"
            ));
        }
        sql.append("\n");

        // 5. Inspecciones Semanales
        List<InspeccionSemanal> inspecciones = inspeccionSemanalRepository.findAll();
        sql.append("-- 5. Tabla: inspecciones_semanales (%d registros)\n".formatted(inspecciones.size()));
        for (InspeccionSemanal ins : inspecciones) {
            sql.append("""
                    INSERT INTO inspecciones_semanales (id, comuna_id, inspector_id, inspector_asociado_id, tipo_ruta, semana_numero, anio, fecha_limite, estado, respaldo_estado_previo, tiene_respaldo_limpieza, creado_en) VALUES (%d, %s, %s, %s, %s, %d, %d, %s, %s, %s, %b, %s) ON CONFLICT (id) DO UPDATE SET comuna_id = EXCLUDED.comuna_id, inspector_id = EXCLUDED.inspector_id, inspector_asociado_id = EXCLUDED.inspector_asociado_id, tipo_ruta = EXCLUDED.tipo_ruta, semana_numero = EXCLUDED.semana_numero, anio = EXCLUDED.anio, fecha_limite = EXCLUDED.fecha_limite, estado = EXCLUDED.estado, respaldo_estado_previo = EXCLUDED.respaldo_estado_previo, tiene_respaldo_limpieza = EXCLUDED.tiene_respaldo_limpieza, creado_en = EXCLUDED.creado_en;
                    """.formatted(
                    ins.getId(),
                    ins.getComuna() != null ? ins.getComuna().getId().toString() : "NULL",
                    ins.getInspector() != null ? ins.getInspector().getId().toString() : "NULL",
                    ins.getInspectorAsociado() != null ? ins.getInspectorAsociado().getId().toString() : "NULL",
                    escapeSql(ins.getTipoRuta() != null ? ins.getTipoRuta().name() : "INSPECTOR"),
                    ins.getSemanaNumero(),
                    ins.getAnio(),
                    escapeTimestamp(ins.getFechaLimite()),
                    escapeSql(ins.getEstado() != null ? ins.getEstado().name() : "EN_PROGRESO"),
                    escapeSql(ins.getRespaldoEstadoPrevio()),
                    Boolean.TRUE.equals(ins.getTieneRespaldoLimpieza()),
                    escapeTimestamp(ins.getCreadoEn())
            ));
        }
        sql.append("\n");

        // 6. Detalle Inspecciones
        List<DetalleInspeccion> detalles = detalleInspeccionRepository.findAll();
        sql.append("-- 6. Tabla: detalle_inspecciones (%d registros)\n".formatted(detalles.size()));
        for (DetalleInspeccion d : detalles) {
            sql.append("""
                    INSERT INTO detalle_inspecciones (id, inspeccion_semanal_id, contenedor_id, creado_por_usuario_id, actualizado_por_usuario_id, porcentaje_estimado, kilos_calculados, porcentaje_estimado_inicial, kilos_calculados_inicial, visitado, fecha_hora_inicial, fecha_hora_actualizacion, observaciones, observaciones_inicial) VALUES (%d, %s, %s, %s, %s, %s, %s, %s, %s, %b, %s, %s, %s, %s) ON CONFLICT (id) DO UPDATE SET inspeccion_semanal_id = EXCLUDED.inspeccion_semanal_id, contenedor_id = EXCLUDED.contenedor_id, creado_por_usuario_id = EXCLUDED.creado_por_usuario_id, actualizado_por_usuario_id = EXCLUDED.actualizado_por_usuario_id, porcentaje_estimado = EXCLUDED.porcentaje_estimado, kilos_calculados = EXCLUDED.kilos_calculados, porcentaje_estimado_inicial = EXCLUDED.porcentaje_estimado_inicial, kilos_calculados_inicial = EXCLUDED.kilos_calculados_inicial, visitado = EXCLUDED.visitado, fecha_hora_inicial = EXCLUDED.fecha_hora_inicial, fecha_hora_actualizacion = EXCLUDED.fecha_hora_actualizacion, observaciones = EXCLUDED.observaciones, observaciones_inicial = EXCLUDED.observaciones_inicial;
                    """.formatted(
                    d.getId(),
                    d.getInspeccionSemanal() != null ? d.getInspeccionSemanal().getId().toString() : "NULL",
                    d.getContenedor() != null ? d.getContenedor().getId().toString() : "NULL",
                    d.getCreadoPorUsuario() != null ? d.getCreadoPorUsuario().getId().toString() : "NULL",
                    d.getActualizadoPorUsuario() != null ? d.getActualizadoPorUsuario().getId().toString() : "NULL",
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
        sql.append("-- 7. Tabla: actualizaciones_detalle (%d registros)\n".formatted(actualizaciones.size()));
        for (ActualizacionDetalle act : actualizaciones) {
            sql.append("""
                    INSERT INTO actualizaciones_detalle (id, detalle_inspeccion_id, usuario_id, porcentaje_estimado, kilos_calculados, observaciones, fecha_hora) VALUES (%d, %s, %s, %s, %s, %s, %s) ON CONFLICT (id) DO UPDATE SET detalle_inspeccion_id = EXCLUDED.detalle_inspeccion_id, usuario_id = EXCLUDED.usuario_id, porcentaje_estimado = EXCLUDED.porcentaje_estimado, kilos_calculados = EXCLUDED.kilos_calculados, observaciones = EXCLUDED.observaciones, fecha_hora = EXCLUDED.fecha_hora;
                    """.formatted(
                    act.getId(),
                    act.getDetalleInspeccion() != null ? act.getDetalleInspeccion().getId().toString() : "NULL",
                    act.getUsuario() != null ? act.getUsuario().getId().toString() : "NULL",
                    act.getPorcentajeEstimado() != null ? act.getPorcentajeEstimado().toString() : "NULL",
                    act.getKilosCalculados() != null ? act.getKilosCalculados().toString() : "0.00",
                    escapeSql(act.getObservaciones()),
                    escapeTimestamp(act.getFechaHora())
            ));
        }
        sql.append("\n");

        // 8. Fotos Inspección
        List<FotoInspeccion> fotos = fotoInspeccionRepository.findAll();
        sql.append("-- 8. Tabla: fotos_inspeccion (%d registros)\n".formatted(fotos.size()));
        for (FotoInspeccion f : fotos) {
            sql.append("""
                    INSERT INTO fotos_inspeccion (id, detalle_inspeccion_id, actualizacion_detalle_id, usuario_id, momento, url_foto, creado_en) VALUES (%d, %s, %s, %s, %s, %s, %s) ON CONFLICT (id) DO UPDATE SET detalle_inspeccion_id = EXCLUDED.detalle_inspeccion_id, actualizacion_detalle_id = EXCLUDED.actualizacion_detalle_id, usuario_id = EXCLUDED.usuario_id, momento = EXCLUDED.momento, url_foto = EXCLUDED.url_foto, creado_en = EXCLUDED.creado_en;
                    """.formatted(
                    f.getId(),
                    f.getDetalleInspeccion() != null ? f.getDetalleInspeccion().getId().toString() : "NULL",
                    f.getActualizacionDetalle() != null ? f.getActualizacionDetalle().getId().toString() : "NULL",
                    f.getUsuario() != null ? f.getUsuario().getId().toString() : "NULL",
                    escapeSql(f.getMomento() != null ? f.getMomento().name() : "INICIAL_ANTES"),
                    escapeSql(f.getUrlFoto()),
                    escapeTimestamp(f.getCreadoEn())
            ));
        }
        sql.append("\n");

        // 9. Outbox Events
        List<OutboxEvent> outboxEvents = outboxEventRepository.findAll();
        sql.append("-- 9. Tabla: outbox_events (%d registros)\n".formatted(outboxEvents.size()));
        for (OutboxEvent o : outboxEvents) {
            sql.append("""
                    INSERT INTO outbox_events (id, aggregate_type, aggregate_id, event_type, payload, status, created_at) VALUES (%d, %s, %s, %s, %s, %s, %s) ON CONFLICT (id) DO UPDATE SET aggregate_type = EXCLUDED.aggregate_type, aggregate_id = EXCLUDED.aggregate_id, event_type = EXCLUDED.event_type, payload = EXCLUDED.payload, status = EXCLUDED.status, created_at = EXCLUDED.created_at;
                    """.formatted(
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
        sql.append("""
                -- 10. Actualización de Secuencias PostgreSQL
                SELECT setval('usuarios_id_seq', COALESCE((SELECT MAX(id) FROM usuarios), 1));
                SELECT setval('comunas_id_seq', COALESCE((SELECT MAX(id) FROM comunas), 1));
                SELECT setval('contenedores_id_seq', COALESCE((SELECT MAX(id) FROM contenedores), 1));
                SELECT setval('asignaciones_inspector_id_seq', COALESCE((SELECT MAX(id) FROM asignaciones_inspector), 1));
                SELECT setval('inspecciones_semanales_id_seq', COALESCE((SELECT MAX(id) FROM inspecciones_semanales), 1));
                SELECT setval('detalle_inspecciones_id_seq', COALESCE((SELECT MAX(id) FROM detalle_inspecciones), 1));
                SELECT setval('actualizaciones_detalle_id_seq', COALESCE((SELECT MAX(id) FROM actualizaciones_detalle), 1));
                SELECT setval('fotos_inspeccion_id_seq', COALESCE((SELECT MAX(id) FROM fotos_inspeccion), 1));
                SELECT setval('outbox_events_id_seq', COALESCE((SELECT MAX(id) FROM outbox_events), 1));
                """);

        return sql.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public void restoreSqlDump(byte[] sqlBytes) {
        if (sqlBytes == null || sqlBytes.length == 0) {
            throw new IllegalArgumentException("El archivo SQL de respaldo está vacío");
        }

        log.info("Iniciando proceso de restauración de base de datos SQL (tamaño: {} bytes)...", sqlBytes.length);

        // Desactivar temporalmente los chequeos de claves foráneas si la sesión lo permite
        try {
            jdbcTemplate.execute("SET session_replication_role = 'replica'");
            log.info("session_replication_role ajustado exitosamente a 'replica'");
        } catch (Exception e) {
            log.warn("Aviso: no se pudo establecer session_replication_role = 'replica': {}", e.getMessage());
        }

        // Limpiar tablas existentes en orden inverso de dependencias FK
        String[] tablesInReverseOrder = {
                "fotos_inspeccion",
                "actualizaciones_detalle",
                "detalle_inspecciones",
                "inspecciones_semanales",
                "asignaciones_inspector",
                "outbox_events",
                "contenedores",
                "comunas",
                "usuarios"
        };

        for (String table : tablesInReverseOrder) {
            try {
                jdbcTemplate.execute("DELETE FROM " + table);
                log.info("Tabla '{}' limpiada exitosamente antes de restaurar.", table);
            } catch (Exception e) {
                log.error("Error crítico al limpiar tabla {} antes de la restauración: {}", table, e.getMessage());
                throw new IllegalStateException("Falló la preparación de la base de datos al limpiar tabla: " + table, e);
            }
        }

        // Parsear sentencias SQL respetando comillas simples en cadenas (URLs, observaciones, etc.)
        String sqlScript = new String(sqlBytes, StandardCharsets.UTF_8);
        List<String> statements = parseSqlScript(sqlScript);

        int index = 0;
        for (String stmt : statements) {
            index++;
            try {
                jdbcTemplate.execute(stmt);
            } catch (Exception e) {
                String snippet = stmt.length() > 120 ? stmt.substring(0, 120) + "..." : stmt;
                log.error("Error fatal al ejecutar sentencia SQL #{} [{}]: {}", index, snippet, e.getMessage());
                throw new IllegalStateException(String.format(
                        "Error fatal durante la restauración en la sentencia #%d [%s]: %s",
                        index, snippet, e.getMessage()
                ), e);
            }
        }

        // Restaurar session_replication_role a origin
        try {
            jdbcTemplate.execute("SET session_replication_role = 'origin'");
            log.info("session_replication_role restaurado exitosamente a 'origin'");
        } catch (Exception e) {
            log.warn("Aviso al restaurar session_replication_role a 'origin': {}", e.getMessage());
        }

        if (entityManager != null) {
            try {
                entityManager.clear();
                log.info("Contexto de persistencia JPA (EntityManager) limpiado tras la restauración.");
            } catch (Exception e) {
                log.warn("Aviso al limpiar EntityManager: {}", e.getMessage());
            }
        }

        log.info("Restauración de base de datos finalizada exitosamente. Total sentencias ejecutadas: {}.", statements.size());
    }

    private List<String> parseSqlScript(String sqlScript) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;

        String[] lines = sqlScript.split("\\r?\\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            // Descartar líneas de comentarios puros fuera de literales de cadena
            if (trimmedLine.startsWith("--") && !inString) {
                continue;
            }

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);

                if (c == '\'') {
                    // Manejar comillas escapadas ('')
                    if (inString && i + 1 < line.length() && line.charAt(i + 1) == '\'') {
                        current.append("''");
                        i++;
                        continue;
                    }
                    inString = !inString;
                    current.append(c);
                } else if (c == ';' && !inString) {
                    String stmt = current.toString().trim();
                    if (!stmt.isEmpty()) {
                        statements.add(stmt);
                    }
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
            if (inString) {
                current.append("\n");
            } else {
                current.append(" ");
            }
        }

        String remaining = current.toString().trim();
        if (!remaining.isEmpty()) {
            statements.add(remaining);
        }

        return statements;
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
