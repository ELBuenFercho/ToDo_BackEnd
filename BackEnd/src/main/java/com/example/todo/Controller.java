package com.example.todo;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class Controller {
    private final Repo repo;

    @Autowired
    public Controller(Repo repo) {
        this.repo = repo;
    }
    //This creates a task
    @PostMapping("/todos/{text}/{priority}")
    public ResponseEntity<Model> createTask(@PathVariable String text,
                                            @PathVariable String priority,
                                            @RequestParam(required = false, defaultValue = "") String dueDate) {
        LocalDate parsedDueDate = null;
        if (!dueDate.isEmpty()) {
            parsedDueDate = LocalDate.parse(dueDate);
        }
        Model createdTask = repo.createTask(text, priority, parsedDueDate);
        if (createdTask != null) {
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT);
        }
    }
    //This reads/gets a particular task
    @GetMapping("/todos/{id}")
    public ResponseEntity<Model> getTaskById(@PathVariable String id) {
        Model task = repo.getTask(id);
        if (task != null) {
            return new ResponseEntity<>(task, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
    //This updates a particular task
    @PutMapping("/todos/update/{id}")
    public ResponseEntity<Model> updateTask(@PathVariable String id,@RequestBody Model updatedTask){
        System.out.println("id: "+id);
        System.out.println("id in updated task: "+updatedTask.getId());
        Model updated = repo.updateTask(id, updatedTask);
        if (updated != null) {
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
    //Marks as done
    @PutMapping("/todos/{id}/done")
    public ResponseEntity<Void> markTaskAsDone(@PathVariable String id) {
        boolean success = repo.markTaskAsDone(id);
        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    //Marks as undone
    @PutMapping("/todos/{id}/undone")
    public ResponseEntity<Void> markTaskAsUndone(@PathVariable String id) {
        boolean success = repo.markTaskAsUndone(id);
        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @DeleteMapping("/todos/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        boolean success = repo.deleteTask(id);
        if (success) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/todos")
    public ResponseEntity<List<Model>> getFilteredSortedAndPaginatedTasks(
            @RequestParam(value = "priority", defaultValue = "ALL") String priority,
            @RequestParam(value = "state", defaultValue = "ALL") String state,
            @RequestParam(value = "searchText", defaultValue = "") String searchText,
            @RequestParam(value = "sortBy", defaultValue = "DUE_DATE") String sortBy,
            @RequestParam(value = "ascending", defaultValue = "true") boolean ascending,
            @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        List<Model> tasks = repo.getFilteredSortedAndPaginatedTasks(priority, state, searchText, sortBy, ascending, pageNumber, pageSize);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }
    @GetMapping("/todos/time")
    public ResponseEntity<List<String>> getAverageTime(){
        List<String> avgTime=repo.getAverageCompletionTimes();
        return new ResponseEntity<>(avgTime,HttpStatus.OK);
    }
}