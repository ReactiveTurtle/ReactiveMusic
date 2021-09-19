package ru.reactiveturtle.reactivemusic.toolkit;

public class Queue<T> {
    private ReactiveList<T> queue = new ReactiveList<>();

    public void add(T item) {
        queue.add(item);
    }

    public T takeFirst() {
        if (isEmpty()) {
            throw new RuntimeException("Queue is empty");
        }
        return queue.remove(0);
    }

    public boolean isEmpty() {
       return queue.isEmpty();
    }

    public void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }
}
