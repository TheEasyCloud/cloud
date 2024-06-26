package net.easycloud.wrapper.event;

import net.easycloud.api.event.Event;
import net.easycloud.api.event.EventHandler;
import net.easycloud.api.event.Subscribe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SimpleEventHandler implements EventHandler {
    private final List<Event> events;

    public SimpleEventHandler() {
        this.events = new ArrayList<>();
    }

    @Override
    public <T extends Event> void call(T event) {
        for (Event event2 : events) {
            var clazz = event2.getClass();

            for (Method method : Arrays.stream(clazz.getDeclaredMethods()).filter(it -> {
                if(it.isAnnotationPresent(Subscribe.class)) {
                }
                if(it.getParameterCount() == 1) {
                }

                return it.isAnnotationPresent(Subscribe.class) && it.getParameterCount() == 1 && Arrays.stream(it.getParameterTypes()).anyMatch(it2 -> it2 == event.getClass());
            }).toList()) {
                try {
                    method.invoke(null, event);
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }
    }

    @Override
    public <T extends Event> void register(T event) {
        events.add(event);
    }
}
