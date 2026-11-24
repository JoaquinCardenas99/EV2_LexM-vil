package cl.lexmovil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static cl.lexmovil.ApiModels.*;

@Service
public class AuthService {
    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();
    public AuthService(UsuarioRepository repository) { this.repository = repository; }
    public Usuario registrar(Registro registro) {
        if (registro.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña demasiado larga");
        }
        var usuario = new Usuario(UUID.randomUUID().toString(), registro.nombreCompleto().trim(), registro.rut().trim(), normalizarCorreo(registro.correo()), registro.tipo());
        try { repository.insertar(usuario, encoder.encode(registro.password())); }
        catch (DuplicateKeyException exception) { throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con este correo"); }
        return usuario;
    }
    public Sesion login(Login login) {
        var usuario = repository.porCorreo(normalizarCorreo(login.correo())).orElseThrow(AuthService::noAutorizado);
        if (login.password().getBytes(StandardCharsets.UTF_8).length > 72 || !encoder.matches(login.password(), repository.passwordHash(usuario.id()))) throw noAutorizado();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // La base de datos almacena el hash, nunca el token utilizable.
        repository.guardarSesion(hash(token), usuario.id(), Instant.now().plus(30, ChronoUnit.DAYS));
        return new Sesion(token, usuario);
    }
    public Usuario autenticar(String authorization) {
        return repository.porSesion(hash(extraerToken(authorization))).orElseThrow(AuthService::noAutorizado);
    }
    public void logout(String authorization) { repository.borrarSesion(hash(extraerToken(authorization))); }
    private String extraerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) throw noAutorizado();
        return authorization.substring(7);
    }
    static String normalizarCorreo(String correo) { return correo.trim().toLowerCase(Locale.ROOT); }
    private static ResponseStatusException noAutorizado() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas o sesión vencida"); }
    private static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
}
