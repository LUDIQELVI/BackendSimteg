package SimTeg.simulateur.BACKEND.Dto.DataType;

import lombok.Data;

import java.util.List;

@Data
public class FraisAssurance {
   private List<String> assurance;
   private List<String> frais;
}
