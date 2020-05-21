package me.fixeddev.ebcm.stack;

import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;

import java.util.List;

public class BasicStackSnapshot implements StackSnapshot {

    private List<String> backing;
    private int position;

    public BasicStackSnapshot(ArgumentStack stack, int position) {
        this.backing = stack.getBacking();
        this.position = position;
    }

    @Override
    public boolean hasNext() {
        return (backing.size() - 1) > position;
    }

    @Override
    public String next() throws NoMoreArgumentsException {
        if (!hasNext()) {
            throw new NoMoreArgumentsException(backing.size(), position);
        }

        position++;

        return backing.get(position);
    }

    @Override
    public String peek() throws NoMoreArgumentsException {
        int nextPosition = position + 1;

        if (!hasNext()) {
            throw new NoMoreArgumentsException(backing.size(), position);
        }

        return backing.get(nextPosition);
    }

    @Override
    public String current() {
        if (position == -1) {
            throw new IllegalStateException("You must advance the stack at least once before using the current() method!");
        }

        return backing.get(position);
    }

    @Override
    public String remove() {
        if (position == -1) {
            throw new IllegalStateException("You must advance the stack at least once before using the remove() method!");
        }

        String toRemove = current();
        backing.remove(current());

        return toRemove;
    }
    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public int getSize() {
        return backing.size();
    }

    @Override
    public int nextInt() throws CommandParseException {
        String next = next();
        try {
            return Integer.parseInt(next);
        } catch (NumberFormatException e) {
            throw new CommandParseException("Failed to parse the string " + next + " as int!");
        }
    }

    @Override
    public float nextFloat() throws CommandParseException {
        String next = next();

        try {
            return Float.parseFloat(next);
        } catch (NumberFormatException e) {
            throw new CommandParseException("Failed to parse the string " + next + " as float!");
        }
    }

    @Override
    public double nextDouble() throws CommandParseException {
        String next = next();

        try {
            return Double.parseDouble(next);
        } catch (NumberFormatException e) {
            throw new CommandParseException("Failed to parse the string " + next + " as double!");
        }
    }

    @Override
    public byte nextByte() throws CommandParseException {
        String next = next();

        try {
            return Byte.parseByte(next);
        } catch (NumberFormatException e) {
            throw new CommandParseException("Failed to parse the string " + next + " as byte!");
        }
    }

    @Override
    public boolean nextBoolean() throws CommandParseException {
        String next = next();

        if (!next.equalsIgnoreCase("true") && !next.equalsIgnoreCase("false")) {
            throw new CommandParseException("Failed to parse the string " + next + " as boolean!");
        }

        return Boolean.parseBoolean(next);
    }

    @Override
    public void markAsConsumed() {
        this.position = backing.size();
    }

}
