package ru.wildmazubot.service;

import org.springframework.stereotype.Service;
import ru.wildmazubot.model.repository.ProjectRepo;

@Service
public class ProjectService {

    private final ProjectRepo projectRepo;

    public ProjectService(ProjectRepo projectRepo) {
        this.projectRepo = projectRepo;
    }
}
