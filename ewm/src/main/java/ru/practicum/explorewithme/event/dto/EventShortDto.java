package ru.practicum.explorewithme.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.user.dto.UserShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventShortDto {
    private long id;
    @NotNull
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    @NotNull
    private CategoryDto categoryDto;
    private int confirmedRequests;
    @NotNull
    private LocalDateTime eventDate;
    @NotNull
    private UserShortDto initiator;
    private boolean paid;
    @NotNull
    @NotBlank
    @Size(min = 3, max = 120)
    private String title;
    private int views;
}
