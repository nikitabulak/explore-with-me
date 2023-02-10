package ru.practicum.explorewithme.event;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.dto.UpdateEventRequest;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class EventPrivateController {
    private final EventService eventService;

    @GetMapping("/{userId}/events")
    public List<EventShortDto> getAllEventsOfUser(@PathVariable long userId,
                                                  @RequestParam(required = false, defaultValue = "0") int from,
                                                  @RequestParam(required = false, defaultValue = "10") int size) {
        return eventService.getAllEventsOfUser(userId, from, size);
    }

    @PatchMapping("/{userId}/events")
    public EventFullDto updateEventOfUser(@PathVariable long userId, @RequestBody UpdateEventRequest updateEventRequest) {
        return eventService.updateEventOfUser(userId, updateEventRequest);
    }

    @PostMapping("/{userId}/events")
    public EventFullDto createEventOfUser(@PathVariable long userId, @RequestBody NewEventDto newEventDto) {
        return eventService.createEventOfUser(userId, newEventDto);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEventOfUser(@PathVariable long userId, @PathVariable long eventId) {
        return eventService.getEventOfUser(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto cancelEventOfUser(@PathVariable long userId, @PathVariable long eventId) {
        return eventService.cancelEventOfUser(userId, eventId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsToEventOfUser(@PathVariable long userId, @PathVariable long eventId) {
        return eventService.getRequestsForEventOfUser(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/confirm")
    public ParticipationRequestDto confirmRequestToEventOfUser(@PathVariable long userId,
                                                               @PathVariable long eventId,
                                                               @PathVariable long reqId) {
        return eventService.confirmRequestToEventOfUser(userId, eventId, reqId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/reject")
    public ParticipationRequestDto rejectRequestToEventOfUser(@PathVariable long userId,
                                                              @PathVariable long eventId,
                                                              @PathVariable long reqId) {
        return eventService.rejectRequestToEventOfUser(userId, eventId, reqId);
    }

    @GetMapping("/{userId}/get_subscription_events")
    public List<EventShortDto> getSubscriptionEvents(@PathVariable long userId,
                                                     @RequestParam(required = false, defaultValue = "0") int from,
                                                     @RequestParam(required = false, defaultValue = "10") int size) {
        return eventService.getSubscriptionEvents(userId, from, size);
    }
}
