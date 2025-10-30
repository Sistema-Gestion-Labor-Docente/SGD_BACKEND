package co.edu.unicauca.sgd.api.service.materias.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import co.edu.unicauca.sgd.api.domain.Departamento;
import co.edu.unicauca.sgd.api.domain.UsuarioDepartamento;
import co.edu.unicauca.sgd.api.dto.ApiResponse;
import co.edu.unicauca.sgd.api.dto.UsuarioDepartamentoDTORequest;
import co.edu.unicauca.sgd.api.dto.materias.UsuarioDepartamentoDTOResponse;
import co.edu.unicauca.sgd.api.mapper.UsuarioDepartamentoMapper;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioDepartamentoServiceImplTest {

    @Mock
    private UsuarioDepartamentoRepository repository;

    @Mock
    private UsuarioDepartamentoMapper mapper;

    private UsuarioDepartamentoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioDepartamentoServiceImpl(repository, mapper);
    }

    @Test
    void obtenerTodos_retornarPaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 5);
        UsuarioDepartamento entity = buildUsuarioDepartamento(10, 20);
        UsuarioDepartamentoDTOResponse dto = new UsuarioDepartamentoDTOResponse();
        Page<UsuarioDepartamento> page = new PageImpl<>(List.of(entity), pageable, 1);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toResponse(entity)).thenReturn(dto);

        ApiResponse<Page<UsuarioDepartamentoDTOResponse>> response = service.obtenerTodos(10, 20, pageable);

        assertEquals(200, response.getCodigo());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
        verify(mapper).toResponse(entity);
    }

    @Test
    void obtenerTodos_cuandoOcurreError_retorna500() {
        Pageable pageable = PageRequest.of(0, 5);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenThrow(new RuntimeException("DB down"));

        ApiResponse<Page<UsuarioDepartamentoDTOResponse>> response = service.obtenerTodos(null, null, pageable);

        assertEquals(500, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void buscarPorUsuario_exito() {
        UsuarioDepartamento entity = buildUsuarioDepartamento(1, 2);
        UsuarioDepartamentoDTOResponse dto = new UsuarioDepartamentoDTOResponse();

        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(dto);

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.buscarPorUsuario(1);

        assertEquals(200, response.getCodigo());
        assertSame(dto, response.getData());
        verify(mapper).toResponse(entity);
    }

    @Test
    void buscarPorUsuario_noEncontradoRetorna404() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.buscarPorUsuario(1);

        assertEquals(404, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void guardar_cuandoYaExisteDevuelve400() {
        UsuarioDepartamentoDTORequest request = buildRequest(1, 2);
        when(repository.existsById(1)).thenReturn(true);

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.guardar(request);

        assertEquals(400, response.getCodigo());
        assertNull(response.getData());
        verify(repository, never()).save(any(UsuarioDepartamento.class));
    }

    @Test
    void guardar_exito() {
        UsuarioDepartamentoDTORequest request = buildRequest(1, 2);
        UsuarioDepartamento entity = buildUsuarioDepartamento(1, 2);
        UsuarioDepartamento saved = buildUsuarioDepartamento(1, 2);
        UsuarioDepartamentoDTOResponse dto = new UsuarioDepartamentoDTOResponse();

        when(repository.existsById(1)).thenReturn(false);
        when(mapper.convertToEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(dto);

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.guardar(request);

        assertEquals(200, response.getCodigo());
        assertSame(dto, response.getData());
        verify(repository).save(entity);
    }

    @Test
    void guardar_cuandoFallaPersistenciaDevuelve500() {
        UsuarioDepartamentoDTORequest request = buildRequest(1, 2);
        UsuarioDepartamento entity = buildUsuarioDepartamento(1, 2);

        when(repository.existsById(1)).thenReturn(false);
        when(mapper.convertToEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenThrow(new RuntimeException("Error persistencia"));

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.guardar(request);

        assertEquals(500, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void actualizar_exito() {
        UsuarioDepartamento existente = buildUsuarioDepartamento(1, 2);
        UsuarioDepartamentoDTORequest request = buildRequest(null, 5);
        UsuarioDepartamento actualizado = buildUsuarioDepartamento(1, 5);
        UsuarioDepartamentoDTOResponse dto = new UsuarioDepartamentoDTOResponse();

        when(repository.findById(1)).thenReturn(Optional.of(existente));
        doNothing().when(mapper).actualizarCamposBasicos(eq(existente), any(UsuarioDepartamentoDTORequest.class));
        when(repository.save(existente)).thenReturn(actualizado);
        when(mapper.toResponse(actualizado)).thenReturn(dto);

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.actualizar(1, request);

        assertEquals(200, response.getCodigo());
        assertSame(dto, response.getData());
        assertEquals(1, request.getOidUsuario());
        verify(mapper).actualizarCamposBasicos(eq(existente), any(UsuarioDepartamentoDTORequest.class));
        verify(repository).save(existente);
    }

    @Test
    void actualizar_noEncontradoDevuelve400() {
        UsuarioDepartamentoDTORequest request = buildRequest(null, 5);
        when(repository.findById(1)).thenReturn(Optional.empty());

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.actualizar(1, request);

        assertEquals(400, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void actualizar_cuandoFallaDevuelve500() throws Exception {
        UsuarioDepartamento existente = buildUsuarioDepartamento(1, 2);
        UsuarioDepartamentoDTORequest request = buildRequest(null, 5);

        when(repository.findById(1)).thenReturn(Optional.of(existente));
        doThrow(new RuntimeException("mapper error")).when(mapper)
                .actualizarCamposBasicos(eq(existente), any(UsuarioDepartamentoDTORequest.class));

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.actualizar(1, request);

        assertEquals(400, response.getCodigo());
        assertNull(response.getData());
    }

    @Test
    void eliminar_exito() {
        when(repository.existsById(1)).thenReturn(true);

        ApiResponse<Void> response = service.eliminar(1);

        assertEquals(200, response.getCodigo());
        assertNull(response.getData());
        verify(repository).deleteById(1);
    }

    @Test
    void eliminar_noExisteDevuelve404() {
        when(repository.existsById(1)).thenReturn(false);

        ApiResponse<Void> response = service.eliminar(1);

        assertEquals(404, response.getCodigo());
        assertNull(response.getData());
        verify(repository, never()).deleteById(1);
    }

    @Test
    void eliminar_errorGeneralDevuelve500() {
        when(repository.existsById(1)).thenThrow(new RuntimeException("DB error"));

        ApiResponse<Void> response = service.eliminar(1);

        assertEquals(500, response.getCodigo());
        assertNull(response.getData());
    }

    private UsuarioDepartamentoDTORequest buildRequest(Integer oidUsuario, Integer oidDepartamento) {
        UsuarioDepartamentoDTORequest request = new UsuarioDepartamentoDTORequest();
        request.setOidUsuario(oidUsuario);
        request.setOidDepartamento(oidDepartamento);
        return request;
    }

    private UsuarioDepartamento buildUsuarioDepartamento(Integer oidUsuario, Integer oidDepartamento) {
        UsuarioDepartamento entity = new UsuarioDepartamento();
        entity.setOidUsuario(oidUsuario);
        Departamento departamento = new Departamento();
        departamento.setOidDepartamento(oidDepartamento);
        departamento.setNombre("Departamento " + oidDepartamento);
        entity.setDepartamento(departamento);
        entity.setFechaCreacion(LocalDateTime.now());
        return entity;
    }
}
