package ru.practicum.explorewithme.user;

import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.dto.UserShortDto;
import ru.practicum.explorewithme.user.model.User;

import java.util.HashSet;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAuthors(),
                user.isSubscriptionAllowed()
        );
    }

    public static UserShortDto toUserShortDto(User user) {
        return new UserShortDto(
                user.getId(),
                user.getName()
        );
    }

    public static User toNewUser(NewUserRequest newUserRequest) {
        return new User(
                0,
                newUserRequest.getName(),
                newUserRequest.getEmail(),
                new HashSet<>(),
                false
        );
    }
}
