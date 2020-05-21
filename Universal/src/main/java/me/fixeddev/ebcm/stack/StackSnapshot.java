package me.fixeddev.ebcm.stack;

import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;

import java.util.ArrayList;
import java.util.List;

public class StackSnapshot {

    List<String> backing;
    int position;

    public StackSnapshot(ArgumentStack stack, int position) {
        this.backing = new ArrayList<>(stack.getBacking());
        this.position = position;
    }

    public boolean hasNext() {
        return (backing.size() - 1) > position;
    }

    public String next() throws NoMoreArgumentsException {
        if (!hasNext()) {
            throw new NoMoreArgumentsException(backing.size(), position);
        }

        position++;

        return backing.get(position);
    }

    public String peek() throws NoMoreArgumentsException {
        int nextPosition = position + 1;

        if (!hasNext()) {
            throw new NoMoreArgumentsException(backing.size(), position);
        }

        return backing.get(nextPosition);
    }

    public String current() {
        if (position == -1) {
            throw new IllegalStateException("You must advance the stack at least once before using the current() method!");
        }

        return backing.get(position);
    }

    public String remove() {
        if (position == -1) {
            throw new IllegalStateException("You must advance the stack at least once before using the remove() method!");
        }

        String toRemove = current();
        backing.remove(current());

        return toRemove;
    }

    public int getPosition() {
        return position;
    }

    public int getSize() {
        return backing.size();
    }

    public int getArgumentsLeft() {
        return (getSize() - 1) - getPosition();
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
        this.position = backing.size();
    }
}
