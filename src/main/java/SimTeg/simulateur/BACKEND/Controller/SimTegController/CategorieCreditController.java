package SimTeg.simulateur.BACKEND.Controller.SimTegController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import SimTeg.simulateur.BACKEND.Dto.DataType.FrAss;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.CategorieDto;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.CategorieCredit;
import SimTeg.simulateur.BACKEND.Service.SimTegService.CategorieService;

import java.util.List;

@RestController
@RequestMapping("api/categorieCredit")
public class CategorieCreditController {

    @Autowired
    private CategorieService categorieService;

    @PostMapping("creer")
    public CategorieCredit CreateCategoria(@io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody CategorieDto categorieDto) {
        return categorieService.CreateCategorieCredit(categorieDto);
    }

    @GetMapping("recuperer/{id}")
    public CategorieDto getCategorieCredit(@PathVariable Integer id) {
        return categorieService.GetCategorieCredit(id);
    }

    @DeleteMapping("supprimer/{id}")
    public boolean deleteCategorieCredit(@PathVariable Integer id) {
        return categorieService.deleteCategorieCredit(id);
    }

    @GetMapping("list")
    public List<CategorieCredit> getAll() {
        return categorieService.listCategorie();
    }

    @PutMapping("update/{id}")
    public CategorieCredit updateCategorieCredit(@PathVariable Integer id, @io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody CategorieDto categorieDto) {
        return categorieService.UpdateCategorieCredit(id, categorieDto);
    }

    @GetMapping("listDesChamp/{id}")
    public FrAss champCategorie(@PathVariable Integer id) {
        return categorieService.getChampCategorie(id);
    }
}