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

    public void pull(int param) {
        if (puller != null) {
            puller.onPull(param);
        }
    }

    public interface Puller {
        void onPull(int param);
    }
}
