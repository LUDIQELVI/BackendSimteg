package SimTeg.simulateur.BACKEND.Dto.DataType;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.time.LocalDate;
@Data
@Embeddable
public class AmortisementLigne {
    private int numero;
    private double capitalRestant;
    private double interet;
    private double principal;
    private  double assurance;
    private double echeanceTotale;
    private LocalDate dateEcheance;
}
