package cl.reciclajelitoral.config;

import cl.reciclajelitoral.entity.*;
import cl.reciclajelitoral.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ComunaRepository comunaRepository;
    private final ContenedorRepository contenedorRepository;
    private final AsignacionInspectorRepository asignacionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.existsByEmail("inspector@reciclajelitoral.cl")) {
            return;
        }

        // 1. Crear Usuario Inspector de Prueba
        Usuario inspector = usuarioRepository.save(Usuario.builder()
                .nombre("Carlos Valenzuela")
                .email("inspector@reciclajelitoral.cl")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .rol(Rol.INSPECTOR)
                .build());

        // 2. Crear Comunas y Contenedores Reales del Litoral Central
        Comuna elQuisco = comunaRepository.save(Comuna.builder().nombre("El Quisco").codigoRegion("V").build());
        Comuna algarrobo = comunaRepository.save(Comuna.builder().nombre("Algarrobo").codigoRegion("V").build());
        Comuna sanAntonio = comunaRepository.save(Comuna.builder().nombre("San Antonio").codigoRegion("V").build());

        // Contenedores El Quisco
        contenedorRepository.saveAll(List.of(
            Contenedor.builder()
                .comuna(elQuisco)
                .nombrePunto("EL TOTORAL")
                .ubicacionDescripcion("FRENTE AL COLEGIO EL TOTORAL")
                .categoria(CategoriaContenedor.MUNICIPAL)
                .kilosMaximos(BigDecimal.valueOf(1000))
                .urlGoogleMaps("https://maps.google.com/maps?q=-33.4203812%2C-71.6253086&z=17&hl=es")
                .latitud(BigDecimal.valueOf(-33.4203812))
                .longitud(BigDecimal.valueOf(-71.6253086))
                .build(),
            Contenedor.builder()
                .comuna(elQuisco)
                .nombrePunto("ISLA NEGRA - LA PERLA")
                .ubicacionDescripcion("AV. CENTRAL CON LA PERLA")
                .categoria(CategoriaContenedor.MUNICIPAL)
                .kilosMaximos(BigDecimal.valueOf(1000))
                .urlGoogleMaps("https://maps.google.com/maps?q=-33.4393117%2C-71.6779972&z=17&hl=es")
                .latitud(BigDecimal.valueOf(-33.4393117))
                .longitud(BigDecimal.valueOf(-71.6779972))
                .build(),
            Contenedor.builder()
                .comuna(elQuisco)
                .nombrePunto("CENTINELA - ACOPIO")
                .ubicacionDescripcion("CAMINO ANTIGUO HACIA ALGARROBO")
                .categoria(CategoriaContenedor.EMPRESA)
                .kilosMaximos(BigDecimal.valueOf(500))
                .urlGoogleMaps("https://maps.google.com/maps?q=-33.3987617%2C-71.6819992&z=17&hl=es")
                .latitud(BigDecimal.valueOf(-33.3987617))
                .longitud(BigDecimal.valueOf(-71.6819992))
                .build()
        ));

        // Contenedores San Antonio
        contenedorRepository.saveAll(List.of(
            Contenedor.builder()
                .comuna(sanAntonio)
                .nombrePunto("CALETA PESCADORES BOCA RIO MAIPO")
                .ubicacionDescripcion("BOCA RIO MAIPO C / L CABRERA TEJAS VERDES")
                .categoria(CategoriaContenedor.EMPRESA)
                .kilosMaximos(BigDecimal.valueOf(500))
                .urlGoogleMaps("https://maps.google.com/maps?q=-33.6191329%2C-71.6223373&z=17&hl=es")
                .latitud(BigDecimal.valueOf(-33.6191329))
                .longitud(BigDecimal.valueOf(-71.6223373))
                .build(),
            Contenedor.builder()
                .comuna(sanAntonio)
                .nombrePunto("ROTONDA PLAZA LA ESTRELLA")
                .ubicacionDescripcion("PLAZA LA ESTRELLA")
                .categoria(CategoriaContenedor.MUNICIPAL)
                .kilosMaximos(BigDecimal.valueOf(1000))
                .urlGoogleMaps("https://maps.google.com/maps?q=-33.6136932%2C-71.6152877&z=17&hl=es")
                .latitud(BigDecimal.valueOf(-33.6136932))
                .longitud(BigDecimal.valueOf(-71.6152877))
                .build()
        ));

        // 3. Asignar Comunas al Inspector
        asignacionRepository.save(AsignacionInspector.builder().inspector(inspector).comuna(elQuisco).build());
        asignacionRepository.save(AsignacionInspector.builder().inspector(inspector).comuna(algarrobo).build());
        asignacionRepository.save(AsignacionInspector.builder().inspector(inspector).comuna(sanAntonio).build());
    }
}
