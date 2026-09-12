package cl.reciclajelitoral.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/admin/users");
        ServletWebRequest webRequest = new ServletWebRequest(mockRequest);

        IllegalArgumentException ex = new IllegalArgumentException("Usuario no encontrado");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Usuario no encontrado", response.getBody().getMessage());
        assertEquals("/api/admin/users", response.getBody().getPath());
    }

    @Test
    void shouldHandleGlobalExceptionSanitizingMessage() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/admin/containers");
        ServletWebRequest webRequest = new ServletWebRequest(mockRequest);

        RuntimeException ex = new RuntimeException("ERROR: relation 'usuarios' does not exist at postgres.internal.query");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        // El mensaje debe estar sanitizado y no exponer detalles técnicos de la BD
        assertEquals("Ha ocurrido un error inesperado en el servidor. Por favor, intenta nuevamente más tarde o contacta al administrador.", response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("ERROR: relation"));
    }

    @Test
    void shouldHandleDataIntegrityViolationException() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/admin/users/1");
        ServletWebRequest webRequest = new ServletWebRequest(mockRequest);

        org.springframework.dao.DataIntegrityViolationException ex =
                new org.springframework.dao.DataIntegrityViolationException("FK constraint violation fk_usuario_comuna");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("No se puede completar la operación debido a restricciones de integridad relacional o datos duplicados en el sistema.", response.getBody().getMessage());
    }

    @Test
    void shouldHandleAccessDeniedException() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/admin/metrics");
        ServletWebRequest webRequest = new ServletWebRequest(mockRequest);

        org.springframework.security.access.AccessDeniedException ex =
                new org.springframework.security.access.AccessDeniedException("Forbidden");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex, webRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("No tienes los permisos necesarios para realizar esta acción.", response.getBody().getMessage());
    }

    @Test
    void shouldHandleNoSuchElementException() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/comunas/999");
        ServletWebRequest webRequest = new ServletWebRequest(mockRequest);

        java.util.NoSuchElementException ex = new java.util.NoSuchElementException("No element");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNoSuchElementException(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("El recurso solicitado no fue encontrado en el sistema.", response.getBody().getMessage());
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/admin/users");
        ServletWebRequest webRequest = new ServletWebRequest(mockRequest);

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("createUserRequest", "email", "Email inválido");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getErrors().containsKey("email"));
        assertEquals("Email inválido", response.getBody().getErrors().get("email"));
    }
}
