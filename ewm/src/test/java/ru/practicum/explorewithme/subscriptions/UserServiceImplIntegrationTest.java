package ru.practicum.explorewithme.subscriptions;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.user.UserServiceImpl;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.model.User;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=explorewithme-test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext
public class UserServiceImplIntegrationTest {
    private final EntityManager em;

    private final UserServiceImpl userService;

    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDB() {
        String sqlQuery = "DELETE FROM users";
        jdbcTemplate.update(sqlQuery);
        sqlQuery = "ALTER TABLE users ALTER COLUMN id RESTART WITH 1";
        jdbcTemplate.update(sqlQuery);
        sqlQuery = "DELETE FROM subscriptions";
        jdbcTemplate.update(sqlQuery);
    }

    @Test
    void subscriptionControl() {
        em.createNativeQuery("insert into users (name, email, subscription_allowed) values (?,?,?)")
                .setParameter(1, "userName")
                .setParameter(2, "userEmail@mail.ru")
                .setParameter(3, false)
                .executeUpdate();
        UserDto userDto = new UserDto(1, "userName", "userEmail@mail.ru", new HashSet<>(), true);
        UserDto resultDto = userService.subscriptionControl(1, true);
        assertThat(resultDto.getId(), equalTo(userDto.getId()));
        assertThat(resultDto.getName(), equalTo(userDto.getName()));
        assertThat(resultDto.getEmail(), equalTo(userDto.getEmail()));
        assertThat(resultDto.getAuthors().size(), equalTo(userDto.getAuthors().size()));
        assertThat(resultDto.isSubscriptionAllowed(), equalTo(userDto.isSubscriptionAllowed()));

        em.createNativeQuery("insert into users (name, email, subscription_allowed) values (?,?,?)")
                .setParameter(1, "user1Name")
                .setParameter(2, "user1Email@mail.ru")
                .setParameter(3, true)
                .executeUpdate();
        UserDto userDto1 = new UserDto(2, "user1Name", "user1Email@mail.ru", new HashSet<>(), false);
        UserDto resultDto1 = userService.subscriptionControl(2, false);
        assertThat(resultDto1.getId(), equalTo(userDto1.getId()));
        assertThat(resultDto1.getName(), equalTo(userDto1.getName()));
        assertThat(resultDto1.getEmail(), equalTo(userDto1.getEmail()));
        assertThat(resultDto.getAuthors().size(), equalTo(userDto.getAuthors().size()));
        assertThat(resultDto1.isSubscriptionAllowed(), equalTo(userDto1.isSubscriptionAllowed()));

    }

    @Test
    void subscribe() {
        em.createNativeQuery("insert into users (name, email, subscription_allowed) values (?,?,?)")
                .setParameter(1, "subscriberName")
                .setParameter(2, "subscriberEmail@mail.ru")
                .setParameter(3, true)
                .executeUpdate();
        em.createNativeQuery("insert into users (name, email, subscription_allowed) values (?,?,?)")
                .setParameter(1, "authorName")
                .setParameter(2, "authorEmail@mail.ru")
                .setParameter(3, true)
                .executeUpdate();
        User author = new User(2, "authorName", "authorEmail@mail.ru", new HashSet<>(), true);
        UserDto userDto = new UserDto(1, "subscriberName", "subscriberEmail@mail.ru", Set.of(author), true);
        UserDto resultDto = userService.subscribe(1, 2);
        assertThat(resultDto.getId(), equalTo(userDto.getId()));
        assertThat(resultDto.getName(), equalTo(userDto.getName()));
        assertThat(resultDto.getEmail(), equalTo(userDto.getEmail()));
        assertThat(resultDto.getAuthors().stream().findFirst().get().getId(), equalTo(userDto.getAuthors().stream().findFirst().get().getId()));
        assertThat(resultDto.isSubscriptionAllowed(), equalTo(userDto.isSubscriptionAllowed()));
    }

    @Test
    void unsubscribe() {
        em.createNativeQuery("insert into users (name, email, subscription_allowed) values (?,?,?)")
                .setParameter(1, "subscriberName")
                .setParameter(2, "subscriberEmail@mail.ru")
                .setParameter(3, true)
                .executeUpdate();
        em.createNativeQuery("insert into users (name, email, subscription_allowed) values (?,?,?)")
                .setParameter(1, "authorName")
                .setParameter(2, "authorEmail@mail.ru")
                .setParameter(3, true)
                .executeUpdate();
        em.createNativeQuery("insert into subscriptions (subscriber_id, author_id) values (?,?)")
                .setParameter(1, 1)
                .setParameter(2, 2)
                .executeUpdate();
        UserDto userDto = new UserDto(1, "subscriberName", "subscriberEmail@mail.ru", new HashSet<>(), true);
        UserDto resultDto = userService.unsubscribe(1, 2);
        assertThat(resultDto.getId(), equalTo(userDto.getId()));
        assertThat(resultDto.getName(), equalTo(userDto.getName()));
        assertThat(resultDto.getEmail(), equalTo(userDto.getEmail()));
        assertThat(resultDto.getAuthors().size(), equalTo(0));
        assertThat(resultDto.getAuthors().size(), equalTo(userDto.getAuthors().size()));
        assertThat(resultDto.isSubscriptionAllowed(), equalTo(userDto.isSubscriptionAllowed()));
    }
}
