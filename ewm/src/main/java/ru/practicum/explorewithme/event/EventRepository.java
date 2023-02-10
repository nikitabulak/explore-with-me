package ru.practicum.explorewithme.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("select i from Event i " +
            "where (upper(i.annotation) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%'))) " +
            " and i.category.id in ?2 " +
            " and i.paid = ?3 " +
            " and i.eventDate >= ?4 " +
            " and i.eventDate < ?5 ")
    List<Event> getEvents(String text,
                          List<Long> categories,
                          boolean paid,
                          LocalDateTime rangeStart,
                          LocalDateTime rangeEnd,
                          Pageable pageable);

    List<Event> findAllByInitiatorId(long userId, Pageable pageable);

    List<Event> findAllByInitiatorIdInAndEventDateIsAfterAndState(Set<Long> authorIds, LocalDateTime eventDate, State state, Pageable pageable);

    Event findEventByIdAndInitiatorId(long eventId, long userId);


    @Query("select i from Event i " +
            "where i.initiator.id in ?1 " +
            " and i.state in ?2 " +
            " and i.category.id in ?3 " +
            " and i.eventDate >= ?4 " +
            " and i.eventDate < ?5 ")
    List<Event> getFullEvents(List<Long> users,
                              List<State> states,
                              List<Long> categories,
                              LocalDateTime rangeStart,
                              LocalDateTime rangeEnd,
                              Pageable pageable);

    List<Event> findEventsByCategoryId(long catId);

    Set<Event> findEventsByIdIn(Set<Long> ids);
}
