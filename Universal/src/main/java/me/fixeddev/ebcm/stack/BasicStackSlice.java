package me.fixeddev.ebcm.stack;

import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;

import java.util.List;

public class BasicStackSlice implements StackSlice {

    private int start;
    private int end;

    private int position;

    private ArgumentStack backing;

    public BasicStackSlice(int start, int end, int position, ArgumentStack backing) {
        this.start = start;
        this.end = end;
        this.position = position;
        this.backing = backing;

        if (position < start) {
            throw new IllegalArgumentException("The position should be after the start of the slice!");
        }

        if (position > end) {
            throw new IllegalArgumentException("The position should be before the end of the slice!");
        }

        if (end >= backing.getSize()) {
            throw new IllegalArgumentException("The end shouldn't be after the end of the ArgumentStack!");
        }
    }


    @Override
    public boolean hasNext() {
        return end >= position;
    }

    @Override
    public String next() throws NoMoreArgumentsException {
        if (!hasNext()) {
            throw new NoMoreArgumentsException(getSize(), position);
        }

        position++;

        return backing.next();
    }

    @Override
    public String peek() throws NoMoreArgumentsException {
        if (!hasNext()) {
            throw new NoMoreArgumentsException(getSize(), position);
        }

        return backing.peek();
    }

    @Override
    public String current() {
        return backing.current();
    }

    @Override
    public int getPosition() {
        return position - start;
    }

    @Override
    public int getSize() {
        return end - start + 1;
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
    public List<String> getBacking() {
        return backing.getBacking().subList(start, end);
    }
}
