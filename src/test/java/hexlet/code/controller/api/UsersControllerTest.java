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
import hexlet.code.dto.user.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
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
public class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Faker faker;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private ModelGenerator modelGenerator;

    private User user;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
        user = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(user);
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
    }

    @Test
    public void testIndex() throws Exception {
        var response = mockMvc.perform(get("/api/users").with(token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        assertThatJson(body).isArray();
        var dto = om.readValue(body, new TypeReference<List<UserDTO>>() {
        });
        var actual = dto.stream()
                .map(userMapper::map)
                .toList();
        var expected = userRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testShow() throws Exception {
        var response = mockMvc.perform(get("/api/users/" + user.getId()).with(token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(user.getId()),
                v -> v.node("firstName").isEqualTo(user.getFirstName()),
                v -> v.node("lastName").isEqualTo(user.getLastName()),
                v -> v.node("email").isEqualTo(user.getEmail())
        );
    }

    @Test
    public void testResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/users/15").with(token))
                .andExpect(status().isNotFound());
        var data = new HashMap<>();
        data.put("firstName", "Luca");
        mockMvc.perform(put("/api/users/15").with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreate() throws Exception {
        var data = Instancio.of(modelGenerator.getUserModel()).create();
        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isCreated());
        var createdUser = userRepository.findByEmail(data.getEmail()).orElse(null);
        assertNotNull(createdUser);
        assertThat(createdUser.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(createdUser.getLastName()).isEqualTo(data.getLastName());
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("firstName", "Luca");
        var request = put("/api/users/" + user.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isOk());
        var updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo(data.get("firstName"));
    }

    @Test
    public void testFailedValidation() throws Exception {
        var createData = Instancio.of(modelGenerator.getUserModel()).create();
        createData.setEmail("aaa");
        var requestCreate = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));
        mockMvc.perform(requestCreate)
                .andExpect(status().isBadRequest());
        var updateData = new HashMap<>();
        updateData.put("email", "aaa");
        var requestUpdate = put("/api/users/" + user.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(requestUpdate)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDestroy() throws Exception {
        mockMvc.perform(delete("/api/users/" + user.getId()).with(token))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        var deletedUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(deletedUser).isNull();
    }

    @Test
    public void testNotAuth() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/" + user.getId()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/users/" + user.getId()))
                .andExpect(status().isUnauthorized());
        var createData = Instancio.of(modelGenerator.getUserModel()).create();
        var requestCreate = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));
        mockMvc.perform(requestCreate)
                .andExpect(status().isUnauthorized());
        var updateData = new HashMap<>();
        updateData.put("email", "aaa");
        var requestUpdate = put("/api/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(requestUpdate)
                .andExpect(status().isUnauthorized());
    }
}
