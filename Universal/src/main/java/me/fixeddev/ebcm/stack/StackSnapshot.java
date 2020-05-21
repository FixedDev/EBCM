package me.fixeddev.ebcm.stack;

import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;

public interface StackSnapshot {
    boolean hasNext();

    String next() throws NoMoreArgumentsException;

    String peek() throws NoMoreArgumentsException;

    String current();

    int getPosition();

    int getSize();

    int nextInt() throws CommandParseException;

    float nextFloat() throws CommandParseException;

    double nextDouble() throws CommandParseException;

    byte nextByte() throws CommandParseException;

    boolean nextBoolean() throws CommandParseException;

    void markAsConsumed();
}