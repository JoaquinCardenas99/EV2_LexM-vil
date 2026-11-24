package cl.lexmovil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import static cl.lexmovil.ApiModels.*;

@Repository
public class UsuarioRepository {
    private final JdbcTemplate jdbc;
    public UsuarioRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    private Usuario usuario(ResultSet rs, int row) throws SQLException {
        return new Usuario(rs.getString("id"), rs.getString("nombre_completo"), rs.getString("rut"), rs.getString("correo"), rs.getString("tipo"));
    }
    public Optional<Usuario> porCorreo(String correo) {
        return jdbc.query("SELECT * FROM usuarios WHERE correo=?", this::usuario, correo).stream().findFirst();
    }
    public String passwordHash(String id) {
        return jdbc.queryForObject("SELECT password_hash FROM usuarios WHERE id=?", String.class, id);
    }
    public void insertar(Usuario usuario, String passwordHash) {
        jdbc.update("INSERT INTO usuarios VALUES (?,?,?,?,?,?)", usuario.id(), usuario.nombreCompleto(), usuario.rut(), usuario.correo(), usuario.tipo(), passwordHash);
    }
    public void guardarSesion(String hash, String id, Instant expira) {
        jdbc.update("DELETE FROM sesiones WHERE expira <= CURRENT_TIMESTAMP");
        jdbc.update("INSERT INTO sesiones VALUES (?,?,?)", hash, id, java.sql.Timestamp.from(expira));
    }
    public Optional<Usuario> porSesion(String hash) {
        return jdbc.query("SELECT u.* FROM usuarios u JOIN sesiones s ON u.id=s.usuario_id WHERE s.token_hash=? AND s.expira > CURRENT_TIMESTAMP", this::usuario, hash).stream().findFirst();
    }
    public void borrarSesion(String hash) { jdbc.update("DELETE FROM sesiones WHERE token_hash=?", hash); }
    public List<Favorito> favoritos(String id) {
        return jdbc.query("SELECT ley_id, nota FROM favoritos WHERE usuario_id=? ORDER BY ley_id", (rs, row) -> new Favorito(rs.getInt(1), rs.getString(2)), id);
    }
    public void agregarFavorito(String id, int leyId) {
        // Repetir la solicitud no modifica la nota ni crea duplicados.
        jdbc.update("INSERT INTO favoritos (usuario_id, ley_id) SELECT ?,? WHERE NOT EXISTS (SELECT 1 FROM favoritos WHERE usuario_id=? AND ley_id=?)", id, leyId, id, leyId);
    }
    public boolean actualizarNota(String id, int leyId, String nota) {
        return jdbc.update("UPDATE favoritos SET nota=? WHERE usuario_id=? AND ley_id=?", nota, id, leyId) > 0;
    }
    public void quitarFavorito(String id, int leyId) {
        jdbc.update("DELETE FROM favoritos WHERE usuario_id=? AND ley_id=?", id, leyId);
    }
}
