package org.webbitserver.metrics.registries;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TaskRegistry {

    private final ConcurrentMap<String, Task> tasks;

    public TaskRegistry() {
        this.tasks = new ConcurrentHashMap<>();
    }

    public void register(String name, Task task){
        tasks.putIfAbsent(name, task);
    }

    public void unregister(String name){
        tasks.remove(name);
    }

    public SortedSet<String> getNames() {
        return Collections.unmodifiableSortedSet(new TreeSet<String>(tasks.keySet()));
    }

    public void runTask(String name) throws Exception {
        final Task task = tasks.get(name);
        if(task == null) {
            throw new NoSuchElementException("No task named " + name + " exists");
        }
        task.execute();
    }
}
