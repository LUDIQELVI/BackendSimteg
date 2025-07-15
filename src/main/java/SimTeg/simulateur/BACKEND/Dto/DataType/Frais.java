package SimTeg.simulateur.BACKEND.Dto.DataType;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Frais {
    private String nom;
    private double montant;
}
