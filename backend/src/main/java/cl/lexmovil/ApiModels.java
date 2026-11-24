package cl.lexmovil;

import jakarta.validation.constraints.*;

public final class ApiModels {
    private ApiModels() {}
    public record Registro(
        @NotBlank @Size(max=120) String nombreCompleto,
        @NotBlank @Size(max=20) String rut,
        @NotBlank @Email @Size(max=254) String correo,
        @Pattern(regexp="Estudiante|Abogado") @NotNull String tipo,
        @NotBlank @Size(min=8,max=64) String password) {}
    public record Login(@NotBlank @Email String correo, @NotBlank @Size(max=64) String password) {}
    public record Usuario(String id, String nombreCompleto, String rut, String correo, String tipo) {}
    public record Sesion(String token, Usuario usuario) {}
    public record Favorito(int leyId, String nota) {}
    public record CrearFavorito(@Min(1) @Max(4) int leyId) {}
    public record Nota(@NotNull @Size(max=2000) String nota) {}
}
