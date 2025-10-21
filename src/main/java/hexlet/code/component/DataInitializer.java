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
        var password = "qwerty";
        var userDto = new UserCreateDTO();
        userDto.setEmail(email);
        userDto.setPassword(password);
        userService.createUser(userDto);

        taskStatusService.createStatus(createTaskStatusCreateDTO("Draft", "draft"));
        taskStatusService.createStatus(createTaskStatusCreateDTO("ToReview", "to_review"));
        taskStatusService.createStatus(createTaskStatusCreateDTO("ToBeFixed", "to_be_fixed"));
        taskStatusService.createStatus(createTaskStatusCreateDTO("ToPublish", " to_publish"));
        taskStatusService.createStatus(createTaskStatusCreateDTO("Published", "published"));
    }

    public TaskStatusCreateDTO createTaskStatusCreateDTO(String name, String slug) {
        var status = new TaskStatusCreateDTO();
        status.setName(name);
        status.setSlug(slug);
        return status;
    }
}
