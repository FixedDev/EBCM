package me.fixeddev.ebcm.stack;

import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;

import java.util.List;

public class SimpleArgumentStack implements ArgumentStack {
    protected List<String> originalArguments;

    protected SimpleArgumentStack parent;

    private int position = 0;

    public SimpleArgumentStack(List<String> originalArguments) {
        this.originalArguments = originalArguments;
    }

    protected SimpleArgumentStack(List<String> originalArguments, int position) {
        this.originalArguments = originalArguments;
        this.position = position;
    }

    protected SimpleArgumentStack(List<String> originalArguments, SimpleArgumentStack parent) {
        this.originalArguments = originalArguments;
        this.parent = parent;
    }

    public boolean hasNext() {
        return (this.originalArguments.size() > this.position);
    }

    public String next() throws NoMoreArgumentsException {
        if (!hasNext()) {
            throw new NoMoreArgumentsException(this.originalArguments.size(), this.position);
        }

        if (this.parent != null) {
            this.parent.position++;
        }

        return this.originalArguments.get(this.position++);
    }

    public String peek() throws NoMoreArgumentsException {
        if (!hasNext()) {
            throw new NoMoreArgumentsException(this.originalArguments.size(), this.position);
        }

        return this.originalArguments.get(this.position);
    }

    public String current() {
        return this.originalArguments.get(this.position - 1);
    }

    public String remove() {
        if (this.position == 0) {
            throw new IllegalStateException("You must advance the stack at least 1 time before calling remove!");
        }

        String toRemove = current();
        getBacking().remove(toRemove);
        this.position--;

        if (this.parent != null) {
            this.position--;
        }

        return toRemove;
    }

    public int getPosition() {
        return this.position;
    }

    public int getSize() {
        return this.originalArguments.size();
    }

    public int getArgumentsLeft() {
        return getSize() - getPosition();
    }

    public int nextInt() throws CommandParseException {
        String next = next();

        try {
            return Integer.parseInt(next);
        } catch (NumberFormatException e) {
            throw new CommandParseException("Failed to parse the string " + next + " as int!");
        }
    }

    public float nextFloat() throws CommandParseException {
        String next = next();

        try {
            return Float.parseFloat(next);
        } catch (NumberFormatException e) {
            throw new CommandParseException("Failed to parse the string " + next + " as float!");
        }
    }

    public double nextDouble() throws CommandParseException {
        String next = next();

        try {
            return Double.parseDouble(next);
        } catch (NumberFormatException e) {
            throw new CommandParseException("Failed to parse the string " + next + " as double!");
        }
    }

    public byte nextByte() throws CommandParseException {
        String next = next();

        try {
            return Byte.parseByte(next);
        } catch (NumberFormatException e) {
            throw new CommandParseException("Failed to parse the string " + next + " as byte!");
        }
    }

    public boolean nextBoolean() throws CommandParseException {
        String next = next();

        if (!next.equalsIgnoreCase("true") && !next.equalsIgnoreCase("false")) {
            throw new CommandParseException("Failed to parse the string " + next + " as boolean!");
        }

        return Boolean.parseBoolean(next);
    }

    public void markAsConsumed() {
        this.position = this.originalArguments.size();
    }

    public void applySnapshot(StackSnapshot snapshot, boolean changeArgs) {
        int offset = snapshot.position - this.position;
        this.position = snapshot.position;

        if (this.parent != null) {
            if (offset < 0) {
                this.parent.position += offset;
            } else {
                this.parent.position -= offset;
            }
        }

        if (changeArgs) {
            int index = 0;
            for (String arg : snapshot.backing) {
                this.originalArguments.set(index, arg);
                index++;
            }
        }
    }

    public List<String> getBacking() {
        return this.originalArguments;
    }

    public StackSlice getSlice(int start, int end) {
        try {
            return new BasicStackSlice(new SimpleArgumentStack(this.originalArguments.subList(start, end), this));
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("The end shouldn't be after the end of the ArgumentStack!");
        }
    }

    public StackSnapshot getSnapshot(boolean useCurrentPos) {
        return new StackSnapshot(this, useCurrentPos ? this.position : -1);
    }
}
