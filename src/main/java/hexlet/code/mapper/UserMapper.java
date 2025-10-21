package hexlet.code.mapper;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JsonNullableMapper jsonNullableMapper;
    @BeforeMapping
    public void encryptPassword(UserCreateDTO dto) {
        var password = dto.getPassword();
        dto.setPassword(passwordEncoder.encode(password));
    }
    @BeforeMapping
    public void encryptUpdatePassword(UserUpdateDTO dto) {
        var password = dto.getPassword();
        if (password != null && password.isPresent()) {
            dto.setPassword(jsonNullableMapper.wrap(passwordEncoder.encode(password.get())));
        }
    }
    public abstract UserDTO map(User user);
    @Mapping(target = "passwordDigest", source = "password")
    public abstract User map(UserCreateDTO dto);
    public abstract User map(UserDTO dto);
    @Mapping(target = "passwordDigest", source = "password")
    public abstract void update(UserUpdateDTO dto, @MappingTarget User user);
}
