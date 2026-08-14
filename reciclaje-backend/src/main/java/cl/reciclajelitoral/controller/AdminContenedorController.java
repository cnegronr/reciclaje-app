package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.ContainerAdminDTO;
import cl.reciclajelitoral.dto.CreateContainerRequest;
import cl.reciclajelitoral.dto.UpdateContainerRequest;
import cl.reciclajelitoral.service.AdminContenedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/containers")
@RequiredArgsConstructor
public class AdminContenedorController {

    private final AdminContenedorService adminContenedorService;

    @GetMapping
    public ResponseEntity<List<ContainerAdminDTO>> getAllContainers() {
        return ResponseEntity.ok(adminContenedorService.getAllContainers());
    }

    @PostMapping
    public ResponseEntity<ContainerAdminDTO> createContainer(@Valid @RequestBody CreateContainerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminContenedorService.createContainer(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContainerAdminDTO> updateContainer(@PathVariable Long id, @Valid @RequestBody UpdateContainerRequest req) {
        return ResponseEntity.ok(adminContenedorService.updateContainer(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContainer(@PathVariable Long id) {
        adminContenedorService.deleteContainer(id);
        return ResponseEntity.noContent().build();
    }
}
