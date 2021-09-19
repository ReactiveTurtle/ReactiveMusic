package ru.reactiveturtle.reactivemusic.toolkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import ru.reactiveturtle.reactivemusic.toolkit.lambda.Function;
import ru.reactiveturtle.reactivemusic.toolkit.lambda.Predicate;
import ru.reactiveturtle.reactivemusic.toolkit.lambda.Void;


public class ReactiveList<T> extends ArrayList<T> {
    public ReactiveList() {
        super();
    }

    public ReactiveList(T[] array) {
        super(Arrays.asList(array));
    }

    public ReactiveList(Collection<? extends T> collection) {
        super(collection);
    }

    public void forEachP(Void<T> v) {
        for (T element : this) {
            v.call(element);
        }
    }

    public void forEachP(ForeachWithIndex<T> v) {
        for (int i = 0; i < this.size(); i++) {
            T element = this.get(i);
            v.call(element, i);
        }
    }

    public T first() {
        T result = firstOrNull((x) -> true);
        if (result == null)
            throw new NullPointerException();
        return result;
    }

    public T first(Predicate<T> predicate) {
        T result = firstOrNull(predicate);
        if (result == null)
            throw new NullPointerException();
        return result;
    }

    public T firstOrNull(Predicate<T> predicate) {
        T result = null;
        for (T element : this) {
            if (predicate.test(element)) {
                result = element;
                break;
            }
        }
        return result;
    }

    public T last() {
        T result = lastOrNull();
        if (result == null)
            throw new NullPointerException();
        return result;
    }

    public T lastOrNull() {
        int lastIndex = size() - 1;
        return lastIndex > - 1 ? get(lastIndex) : null;
    }

    public boolean contains(Predicate<T> predicate) {
        boolean result = false;
        Iterator<T> iterator = iterator();
        while (iterator.hasNext() && !result) {
            T element = iterator.next();
            result = predicate.test(element);
        }
        return result;
    }

    public int indexOf(Predicate<T> predicate) {
        int result = -1;
        for (int i = 0; i < size(); i++) {
            T element = get(i);
            if (predicate.test(element)) {
                result = i;
                break;
            }
        }
        return result;
    }

    public void removeAll(Predicate<T> predicate) {
        for (int i = 0; i < size(); i++) {
            T element = get(i);
            if (predicate.test(element)) {
                remove(i);
                i--;
            }
        }
    }

    public ReactiveList<T> filter(Predicate<T> predicate) {
        ReactiveList<T> resultList = new ReactiveList<>();
        for (T element : this) {
            if (predicate.test(element)) {
                resultList.add(element);
            }
        }
        return resultList;
    }

    public <R> ReactiveList<R> map(Function<T, R> function) {
        ReactiveList<R> resultList = new ReactiveList<>();
        for (T element : this) {
            resultList.add(function.test(element));
        }
        return resultList;
    }

    public interface ForeachWithIndex<E> {
        void call(E e, int index);
    }
}
