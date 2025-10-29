package hexlet.code.component;

import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class DataInitializer implements ApplicationRunner {
    private final PasswordEncoder passwordEncoder;
    private final TaskStatusRepository taskStatusRepository;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        var email = "hexlet@example.com";
        var password = "qwerty";
        if (userRepository.findByEmail(email).isEmpty()) {
            var user = new User();
            user.setEmail(email);
            user.setPasswordDigest(passwordEncoder.encode(password));
            userRepository.save(user);
        }

        String[][] statuses = {
                {"Draft", "draft"},
                {"ToReview", "to_review"},
                {"ToBeFixed", "to_be_fixed"},
                {"ToPublish", " to_publish"},
                {"Published", "published"}
        };

        for (String[] status : statuses) {
            String name = status[0];
            String slug = status[1];

            if (taskStatusRepository.findBySlug(slug).isEmpty()) {
                var taskStatus = new TaskStatus();
                taskStatus.setSlug(slug);
                taskStatus.setName(name);
                taskStatusRepository.save(taskStatus);
            }
        }

        String[] labels = {"feature", "bug"};
        for (var name : labels) {
            if (labelRepository.findByName(name).isEmpty()) {
                var label = new Label();
                label.setName(name);
                labelRepository.save(label);
            }
        }
    }
}
