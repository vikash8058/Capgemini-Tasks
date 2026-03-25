# Todo Task Manager — Spring Boot REST API
### By Vikash Prajapati

---

## What is this project?
A simple REST API to manage tasks. You can create, read, update, and delete tasks.
Each task has a title, description, and a status (PENDING or DONE).

---

## Tech Stack
| Technology | Purpose |
|---|---|
| Spring Boot 3.2.0 | Main framework |
| Spring Data JPA | Database operations |
| Spring Validation | Input validation |
| MySQL | Database |
| Lombok | Removes boilerplate code |

---

## Project Structure
```
com.todo/
├── controller/
│   └── TaskController.java        ← REST endpoints (HTTP layer)
├── dto/
│   ├── TaskRequestDTO.java        ← Input from client
│   └── TaskResponseDTO.java       ← Output to client
├── exception/
│   ├── ErrorResponse.java         ← Shape of error JSON
│   ├── GlobalExceptionHandler.java← Catches all exceptions
│   └── TaskNotFoundException.java ← Custom exception
├── model/
│   ├── Task.java                  ← DB Entity (maps to tasks table)
│   └── TaskStatus.java            ← Enum: PENDING / DONE
├── repository/
│   └── TaskRepository.java        ← JPA - talks to DB
├── service/
│   └── TaskService.java           ← Business logic
└── TodoTaskManagerApplication.java← Main entry point
```

---

## Layered Architecture
```
Client
  ↓ HTTP Request
Controller       ← receives request, calls service
  ↓
Service          ← business logic, calls repository
  ↓
Repository       ← talks to MySQL database
  ↓
Database
  ↑
(response flows back up the same way)
```

---

## Setup & Run

### Step 1 — Create MySQL Database
```sql
CREATE DATABASE todo_db;
```

### Step 2 — Update application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/todo_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.application.name=todo-task-manager
```
> ddl-auto=update means Spring Boot auto creates the tasks table. No need to create manually.

### Step 3 — Run
Run `TodoTaskManagerApplication.java` in your IDE.
You should see: `Tomcat started on port(s): 8080`

---

## All Files Explained

### 1. `TaskStatus.java` — Enum
```java
public enum TaskStatus {
    PENDING,   // task not done yet
    DONE       // task completed
}
```
- Only 2 fixed values allowed
- Saves "PENDING" or "DONE" as string in DB (not 0 or 1)

---

### 2. `Task.java` — Entity (DB Table)
```java
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
}
```
| Annotation | Meaning |
|---|---|
| `@Entity` | This class = DB table |
| `@Table(name="tasks")` | Table name in MySQL = tasks |
| `@Id` | Primary key |
| `@GeneratedValue` | Auto increment (1, 2, 3...) |
| `@Enumerated(EnumType.STRING)` | Saves "PENDING"/"DONE" not 0/1 |
| `@Data` | Lombok adds getters/setters |

---

### 3. `TaskRepository.java` — Repository
```java
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // free methods: save(), findById(), findAll(), deleteById()
}
```
- `JpaRepository<Task, Long>` → Task = which entity, Long = type of id
- No need to write SQL queries for basic operations

---

### 4. `TaskRequestDTO.java` — Input DTO
```java
public class TaskRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;
    // NO status field — always set to PENDING in service
}
```
- Client sends only title and description
- Status is NOT accepted from client — always defaults to PENDING
- `@NotBlank` = field cannot be null, empty, or just spaces

---

### 5. `TaskResponseDTO.java` — Output DTO
```java
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
}
```
- What we send BACK to client
- Has all 4 fields including id and status

| TaskRequestDTO (input) | TaskResponseDTO (output) |
|---|---|
| Client → Server | Server → Client |
| title, description only | id, title, description, status |
| Has @NotBlank validation | No validation needed |

---

### 6. `TaskService.java` — Service (Business Logic)
```java
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // convert Task entity → TaskResponseDTO
    private TaskResponseDTO mapToDTO(Task task) { ... }

    // create new task — status always PENDING
    public TaskResponseDTO createTask(TaskRequestDTO dto) { ... }

    // get all tasks
    public List<TaskResponseDTO> getAllTasks() { ... }

    // get task by id — throws TaskNotFoundException if not found
    public TaskResponseDTO getTaskById(Long id) { ... }

    // update title and description
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO dto) { ... }

    // set status to DONE
    public TaskResponseDTO completeTask(Long id) { ... }

    // delete task
    public void deleteTask(Long id) { ... }
}
```
- `@Service` marks it as service layer bean
- `@Autowired` injects TaskRepository automatically
- `mapToDTO()` manually converts Task entity to TaskResponseDTO
- Every method that needs a task by id will throw `TaskNotFoundException` if not found

---

### 7. `TaskController.java` — Controller (REST Endpoints)
```java
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping            // POST /tasks
    @GetMapping             // GET /tasks
    @GetMapping("/{id}")    // GET /tasks/{id}
    @PutMapping("/{id}")    // PUT /tasks/{id}
    @PatchMapping("/{id}/complete")  // PATCH /tasks/{id}/complete
    @DeleteMapping("/{id}") // DELETE /tasks/{id}
}
```
| Annotation | Meaning |
|---|---|
| `@RestController` | Handles HTTP requests, returns JSON |
| `@RequestMapping("/tasks")` | Base URL for all endpoints |
| `@Valid` | Triggers validation on RequestDTO |
| `@RequestBody` | Reads JSON from request body |
| `@PathVariable` | Reads {id} from URL |

---

### 8. `TaskNotFoundException.java` — Custom Exception
```java
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}
```
- Extends `RuntimeException` — no need to declare with throws
- Thrown in service when task not found by id
- Caught by `GlobalExceptionHandler`

---

### 9. `ErrorResponse.java` — Error Shape
```java
public class ErrorResponse {
    private String error;      // ex: "NOT_FOUND"
    private String message;    // ex: "Task with id 5 not found"
    private LocalDateTime timestamp;  // when error happened
}
```
Every error returns this JSON shape:
```json
{
    "error": "NOT_FOUND",
    "message": "Task with id 5 not found",
    "timestamp": "2024-03-25T10:30:00"
}
```

---

### 10. `GlobalExceptionHandler.java` — Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)       // 404
    @ExceptionHandler(MethodArgumentNotValidException.class) // 400
    @ExceptionHandler(Exception.class)                   // 500
}
```
| Handler | When Triggered | HTTP Status |
|---|---|---|
| `TaskNotFoundException` | Task not found by id | 404 NOT FOUND |
| `MethodArgumentNotValidException` | @NotBlank fails | 400 BAD REQUEST |
| `Exception` | Any unexpected error | 500 INTERNAL SERVER ERROR |

