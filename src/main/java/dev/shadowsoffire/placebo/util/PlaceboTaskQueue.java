package dev.shadowsoffire.placebo.util;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;

public class PlaceboTaskQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger("Placebo/TaskQueue");
    private static final Queue<Pair<Identifier, Task>> TASKS = new ArrayDeque<>();
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Pair<Identifier, Task>> it = TASKS.iterator();
            while (it.hasNext()) {
                Pair<Identifier, Task> current = it.next();
                try {
                    if (current.getRight().execute().isCompleted()) {
                        it.remove();
                    }
                } catch (Exception ex) {
                    LOGGER.error("An exception occurred while running task with ID {}. Terminating task.", current.getLeft(), ex);
                    it.remove();
                }
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> TASKS.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> TASKS.clear());
    }

    public static void submitTask(Identifier id, Task task) {
        init();
        TASKS.add(Pair.of(id, task));
    }

    public static void submitDelayedTask(Identifier id, int delay, Task task) {
        init();
        TASKS.add(Pair.of(id, new DelayedTask(delay, task)));
    }

    @FunctionalInterface
    public interface Task {
        Status execute();
    }

    public enum Status {
        RUNNING,
        COMPLETED;

        public boolean isCompleted() {
            return this == COMPLETED;
        }
    }

    private static class DelayedTask implements Task {
        private int delay;
        private final Task task;

        private DelayedTask(int delay, Task task) {
            this.delay = delay;
            this.task = task;
        }

        @Override
        public Status execute() {
            if (this.delay-- > 0) {
                return Status.RUNNING;
            }
            return this.task.execute();
        }
    }
}
