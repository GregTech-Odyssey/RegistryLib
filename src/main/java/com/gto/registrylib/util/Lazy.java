package com.gto.registrylib.util;

import java.util.function.Supplier;

public final class Lazy<T> implements Supplier<T> {
  private static final Object UNINITIALIZED = new Object();
  private Supplier<? extends T> delegate;
  private Object value = UNINITIALIZED;

  private Lazy(Supplier<? extends T> delegate) {
    this.delegate = delegate;
  }

  public static <T> Lazy<T> of(Supplier<? extends T> delegate) {
    return new Lazy<>(delegate);
  }

  @Override
  public T get() {
    var value = this.value;
    if (value == UNINITIALIZED) {
      synchronized (this) {
        if (this.delegate != null) {
          this.value = this.delegate.get();
          this.delegate = null;
        }
        value = this.value;
      }
    }
    return (T) value;
  }
}
