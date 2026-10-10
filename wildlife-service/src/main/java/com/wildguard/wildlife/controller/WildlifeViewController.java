package com.wildguard.wildlife.controller;

import com.wildguard.wildlife.entity.Animal;
import com.wildguard.wildlife.entity.Habitat;
import com.wildguard.wildlife.entity.ProtectedArea;
import com.wildguard.wildlife.entity.Species;
import com.wildguard.wildlife.entity.WaterSource;
import com.wildguard.wildlife.entity.Zone;
import com.wildguard.wildlife.service.WildlifeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/wildlife")
public class WildlifeViewController {

    private final WildlifeService wildlifeService;

    @Autowired
    public WildlifeViewController(WildlifeService wildlifeService) {
        this.wildlifeService = wildlifeService;
    }

    // -------------------------------------------------------------------------
    // Animals & QR Passport Page
    // -------------------------------------------------------------------------
    @GetMapping("/animals")
    public String showAnimalsPage(@RequestParam(required = false) String healthStatus,
                                  @RequestParam(required = false) Long zoneId,
                                  @RequestParam(required = false) Long speciesId,
                                  Model model) {
        model.addAttribute("animals", wildlifeService.getAllAnimals(zoneId, speciesId, healthStatus));
        model.addAttribute("speciesList", wildlifeService.getAllSpecies());
        model.addAttribute("zones", wildlifeService.getAllZones());
        model.addAttribute("newAnimal", new Animal());
        model.addAttribute("filterHealth", healthStatus);
        model.addAttribute("selectedZoneId", zoneId);
        model.addAttribute("selectedSpeciesId", speciesId);
        return "animals";
    }

    @PostMapping("/animals/add")
    public String addAnimal(@ModelAttribute Animal animal) {
        wildlifeService.saveAnimal(animal);
        return "redirect:/wildlife/animals";
    }

    @GetMapping("/animals/{id}")
    public String showAnimalDetails(@PathVariable Long id, Model model) {
        Animal animal = wildlifeService.getAnimalById(id);
        model.addAttribute("animal", animal);
        return "animal-details";
    }

    // -------------------------------------------------------------------------
    // Habitats & Water Watchlist Page
    // -------------------------------------------------------------------------
    @GetMapping("/habitats")
    public String showHabitatsPage(Model model) {
        model.addAttribute("habitats", wildlifeService.getAllHabitats());
        model.addAttribute("waterSources", wildlifeService.getAllWaterSources());
        model.addAttribute("zones", wildlifeService.getAllZones());
        model.addAttribute("areas", wildlifeService.getAllAreas());
        model.addAttribute("newWaterSource", new WaterSource());
        model.addAttribute("newHabitat", new Habitat());
        return "habitats";
    }

    @PostMapping("/water-sources/{id}/toggle")
    public String toggleWaterStatus(@PathVariable Long id, @RequestParam String currentStatus) {
        String nextStatus = "LOW".equalsIgnoreCase(currentStatus) ? "NORMAL" : "LOW";
        wildlifeService.updateWaterStatus(id, nextStatus);
        return "redirect:/wildlife/habitats";
    }

    @PostMapping("/water-sources/add")
    public String addWaterSource(@ModelAttribute WaterSource waterSource) {
        wildlifeService.saveWaterSource(waterSource);
        return "redirect:/wildlife/habitats";
    }

    @PostMapping("/habitats/add")
    public String addHabitat(@ModelAttribute Habitat habitat) {
        wildlifeService.saveHabitat(habitat);
        return "redirect:/wildlife/habitats";
    }

    // -------------------------------------------------------------------------
    // Species Management (Utility Routes)
    // -------------------------------------------------------------------------
    @PostMapping("/species/add")
    public String addSpecies(@ModelAttribute Species species) {
        wildlifeService.saveSpecies(species);
        return "redirect:/wildlife/animals";
    }

    // -------------------------------------------------------------------------
    // Zones & Protected Areas (Utility Routes)
    // -------------------------------------------------------------------------
    @PostMapping("/zones/add")
    public String addZone(@ModelAttribute Zone zone) {
        wildlifeService.saveZone(zone);
        return "redirect:/wildlife/habitats";
    }

    @PostMapping("/areas/add")
    public String addProtectedArea(@ModelAttribute ProtectedArea area) {
        wildlifeService.saveArea(area);
        return "redirect:/wildlife/habitats";
    }
}