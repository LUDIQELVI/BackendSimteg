package SimTeg.simulateur.BACKEND.Repository.SimTegRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.HistoriqueTauxUsure;

import java.util.List;

public interface HistoriqueRepository  extends JpaRepository<HistoriqueTauxUsure,Integer> {
    List<HistoriqueTauxUsure>findByTauxUsureId(Integer idTauxUsure);
}
