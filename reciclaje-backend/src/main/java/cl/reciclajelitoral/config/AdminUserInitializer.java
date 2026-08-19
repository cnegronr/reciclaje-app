package cl.reciclajelitoral.config;

import cl.reciclajelitoral.entity.Rol;
import cl.reciclajelitoral.entity.Usuario;
import cl.reciclajelitoral.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.initial.email:admin@reciclajelitoral.cl}")
    private String adminEmail;

    @Value("${admin.initial.name:Administrador General}")
    private String adminName;

    @Value("${admin.initial.password:Password123!}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            log.info("Inicializando usuario Administrador por defecto ({})", adminEmail);
            Usuario admin = Usuario.builder()
                    .nombre(adminName)
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);
            log.info("Usuario Administrador inicial creado exitosamente.");
        }
    }
}
