package cl.lexmovil;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static cl.lexmovil.ApiModels.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final AuthService auth;
    private final UsuarioRepository repository;
    public ApiController(AuthService auth, UsuarioRepository repository) { this.auth = auth; this.repository = repository; }
    @PostMapping("/auth/register") @ResponseStatus(HttpStatus.CREATED)
    public Usuario registrar(@Valid @RequestBody Registro registro) { return auth.registrar(registro); }
    @PostMapping("/auth/login")
    public Sesion login(@Valid @RequestBody Login login) { return auth.login(login); }
    @GetMapping("/auth/me")
    public Usuario perfil(@RequestHeader(value="Authorization", required=false) String header) { return auth.autenticar(header); }
    @PostMapping("/auth/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(value="Authorization", required=false) String header) { auth.logout(header); }
    @GetMapping("/favorites")
    public List<Favorito> favoritos(@RequestHeader(value="Authorization", required=false) String header) { return repository.favoritos(auth.autenticar(header).id()); }
    @PostMapping("/favorites") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void agregar(@RequestHeader(value="Authorization", required=false) String header, @Valid @RequestBody CrearFavorito body) { repository.agregarFavorito(auth.autenticar(header).id(), body.leyId()); }
    @PutMapping("/favorites/{leyId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void nota(@RequestHeader(value="Authorization", required=false) String header, @PathVariable int leyId, @Valid @RequestBody Nota body) {
        if (!repository.actualizarNota(auth.autenticar(header).id(), leyId, body.nota())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorito no encontrado");
    }
    @DeleteMapping("/favorites/{leyId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(@RequestHeader(value="Authorization", required=false) String header, @PathVariable int leyId) { repository.quitarFavorito(auth.autenticar(header).id(), leyId); }
}
