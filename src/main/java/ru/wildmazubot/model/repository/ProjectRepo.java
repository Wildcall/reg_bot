package ru.wildmazubot.model.repository;

import org.springframework.data.repository.CrudRepository;
import ru.wildmazubot.model.entity.core.Project;

public interface ProjectRepo extends CrudRepository<Project, Long> {
}
