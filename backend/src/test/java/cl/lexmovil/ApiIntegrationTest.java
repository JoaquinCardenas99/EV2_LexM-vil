package cl.lexmovil;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    private String registrarEIngresar() throws Exception {
        String correo = UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
            {"nombreCompleto":"Usuario Prueba","rut":"12.345.678-5","correo":"%s","tipo":"Estudiante","password":"Clave123!"}
            """.formatted(correo))).andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist());
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"correo\":\"" + correo + "\",\"password\":\"Clave123!\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(response).get("token").asText();
    }
    @Test void sesionSeRestauraYSeRevoca() throws Exception {
        String token = registrarEIngresar();
        mvc.perform(get("/api/auth/me").header("Authorization", token)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").header("Authorization", token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").header("Authorization", token)).andExpect(status().isUnauthorized());
    }
    @Test void favoritosCrudYAislamientoEntreUsuarios() throws Exception {
        String token = registrarEIngresar();
        String otro = registrarEIngresar();
        mvc.perform(post("/api/favorites").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"leyId\":1}")).andExpect(status().isNoContent());
        mvc.perform(put("/api/favorites/1").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content("{\"nota\":\"Estudiar mañana\"}")).andExpect(status().isNoContent());
        mvc.perform(get("/api/favorites").header("Authorization", token)).andExpect(jsonPath("$[0].nota").value("Estudiar mañana"));
        mvc.perform(get("/api/favorites").header("Authorization", otro)).andExpect(content().json("[]"));
        mvc.perform(put("/api/favorites/1").header("Authorization", otro).contentType(MediaType.APPLICATION_JSON).content("{\"nota\":\"Otra\"}")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/favorites/1").header("Authorization", token)).andExpect(status().isNoContent());
        mvc.perform(get("/api/favorites").header("Authorization", token)).andExpect(content().json("[]"));
    }
    @Test void rechazaAccesoAnonimoYDatosInvalidos() throws Exception {
        mvc.perform(get("/api/favorites")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/favorites").header("Authorization", registrarEIngresar()).contentType(MediaType.APPLICATION_JSON).content("{\"leyId\":999}")).andExpect(status().isBadRequest());
    }
    @Test void catalogoYDetalleDisponibles() throws Exception {
        mvc.perform(get("/api/laws")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/api/laws/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        mvc.perform(get("/api/laws/999")).andExpect(status().isNotFound());
    }
}
