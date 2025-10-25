package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {
    @NotBlank
    @Size(min = 1)
    private String title;
    private Integer index;
    @JsonProperty("assignee_id")
    private Long assigneeId;
    private String content;
    @NotBlank
    @Size(min = 1)
    private String status;
    private Set<Long> taskLabelIds;
}
