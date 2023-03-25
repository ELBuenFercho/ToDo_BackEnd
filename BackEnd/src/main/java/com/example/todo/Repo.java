package com.example.todo;

import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.function.BiFunction;

@Repository
public class Repo {
    private Map<String, Model> listOfTasks = new HashMap<>();

    // Creates a new task if a task with the same description does not exist
    public Model createTask(String description, String priority, LocalDate dueDate) {
        // Check if a task with the same description already exists
        for (Model task : listOfTasks.values()) {
            if (task.getText().equalsIgnoreCase(description)) {
                // A task with the same description exists, return null
                return null;
            }
        }
        // Generate a unique UUID
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (listOfTasks.containsKey(id));

        // If no task with the same description is found, create and add the new task
        Model task = new Model();
        task.setId(id);
        task.setText(description);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setCreationDate(LocalDateTime.now());
        listOfTasks.put(id, task);
        return task;
    }

    // Deletes a task by ID, returns true if the task was deleted, false if the task was not found
    public boolean deleteTask(String id) {
        Model task = listOfTasks.get(id);
        if (task == null) {
            return false;
        }
        listOfTasks.remove(id);
        return true;
    }

    // Updates a task with the given ID, returns the updated task or null if the task was not found
    public Model updateTask(String id, Model updatedTask) {
        System.out.println("id: "+id);
        System.out.println("id in updated task: "+updatedTask.getId());
        Model currentTask = listOfTasks.get(id);
        if (currentTask == null) {
            return null;
        }
        updatedTask.setId(id);
        listOfTasks.put(id, updatedTask);
        return updatedTask;
    }

    // Mark a task as done, returns true if the task was found and updated, false otherwise
    public boolean markTaskAsDone(String id) {
        Model task = listOfTasks.get(id);
        if (task == null || task.isDone()) {
            return false;
        }
        task.setDone(true);
        task.setDoneDate(LocalDateTime.now());
        return true;
    }

    // Mark a task as undone, returns true if the task was found and updated, false otherwise
    public boolean markTaskAsUndone(String id) {
        Model task = listOfTasks.get(id);
        if (task == null || !task.isDone()) {
            return false;
        }
        task.setDone(false);
        task.setDoneDate(null);
        return true;
    }

    // Retrieves a task by ID, returns the task or null if the task was not found
    public Model getTask(String id) {
        return listOfTasks.get(id);
    }
    // Retrieves all tasks
    public List<Model> getAllTasks() {
        return new ArrayList<>(listOfTasks.values());
    }
    public List<Model> getFilteredSortedAndPaginatedTasks(String priority, String state, String searchText, String sortBy, boolean ascending, int pageNumber, int pageSize) {
        Stream<Model> taskStream = listOfTasks.values().stream();

        // Filter by priority
        if (!"ALL".equalsIgnoreCase(priority)) {
            taskStream = taskStream.filter(task -> priority.equalsIgnoreCase(task.getPriority()));
        }

        // Filter by state
        if ("DONE".equalsIgnoreCase(state)) {
            taskStream = taskStream.filter(Model::isDone);
        } else if ("UNDONE".equalsIgnoreCase(state)) {
            taskStream = taskStream.filter(task -> !task.isDone());
        }

        // Filter by name
        if (searchText != null && !searchText.trim().isEmpty()) {
            taskStream = taskStream.filter(task -> task.getText().toLowerCase().contains(searchText.trim().toLowerCase()));
        }

        // Sort tasks
        Comparator<Model> comparator;
        if ("PRIORITY".equalsIgnoreCase(sortBy)) {
            comparator = (task1, task2) -> {
                List<String> priorities = Arrays.asList("high", "medium", "low");
                int priorityIndex1 = priorities.indexOf(task1.getPriority().toLowerCase());
                int priorityIndex2 = priorities.indexOf(task2.getPriority().toLowerCase());
                return Integer.compare(priorityIndex1, priorityIndex2);
            };
        } else { // Default sorting is by due date
            comparator = (task1, task2) -> {
                LocalDate date1 = task1.getDueDate();
                LocalDate date2 = task2.getDueDate();

                if (date1 == null && date2 == null) {
                    return 0;
                } else if (date1 == null) {
                    return 1;
                } else if (date2 == null) {
                    return -1;
                } else {
                    return date1.compareTo(date2);
                }
            };
        }
        if (!ascending) {
            comparator = comparator.reversed();
        }
        taskStream = taskStream.sorted(comparator);

        // Paginate tasks
        List<Model> paginatedTasks = taskStream.skip((long) pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        return paginatedTasks;
    }

    public List<String> getAverageCompletionTimes() {
        List<String> averages = new ArrayList<>();

        // Check if there are no tasks in the listOfTasks hashmap
        if (listOfTasks.isEmpty()) {
            return Arrays.asList("No Data/Tasks", "No Data/Tasks", "No Data/Tasks", "No Data/Tasks");
        }

        // Function to calculate average duration
        BiFunction<List<Model>, String, String> calculateAverage = (tasks, priority) -> {
            Stream<Model> taskStream = tasks.stream();

            if (!"ALL".equalsIgnoreCase(priority)) {
                taskStream = taskStream.filter(task -> priority.equalsIgnoreCase(task.getPriority()));
            }

            List<Duration> durations = taskStream
                    .filter(task -> task.isDone() && task.getDoneDate() != null && task.getCreationDate() != null)
                    .map(task -> Duration.between(task.getCreationDate(), task.getDoneDate()))
                    .collect(Collectors.toList());

            if (durations.isEmpty()) {
                return "No Data/Tasks";
            }

            Duration totalDuration = durations.stream().reduce(Duration.ZERO, Duration::plus);
            Duration averageDuration = totalDuration.dividedBy(durations.size());

            return averageDuration.toString();
        };

        // Calculate averages for all tasks, high priority tasks, medium priority tasks, and low priority tasks
        List<String> priorities = Arrays.asList("ALL", "high", "medium", "low");
        List<Model> tasks = getAllTasks();

        for (String priority : priorities) {
            averages.add(calculateAverage.apply(tasks, priority));
        }

        return averages;
    }
}
