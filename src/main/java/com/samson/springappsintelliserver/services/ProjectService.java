package com.samson.springappsintelliserver.services;

import com.samson.springappsintelliserver.models.Project;
import com.samson.springappsintelliserver.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> getProjects() {
        return this.projectRepository.findAll();
    }

    public Optional<Project> getProjectById(Integer projectId) {
        return this.projectRepository.findById(projectId);
    }

    public Project addProject(Project project) {
        return this.projectRepository.save(project);
    }
}
