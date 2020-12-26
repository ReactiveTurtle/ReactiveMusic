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

    public void pull(String param) {
        if (puller != null) {
            puller.onPull(param);
        }
    }

    public interface Puller {
        void onPull(String param);
    }
}