- `@RestControllerAdvice` = catches exceptions from ALL controllers globally

---

## REST API Endpoints

| Method | URL | What it does | Request Body |
|---|---|---|---|
| POST | /tasks | Create new task | title, description |
| GET | /tasks | Get all tasks | none |
| GET | /tasks/{id} | Get task by id | none |
| PUT | /tasks/{id} | Update task | title, description |
| PATCH | /tasks/{id}/complete | Mark as DONE | none |
| DELETE | /tasks/{id} | Delete task | none |

---

## HTTP Status Codes Used
| Code | Meaning | Used When |
|---|---|---|
| 200 OK | Success | GET, PUT, PATCH |
| 201 CREATED | Resource created | POST /tasks |
| 204 NO CONTENT | Deleted | DELETE |
| 400 BAD REQUEST | Validation failed | Empty title/description |
| 404 NOT FOUND | Task not found | Wrong id |
| 500 INTERNAL SERVER ERROR | Unexpected error | Server crash |

---

## Postman Test Cases

### 1. Create Task — POST /tasks
```
URL    : http://localhost:8080/tasks
Method : POST
Header : Content-Type: application/json
Body   :
{
    "title": "Buy Groceries",
    "description": "Buy milk, eggs and bread"
}
```
Response (201):
```json
{
    "id": 1,
    "title": "Buy Groceries",
    "description": "Buy milk, eggs and bread",
    "status": "PENDING"
}
```

---

### 2. Get All Tasks — GET /tasks
```
URL    : http://localhost:8080/tasks
Method : GET
```
Response (200):
```json
[
    {
        "id": 1,
        "title": "Buy Groceries",
        "description": "Buy milk, eggs and bread",
        "status": "PENDING"
    }
]
```

---

### 3. Get Task By Id — GET /tasks/{id}
```
URL    : http://localhost:8080/tasks/1
Method : GET
```
Response (200):
```json
{
    "id": 1,
    "title": "Buy Groceries",
    "description": "Buy milk, eggs and bread",
    "status": "PENDING"
}
```

---

### 4. Update Task — PUT /tasks/{id}
```
URL    : http://localhost:8080/tasks/1
Method : PUT
Header : Content-Type: application/json
Body   :
{
    "title": "Buy Groceries Updated",
    "description": "Buy milk, eggs, bread and butter"
}
```
Response (200):
```json
{
    "id": 1,
    "title": "Buy Groceries Updated",
    "description": "Buy milk, eggs, bread and butter",
    "status": "PENDING"
}
```

---

### 5. Complete Task — PATCH /tasks/{id}/complete
```
URL    : http://localhost:8080/tasks/1/complete
Method : PATCH
```
Response (200):
```json
{
    "id": 1,
    "title": "Buy Groceries Updated",
    "description": "Buy milk, eggs, bread and butter",
    "status": "DONE"
}
```

---

### 6. Delete Task — DELETE /tasks/{id}
```
URL    : http://localhost:8080/tasks/1
Method : DELETE
```
Response: 204 No Content

---

### 7. Task Not Found — GET /tasks/999
```
URL    : http://localhost:8080/tasks/999
Method : GET
```
Response (404):
```json
{
    "error": "NOT_FOUND",
    "message": "Task with id 999 not found",
    "timestamp": "2024-03-25T10:30:00"
}
```

---

### 8. Validation Failed — POST /tasks with empty body
```
URL    : http://localhost:8080/tasks
Method : POST
Body   :
{
    "title": "",
    "description": ""
}
```
Response (400):
```json
{
    "title": "Title is required",
    "description": "Description is required"
}
```

---

## Key Concepts Summary

| Concept | What it means in simple words |
|---|---|
| `@Entity` | This Java class = one table in DB |
| `@Repository` | This class talks to the DB |
| `@Service` | This class has all business logic |
| `@RestController` | This class handles HTTP requests |
| `@Autowired` | Spring injects the object automatically |
| `DTO` | Data Transfer Object — what comes in and goes out |
| `JpaRepository` | Gives free DB methods like save, findAll, delete |
| `@NotBlank` | Field cannot be empty or null |
| `@Valid` | Triggers validation on the DTO |
| `@ExceptionHandler` | Method that handles a specific exception |
| `@RestControllerAdvice` | Catches exceptions from all controllers |
| `@Enumerated(EnumType.STRING)` | Saves enum as "PENDING"/"DONE" not 0/1 |
| `RuntimeException` | Exception that Spring catches automatically |
| `ResponseEntity` | HTTP response wrapper — has body + status code |
| `@PathVariable` | Reads value from URL like /tasks/{id} |
| `@RequestBody` | Reads JSON from request body |

---
*Project by Vikash Prajapati*
