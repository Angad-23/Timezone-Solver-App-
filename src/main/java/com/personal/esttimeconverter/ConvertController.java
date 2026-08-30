package com.personal.esttimeconverter;

import com.personal.esttimeconverter.roster.RosterService;
import com.personal.esttimeconverter.session.SessionForm;
import com.personal.esttimeconverter.session.SessionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ConvertController {

    private final RosterService rosterService;
    private final SessionService sessionService;

    public ConvertController(RosterService rosterService, SessionService sessionService) {
        this.rosterService = rosterService;
        this.sessionService = sessionService;
    }

    @GetMapping("/")
    public String index(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ConvertForm());
        }
        addCommonAttributes(model);
        return "index";
    }

    @PostMapping("/convert")
    public String convert(@Valid @ModelAttribute("form") ConvertForm form,
                           BindingResult bindingResult,
                           Model model) {
        addCommonAttributes(model);

        if (bindingResult.hasErrors()) {
            return "index";
        }

        TimeConverter.ConvertedTimes result = TimeConverter.convert(
                form.getDate().atTime(form.getStartTime()),
                form.getOffsetHours(),
                form.getDurationMinutes()
        );

        model.addAttribute("result", result);
        return "index";
    }

    @PostMapping("/convert-bulk")
    public String convertBulk(@RequestParam String bulkInput,
                               @RequestParam(defaultValue = "4") int bulkOffsetHours,
                               @RequestParam(defaultValue = "30") int bulkDefaultDuration,
                               Model model) {
        model.addAttribute("form", new ConvertForm());
        model.addAttribute("bulkInput", bulkInput);
        model.addAttribute("bulkOffsetHours", bulkOffsetHours);
        model.addAttribute("bulkDefaultDuration", bulkDefaultDuration);
        addCommonAttributes(model);

        List<BulkRow> rows = BulkConverter.convertLines(bulkInput, bulkOffsetHours, bulkDefaultDuration);
        model.addAttribute("bulkRows", rows);

        return "index";
    }

    private void addCommonAttributes(Model model) {
        if (!model.containsAttribute("bulkOffsetHours")) {
            model.addAttribute("bulkOffsetHours", 4);
        }
        if (!model.containsAttribute("bulkDefaultDuration")) {
            model.addAttribute("bulkDefaultDuration", 30);
        }
        if (!model.containsAttribute("sessionForm")) {
            model.addAttribute("sessionForm", new SessionForm());
        }
        model.addAttribute("learners", rosterService.getLearners());
        model.addAttribute("tutors", rosterService.getTutors());
        model.addAttribute("pendingSessions", sessionService.getAll());
    }
}
