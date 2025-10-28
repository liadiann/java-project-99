package hexlet.code.service;

import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceExistsException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskStatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskStatusService {
    private final TaskStatusRepository repository;
    private final TaskStatusMapper mapper;

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
        if (repository.findBySlug(status.getSlug()).isPresent()) {
            throw new  ResourceExistsException("Status already exists");
        }
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
        repository.deleteById(id);
    }
}
