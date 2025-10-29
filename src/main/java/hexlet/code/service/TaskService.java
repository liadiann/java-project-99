package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;

import java.util.List;

public interface TaskService {
    List<TaskDTO> getAll(TaskParamsDTO params);
    TaskDTO findById(Long id);
    TaskDTO createTask(TaskCreateDTO data);
    TaskDTO updateTask(Long id, TaskUpdateDTO data);
    void deleteTask(Long id);
}
