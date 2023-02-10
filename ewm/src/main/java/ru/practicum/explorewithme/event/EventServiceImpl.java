package ru.practicum.explorewithme.event;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.CategoryRepository;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.NewEventHit;
import ru.practicum.explorewithme.event.model.State;
import ru.practicum.explorewithme.exception.CategoryNotFoundException;
import ru.practicum.explorewithme.exception.EventNotFoundException;
import ru.practicum.explorewithme.exception.ParticipantLimitReachedException;
import ru.practicum.explorewithme.exception.UserNotFoundException;
import ru.practicum.explorewithme.pageable.OffsetLimitPageable;
import ru.practicum.explorewithme.request.RequestMapper;
import ru.practicum.explorewithme.request.RequestRepository;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.request.model.Request;
import ru.practicum.explorewithme.request.model.Status;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.user.model.User;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private static final LocalDateTime GET_EVENT_VIEWS_START_DATE = LocalDateTime.of(2020, 1, 1, 0, 0);

    //Public___________________________________________________
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventClient eventClient;

    public EventServiceImpl(EventRepository eventRepository, RequestRepository requestRepository, CategoryRepository categoryRepository, UserRepository userRepository, EventClient eventClient) {
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.eventClient = eventClient;
    }

    @Override
    public List<EventShortDto> getAllEvents(String text, List<Long> categories, boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean onlyAvailable, String sort, int from, int size) {
        List<Event> events = eventRepository.getEvents(text, categories, paid, rangeStart, rangeEnd, OffsetLimitPageable.of(from, size));
        if (onlyAvailable) {
            events = events.stream()
                    .filter(x -> getConfirmedRequestsCount(x.getId()) <= x.getParticipantLimit())
                    .collect(Collectors.toList());
        }
        if (!sort.equals("EVENT_DATE") && !sort.equals("VIEWS")) {
            throw new IllegalArgumentException("Неверный параметр сортировки!");
        }
        if (sort.equals("EVENT_DATE")) {
            return events.stream()
                    .sorted(Comparator.comparing(Event::getEventDate))
                    .map(x -> EventMapper.toEventShortDto(x, getConfirmedRequestsCount(x.getId()), getEventViews(x)))
                    .collect(Collectors.toList());
        } else {
            return events.stream()
                    .sorted((x, y) -> getEventViews(x) - getEventViews(y))
                    .map(x -> EventMapper.toEventShortDto(x, getConfirmedRequestsCount(x.getId()), getEventViews(x)))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public EventFullDto getEvent(long eventId, HttpServletRequest httpServletRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Событие с таким id не найдено!"));
        eventClient.hit(new NewEventHit("ewm",
                httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        return EventMapper.toEventFullDto((event),
                getConfirmedRequestsCount(eventId),
                getEventViews(event));
    }

    //Private___________________________________________________
    @Override
    public List<EventShortDto> getAllEventsOfUser(long userId, int from, int size) {
        List<Event> events = eventRepository.findAllByInitiatorId(userId, OffsetLimitPageable.of(from, size));
        return events.stream()
                .sorted((x, y) -> getEventViews(x) - getEventViews(y))
                .map(x -> EventMapper.toEventShortDto(x, getConfirmedRequestsCount(x.getId()), getEventViews(x)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventOfUser(long userId, UpdateEventRequest updateEventRequest) {
        if (updateEventRequest.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            Event event = eventRepository.findById(updateEventRequest.getEventId()).orElseThrow(() -> new EventNotFoundException("Событие с таким id не найдено!"));
            if (event.getState().equals(State.PENDING) || event.getState().equals(State.CANCELED)) {
                Category category = categoryRepository.findById(updateEventRequest.getCategory()).orElseThrow(() -> new CategoryNotFoundException("Категория с таким id не найдена!"));
                EventMapper.updateEvent(event, category, updateEventRequest);
                event.setState(State.PENDING);
                event = eventRepository.save(event);
                return EventMapper.toEventFullDto(event, getConfirmedRequestsCount(event.getId()), getEventViews(event));
            } else {
                throw new IllegalArgumentException("Only pending or canceled events can be changed");
            }
        } else {
            throw new IllegalArgumentException("Before the event can not be less than two hours");
        }
    }

    @Override
    @Transactional
    public EventFullDto createEventOfUser(long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(() -> new CategoryNotFoundException("Категория с таким id не найдена!"));
            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
            Event event = EventMapper.toNewEvent(newEventDto, category, LocalDateTime.now(), user, State.PENDING);
            event = eventRepository.save(event);
            return EventMapper.toEventFullDto(event, getConfirmedRequestsCount(event.getId()), getEventViews(event));
        } else {
            throw new IllegalArgumentException("Before the event can not be less than two hours");
        }
    }

    @Override
    public EventFullDto getEventOfUser(long userId, long eventId) {
        Event event = eventRepository.findEventByIdAndInitiatorId(eventId, userId);
        if (event != null) {
            return EventMapper.toEventFullDto(event, getConfirmedRequestsCount(event.getId()), getEventViews(event));
        } else {
            throw new IllegalArgumentException(String.format("Event with id = %d created by user with id =%d not found", eventId, userId));
        }
    }

    @Override
    @Transactional
    public EventFullDto cancelEventOfUser(long userId, long eventId) {
        Event event = eventRepository.findEventByIdAndInitiatorId(eventId, userId);
        if (event != null && event.getState().equals(State.PENDING)) {
            event.setState(State.CANCELED);
            event = eventRepository.save(event);
            return EventMapper.toEventFullDto(event, getConfirmedRequestsCount(event.getId()), getEventViews(event));
        } else {
            throw new IllegalArgumentException(String.format("Event with id = %d created by user with id =%d " +
                    "and with PENDING state not found", eventId, userId));
        }
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForEventOfUser(long userId, long eventId) {
        List<Request> requests = requestRepository.findAllByEventIdAndEventInitiatorId(eventId, userId);
        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto confirmRequestToEventOfUser(long userId, long eventId, long reqId) {
        Event event = eventRepository.findEventByIdAndInitiatorId(eventId, userId);
        Request request = requestRepository.findRequestByIdAndEventIdAndEventInitiatorId(reqId, eventId, userId);
        if (request != null) {
            if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
                request.setStatus(Status.CONFIRMED);
                request = requestRepository.save(request);
                return RequestMapper.toParticipationRequestDto(request);
            }
            if (event.getParticipantLimit() != getConfirmedRequestsCount(eventId)) {
                request.setStatus(Status.CONFIRMED);
                request = requestRepository.save(request);
                if (event.getParticipantLimit() == getConfirmedRequestsCount(eventId)) {
                    List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(eventId, Status.PENDING);
                    for (Request pendingRequest : pendingRequests) {
                        pendingRequest.setStatus(Status.REJECTED);
                        requestRepository.save(pendingRequest);
                    }
                }
                return RequestMapper.toParticipationRequestDto(request);
            } else {
                throw new ParticipantLimitReachedException();
            }
        } else {
            throw new IllegalArgumentException(String.format("Request with id = %d to event with id = %d created by user with id =%d not found", reqId, eventId, userId));
        }
    }

    @Override
    @Transactional
    public ParticipationRequestDto rejectRequestToEventOfUser(long userId, long eventId, long reqId) {
        Request request = requestRepository.findRequestByIdAndEventIdAndEventInitiatorId(reqId, eventId, userId);
        if (request != null) {
            request.setStatus(Status.REJECTED);
            requestRepository.save(request);
            return RequestMapper.toParticipationRequestDto(request);
        } else {
            throw new IllegalArgumentException(String.format("Request with id = %d to event with id = %d created by user with id =%d not found", reqId, eventId, userId));
        }
    }

    //Admin___________________________________________________
    @Override
    public List<EventFullDto> getFullEvents(List<Long> users, List<State> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        List<Event> events = eventRepository.getFullEvents(users, states, categories, rangeStart, rangeEnd, OffsetLimitPageable.of(from, size));
        return events.stream()
                .map(x -> EventMapper.toEventFullDto(x, getConfirmedRequestsCount(x.getId()), getEventViews(x)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto editEvent(long eventId, AdminUpdateEventRequest adminUpdateEventRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Событие с таким id не найдено"));
        Category category = categoryRepository.findById(adminUpdateEventRequest.getCategory()).orElseThrow(() -> new CategoryNotFoundException("Категория с таким id не найдена"));
        EventMapper.updateAdminEvent(event, adminUpdateEventRequest, category);
        event = eventRepository.save(event);
        return EventMapper.toEventFullDto(event, getConfirmedRequestsCount(event.getId()), getEventViews(event));
    }

    @Override
    @Transactional
    public EventFullDto publishEvent(long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Событие с таким id не найдено"));
        if (event.getState().equals(State.PENDING) && event.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            event.setState(State.PUBLISHED);
            event = eventRepository.save(event);
            return EventMapper.toEventFullDto(event, getConfirmedRequestsCount(event.getId()), getEventViews(event));
        } else {
            throw new IllegalArgumentException("Событие не ожидает публикации, либо до него осталось менее часа");
        }

    }

    @Override
    @Transactional
    public EventFullDto rejectEvent(long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Событие с таким id не найдено"));
        if (!event.getState().equals(State.PUBLISHED)) {
            event.setState(State.CANCELED);
            event = eventRepository.save(event);
            return EventMapper.toEventFullDto(event, getConfirmedRequestsCount(event.getId()), getEventViews(event));
        } else {
            throw new IllegalArgumentException("Событие не опубликовано");
        }
    }

    @Override
    public List<EventShortDto> getSubscriptionEvents(long userId, int from, int size) {
        User subscriber = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь с таким id не найден!"));
        Set<Long> authorIds = subscriber.getAuthors().stream().map(User::getId).collect(Collectors.toSet());
        List<Event> subscriptionEvents = eventRepository.findAllByInitiatorIdInAndEventDateIsAfterAndState(authorIds,
                LocalDateTime.now().plusHours(2),
                State.PUBLISHED,
                OffsetLimitPageable.of(from, size));
        return subscriptionEvents.stream()
                .map(x -> EventMapper.toEventShortDto(x, getConfirmedRequestsCount(x.getId()), getEventViews(x)))
                .collect(Collectors.toList());
    }

    public int getConfirmedRequestsCount(long eventId) {
        return requestRepository.findByEventIdAndStatus(eventId, Status.CONFIRMED).size();
    }

    public int getEventViews(Event event) {
        return eventClient.getStats(GET_EVENT_VIEWS_START_DATE, event.getEventDate(), List.of("/events/" + event.getId()), false).getHits();
    }
}
