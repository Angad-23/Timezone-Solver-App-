package com.personal.esttimeconverter.roster;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class RosterController {

    private final RosterService rosterService;

    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    @GetMapping("/roster")
    public String roster(Model model) {
        model.addAttribute("learners", rosterService.getLearners());
        model.addAttribute("tutors", rosterService.getTutors());
        return "roster";
    }

    @PostMapping("/roster/add")
    public String addPerson(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam PersonRole role,
                             RedirectAttributes redirectAttributes) {
        try {
            rosterService.addPerson(name, email, role);
            redirectAttributes.addFlashAttribute("message", "Added " + name + ".");
        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("error", "Couldn't add that person: " + e.getMessage());
        }
        return "redirect:/roster";
    }

    @PostMapping("/roster/import")
    public String importRoster(@RequestPart("file") MultipartFile file,
                                @RequestParam PersonRole role,
                                RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Choose a file first.");
            return "redirect:/roster";
        }
        try {
            RosterService.ImportResult result = rosterService.importFromFile(file, role);
            redirectAttributes.addFlashAttribute("message",
                    "Imported " + result.learnersImported() + " learners and "
                            + result.tutorsImported() + " tutors.");
        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }
        return "redirect:/roster";
    }
}
