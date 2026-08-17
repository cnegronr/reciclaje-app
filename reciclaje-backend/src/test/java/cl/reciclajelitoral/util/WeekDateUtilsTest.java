package cl.reciclajelitoral.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeekDateUtilsTest {

    @Test
    @DisplayName("Domingo antes de las 20:00 hrs debe pertenecer a la semana actual")
    void domingoAntesDeLas20PerteneceASemanaActual() {
        // Domingo 16 de Agosto de 2026, 18:25:51
        LocalDateTime domingoTarde = LocalDateTime.of(2026, 8, 16, 18, 25, 51);
        int week = WeekDateUtils.getWeekNumber(domingoTarde);
        assertEquals(33, week, "Domingo 16 de Agosto a las 18:25 hrs debe ser la Semana 33");
    }

    @Test
    @DisplayName("Domingo despues de las 20:00 hrs debe pertenecer a la semana siguiente (corte de ciclo)")
    void domingoDespuesDeLas20PerteneceASemanaSiguiente() {
        // Domingo 16 de Agosto de 2026, 20:15:00
        LocalDateTime domingoNoche = LocalDateTime.of(2026, 8, 16, 20, 15, 0);
        int week = WeekDateUtils.getWeekNumber(domingoNoche);
        assertEquals(34, week, "Domingo 16 de Agosto a las 20:15 hrs debe pertenecer a la Semana 34");
    }

    @Test
    @DisplayName("Lunes debe pertenecer a la nueva semana ISO")
    void lunesPerteneceANuevaSemana() {
        // Lunes 17 de Agosto de 2026, 09:00:00
        LocalDateTime lunesManana = LocalDateTime.of(2026, 8, 17, 9, 0, 0);
        int week = WeekDateUtils.getWeekNumber(lunesManana);
        assertEquals(34, week, "Lunes 17 de Agosto debe ser la Semana 34");
    }

    @Test
    @DisplayName("Fecha limite semanal debe calcular el domingo 20:00 hrs correctamente")
    void calcularFechaLimiteSemanalCorrectamente() {
        LocalDateTime lunesManana = LocalDateTime.of(2026, 8, 17, 9, 0, 0);
        LocalDateTime limite = WeekDateUtils.calcularFechaLimiteSemanal(lunesManana);
        assertEquals(LocalDateTime.of(2026, 8, 23, 20, 0, 0), limite);
    }
}
