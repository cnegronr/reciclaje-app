package cl.reciclajelitoral.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

public class WeekDateUtils {

    public static final ZoneId CHILE_ZONE = ZoneId.of("America/Santiago");

    public static ZonedDateTime nowInChile() {
        return ZonedDateTime.now(CHILE_ZONE);
    }

    public static int getWeekNumber(LocalDateTime dt) {
        if (dt == null) return -1;
        ZonedDateTime zdt = dt.atZone(CHILE_ZONE);
        if (zdt.getDayOfWeek() == DayOfWeek.SUNDAY && zdt.getHour() >= 20) {
            zdt = zdt.plusHours(4);
        }
        return zdt.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    public static int getYear(LocalDateTime dt) {
        if (dt == null) return -1;
        ZonedDateTime zdt = dt.atZone(CHILE_ZONE);
        if (zdt.getDayOfWeek() == DayOfWeek.SUNDAY && zdt.getHour() >= 20) {
            zdt = zdt.plusHours(4);
        }
        return zdt.getYear();
    }

    public static int getCurrentWeekNumber() {
        return getWeekNumber(nowInChile().toLocalDateTime());
    }

    public static int getCurrentYear() {
        return getYear(nowInChile().toLocalDateTime());
    }

    public static LocalDateTime calcularFechaLimiteSemanal(LocalDateTime ahora) {
        if (ahora == null) ahora = nowInChile().toLocalDateTime();
        LocalDateTime domingo20 = ahora.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .withHour(20).withMinute(0).withSecond(0).withNano(0);

        if (ahora.isAfter(domingo20)) {
            domingo20 = domingo20.plusWeeks(1);
        }
        return domingo20;
    }
}
