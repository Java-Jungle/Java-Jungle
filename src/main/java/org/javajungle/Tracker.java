package org.javajungle;

/** Receives a topic only when a Story Mode submission is wrong. */
@FunctionalInterface
public interface Tracker {
    void mistake(Topic topic);
}
