package cl.lexmovil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/** Catálogo académico de demostración; no representa legislación oficial vigente. */
@RestController
@RequestMapping("/api/laws")
public class CatalogoController {
    public record Articulo(String numero, String texto) {}
    public record Ley(int id, String codigo, String titulo, String categoria,
                      String descripcion, List<Articulo> articulos) {}
    private final List<Ley> leyes;

    public CatalogoController(ObjectMapper mapper) throws IOException {
        try (var input = new ClassPathResource("catalogo.json").getInputStream()) {
            leyes = List.copyOf(mapper.readValue(input, new TypeReference<List<Ley>>() {}));
        }
    }
    @GetMapping
    public List<Ley> todas() { return leyes; }

    @GetMapping("/{id}")
    public Ley detalle(@PathVariable int id) {
        return leyes.stream().filter(ley -> ley.id() == id).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Norma no encontrada"));
    }
}
