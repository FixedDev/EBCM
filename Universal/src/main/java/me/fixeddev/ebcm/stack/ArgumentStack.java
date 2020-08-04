package me.fixeddev.ebcm.stack;

import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;
import me.fixeddev.ebcm.stack.StackSlice;
import me.fixeddev.ebcm.stack.StackSnapshot;

import java.util.List;

public interface ArgumentStack {
    boolean hasNext();

    String next() throws NoMoreArgumentsException;

    String peek() throws NoMoreArgumentsException;

    String current();

    String remove();

    int getPosition();

    int getSize();

    int getArgumentsLeft();

    int nextInt() throws CommandParseException;

    float nextFloat() throws CommandParseException;

    double nextDouble() throws CommandParseException;

    byte nextByte() throws CommandParseException;

    boolean nextBoolean() throws CommandParseException;

    void markAsConsumed();

    List<String> getBacking();

    StackSlice getSlice(int start, int end);

    default StackSlice getSliceFrom(int start) {
        return getSlice(start, getSize());
    }

    default StackSlice getSliceTo(int end) {
        return getSlice(getPosition(), end);
    }

    default StackSlice getSlice(int size) {
        // Add + 1 to considerate the exclusive end position.
        return getSliceTo(getPosition() + size);
    }

    default StackSnapshot getSnapshot() {
        return getSnapshot(true);
    }

    StackSnapshot getSnapshot(boolean useCurrentPos);

    default void applySnapshot(StackSnapshot snapshot) {
        applySnapshot(snapshot, true);
    }

    void applySnapshot(StackSnapshot snapshot, boolean changeArgs);

}
