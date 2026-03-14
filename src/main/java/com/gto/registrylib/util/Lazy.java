package com.gto.registrylib.util;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {

  private final Supplier<? extends T> delegate;
  private T value;
  private boolean resolved;

  private Lazy(Supplier<? extends T> delegate) {
    this.delegate = delegate;
  }

  public static <T> Lazy<T> of(Supplier<? extends T> delegate) {
    return new Lazy<>(delegate);
  }

  @Override
  public synchronized T get() {
    if (!resolved) {
      value = delegate.get();
      resolved = true;
    }
    return value;
  }
}
