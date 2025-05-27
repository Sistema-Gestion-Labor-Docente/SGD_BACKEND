package co.edu.unicauca.sed.api.service.proceso;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unicauca.sed.api.domain.Oficio;
import co.edu.unicauca.sed.api.repository.OficioRepository;

@Service
public class OficioService {

    @Autowired
    private OficioRepository oficioRepository;

    public List<Oficio> findAll() {
        List<Oficio> list = new ArrayList<>();
        this.oficioRepository.findAll().forEach(list::add);
        return list;
    }

    public Oficio findByOid(Integer oid) {
        Optional<Oficio> resultado = this.oficioRepository.findById(oid);
    
        if (resultado.isPresent()) {
            return resultado.get();
        }
    
        return null;
    }
    

    public Oficio save(Oficio oficio) {
        Oficio result = null;
        try {
            result = this.oficioRepository.save(oficio);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public void delete(Integer oid) {
        this.oficioRepository.deleteById(oid);
    }
}
