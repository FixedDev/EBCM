package me.fixeddev.ebcm;

import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;

import java.util.List;

public class ArgumentStack implements Cloneable {
    protected List<String> originalArguments;

    private int position = -1;

    public ArgumentStack(List<String> originalArguments) {
        this.originalArguments = originalArguments;
    }

    protected ArgumentStack(List<String> originalArguments, int position) {
        this.originalArguments = originalArguments;

        this.position = position;
    }

    public boolean hasNext() {
        return (originalArguments.size() - 1) > position;
    }

    public String next() throws NoMoreArgumentsException {
        if (!hasNext()) {
            throw new NoMoreArgumentsException(originalArguments.size(), position);
        }

        position++;

        return originalArguments.get(position);
    }

    public String peek() throws NoMoreArgumentsException {
        int nextPosition = position + 1;

        if (!hasNext()) {
            throw new NoMoreArgumentsException(originalArguments.size(), position);
        }

        return originalArguments.get(nextPosition);
    }

    public String current() {
        return originalArguments.get(position);
    }

    public int getPosition() {
        return position;
    }

    public int getSize() {
        return originalArguments.size();
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

    public boolean nextBoolean() throws  CommandParseException {
        String next = next();

        if (!next.equalsIgnoreCase("true") && !next.equalsIgnoreCase("false")) {
            throw new CommandParseException("Failed to parse the string " + next + " as boolean!");
        }

        return Boolean.parseBoolean(next);
    }

    public void markAsConsumed() {
        this.position = originalArguments.size();
    }

    @Override
    public ArgumentStack clone() throws CloneNotSupportedException {
        return (ArgumentStack) super.clone();
    }
}
