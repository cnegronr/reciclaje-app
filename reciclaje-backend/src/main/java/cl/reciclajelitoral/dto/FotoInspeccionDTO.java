package cl.reciclajelitoral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FotoInspeccionDTO {
    private Long id;
    private String momento; // INICIAL_ANTES | INICIAL_DESPUES | ACTUALIZACION_ANTES | ACTUALIZACION_DESPUES
    private String urlFoto;
    private LocalDateTime creadoEn;
    private Long usuarioId;
    private String usuarioNombre;
}
