package com.gto.registrylib.util.registry;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractRegistry<E> implements Iterable<E> {

    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    @Getter
    private boolean frozen = false;

    private ProtectionDomain creatorLocation;

    protected AbstractRegistry() {
        WALKER.walk(
                frames -> {
                    frames
                            .skip(2)
                            .limit(1)
                            .forEach(frame -> creatorLocation = frame.getDeclaringClass().getProtectionDomain());
                    return null;
                });
    }

    public abstract int size();

    public abstract void forEach(Consumer<? super E> action);

    public abstract @NonNull Iterator<E> iterator();

    public abstract Spliterator<E> spliterator();

    public Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    protected final void checkModifiable() {
        if (this.frozen) throw new IllegalStateException("Cannot modify registry: it is frozen!");
    }

    public void freeze() {
        checkCallerIsCreator();
        if (frozen) throw new IllegalStateException("Registry is already frozen!");
        this.frozen = true;
    }

    private void checkCallerIsCreator() {
        if (!WALKER.walk(
                frames -> frames
                        .skip(2)
                        .limit(1)
                        .allMatch(
                                frame -> {
                                    var pd = frame.getDeclaringClass().getProtectionDomain();
                                    return pd == creatorLocation;
                                }))) {
            throw new IllegalStateException("Registry can only be frozen by the class that created it!");
        }
    }
}
