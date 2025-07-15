package SimTeg.simulateur.BACKEND.Dto.DataType;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Assurance {
   private String nom;
   private double montant;
}
