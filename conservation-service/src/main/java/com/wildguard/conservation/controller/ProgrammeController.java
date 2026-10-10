package com.wildguard.conservation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.wildguard.conservation.entity.Programme.ProgrammeStatus;
import com.wildguard.conservation.entity.Programme;
import com.wildguard.conservation.entity.ProgrammeActivity;
import com.wildguard.conservation.service.ProgrammeService;
import com.wildguard.conservation.util.LoginCheck.LoginUser;
import com.wildguard.conservation.util.LoginCheck;

import jakarta.servlet.http.HttpServletRequest;

// REST API for conservation programmes. Everyone on the staff can see them,
// only forest officer and admin can create / change them and record work.
@RestController
@RequestMapping("/api/programmes")
@CrossOrigin(origins = "${wildguard.allowed-origins}")
public class ProgrammeController {

    private ProgrammeService programmeService;
    private LoginCheck login;

    public ProgrammeController(ProgrammeService programmeService, LoginCheck login) {
        this.programmeService = programmeService;
        this.login = login;
    }

    @GetMapping
    public List<Programme> list(@RequestParam(required = false) ProgrammeStatus status, HttpServletRequest request) {
        login.check(request);
        return programmeService.list(status);
    }

    // one programme with its activities
    @GetMapping("/{id}")
    public Programme get(@PathVariable Long id, HttpServletRequest request) {
        login.check(request);
        return programmeService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Programme create(@RequestBody Programme form, HttpServletRequest request) {
        login.check(request, "ADMIN", "FOREST_OFFICER");
        return programmeService.create(form);
    }

    @PutMapping("/{id}")
    public Programme update(@PathVariable Long id, @RequestBody Programme form, HttpServletRequest request) {
        login.check(request, "ADMIN", "FOREST_OFFICER");
        return programmeService.update(id, form);
    }

    // the answer already has the new progress %
    @PostMapping("/{id}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public Programme addActivity(@PathVariable Long id, @RequestBody ProgrammeActivity form, HttpServletRequest request) {
        LoginUser user = login.check(request, "ADMIN", "FOREST_OFFICER");
        return programmeService.addActivity(id, form, user);
    }
}
