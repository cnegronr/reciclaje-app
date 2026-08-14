package cl.reciclajelitoral.dto;

import cl.reciclajelitoral.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    private String email;

    private String password;

    private Rol rol;

    private Boolean activo;

    private List<Long> comunaIds;

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Rol getRol() { return rol; }
    public Boolean getActivo() { return activo; }
    public List<Long> getComunaIds() { return comunaIds; }
}
