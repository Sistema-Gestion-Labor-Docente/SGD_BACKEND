package co.edu.unicauca.sgd.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.UsuarioDepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.UsuarioDepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.security.JwtTokenService;
import co.edu.unicauca.sgd.api.security.RolExtractorService;
import co.edu.unicauca.sgd.api.service.materias.UsuarioDepartamentoService;

@WebMvcTest(value = UsuarioDepartamentoController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
class UsuarioDepartamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioDepartamentoService service;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private RolExtractorService rolExtractorService;

    @Test
    void findAll_invocaServicioConParametros() throws Exception {
        Page<UsuarioDepartamentoDTOResponse> page = new PageImpl<>(
                List.of(new UsuarioDepartamentoDTOResponse()),
                PageRequest.of(0, 10),
                1);
        ApiResponse<Page<UsuarioDepartamentoDTOResponse>> response = new ApiResponse<>(200, "ok", page);
        when(service.obtenerTodos(eq(1), eq(2), any(PageRequest.class))).thenReturn(response);

        mockMvc.perform(get("/api/departamentos/usuarios")
                .param("oidUsuario", "1")
                .param("oidDepartamento", "2")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk());

        verify(service).obtenerTodos(eq(1), eq(2), any(PageRequest.class));
    }

    @Test
    void findByUsuario_delegaEnServicio() throws Exception {
        ApiResponse<UsuarioDepartamentoDTOResponse> response = new ApiResponse<>(200, "ok",
                new UsuarioDepartamentoDTOResponse());
        when(service.buscarPorUsuario(9)).thenReturn(response);

        mockMvc.perform(get("/api/departamentos/usuarios/{oidUsuario}", 9))
            .andExpect(status().isOk());

        verify(service).buscarPorUsuario(9);
    }

    @Test
    void listarProfesoresPorTipo_delegaEnElServicio() throws Exception {
        ApiResponse<List<UsuarioDepartamentoDTOResponse>> response =
                new ApiResponse<>(200, "ok", Collections.emptyList());
        when(service.obtenerProfesoresPorTipoActividad("DOCENCIA", 5)).thenReturn(response);

        mockMvc.perform(get("/api/departamentos/usuarios/actividades")
                .param("filtro", "DOCENCIA")
                .param("oidDepartamento", "5"))
            .andExpect(status().isOk());

        verify(service).obtenerProfesoresPorTipoActividad("DOCENCIA", 5);
    }

    @Test
    void save_retornarRespuestaDelServicio() throws Exception {
        UsuarioDepartamentoDTORequest request = new UsuarioDepartamentoDTORequest();
        request.setOidUsuario(10);
        request.setOidDepartamento(20);

        ApiResponse<UsuarioDepartamentoDTOResponse> response =
                new ApiResponse<>(200, "creado", new UsuarioDepartamentoDTOResponse());
        when(service.guardar(any(UsuarioDepartamentoDTORequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/departamentos/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(service).guardar(any(UsuarioDepartamentoDTORequest.class));
    }

    @Test
    void update_delegaEnServicio() throws Exception {
        UsuarioDepartamentoDTORequest request = new UsuarioDepartamentoDTORequest();
        request.setOidUsuario(10);
        request.setOidDepartamento(30);
        ApiResponse<UsuarioDepartamentoDTOResponse> response =
                new ApiResponse<>(200, "actualizado", new UsuarioDepartamentoDTOResponse());
        when(service.actualizar(eq(10), any(UsuarioDepartamentoDTORequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/departamentos/usuarios/{oidUsuario}", 10)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(service).actualizar(eq(10), any(UsuarioDepartamentoDTORequest.class));
    }

    @Test
    void delete_retornarRespuestaExitosa() throws Exception {
        ApiResponse<Void> servicioResp = new ApiResponse<>(200, "eliminado", null);
        when(service.eliminar(15)).thenReturn(servicioResp);

        mockMvc.perform(delete("/api/departamentos/usuarios/{oidUsuario}", 15))
            .andExpect(status().isOk());

        verify(service).eliminar(15);
    }
}
