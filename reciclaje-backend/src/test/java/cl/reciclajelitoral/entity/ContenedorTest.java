package cl.reciclajelitoral.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ContenedorTest {

    @ParameterizedTest(name = "Categoría EMPRESA (500kg máx) al {0}% debe calcular {1}kg")
    @CsvSource({
        "0, 0.0",
        "50, 250.0",
        "75, 375.0",
        "100, 500.0"
    })
    @DisplayName("Debe calcular kilos correctamente para categoría EMPRESA (Máx 500kg)")
    void calcularKilosEmpresa(double porcentaje, double kilosEsperados) {
        Contenedor contenedor = Contenedor.builder()
                .nombrePunto("Punto Empresa Test")
                .categoria(CategoriaContenedor.EMPRESA)
                .kilosMaximos(BigDecimal.valueOf(500))
                .build();

        BigDecimal resultado = contenedor.calcularKilos(BigDecimal.valueOf(porcentaje));

        assertEquals(0, BigDecimal.valueOf(kilosEsperados).compareTo(resultado));
    }

    @ParameterizedTest(name = "Categoría MUNICIPAL (1000kg máx) al {0}% debe calcular {1}kg")
    @CsvSource({
        "0, 0.0",
        "50, 500.0",
        "75, 750.0",
        "100, 1000.0"
    })
    @DisplayName("Debe calcular kilos correctamente para categoría MUNICIPAL (Máx 1000kg)")
    void calcularKilosMunicipal(double porcentaje, double kilosEsperados) {
        Contenedor contenedor = Contenedor.builder()
                .nombrePunto("Punto Municipal Test")
                .categoria(CategoriaContenedor.MUNICIPAL)
                .kilosMaximos(BigDecimal.valueOf(1000))
                .build();

        BigDecimal resultado = contenedor.calcularKilos(BigDecimal.valueOf(porcentaje));

        assertEquals(0, BigDecimal.valueOf(kilosEsperados).compareTo(resultado));
    }

    @Test
    @DisplayName("Debe retornar 0 si el porcentaje ingresado es null")
    void calcularKilosPorcentajeNull() {
        Contenedor contenedor = Contenedor.builder()
                .categoria(CategoriaContenedor.EMPRESA)
                .build();

        BigDecimal resultado = contenedor.calcularKilos(null);

        assertEquals(BigDecimal.ZERO, resultado);
    }

    @Test
    @DisplayName("Debe asignar y obtener el sector correctamente")
    void getAndSetSector() {
        Contenedor contenedor = new Contenedor();
        contenedor.setSector("ALGARROBO SUR");
        assertEquals("ALGARROBO SUR", contenedor.getSector());
    }
}
