package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private Integer index;
    @JsonProperty("assignee_id")
    private Long assigneeId;
    private String title;
    private String content;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
    private Set<Long> labelIds;
}
