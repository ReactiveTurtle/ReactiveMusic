package ru.reactiveturtle.tools.reactiveuvm;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StateKeeper {
    private Object state;
    private List<Binder> binders = new ArrayList<>();

    public StateKeeper(Object state) {
        this.state = state;
    }

    public void changeState(Object state) {
        this.state = state;
        for (Binder binder : binders) {
            binder.call();
        }
    }

    public Object getState() {
        return state;
    }

    public Binder subscribe(Binder.Callback callback) {
        Binder binder = new Binder(this, null, null, null, callback);
        binders.add(binder);
        return binder;
    }

    public Binder subscribe(View view, String method, Class<?> paramType) {
        return subscribe(view, method, paramType, null);
    }

    public Binder subscribe(View view, String method, Class<?> paramType, Binder.Callback callback) {
        Binder binder = new Binder(this, view, method, paramType, callback);
        binders.add(binder);
        return binder;
    }

    public boolean unsubscribe(Binder binder) {
        return binders.remove(binder);
    }

    public static class Binder {
        private StateKeeper stateKeeper;
        private View view;
        private String methodName;
        private Class<?> paramType;
        private Callback callback;

        private Binder(StateKeeper stateKeeper,
                       View view,
                       String methodName,
                       Class<?> paramType) {
            init(stateKeeper, view, methodName, paramType);
        }

        private Binder(@NonNull StateKeeper stateKeeper,
                       View view,
                       String methodName,
                       Class<?> paramType,
                       Callback callback) {
            init(stateKeeper, view, methodName, paramType);
            this.callback = callback;
        }

        private void init(StateKeeper stateKeeper,
                          View view,
                          String methodName,
                          Class<?> paramType) {
            this.stateKeeper = stateKeeper;
            this.view = view;
            this.methodName = methodName;
            this.paramType = paramType;
        }

        public void unsubscribe() {
            stateKeeper.unsubscribe(this);
            callback = null;
            stateKeeper = null;
            view = null;
        }

        public interface Callback {
            void onInvoke(View view, Object value);
        }

        public Binder call() {
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    if (view != null && methodName != null && paramType != null) {
                        Method method = view.getClass().getMethod(methodName, paramType);
                        method.invoke(view, stateKeeper.state);
                    }
                    if (callback != null) {
                        callback.onInvoke(view, stateKeeper.state);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    throw new IllegalStateException();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new IllegalStateException();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new IllegalStateException();
                }
            });
            return this;
        }
    }
}