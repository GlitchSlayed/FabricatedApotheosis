package net.neoforged.bus.api;

public interface ICancellableEvent {

    default boolean isCanceled() {
        return false;
    }

    default void setCanceled(boolean canceled) {
    }
}
