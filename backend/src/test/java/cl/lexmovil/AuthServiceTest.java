package cl.lexmovil;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import static cl.lexmovil.ApiModels.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class AuthServiceTest {
    @Test void registraHashYNormalizaCorreo() {
        var repository = mock(UsuarioRepository.class);
        var service = new AuthService(repository);
        var usuario = service.registrar(new Registro(" Nombre Apellido ", "12.345.678-5", " TEST@Example.com ", "Estudiante", "Clave123!"));
        var hash = ArgumentCaptor.forClass(String.class);
        verify(repository).insertar(eq(usuario), hash.capture());
        assertEquals("test@example.com", usuario.correo());
        assertNotEquals("Clave123!", hash.getValue());
        assertTrue(new BCryptPasswordEncoder().matches("Clave123!", hash.getValue()));
    }
    @Test void passwordIncorrectaNoCreaSesion() {
        var repository = mock(UsuarioRepository.class);
        var usuario = new Usuario("1", "Nombre Apellido", "12.345.678-5", "test@example.com", "Estudiante");
        when(repository.porCorreo(usuario.correo())).thenReturn(Optional.of(usuario));
        when(repository.passwordHash("1")).thenReturn(new BCryptPasswordEncoder().encode("Clave123!"));
        assertThrows(ResponseStatusException.class, () -> new AuthService(repository).login(new Login(usuario.correo(), "Incorrecta!")));
        verify(repository, never()).guardarSesion(anyString(), anyString(), any());
    }
}
