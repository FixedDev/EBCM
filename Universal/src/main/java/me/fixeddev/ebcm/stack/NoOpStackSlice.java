package me.fixeddev.ebcm.stack;

import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;

import java.util.Collections;
import java.util.List;

public class NoOpStackSlice implements StackSlice {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public String next() throws NoMoreArgumentsException {
        throw new NoMoreArgumentsException(getSize(), -1);
    }

    @Override
    public String peek() throws NoMoreArgumentsException {
        throw new NoMoreArgumentsException(getSize(), -1);
    }

    @Override
    public String current() {
        throw new IllegalStateException("You must advance the stack at least once before using the current() method!");
    }

    @Override
    public int getPosition() {
        return -1;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int nextInt() throws CommandParseException {
        throw new NoMoreArgumentsException(getSize(), -1);
    }

    @Override
    public float nextFloat() throws CommandParseException {
        throw new NoMoreArgumentsException(getSize(), -1);
    }

    @Override
    public double nextDouble() throws CommandParseException {
        throw new NoMoreArgumentsException(getSize(), -1);
    }

    @Override
    public byte nextByte() throws CommandParseException {
        throw new NoMoreArgumentsException(getSize(), -1);
    }

    @Override
    public boolean nextBoolean() throws CommandParseException {
        throw new NoMoreArgumentsException(getSize(), -1);
    }

    @Override
    public List<String> getBacking() {
        return Collections.emptyList();
    }
}
