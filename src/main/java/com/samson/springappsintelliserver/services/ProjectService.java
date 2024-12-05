package com.samson.springappsintelliserver.services;

import com.samson.springappsintelliserver.models.Project;
import com.samson.springappsintelliserver.models.Users;
import com.samson.springappsintelliserver.repositories.ProjectRepository;
import com.samson.springappsintelliserver.repositories.UserRepository;
import com.samson.springappsintelliserver.types.UserType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final JWTService jwtService;
    private final MyUserDetailsService usersService;
    private final UserRepository userRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, JWTService jwtService, MyUserDetailsService usersService, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.jwtService = jwtService;
        this.usersService = usersService;
        this.userRepository = userRepository;
    }

    public List<Project> getProjects() {
        return this.projectRepository.findAll();
    }

    public Optional<Project> getProjectById(Integer projectId) {
        return this.projectRepository.findById(projectId);
    }

    public Project addProject(@NonNull Project project) {
        verifyAuthorities();

        // at this stage the user is known to be an admin
        if (project.getManager() == null) {
            Users user = userRepository.findByUsername(extractUsername());

            project.setManager(user);
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        project.setProjectStartDate(LocalDateTime.parse(dateTimeFormatter.format(now), dateTimeFormatter));

        return this.projectRepository.save(project);
    }

    public Project updateProject(@NonNull Integer id, @NonNull Project updatedProject){
        verifyAuthorities();

        Project currentProject = this.projectRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("Project not found")
        );

        Optional.ofNullable(updatedProject.getProjectName())
                .ifPresent(currentProject::setProjectName);

        Optional.ofNullable(updatedProject.getProjectDescription())
                .ifPresent(currentProject::setProjectDescription);

        Optional.ofNullable(updatedProject.getProjectStatus())
                .ifPresent(currentProject::setProjectStatus);

        Optional.ofNullable(updatedProject.getProjectStartDate())
                .ifPresent(currentProject::setProjectStartDate);

        Optional.ofNullable(updatedProject.getProjectEndDate())
                .ifPresent(currentProject::setProjectEndDate);

        return this.projectRepository.save(currentProject);
    }

    public void deleteProject(Integer projectId) {
        verifyAuthorities();
        this.projectRepository.deleteById(projectId);
    }

    private String extractUsername() {
        // extract username from the token via servlet request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").substring(7);

        return jwtService.extractUsername(token);
    }

    private void verifyAuthorities() {
        UserDetails userDetails = usersService.loadUserByUsername(extractUsername());

        // check if user is an admin from the authorities
        userDetails.getAuthorities()
                .stream()
                .filter(authority -> authority.getAuthority().equals(UserType.ADMIN.name()))
                .findAny()
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.FORBIDDEN, "Unauthorized access - Only admins can perform this operation"
                        )
                );
    }
}
