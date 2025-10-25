package hexlet.code.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.ModelGenerator;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private ModelGenerator modelGenerator;
    @Autowired
    private TaskSpecification specBuilder;

    private User user;
    private Task task;
    private TaskStatus status;
    private Label label;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void beforeEach() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();
        user = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(user);
        status = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(status);
        label = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(label);
        var labels = Set.of(label);
        task = Instancio.of(modelGenerator.getTaskModel()).create();
        task.setAssignee(user);
        task.setTaskStatus(status);
        task.setLabels(labels);
        taskRepository.save(task);
        token = jwt();
    }

    @AfterEach
    public void afterEach() {
        taskRepository.deleteAll();
    }

    @Test
    public void testIndex() throws Exception {
        var response = mockMvc.perform(get("/api/tasks").with(token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        assertThatJson(body).isArray();
        var dto = om.readValue(body, new TypeReference<List<TaskDTO>>() {
        });
        var actual = dto.stream().map(taskMapper::map).toList();
        var expected = taskRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testIndexWithParams() throws Exception {
        var params = new TaskParamsDTO();
        params.setAssigneeId(user.getId());
        params.setStatus(status.getSlug());
        params.setLabelId(label.getId());
        params.setTitleCont(task.getName() + "!!!");
        var response = mockMvc.perform(get("/api/tasks?assigneeId=" + params.getAssigneeId()
                + "&status=" + params.getStatus() + "&labelId=" + params.getLabelId()
                + "&titleCont=" + params.getTitleCont()).with(token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        assertThatJson(body).isArray();
        var dto = om.readValue(body, new TypeReference<List<TaskDTO>>() {
        });
        var actual = dto.stream().map(taskMapper::map).toList();
        var spec = specBuilder.build(params);
        var expected = taskRepository.findAll(spec);
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testShow() throws Exception {
        var response = mockMvc.perform(get("/api/tasks/" + task.getId()).with(token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("title").isEqualTo(task.getName()),
                v -> v.node("content").isEqualTo(task.getDescription()),
                v -> v.node("status").isEqualTo(task.getTaskStatus().getSlug()),
                v -> v.node("assignee_id").isEqualTo(task.getAssignee().getId()),
                v -> v.node("index").isEqualTo(task.getIndex())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var data = new TaskCreateDTO();
        data.setTitle("Work");
        data.setStatus(status.getSlug());
        data.setTaskLabelIds(Set.of(label.getId()));
        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isCreated());
        var createdTask = taskRepository.findByName(data.getTitle()).orElse(null);
        assertNotNull(createdTask);
        assertThat(createdTask.getName()).isEqualTo(data.getTitle());
        assertThat(createdTask.getTaskStatus().getSlug()).isEqualTo(data.getStatus());
        assertThat(createdTask.getLabels().size()).isEqualTo(1);
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("title", "Task 1");
        var request = put("/api/tasks/" + task.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request).andExpect(status().isOk());
        var updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getName()).isEqualTo(data.get("title"));
    }

    @Test
    public void testDestroy() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + task.getId()).with(token))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        var deletedStatus = taskRepository.findById(task.getId()).orElse(null);
        assertThat(deletedStatus).isNull();
    }

    @Test
    public void testResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/15").with(token))
                .andExpect(status().isNotFound());
        var data = new HashMap<>();
        data.put("title", "Task 2");
        mockMvc.perform(put("/api/tasks/15")
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void failedValidation() throws Exception {
        var createdData = new TaskCreateDTO();
        createdData.setTitle("");
        createdData.setStatus("");
        var requestCreate = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createdData));
        mockMvc.perform(requestCreate)
                .andExpect(status().isBadRequest());
        var updatedData = new HashMap<>();
        updatedData.put("title", "");
        var requestUpdate = put("/api/tasks/" + task.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updatedData));
        mockMvc.perform(requestUpdate).andExpect(status().isBadRequest());
    }

    @Test
    public void testNotAuth() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + task.getId()))
                .andExpect(status().isUnauthorized());
        var createdData = new TaskCreateDTO();
        createdData.setTitle("Work");
        createdData.setStatus("draft");
        var requestCreated = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createdData));
        mockMvc.perform(requestCreated)
                .andExpect(status().isUnauthorized());
        var updatedData = new HashMap<>();
        updatedData.put("title", "Task 2");
        var requestUpdate = put("/api/tasks/" + task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updatedData));
        mockMvc.perform(requestUpdate).andExpect(status().isUnauthorized());
    }
}
