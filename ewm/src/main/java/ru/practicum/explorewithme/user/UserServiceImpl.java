package ru.practicum.explorewithme.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.exception.SubscriptionIsNotAllowedException;
import ru.practicum.explorewithme.exception.UserNotFoundException;
import ru.practicum.explorewithme.pageable.OffsetLimitPageable;
import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDto> getAllUsers(List<Long> ids, int from, int size) {
        List<User> users = userRepository.findUsersByIdIn(ids, OffsetLimitPageable.of(from, size));
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        User user = UserMapper.toNewUser(newUserRequest);
        user = userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        }
    }

    @Override
    @Transactional
    public UserDto subscriptionControl(long userId, boolean onOff) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        user.setSubscriptionAllowed(onOff);
        user = userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto subscribe(long userId, long authorId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        User author = userRepository.findById(authorId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        if (author.isSubscriptionAllowed()) {
            user.getAuthors().add(author);
            user = userRepository.save(user);
            return UserMapper.toUserDto(user);
        } else {
            throw new SubscriptionIsNotAllowedException("Данный автор запретил подписку на себя!");
        }
    }

    @Override
    @Transactional
    public UserDto unsubscribe(long userId, long authorId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        User author = userRepository.findById(authorId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        user.getAuthors().remove(author);
        user = userRepository.save(user);
        return UserMapper.toUserDto(user);
    }
}
