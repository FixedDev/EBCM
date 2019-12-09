package me.fixeddev.ebcm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ListAppender<T> {
    private static final int DEFAULT_SIZE = 10;

    private Object[] content;
    private int size;

    public ListAppender(int size) {
        this.content = new Object[size];
    }

    public ListAppender() {
        this(DEFAULT_SIZE);
    }

    /**
     * Grows the appender content backing to the actual size + {@param size}
     *
     * @param size The new size to add
     */
    private void grow(int size) {
        if (size == 0) return;

        content = Arrays.copyOf(content, content.length + size);
    }

    public ListAppender<T> set(Collection<? extends T> object) {
        content = object.toArray();

        return this;
    }

    public ListAppender<T> add(T object) {
        if (object == null) {
            throw new IllegalArgumentException("The provided object is null");
        }

        grow(1);
        content[size++] = object;

        return this;
    }

    public List<T> toList() {
        return new ArrayList(Arrays.asList(content));
    }
}
