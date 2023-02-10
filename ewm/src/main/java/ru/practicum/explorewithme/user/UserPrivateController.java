package ru.practicum.explorewithme.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.user.dto.UserDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/subscriptions")
public class UserPrivateController {
    private final UserService userService;

    @PatchMapping("/{userId}/{onOff}")
    public UserDto subscriptionControl(@PathVariable long userId,
                                       @PathVariable boolean onOff) {
        return userService.subscriptionControl(userId, onOff);
    }

    @PatchMapping("/{userId}/subscribe/{authorId}")
    public UserDto subscribe(@PathVariable long userId,
                             @PathVariable long authorId) {
        return userService.subscribe(userId, authorId);
    }

    @PatchMapping("/{userId}/unsubscribe/{authorId}")
    public UserDto unsubscribe(@PathVariable long userId,
                               @PathVariable long authorId) {
        return userService.unsubscribe(userId, authorId);
    }
}
