package SimTeg.simulateur.BACKEND.Entity.SimTegEntity;

import SimTeg.simulateur.BACKEND.Dto.DataType.DeblocageLigne;
import SimTeg.simulateur.BACKEND.Dto.DataType.AmortisementLigne;
import SimTeg.simulateur.BACKEND.Dto.DataType.Assurance;
import SimTeg.simulateur.BACKEND.Dto.DataType.Frais;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.Frequence;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.TypeEprunteur;
import SimTeg.simulateur.BACKEND.Entity.AbstractEntity;
import SimTeg.simulateur.BACKEND.User.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@ToString(exclude = {"categorieCredit"})
@Data
public class Simulation extends AbstractEntity {

  @Column(name = "teg")
  private double teg;

  @Column(name = "echeance")
  private double echeance;

  @Column(name = "cout_total")
  private double coutTotal;

  @ElementCollection
  @CollectionTable(name = "simulation_amortissements", joinColumns = @JoinColumn(name = "simulation_id"))
  private List<AmortisementLigne> tableauAmortisement;

  @ElementCollection
  @CollectionTable(name = "simulation_deblocages", joinColumns = @JoinColumn(name = "simulation_id"))
  private List<DeblocageLigne> tableauDeblocages;

  @Column(name = "montant")
  private double montant;

  @Column(name = "date_premiere_echeance")
  private LocalDate datePremiereEcheance;

  @Column(name = "date_debut")
  private LocalDate dateDebut;

  @Column(name = "taux_nominal")
  private double tauxNominal;

  @ElementCollection
  @CollectionTable(name = "simulation_frais", joinColumns = @JoinColumn(name = "simulation_id"))
  private List<Frais> fraisJson;

  @ElementCollection
  @CollectionTable(name = "simulation_assurances", joinColumns = @JoinColumn(name = "simulation_id"))
  private List<Assurance> assuranceJson;

  @Column(name = "date_fin")
  private LocalDate dateFin;

  @Column(name = "duree")
  private double duree;

  @Column(name = "type_emprunteur")
  private String typeEmprunteurLabel;

  @Transient
  private TypeEprunteur typeEmprunteur;

  @Enumerated(EnumType.STRING)
  @Column(name = "frequence")
  private Frequence frequence;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  @ManyToOne(optional = false)
  @JoinColumn(name = "categorie_credit_id")
  private CategorieCredit categorieCredit;

  @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JsonManagedReference
  @JsonIgnore
  private List<Notification> notifications;

  /**
   * Conversion du label String en enum après chargement depuis la base
   */
  @PostLoad
  private void mapLabelToEnum() {
    if (this.typeEmprunteurLabel != null) {
      try {
        this.typeEmprunteur = TypeEprunteur.fromString(this.typeEmprunteurLabel);
      } catch (IllegalArgumentException e) {
        this.typeEmprunteur = null; // si label inconnu
      }
    }
  }

  /**
   * Conversion de l'enum en String avant sauvegarde en base
   * Cette méthode est appelée explicitement dans onCreate et onUpdate
   */
  private void mapEnumToLabel() {
    if (this.typeEmprunteur != null) {
      this.typeEmprunteurLabel = this.typeEmprunteur.name();
    } else {
      this.typeEmprunteurLabel = null;
    }
  }

  @PrePersist
  protected void onCreate() {
    this.setCreationDate(LocalDateTime.now());
    this.setLastModifyDate(LocalDateTime.now());
    mapEnumToLabel();
  }

  @PreUpdate
  protected void onUpdate() {
    this.setLastModifyDate(LocalDateTime.now());
    mapEnumToLabel();
  }
}
