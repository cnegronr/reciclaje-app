package cl.reciclajelitoral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionStatusResponse {
    private boolean active;
    private boolean emailUpdated;
    private boolean passwordUpdated;
    private boolean deactivated;
    private String message;
}
