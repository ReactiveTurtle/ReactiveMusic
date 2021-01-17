package ru.reactiveturtle.tools.reactiveuvm;

public class StringBridge extends Bridge {
    private Puller puller;

    public StringBridge(String name) {
        super(name);
    }

    public StringBridge connect(Puller puller) {
        this.puller = puller;
        return this;
    }

    public StringBridge pull(String value) {
        puller.onPull(value);
        return this;
    }

    public interface Puller {
        void onPull(String string);
    }
}
