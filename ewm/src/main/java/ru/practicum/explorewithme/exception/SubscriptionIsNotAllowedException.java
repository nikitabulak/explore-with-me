package ru.practicum.explorewithme.exception;

public class SubscriptionIsNotAllowedException extends RuntimeException {
    public SubscriptionIsNotAllowedException(String message) {
        super(message);
    }
}
