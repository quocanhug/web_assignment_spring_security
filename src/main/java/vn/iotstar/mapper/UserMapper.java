package vn.iotstar.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.User;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "productCount", expression = "java(user.getProducts() != null ? (long) user.getProducts().size() : 0L)")
    UserDTO toDTO(User user);

    List<UserDTO> toDtoList(List<User> users);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "products", ignore = true)
    User toEntity(UserDTO dto);
}
