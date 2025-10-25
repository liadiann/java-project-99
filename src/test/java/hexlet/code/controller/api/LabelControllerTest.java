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
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
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
public class LabelControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private LabelMapper labelMapper;
    @Autowired
    private ObjectMapper om;
    @Autowired
    private ModelGenerator modelGenerator;

    private Label label;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void beforeEach() {
        labelRepository.deleteAll();
        label = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(label);
        token = jwt();
    }

    @Test
    public void testIndex() throws Exception {
        var response = mockMvc.perform(get("/api/labels").with(token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        assertThatJson(body).isArray();
        var dto = om.readValue(body, new TypeReference<List<LabelDTO>>() {
        });
        var actual = dto.stream()
                .map(labelMapper::map)
                .toList();
        var expected = labelRepository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testShow() throws Exception {
        var response = mockMvc.perform(get("/api/labels/" + label.getId()).with(token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(label.getName()),
                v -> v.node("id").isEqualTo(label.getId())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var data = Instancio.of(modelGenerator.getLabelModel()).create();
        data.setName("Feature");
        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request)
                .andExpect(status().isCreated());
        var createdLabel = labelRepository.findByName(data.getName()).orElse(null);
        assertNotNull(createdLabel);
        assertThat(createdLabel.getName()).isEqualTo(data.getName());
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("name", "Bug");
        var request = put("/api/labels/" + label.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));
        mockMvc.perform(request).andExpect(status().isOk());
        var updatedLabel = labelRepository.findById(label.getId()).orElseThrow();
        assertThat(updatedLabel.getName()).isEqualTo(data.get("name"));
    }

    @Test
    public void testDestroy() throws Exception {
        mockMvc.perform(delete("/api/labels/" + label.getId()).with(token))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        var deletedLabel = labelRepository.findById(label.getId()).orElse(null);
        assertThat(deletedLabel).isNull();
    }

    @Test
    public void testResourceNotFound() throws Exception {
        mockMvc.perform(get("/api/labels/15").with(token))
                .andExpect(status().isNotFound());
        var data = new HashMap<>();
        data.put("name", "bug");
        mockMvc.perform(put("/api/labels/15")
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void failedValidation() throws Exception {
        var createdData = Instancio.of(modelGenerator.getLabelModel()).create();
        createdData.setName("aa");
        var requestCreate = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createdData));
        mockMvc.perform(requestCreate).andExpect(status().isBadRequest());

        var updateData = new HashMap<>();
        updateData.put("name", "v");
        var requestUpdate = put("/api/labels/" + label.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(requestUpdate).andExpect(status().isBadRequest());
    }

    @Test
    public void testNotAuth() throws Exception {
        mockMvc.perform(delete("/api/labels/" + label.getId()))
                .andExpect(status().isUnauthorized());
        var createData = Instancio.of(modelGenerator.getLabelModel()).create();
        var requestCreate = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));
        mockMvc.perform(requestCreate)
                .andExpect(status().isUnauthorized());
        var updateData = new HashMap<>();
        updateData.put("name", "Bugg");
        var requestUpdate = put("/api/labels/" + label.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(requestUpdate)
                .andExpect(status().isUnauthorized());
    }
}
