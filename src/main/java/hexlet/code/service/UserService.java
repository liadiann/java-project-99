package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;

    public List<UserDTO> getAll() {
        var users = userRepository.findAll();
        return users.stream().map(userMapper::map).toList();
    }

    public UserDTO findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " not found"));
        return userMapper.map(user);
    }

    public UserDTO createUser(UserCreateDTO data) {
        var user = userMapper.map(data);
        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDTO updateUser(Long id, UserUpdateDTO data) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " not found"));
        userMapper.update(data, user);
        userRepository.save(user);
        return userMapper.map(user);
    }

    public void deleteUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " not found"));
        var hasTasks = taskRepository.existsByAssignee(user);
        if (hasTasks) {
            throw new IllegalStateException("User has task");
        }
        userRepository.deleteById(id);
    }
}
