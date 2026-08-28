package com.gto.registrylib.util.registry;

import java.util.*;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

public final class ListRegistry<E> extends AbstractRegistry<E> {

    private List<E> list;

    public ListRegistry() {
        this(new ArrayList<>());
    }

    public ListRegistry(List<E> list) {
        this.list = list;
    }

    public void clear() {
        checkModifiable();
        list = Collections.emptyList();
        freeze();
    }

    public void consume(Consumer<? super E> consumer) {
        list.forEach(consumer);
        clear();
    }

    public void add(E element) {
        checkModifiable();
        list.add(element);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {
        list.forEach(consumer);
    }

    @Override
    public Spliterator<E> spliterator() {
        return list.spliterator();
    }

    @Override
    public @Nonnull Iterator<E> iterator() {
        return list.iterator();
    }
}
