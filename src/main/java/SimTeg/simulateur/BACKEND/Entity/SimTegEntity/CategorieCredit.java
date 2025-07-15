package SimTeg.simulateur.BACKEND.Entity.SimTegEntity;

import SimTeg.simulateur.BACKEND.Entity.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"tauxUsures", "simulations"})
public class CategorieCredit extends AbstractEntity {

    @Column(name = "nom_categorie", nullable = false)
    private String nomCategorie;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Type(JsonType.class)
    @Column(name = "frais_obligatoire", columnDefinition = "json")
    private List<String> fraisObligatoire = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "assurances", columnDefinition = "json")
    private List<String> assurances = new ArrayList<>();

    @OneToMany(mappedBy = "categorieCredit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private List<TauxUsure> tauxUsures = new ArrayList<>();

    @OneToMany(mappedBy = "categorieCredit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private List<Simulation> simulations = new ArrayList<>();
}
