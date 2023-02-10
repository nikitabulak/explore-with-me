package ru.practicum.explorewithme.subscriptions;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.event.EventServiceImpl;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.model.State;
import ru.practicum.explorewithme.user.dto.UserShortDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=explorewithme-test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext
public class EventServiceImplIntegrationTest {
    private final EntityManager em;

    private final EventServiceImpl eventService;

    @Test
    void getSubscriptionEvents() {
        em.createNativeQuery("insert into users (name, email, subscription_allowed) values (?,?,?)")
                .setParameter(1, "subscriberName")
                .setParameter(2, "subscriberEmail@mail.ru")
                .setParameter(3, true)
                .executeUpdate();
        em.createNativeQuery("insert into users (name, email, subscription_allowed) values (?,?,?)")
                .setParameter(1, "author1Name")
                .setParameter(2, "author1Email@mail.ru")
                .setParameter(3, true)
                .executeUpdate();
        em.createNativeQuery("insert into users (name, email, subscription_allowed) values (?,?,?)")
                .setParameter(1, "author2Name")
                .setParameter(2, "author2Email@mail.ru")
                .setParameter(3, true)
                .executeUpdate();
        em.createNativeQuery("insert into subscriptions (subscriber_id, author_id) values (?,?)")
                .setParameter(1, 1)
                .setParameter(2, 2)
                .executeUpdate();
        em.createNativeQuery("insert into subscriptions (subscriber_id, author_id) values (?,?)")
                .setParameter(1, 1)
                .setParameter(2, 3)
                .executeUpdate();
        em.createNativeQuery("insert into categories (name) values (?)")
                .setParameter(1, "testCategory")
                .executeUpdate();
        em.createNativeQuery("insert into events (annotation, category_id, created_on, description, event_date, initiator_id, lat, lon, paid, participant_limit, published_on, request_moderation, state, title) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
                .setParameter(1, "test1Annotation")
                .setParameter(2, 1)
                .setParameter(3, LocalDateTime.of(2020, 01, 01, 15, 00))
                .setParameter(4, "test1Description")
                .setParameter(5, LocalDateTime.of(2023, 01, 01, 15, 00).plusHours(7))
                .setParameter(6, 2)
                .setParameter(7, 0)
                .setParameter(8, 0)
                .setParameter(9, true)
                .setParameter(10, 0)
                .setParameter(11, LocalDateTime.of(2020, 01, 01, 15, 00).plusHours(2))
                .setParameter(12, true)
                .setParameter(13, State.PUBLISHED.toString())
                .setParameter(14, "test1Title")
                .executeUpdate();
        em.createNativeQuery("insert into events (annotation, category_id, created_on, description, event_date, initiator_id, lat, lon, paid, participant_limit, published_on, request_moderation, state, title) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
                .setParameter(1, "test2Annotation")
                .setParameter(2, 1)
                .setParameter(3, LocalDateTime.of(2020, 01, 01, 15, 00))
                .setParameter(4, "test2Description")
                .setParameter(5, LocalDateTime.of(2023, 01, 01, 15, 00).plusHours(7))
                .setParameter(6, 3)
                .setParameter(7, 0)
                .setParameter(8, 0)
                .setParameter(9, true)
                .setParameter(10, 0)
                .setParameter(11, LocalDateTime.of(2020, 01, 01, 15, 00).plusHours(2))
                .setParameter(12, true)
                .setParameter(13, State.PUBLISHED.toString())
                .setParameter(14, "test2Title")
                .executeUpdate();

        EventShortDto eventShortDto1 = new EventShortDto(1,
                "test1Annotation",
                new CategoryDto(1, "testCategory"),
                0,
                LocalDateTime.of(2023, 01, 01, 15, 00).plusHours(7),
                new UserShortDto(2, "author1Name"),
                true,
                "test1Title",
                0);
        EventShortDto eventShortDto2 = new EventShortDto(2,
                "test2Annotation",
                new CategoryDto(1, "testCategory"),
                0,
                LocalDateTime.of(2023, 01, 01, 15, 00).plusHours(7),
                new UserShortDto(3, "author2Name"),
                true,
                "test2Title",
                0);
        assertThat(eventService.getSubscriptionEvents(1, 0, 10), equalTo(List.of(eventShortDto1, eventShortDto2)));
    }
}
