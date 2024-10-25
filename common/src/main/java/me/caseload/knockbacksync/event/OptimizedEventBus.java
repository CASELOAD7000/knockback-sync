package me.caseload.knockbacksync.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimizedEventBus implements EventBus {
    private final Map<Class<? extends Event>, List<OptimizedListener>> listenerMap = new HashMap<>();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    @Override
    public void registerListeners(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            KBSyncEventHandler annotation = method.getAnnotation(KBSyncEventHandler.class);
            if (annotation != null && method.getParameterCount() == 1) {
                Class<?> eventType = method.getParameterTypes()[0];
                if (Event.class.isAssignableFrom(eventType)) {
                    try {
                        MethodHandle handle = lookup.unreflect(method);
                        OptimizedListener optimizedListener = new OptimizedListener(listener, handle, annotation.priority());
                        listenerMap.computeIfAbsent((Class<? extends Event>) eventType, k -> new ArrayList<>()).add(optimizedListener);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // Sort listeners by priority
        listenerMap.values().forEach(list -> list.sort((a, b) -> Integer.compare(b.priority, a.priority)));
    }

    @Override
    public void unregisterListeners(Object listener) {
        listenerMap.values().forEach(list -> list.removeIf(l -> l.listener == listener));
    }

    @Override
    public void post(Event event) {
        List<OptimizedListener> listeners = listenerMap.get(event.getClass());
        if (listeners != null) {
            for (OptimizedListener listener : listeners) {
                try {
                    listener.handle.invoke(listener.listener, event);
                    if (event.isCancelled()) {
                        break;
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    private static class OptimizedListener {
        final Object listener;
        final MethodHandle handle;
        final int priority;

        OptimizedListener(Object listener, MethodHandle handle, int priority) {
            this.listener = listener;
            this.handle = handle;
            this.priority = priority;
        }
    }
}