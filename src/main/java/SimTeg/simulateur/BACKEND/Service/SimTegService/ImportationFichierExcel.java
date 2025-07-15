package SimTeg.simulateur.BACKEND.Service.SimTegService;

import SimTeg.simulateur.BACKEND.Dto.DataType.Assurance;
import SimTeg.simulateur.BACKEND.Dto.DataType.DeblocageLigne;
import SimTeg.simulateur.BACKEND.Dto.DataType.Frais;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.Frequence;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.TypeEprunteur;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class ImportationFichierExcel {

    public SimulationDtoRequest LireSimulation(InputStream fileStream) throws Exception {
        Workbook workbook = new XSSFWorkbook(fileStream);
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(1); // Supposons QUE LES DONNEES SONT AU NIVEAU DE LA DEUXIEME LIGNE

        SimulationDtoRequest dto = new SimulationDtoRequest();

        dto.setMontantEmprunte(getValeurNumeriqueCellule(row, 0));
        dto.setTauxInteretNominal(getValeurNumeriqueCellule(row, 1));
        dto.setCategorieCreditId((int) getValeurNumeriqueCellule(row, 2));
        dto.setDuree((int) getValeurNumeriqueCellule(row, 3));
        dto.setTypeEprunteur(TypeEprunteur.valueOf(getChaineCaractereCellule(row, 4).toUpperCase()));
        dto.setFrequence(Frequence.valueOf(getChaineCaractereCellule(row, 5).toUpperCase()));
        dto.setDatePremiereEcheance(LocalDate.parse(getChaineCaractereCellule(row, 6), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //Lecture de la liste des frais
        List<Frais> fraisList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int nameIndex = 7 + (i * 2);
            if (row.getCell(nameIndex) == null) break;
            String nom = getChaineCaractereCellule(row, nameIndex);
            double montant = getValeurNumeriqueCellule(row, nameIndex + 1);
            if (nom != null) {
                Frais f= new Frais();
                f.setMontant(montant);
                f.setNom(nom);
                fraisList.add(f);
            }
        }
        dto.setFraisList(fraisList);

        // Lire liste des Assurances
        List<Assurance> assuranceList = new ArrayList<>();
        int assuranceStart = 17;
        for (int i = 0; i < 5; i++) {
            int nameIndex = assuranceStart + (i * 2);
            if (row.getCell(nameIndex) == null) break;
            String nom = getChaineCaractereCellule(row, nameIndex);
            double montant = getValeurNumeriqueCellule(row, nameIndex + 1);
            if (nom != null) {
                Assurance a =new Assurance();
                a.setMontant(montant);
                a.setNom(nom);

                assuranceList.add(a);
            }
        }
        dto.setAssuranceList(assuranceList);

        // Lire les déblocages
        List<DeblocageLigne> deblocages = new ArrayList<>();
        int deblocageStart = 27;
        for (int i = 0; i < 5; i++) {
            int base = deblocageStart + (i * 3);
            if (row.getCell(base) == null) break;
            double montant = getValeurNumeriqueCellule(row, base);
            LocalDate date = LocalDate.parse(getChaineCaractereCellule(row, base + 1));
            int numero = (int) getValeurNumeriqueCellule(row, base + 2);
            DeblocageLigne d=new DeblocageLigne();
            d.setMontant(montant);
            d.setDateDeblocage(date);
            d.setNumero(numero);
            deblocages.add(d);
        }
        dto.setTableauDeblocages(deblocages);

        workbook.close();
        return dto;
    }

    private double getValeurNumeriqueCellule(Row row, int col) {
        Cell cell = row.getCell(col);
        return cell != null ? cell.getNumericCellValue() : 0.0;
    }

    private String getChaineCaractereCellule(Row row, int col) {
        Cell cell = row.getCell(col);
        return cell != null ? cell.toString().trim() : null;
    }

}
