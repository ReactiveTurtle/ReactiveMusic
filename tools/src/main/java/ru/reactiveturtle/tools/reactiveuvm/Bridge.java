package ru.reactiveturtle.tools.reactiveuvm;

public class Bridge {
    private Puller puller;
    private String name;

    public Bridge(String name) {
        this.name = name;
    }

    public Bridge connect(Puller puller) {
        this.puller = puller;
        return this;
    }

    public void pull() {
        if (puller != null) {
            puller.onPull();
        }
    }

    public String getName() {
        return name;
    }

    public interface Puller {
        void onPull();
    }
}
