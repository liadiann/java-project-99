package hexlet.code.service;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;

import java.util.List;

public interface LabelService {
    List<LabelDTO> getAll();
    LabelDTO findById(Long id);
    LabelDTO createLabel(LabelCreateDTO data);
    LabelDTO updateLabel(Long id, LabelUpdateDTO data);
    void delete(Long id);
}
