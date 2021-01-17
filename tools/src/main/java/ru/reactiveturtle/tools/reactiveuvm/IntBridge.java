package ru.reactiveturtle.tools.reactiveuvm;

public class IntBridge extends Bridge {
    private Puller puller;

    public IntBridge(String name) {
        super(name);
    }

    public IntBridge connect(Puller puller) {
        this.puller = puller;
        return this;
    }

    public IntBridge pull(int value) {
        puller.onPull(value);
        return this;
    }

    public interface Puller {
        void onPull(int value);
    }
}
