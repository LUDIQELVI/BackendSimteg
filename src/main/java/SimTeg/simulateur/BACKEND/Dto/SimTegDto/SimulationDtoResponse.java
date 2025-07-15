package SimTeg.simulateur.BACKEND.Dto.SimTegDto;

import SimTeg.simulateur.BACKEND.Dto.DataType.DeblocageLigne;
import lombok.Data;
import SimTeg.simulateur.BACKEND.Dto.DataType.AmortisementLigne;
import java.util.List;

@Data
public class SimulationDtoResponse {
    private double tegAnnuel;
    private double echeance;
    private double coutTotal;
    private List<AmortisementLigne> tableauAmortissement;
    private List<DeblocageLigne> tableauDeblocages;

    // Getter explicite (facultatif car Lombok génère déjà)
    public double getCoutTotal() {
        return this.coutTotal;
    }

    // Setter corrigé (avec bonne casse et bonne logique)
    public void setCoutTotal(double totalCost) {
        this.coutTotal = totalCost;
    }
}
