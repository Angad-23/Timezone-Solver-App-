package com.personal.esttimeconverter.session;

import com.personal.esttimeconverter.ConvertForm;
import com.personal.esttimeconverter.roster.Person;
import com.personal.esttimeconverter.roster.PersonRole;
import com.personal.esttimeconverter.roster.RosterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.personal.esttimeconverter.TimeConverter;

import java.io.IOException;

@Controller
public class SessionController {

    private final RosterService rosterService;
    private final SessionService sessionService;

    public SessionController(RosterService rosterService, SessionService sessionService) {
        this.rosterService = rosterService;
        this.sessionService = sessionService;
    }

    @PostMapping("/session/add")
    public String addSession(@Valid @ModelAttribute("sessionForm") SessionForm form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // Re-render the page directly (rather than redirecting) so the
            // validation messages next to the session form are visible.
            model.addAttribute("form", new ConvertForm());
            model.addAttribute("bulkOffsetHours", 4);
            model.addAttribute("bulkDefaultDuration", 30);
            model.addAttribute("learners", rosterService.getLearners());
            model.addAttribute("tutors", rosterService.getTutors());
            model.addAttribute("pendingSessions", sessionService.getAll());
            return "index";
        }

        Person learner = rosterService.findByEmailAndRole(form.getLearnerEmail(), PersonRole.LEARNER);
        Person tutor = rosterService.findByEmailAndRole(form.getTutorEmail(), PersonRole.TUTOR);

        if (learner == null || tutor == null) {
            redirectAttributes.addFlashAttribute("error", "Couldn't find that learner or tutor in the roster.");
            return "redirect:/";
        }

        TimeConverter.ConvertedTimes times = TimeConverter.convert(
                form.getDate().atTime(form.getStartTime()),
                form.getOffsetHours(),
                form.getDurationMinutes()
        );

        sessionService.add(new SessionRow(
                learner.getEmail(), learner.getName(),
                tutor.getEmail(), tutor.getName(),
                times.lessonStartTime(), times.lessonEndTime(),
                form.getSubject()
        ));

        return "redirect:/";
    }

    @PostMapping("/sessions/clear")
    public String clearSessions() {
        sessionService.clear();
        return "redirect:/";
    }

    @GetMapping("/sessions/download")
    public ResponseEntity<byte[]> downloadSessions() throws IOException {
        byte[] content = SessionExcelExporter.export(sessionService.getAll());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "lesson-upload.xlsx");

        return ResponseEntity.ok().headers(headers).body(content);
    }
}
