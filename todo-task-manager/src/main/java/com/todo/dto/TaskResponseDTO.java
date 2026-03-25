package com.todo.dto;

import com.todo.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO - what we send back to client (output)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

    // send all fields back to client
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;

}