package cl.reciclajelitoral.dto;

import cl.reciclajelitoral.entity.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAdminDTO {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private Boolean activo;
    private List<Long> comunaIds;
    private List<String> comunaNombres;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public Rol getRol() { return rol; }
    public Boolean getActivo() { return activo; }
    public List<Long> getComunaIds() { return comunaIds; }
    public List<String> getComunaNombres() { return comunaNombres; }
}
