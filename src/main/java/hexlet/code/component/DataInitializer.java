package hexlet.code.component;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    @Autowired
    private UserService userService;
    @Autowired
    private TaskStatusService taskStatusService;
    @Autowired
    private TaskStatusRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        var email = "hexlet@example.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            var password = "qwerty";
            var userDto = new UserCreateDTO();
            userDto.setEmail(email);
            userDto.setPassword(password);
            userService.createUser(userDto);
        }
        String[][] statuses = {
                {"Draft", "draft"},
                {"ToReview", "to_review"},
                {"ToBeFixed", "to_be_fixed"},
                {"ToPublish", " to_publish"},
                {"Published", "published"}
        };
        for (var status: statuses) {
            var name = status[0];
            var slug = status[1];
            if (repository.findBySlug(slug).isEmpty()) {
                taskStatusService.createStatus(createTaskStatusCreateDTO(name, slug));
            }
        }
    }

    public TaskStatusCreateDTO createTaskStatusCreateDTO(String name, String slug) {
        var status = new TaskStatusCreateDTO();
        status.setName(name);
        status.setSlug(slug);
        return status;
    }
}
