package SimTeg.simulateur.BACKEND.Dto.SimTegDto;

import lombok.Data;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.TypeEprunteur;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class TauxUsureDto {
    private double seuil;
    private int annee;
    private Integer categorieId;
    @JsonProperty("typeEprunteur")
    private TypeEprunteur typeEprunteur;

    @JsonProperty("typeEprunteur")
    public void setTypeEprunteur(TypeEprunteur typeEprunteur) {
        System.out.println("Setting typeEprunteur: " + (typeEprunteur != null ? typeEprunteur.name() : "null"));
        this.typeEprunteur = typeEprunteur;
    }

    private double tauxUsure;
}