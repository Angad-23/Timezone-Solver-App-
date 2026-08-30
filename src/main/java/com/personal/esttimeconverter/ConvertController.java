package com.personal.esttimeconverter;

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

    @GetMapping("/")
    public String index(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ConvertForm());
        }
        model.addAttribute("bulkOffsetHours", 4);
        model.addAttribute("bulkDefaultDuration", 30);
        return "index";
    }

    @PostMapping("/convert")
    public String convert(@Valid @ModelAttribute("form") ConvertForm form,
                           BindingResult bindingResult,
                           Model model) {
        model.addAttribute("bulkOffsetHours", 4);
        model.addAttribute("bulkDefaultDuration", 30);

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

        List<BulkRow> rows = BulkConverter.convertLines(bulkInput, bulkOffsetHours, bulkDefaultDuration);
        model.addAttribute("bulkRows", rows);

        return "index";
    }
}
