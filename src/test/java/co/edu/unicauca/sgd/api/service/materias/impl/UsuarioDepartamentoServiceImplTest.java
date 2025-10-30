package co.edu.unicauca.sgd.api.service.materias.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoAlreadyExistsException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoInternalException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoNotFoundException;
import co.edu.unicauca.sgd.api.exception.UsuarioDepartamentoValidationException;
import co.edu.unicauca.sgd.api.mapper.UsuarioDepartamentoMapper;
import co.edu.unicauca.sgd.api.repository.UsuarioActividadCalendarioRepository;
import co.edu.unicauca.sgd.api.repository.UsuarioDepartamentoRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioDepartamentoServiceImplTest {

    @Mock
    private UsuarioDepartamentoRepository repository;

    @Mock
    private UsuarioDepartamentoMapper mapper;

    @Mock
    private UsuarioActividadCalendarioRepository usuarioActividadCalendarioRepository;

    private UsuarioDepartamentoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UsuarioDepartamentoServiceImpl(repository, mapper, usuarioActividadCalendarioRepository);
    }

    @Test
    void obtenerTodos_retornarPaginaMapeada() {
        Pageable pageable = PageRequest.of(0, 5);
        UsuarioDepartamento entity = buildUsuarioDepartamento(10, 20);
        UsuarioDepartamentoDTOResponse dto = new UsuarioDepartamentoDTOResponse();
        Page<UsuarioDepartamento> page = new PageImpl<>(List.of(entity), pageable, 1);

        when(repository.findAll(ArgumentMatchers.<Specification<UsuarioDepartamento>>any(), eq(pageable))).thenReturn(page);
        when(mapper.toResponse(entity)).thenReturn(dto);
        when(usuarioActividadCalendarioRepository.sumarHorasPorUsuarios(anyList())).thenReturn(Collections.emptyList());

        ApiResponse<Page<UsuarioDepartamentoDTOResponse>> response = service.obtenerTodos(10, 20, pageable);

        assertEquals(200, response.getCodigo());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getTotalElements());
        verify(repository).findAll(ArgumentMatchers.<Specification<UsuarioDepartamento>>any(), eq(pageable));
        verify(mapper).toResponse(entity);
    }

    @Test
    void obtenerTodos_cuandoOcurreError_lanzaExcepcionInterna() {
        Pageable pageable = PageRequest.of(0, 5);
        when(repository.findAll(ArgumentMatchers.<Specification<UsuarioDepartamento>>any(), eq(pageable))).thenThrow(new RuntimeException("DB down"));

        assertThrows(UsuarioDepartamentoInternalException.class,
                () -> service.obtenerTodos(null, null, pageable));
    }

    @Test
    void buscarPorUsuario_exito() {
        UsuarioDepartamento entity = buildUsuarioDepartamento(1, 2);
        UsuarioDepartamentoDTOResponse dto = new UsuarioDepartamentoDTOResponse();

        when(repository.findById(1)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(dto);
        when(usuarioActividadCalendarioRepository.sumarHorasPorUsuario(1)).thenReturn(5f);

        ApiResponse<UsuarioDepartamentoDTOResponse> response = service.buscarPorUsuario(1);

        assertEquals(200, response.getCodigo());
        assertSame(dto, response.getData());
        verify(mapper).toResponse(entity);
    }

    @Test
    void buscarPorUsuario_noEncontradoLanzaExcepcion() {
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UsuarioDepartamentoNotFoundException.class, () -> service.buscarPorUsuario(1));
    }

    @Test
    void guardar_camposObligatoriosFaltantesLanzaValidacion() {
        UsuarioDepartamentoDTORequest request = buildRequest(null, null);

        assertThrows(UsuarioDepartamentoValidationException.class, () -> service.guardar(request));
    }

    @Test
    void guardar_cuandoYaExisteLanzaConflicto() {
        UsuarioDepartamentoDTORequest request = buildRequest(1, 2);
        when(repository.existsById(1)).thenReturn(true);

        assertThrows(UsuarioDepartamentoAlreadyExistsException.class, () -> service.guardar(request));
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
    void guardar_cuandoFallaPersistenciaLanzaExcepcionInterna() {
        UsuarioDepartamentoDTORequest request = buildRequest(1, 2);
        UsuarioDepartamento entity = buildUsuarioDepartamento(1, 2);

        when(repository.existsById(1)).thenReturn(false);
        when(mapper.convertToEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenThrow(new RuntimeException("Error persistencia"));

        assertThrows(UsuarioDepartamentoInternalException.class, () -> service.guardar(request));
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
    void actualizar_sinDepartamentoEnRequestLanzaValidacion() {
        UsuarioDepartamentoDTORequest request = buildRequest(null, null);

        assertThrows(UsuarioDepartamentoValidationException.class, () -> service.actualizar(1, request));
    }

    @Test
    void actualizar_noEncontradoLanzaExcepcion() {
        UsuarioDepartamentoDTORequest request = buildRequest(null, 5);
        when(repository.findById(1)).thenReturn(Optional.empty());

        assertThrows(UsuarioDepartamentoNotFoundException.class, () -> service.actualizar(1, request));
    }

    @Test
    void actualizar_cuandoMapperFallaLanzaExcepcionInterna() throws Exception {
        UsuarioDepartamento existente = buildUsuarioDepartamento(1, 2);
        UsuarioDepartamentoDTORequest request = buildRequest(null, 5);

        when(repository.findById(1)).thenReturn(Optional.of(existente));
        doThrow(new RuntimeException("mapper error")).when(mapper)
                .actualizarCamposBasicos(eq(existente), any(UsuarioDepartamentoDTORequest.class));

        assertThrows(UsuarioDepartamentoInternalException.class, () -> service.actualizar(1, request));
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
    void eliminar_noExisteLanzaNotFound() {
        when(repository.existsById(1)).thenReturn(false);

        assertThrows(UsuarioDepartamentoNotFoundException.class, () -> service.eliminar(1));
        verify(repository, never()).deleteById(1);
    }

    @Test
    void eliminar_errorGeneralLanzaInterna() {
        when(repository.existsById(1)).thenThrow(new RuntimeException("DB error"));

        assertThrows(UsuarioDepartamentoInternalException.class, () -> service.eliminar(1));
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
