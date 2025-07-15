package SimTeg.simulateur.BACKEND.Dto.SimTegDto;

import SimTeg.simulateur.BACKEND.Dto.DataType.DeblocageLigne;
import lombok.Data;
import SimTeg.simulateur.BACKEND.Dto.DataType.Assurance;
import SimTeg.simulateur.BACKEND.Dto.DataType.Frais;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.Frequence;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.TypeEprunteur;

import java.time.LocalDate;
import java.util.List;

@Data
public class SimulationDtoRequest {
    private  double montantEmprunte;
    private double tauxInteretNominal; // mensualité ou annuité selon la fréquence
    private Integer categorieCreditId;
    private double duree;
    private TypeEprunteur typeEprunteur;
    private Frequence frequence;
    private List<Frais> fraisList;
    private  List<Assurance> assuranceList;
    private List<DeblocageLigne> TableauDeblocages;
    private LocalDate DatePremiereEcheance;




}
