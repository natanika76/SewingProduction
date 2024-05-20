package ru.vilas.sewing.service.admin;

import lombok.Data;
import ru.vilas.sewing.model.Task;
import ru.vilas.sewing.model.User;

import java.util.Objects;

@Data
public class TaskAndUser {
    private final Task task;
    private final User seamstress;

    TaskAndUser(Task task, User seamstress) {
        this.task = task;
        this.seamstress = seamstress;
    }

    // реализация методов equals и hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskAndUser that = (TaskAndUser) o;
        return task.equals(that.task) && seamstress.equals(that.seamstress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, seamstress);
    }
}