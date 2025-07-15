package SimTeg.simulateur.BACKEND.Dto.DataType;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.LocalDate;
@Data
@Embeddable
public class DeblocageLigne {
    private double montant;
     private LocalDate dateDeblocage;
     private int numero;
}
