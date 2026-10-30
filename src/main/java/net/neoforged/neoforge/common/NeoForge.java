package net.neoforged.neoforge.common;

public class NeoForge {

    public static final EventBus EVENT_BUS = new EventBus();

    public static class EventBus {

        public <T> T post(T event) {
            return event;
        }

        public void register(Object target) {
        }
    }
}
