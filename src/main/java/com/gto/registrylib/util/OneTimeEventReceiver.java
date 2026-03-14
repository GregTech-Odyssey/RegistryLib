package com.gto.registrylib.util;

import com.gto.registrylib.RegistryLib;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.NeoForge;

public class OneTimeEventReceiver<T extends Event> implements Consumer<T> {

  private static final List<Runnable> toUnregister = new ArrayList<>();

  public static <T extends Event & IModBusEvent> void addModListener(
      RegistryLib owner, Class<? super T> evtClass, Consumer<? super T> listener) {
    addModListener(owner, EventPriority.NORMAL, evtClass, listener);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Event & IModBusEvent> void addModListener(
      RegistryLib owner,
      EventPriority priority,
      Class<? super T> evtClass,
      Consumer<? super T> listener) {
    IEventBus bus = owner.getModEventBus();
    if (bus != null) {
      addListener(bus, priority, (Class<T>) evtClass, listener);
    }
  }

  public static <T extends Event> void addForgeListener(
      Class<? super T> evtClass, Consumer<? super T> listener) {
    addForgeListener(EventPriority.NORMAL, evtClass, listener);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Event> void addForgeListener(
      EventPriority priority, Class<? super T> evtClass, Consumer<? super T> listener) {
    addListener(NeoForge.EVENT_BUS, priority, (Class<T>) evtClass, listener);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Event> void addListener(
      IEventBus bus,
      EventPriority priority,
      Class<? super T> evtClass,
      Consumer<? super T> listener) {
    bus.addListener(
        priority, false, (Class<T>) evtClass, new OneTimeEventReceiver<>(bus, listener));
  }

  public static void unregister(RegistryLib owner, Object listener, Class<? extends Event> event) {
    IEventBus bus = owner.getModEventBus();
    if (bus != null) {
      toUnregister.add(() -> bus.unregister(listener));
    }
  }

  private final IEventBus bus;
  private final Consumer<? super T> listener;
  private final AtomicBoolean consumed = new AtomicBoolean();

  public OneTimeEventReceiver(IEventBus bus, Consumer<? super T> listener) {
    this.bus = bus;
    this.listener = listener;
  }

  @Override
  public void accept(T event) {
    if (consumed.compareAndSet(false, true)) {
      listener.accept(event);
      toUnregister.add(() -> bus.unregister(this));
    }
  }
}
