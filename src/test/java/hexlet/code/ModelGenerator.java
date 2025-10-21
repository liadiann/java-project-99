package hexlet.code;

import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import hexlet.code.model.User;
import lombok.Getter;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ModelGenerator {
    private Model<User> userModel;
    private Model<TaskStatus> taskStatusModel;
    private Model<Task> taskModel;
    @Autowired
    private Faker faker;
    @PostConstruct
    public void init() {
        userModel = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest), () -> faker.text().text(3, 20))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .toModel();
        var name = faker.text().text(1, 20);
        var capitalizedName = name.substring(0, 1).toUpperCase() + name.substring(1);
        taskStatusModel = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getName), () -> capitalizedName)
                .supply(Select.field(TaskStatus::getSlug), () -> capitalizedName.toLowerCase())
                .toModel();
        taskModel = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .supply(Select.field(Task::getName), () -> faker.name().title())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence())
                .supply(Select.field(Task::getIndex), () -> faker.number().randomDigit())
                .ignore(Select.field(Task::getTaskStatus))
                .ignore(Select.field(Task::getAssignee))
                .toModel();
    }
}
