package com.samson.springappsintelliserver.controllers;

import com.samson.springappsintelliserver.models.Project;
import com.samson.springappsintelliserver.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // endpoint should be protected as used only if needed
    @GetMapping(path = "api/projects")
    public List<Project> getProjects() {
        return this.projectService.getProjects();
    }

    @GetMapping(path = "api/projects/{id}")
    public Optional<Project> getProjectById(@PathVariable("id") Integer projectId) {
        return this.projectService.getProjectById(projectId);
    }

    // endpoint should be protected as such - only admin can create projects
    @PostMapping(path = "api/projects")
    public Project createProject(@RequestBody Project project) {
        return this.projectService.addProject(project);
    }

    // endpoint should be protected as such - only admin can update projects
    @PatchMapping(path = "api/projects/{id}")
    public Project updateProject(@PathVariable("id") Integer projectId, @RequestBody Project project) {
        return this.projectService.updateProject(projectId, project);
    }

}
