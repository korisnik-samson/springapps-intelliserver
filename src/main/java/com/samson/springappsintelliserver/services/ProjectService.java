package com.samson.springappsintelliserver.services;

import com.samson.springappsintelliserver.models.Project;
import com.samson.springappsintelliserver.models.UserPrincipal;
import com.samson.springappsintelliserver.repositories.ProjectRepository;
import com.samson.springappsintelliserver.types.UserType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final JWTService jwtService;
    private final MyUserDetailsService usersService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, JWTService jwtService, MyUserDetailsService usersService) {
        this.projectRepository = projectRepository;
        this.jwtService = jwtService;
        this.usersService = usersService;
    }

    public List<Project> getProjects() {
        return this.projectRepository.findAll();
    }

    public Optional<Project> getProjectById(Integer projectId) {
        return this.projectRepository.findById(projectId);
    }

    public Project addProject(Project project) {
        // get the user token via http request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").substring(7);

        // extract user authority from token
        String username = jwtService.extractUsername(token);
        UserDetails userDetails = usersService.loadUserByUsername(username);

        // check if user is an admin from the authorities
        userDetails.getAuthorities()
                .stream()
                .filter(authority -> authority.getAuthority().equals(UserType.ADMIN.name()))
                .findAny()
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.FORBIDDEN, "Unauthorized access - Only admin can create projects"
                        )
                );

        return this.projectRepository.save(project);
    }

    private boolean verifyAuthorities() {
        return false;
    }
}
