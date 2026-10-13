package mate.academy.car_sharing_app.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import mate.academy.car_sharing_app.dto.userDto.UserRegistrationRequestDto;
import mate.academy.car_sharing_app.dto.userDto.UserResponseDto;
import mate.academy.car_sharing_app.model.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toEntity_shouldMapRequestFieldsAndIgnoreIdAndRole() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setFirstName("John");
        requestDto.setLastName("Smith");
        requestDto.setPassword("password123");

        User user = userMapper.toEntity(requestDto);

        assertNull(user.getId());
        assertNull(user.getRole());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void toDto_shouldMapUserFields() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Smith");

        UserResponseDto responseDto = userMapper.toDto(user);

        assertEquals(1L, responseDto.getId());
        assertEquals("test@example.com", responseDto.getEmail());
        assertEquals("John", responseDto.getFirstName());
        assertEquals("Smith", responseDto.getLastName());
    }
}
