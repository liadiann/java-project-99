package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStatusRepository repository;
    @Autowired
    private TaskStatusMapper mapper;

    public List<TaskStatusDTO> getAll() {
        var statuses = repository.findAll();
        return statuses.stream().map(mapper::map).toList();
    }

    public TaskStatusDTO findById(Long id) {
        var status = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
        return mapper.map(status);
    }

    public TaskStatusDTO createStatus(TaskStatusCreateDTO data) {
        var status = mapper.map(data);
        repository.save(status);
        return mapper.map(status);
    }

    public TaskStatusDTO updateStatus(Long id, TaskStatusUpdateDTO data) {
        var status = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
        mapper.update(data, status);
        repository.save(status);
        return mapper.map(status);
    }

    public void deleteStatus(Long id) {
        var status = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
        var hasTasks = taskRepository.existsByTaskStatusId(id);
        if (hasTasks) {
            throw new IllegalStateException("Status has task");
        }
        repository.deleteById(id);
    }
}
