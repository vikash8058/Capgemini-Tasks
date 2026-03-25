package com.todo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO - client sends only title and description
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDTO {

    // title is required
    @NotBlank(message = "Title is required")
    private String title;

    // description is required
    @NotBlank(message = "Description is required")
    private String description;

}