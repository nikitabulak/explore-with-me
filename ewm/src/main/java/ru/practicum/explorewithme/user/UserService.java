package ru.practicum.explorewithme.user;

import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;

import java.util.List;

public interface UserService {
    //Admin___________________________________________________
    List<UserDto> getAllUsers(List<Long> ids, int from, int size);

    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUser(long userId);

    UserDto subscriptionControl(long userId, boolean onOff);

    UserDto subscribe(long userId, long authorId);

    UserDto unsubscribe(long userId, long authorId);


}
