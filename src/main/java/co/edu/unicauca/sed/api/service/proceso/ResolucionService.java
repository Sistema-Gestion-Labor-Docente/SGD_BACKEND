package co.edu.unicauca.sed.api.service.proceso;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sed.api.domain.Resolucion;
import co.edu.unicauca.sed.api.repository.ResolucionRepository;

@Service
public class ResolucionService {
    @Autowired
    private ResolucionRepository resolucionRepository;

    public List<Resolucion> findAll() {
        List<Resolucion> list = new ArrayList<>();
        this.resolucionRepository.findAll().forEach(list::add);
        return list;
    }

    public Resolucion findByOid(Integer oid) {
        Optional<Resolucion> resultado = this.resolucionRepository.findById(oid);

        if (resultado.isPresent()) {
            return resultado.get();
        }

        return null;
    }

    public Resolucion save(Resolucion resolucion) {
        Resolucion result = null;
        try {
            result = this.resolucionRepository.save(resolucion);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public void delete(Integer oid) {
        this.resolucionRepository.deleteById(oid);
    }
}
