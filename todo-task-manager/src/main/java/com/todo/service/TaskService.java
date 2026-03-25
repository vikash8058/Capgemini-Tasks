package com.todo.service;

import com.todo.dto.TaskRequestDTO;
import com.todo.dto.TaskResponseDTO;
import com.todo.exception.TaskNotFoundException;
import com.todo.model.Task;
import com.todo.model.TaskStatus;
import com.todo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // convert Task entity to TaskResponseDTO
    private TaskResponseDTO mapToDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        return dto;
    }

    //create new task
    public TaskResponseDTO createTask(TaskRequestDTO dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(TaskStatus.PENDING); // always PENDING by default
        Task saved = taskRepository.save(task);
        return mapToDTO(saved);
    }

    //get all tasks
    public List<TaskResponseDTO> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(task -> mapToDTO(task))
                .collect(Collectors.toList());
    }

    // get task by id
    public TaskResponseDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
        return mapToDTO(task);
    }

    //update task
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        Task updated = taskRepository.save(task);
        return mapToDTO(updated);
    }

    //mark task as DONE
    public TaskResponseDTO completeTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
        task.setStatus(TaskStatus.DONE); // set status to DONE
        Task updated = taskRepository.save(task);
        return mapToDTO(updated);
    }

    //delete task
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
        taskRepository.delete(task);
    }

}