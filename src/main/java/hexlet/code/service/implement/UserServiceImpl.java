package hexlet.code.service.implement;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDTO> getAll() {
        var users = userRepository.findAll();
        return users.stream().map(userMapper::map).toList();
    }

    @Override
    public UserDTO findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " not found"));
        return userMapper.map(user);
    }

    @Override
    public UserDTO createUser(UserCreateDTO data) {
        var user = userMapper.map(data);
        userRepository.save(user);
        return userMapper.map(user);
    }

    @Override
    public UserDTO updateUser(Long id, UserUpdateDTO data) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " not found"));
        userMapper.update(data, user);
        userRepository.save(user);
        return userMapper.map(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
