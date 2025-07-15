package SimTeg.simulateur.BACKEND.Entity.SimTegEntity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.TypeEprunteur;
import SimTeg.simulateur.BACKEND.Entity.AbstractEntity;

@Entity
@Data
@ToString(exclude = {"categorieCredit"})
public class TauxUsure  extends AbstractEntity {
    @Column(name="seuil")
    private double seuil;
    @Column(name="annee")
    private double annee;
    @ManyToOne(optional = false)
    @JoinColumn(name = "categorieCredit_id")
    private CategorieCredit categorieCredit;
    @Column(name = "type_emprunteur")
    @Enumerated(EnumType.STRING)
    private TypeEprunteur typeEmprunteur;

    @Column(name = "tauxTEGMoyen")
    private double tauxTEGMoyen;
    @Column(name ="tauxUsure")
    private  double tauxUsure;

}