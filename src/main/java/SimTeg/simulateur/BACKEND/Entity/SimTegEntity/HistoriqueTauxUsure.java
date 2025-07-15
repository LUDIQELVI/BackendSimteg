package SimTeg.simulateur.BACKEND.Entity.SimTegEntity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "historique_taux_usure")
public class HistoriqueTauxUsure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer tauxUsureId;

    private double ancienTauxUsure;
    private double nouveauTauxUsure;

    private double ancienSeuil;
    private double nouveauSeuil;

    private double ancienneAnnee;
    private double nouvelleAnnee;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    private String typeChangement; // mise a jour

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
    }
}
