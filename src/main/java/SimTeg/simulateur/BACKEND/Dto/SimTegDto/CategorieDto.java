package SimTeg.simulateur.BACKEND.Dto.SimTegDto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Data;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Simulation;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.TauxUsure;

import java.util.List;

@Data
public class CategorieDto {
    private Integer id;

    private String nomCategorie;

    private String description;

    private List<String> fraisObligatoire;

    private List<String> assurances;


}
