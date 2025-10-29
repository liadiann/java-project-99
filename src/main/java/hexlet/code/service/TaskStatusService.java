package hexlet.code.service;

import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;

import java.util.List;

public interface TaskStatusService {
    List<TaskStatusDTO> getAll();
    TaskStatusDTO findById(Long id);
    TaskStatusDTO createStatus(TaskStatusCreateDTO data);
    TaskStatusDTO updateStatus(Long id, TaskStatusUpdateDTO data);
    void deleteStatus(Long id);
}
