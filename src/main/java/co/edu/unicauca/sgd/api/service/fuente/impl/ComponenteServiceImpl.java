package co.edu.unicauca.sgd.api.service.fuente.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sgd.api.domain.Componente;
import co.edu.unicauca.sgd.api.repository.ComponenteRepository;
import co.edu.unicauca.sgd.api.service.fuente.ComponenteService;

@Service
public class ComponenteServiceImpl implements ComponenteService {

    @Autowired
    private ComponenteRepository componenteRepository;

    @Override
    public Componente guardar(Componente componente) {
        return componenteRepository.save(componente);
    }

    @Override
    public Componente buscarPorId(Integer id) {
        return componenteRepository.findById(id).orElseThrow(() -> new RuntimeException("Componente no encontrado con ID: " + id));
    }

    @Override
    public Page<Componente> listar(Pageable pageable) {
        return componenteRepository.findAll(pageable);
    }
}