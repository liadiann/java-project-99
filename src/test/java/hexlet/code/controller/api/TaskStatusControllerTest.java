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
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.instancio.Instancio;
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

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TaskStatusRepository repository;
    @Autowired
    private TaskStatusMapper mapper;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private ModelGenerator modelGenerator;

    private TaskStatus status;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void beforeEach() {
        repository.deleteAll();
        status = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        repository.save(status);
        token = jwt();
    }

    @Test
    public void testIndex() throws Exception {
        var response = mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        assertThatJson(body).isArray();
        var dto = om.readValue(body, new TypeReference<List<TaskStatusDTO>>() {
        });
        var actual = dto.stream()
                .map(mapper::map)
                .toList();
        var expected = repository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testShow() throws Exception {
        var response = mockMvc.perform(get("/api/task_statuses/" + status.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(status.getName()),
                v -> v.node("slug").isEqualTo(status.getSlug()),
                v -> v.node("id").isEqualTo(status.getId())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var data = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        data.setName("Draft");
        data.setSlug("draft");
        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isCreated());
        var createdStatus = repository.findBySlug(data.getSlug()).orElse(null);
        assertNotNull(createdStatus);
        assertThat(createdStatus.getName()).isEqualTo(data.getName());
        assertThat(createdStatus.getSlug()).isEqualTo(data.getSlug());
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("name", "Draft");
        var request = put("/api/task_statuses/" + status.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request).andExpect(status().isOk());
        var updatedStatus = repository.findById(status.getId()).orElseThrow();
        assertThat(updatedStatus.getName()).isEqualTo(data.get("name"));
    }

    @Test
    public void testDestroy() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/" + status.getId()).with(token))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        var deletedStatus = repository.findById(status.getId()).orElse(null);
        assertThat(deletedStatus).isNull();
    }

    @Test
    public void testResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/task_statuses/150").with(token))
                .andExpect(status().isNotFound());
        var data = new HashMap<>();
        data.put("name", "new");
        mockMvc.perform(put("/api/task_statuses/150")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void failedValidation() throws Exception {
        var createdData = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        createdData.setName("");
        createdData.setSlug("");
        var requestCreate = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createdData));
        mockMvc.perform(requestCreate).andExpect(status().isBadRequest());

        var updateData = new HashMap<>();
        updateData.put("slug", "");
        var requestUpdate = put("/api/task_statuses/" + status.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(requestUpdate).andExpect(status().isBadRequest());
    }

    @Test
    public void testNotAuth() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/" + status.getId()))
                .andExpect(status().isUnauthorized());
        var createData = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        var requestCreate = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));
        mockMvc.perform(requestCreate)
                .andExpect(status().isUnauthorized());
        var updateData = new HashMap<>();
        updateData.put("name", "new");
        var requestUpdate = put("/api/task_statuses/" + status.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(requestUpdate)
                .andExpect(status().isUnauthorized());
    }
}
