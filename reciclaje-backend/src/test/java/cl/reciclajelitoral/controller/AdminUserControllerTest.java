package cl.reciclajelitoral.controller;

import cl.reciclajelitoral.dto.CreateUserRequest;
import cl.reciclajelitoral.dto.UpdateUserRequest;
import cl.reciclajelitoral.dto.UserAdminDTO;
import cl.reciclajelitoral.entity.Rol;
import cl.reciclajelitoral.service.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController adminUserController;

    private UserAdminDTO sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = UserAdminDTO.builder()
                .id(1L)
                .nombre("Carlos Negrón")
                .email("inspector@reciclajelitoral.cl")
                .rol(Rol.INSPECTOR)
                .activo(true)
                .build();
    }

    @Test
    void shouldGetAllUsers() {
        when(adminUserService.getAllUsers()).thenReturn(List.of(sampleDto));

        ResponseEntity<List<UserAdminDTO>> response = adminUserController.getAllUsers(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(adminUserService).getAllUsers();
    }

    @Test
    void shouldGetActiveUsersOnly() {
        when(adminUserService.getActiveUsers()).thenReturn(List.of(sampleDto));

        ResponseEntity<List<UserAdminDTO>> response = adminUserController.getAllUsers(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(adminUserService).getActiveUsers();
    }

    @Test
    void shouldCreateUser() {
        CreateUserRequest req = CreateUserRequest.builder()
                .nombre("Carlos Negrón")
                .email("inspector@reciclajelitoral.cl")
                .password("Pass123!")
                .rol(Rol.INSPECTOR)
                .build();

        when(adminUserService.createUser(req)).thenReturn(sampleDto);

        ResponseEntity<UserAdminDTO> response = adminUserController.createUser(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Carlos Negrón", response.getBody().getNombre());
    }

    @Test
    void shouldUpdateUser() {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .nombre("Carlos Negrón Updated")
                .email("inspector@reciclajelitoral.cl")
                .build();

        when(adminUserService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(sampleDto);

        ResponseEntity<UserAdminDTO> response = adminUserController.updateUser(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldDeleteUserSoftly() {
        ResponseEntity<Void> response = adminUserController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(adminUserService).deleteUser(1L);
    }

    @Test
    void shouldHardDeleteUser() {
        ResponseEntity<Void> response = adminUserController.hardDeleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(adminUserService).hardDeleteUser(1L);
    }
}
