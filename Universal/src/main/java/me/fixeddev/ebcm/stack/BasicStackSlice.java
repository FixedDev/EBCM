package me.fixeddev.ebcm.stack;

import me.fixeddev.ebcm.exception.CommandParseException;
import me.fixeddev.ebcm.exception.NoMoreArgumentsException;

import java.util.List;

public class BasicStackSlice implements StackSlice {

    private ArgumentStack backing;

    public BasicStackSlice(ArgumentStack backing) {
        this.backing = backing;
    }

    @Override
    public boolean hasNext() {
        return backing.hasNext();
    }

    @Override
    public String next() throws NoMoreArgumentsException {
        return backing.next();
    }

    @Override
    public String peek() throws NoMoreArgumentsException {
        return backing.peek();
    }

    @Override
    public String current() {
        return backing.current();
    }

    @Override
    public String remove() {
        return backing.remove();
    }

    @Override
    public int getPosition() {
        return backing.getPosition();
    }

    @Override
    public int getSize() {
        return backing.getSize();
    }

    @Override
    public int getArgumentsLeft() {
        return backing.getArgumentsLeft();
    }

    @Override
    public int nextInt() throws CommandParseException {
        return backing.nextInt();
    }

    @Override
    public float nextFloat() throws CommandParseException {
        return backing.nextFloat();
    }

    @Override
    public double nextDouble() throws CommandParseException {
        return backing.nextDouble();
    }

    @Override
    public byte nextByte() throws CommandParseException {
        return backing.nextByte();
    }

    @Override
    public boolean nextBoolean() throws CommandParseException {
        return backing.nextBoolean();
    }

    @Override
    public List<String> getBacking() {
        return backing.getBacking();
    }




}
