package cl.reciclajelitoral.service;

import cl.reciclajelitoral.entity.*;
import cl.reciclajelitoral.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspeccionSemanalService {

    private final InspeccionSemanalRepository inspeccionRepository;
    private final DetalleInspeccionRepository detalleRepository;
    private final ContenedorRepository contenedorRepository;

    // Método principal que calcula la fecha límite tomando la fecha actual
    public LocalDateTime calcularFechaLimiteSemanal() {
        return calcularFechaLimiteSemanal(LocalDateTime.now());
    }

    // Sobrecarga testeable que acepta una fecha arbitraria
    public LocalDateTime calcularFechaLimiteSemanal(LocalDateTime ahora) {
        LocalDateTime domingo20 = ahora.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .withHour(20).withMinute(0).withSecond(0).withNano(0);

        if (ahora.isAfter(domingo20)) {
            domingo20 = domingo20.plusWeeks(1);
        }
        return domingo20;
    }

    @Transactional
    public InspeccionSemanal registrarOActualizarInspeccion(
            Long inspeccionSemanalId,
            Long contenedorId,
            BigDecimal porcentajeEstimado,
            String observaciones,
            List<String> fotosAntesUrls,
            List<String> fotosDespuesUrls,
            boolean esActualizacion
    ) {
        DetalleInspeccion detalle = detalleRepository.findByInspeccionSemanalIdAndContenedorId(inspeccionSemanalId, contenedorId)
                .orElseGet(() -> {
                    InspeccionSemanal inspeccion = inspeccionRepository.findById(inspeccionSemanalId)
                            .orElseThrow(() -> new IllegalArgumentException("Inspección semanal no encontrada"));
                    Contenedor contenedor = contenedorRepository.findById(contenedorId)
                            .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado"));
                    return DetalleInspeccion.builder()
                            .inspeccionSemanal(inspeccion)
                            .contenedor(contenedor)
                            .visitado(false)
                            .build();
                });

        Contenedor contenedor = detalle.getContenedor();
        BigDecimal kilos = contenedor.calcularKilos(porcentajeEstimado);

        detalle.setPorcentajeEstimado(porcentajeEstimado);
        detalle.setKilosCalculados(kilos);
        detalle.setObservaciones(observaciones);
        detalle.setVisitado(true);

        LocalDateTime ahora = LocalDateTime.now();

        if (!esActualizacion || detalle.getFechaHoraInicial() == null) {
            // INSPECCIÓN INICIAL
            detalle.setFechaHoraInicial(ahora);
            
            if (fotosAntesUrls != null) {
                fotosAntesUrls.forEach(url -> detalle.addFoto(FotoInspeccion.builder()
                        .momento(MomentoFoto.INICIAL_ANTES)
                        .urlFoto(url)
                        .creadoEn(ahora)
                        .build()));
            }
            if (fotosDespuesUrls != null) {
                fotosDespuesUrls.forEach(url -> detalle.addFoto(FotoInspeccion.builder()
                        .momento(MomentoFoto.INICIAL_DESPUES)
                        .urlFoto(url)
                        .creadoEn(ahora)
                        .build()));
            }
        } else {
            // EDICIÓN / ACTUALIZACIÓN
            detalle.setFechaHoraActualizacion(ahora);

            if (fotosAntesUrls != null) {
                fotosAntesUrls.forEach(url -> detalle.addFoto(FotoInspeccion.builder()
                        .momento(MomentoFoto.ACTUALIZACION_ANTES)
                        .urlFoto(url)
                        .creadoEn(ahora)
                        .build()));
            }
            if (fotosDespuesUrls != null) {
                fotosDespuesUrls.forEach(url -> detalle.addFoto(FotoInspeccion.builder()
                        .momento(MomentoFoto.ACTUALIZACION_DESPUES)
                        .urlFoto(url)
                        .creadoEn(ahora)
                        .build()));
            }
        }

        detalleRepository.save(detalle);
        return detalle.getInspeccionSemanal();
    }
}
