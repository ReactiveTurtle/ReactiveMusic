package ru.reactiveturtle.reactivemusic.toolkit;

public class Stack<T> {
    private ReactiveList<T> stack = new ReactiveList<>();

    public void add(T item) {
        stack.add(item);
    }

    public T pop() {
        if (isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        return stack.remove(stack.size() - 1);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public void clear() {
        stack.clear();
    }

    public int size() {
        return stack.size();
    }
}
