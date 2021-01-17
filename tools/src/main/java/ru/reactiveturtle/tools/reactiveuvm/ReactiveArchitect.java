package ru.reactiveturtle.tools.reactiveuvm;

import java.util.HashMap;
import java.util.Objects;

public class ReactiveArchitect {

    private static HashMap<String, StateKeeper> states = new HashMap<>();

    public static StateKeeper createState(String name, Object def) {
        StateKeeper stateKeeper = new StateKeeper(def);
        states.put(name, stateKeeper);
        return stateKeeper;
    }

    public static StateKeeper getStateKeeper(String name) {
        return (StateKeeper) states.get(name);
    }

    public static void changeState(String name, Object state) {
        StateKeeper binder = states.get(name);
        Objects.requireNonNull(binder);
        binder.changeState(state);
    }

    /*
     * Bridge String naming rule (SrcObject)To(DstObject)
     * Bridge variable naming rule SrcObject_To_DstObject
     * */
    private static HashMap<String, Bridge> bridges = new HashMap<>();

    public static Bridge createBridge(String name) {
        Bridge bridge = new Bridge(name);
        bridges.put(name, bridge);
        return bridge;
    }

    public static StringBridge createStringBridge(String name) {
        StringBridge bridge = new StringBridge(name);
        bridges.put(name, bridge);
        return bridge;
    }

    public static StringBridge getStringBridge(String name) {
        return (StringBridge) bridges.get(name);
    }

    public static IntBridge createIntBridge(String name) {
        IntBridge bridge = new IntBridge(name);
        bridges.put(name, bridge);
        return bridge;
    }

    public static IntBridge getIntBridge(String name) {
        return (IntBridge) bridges.get(name);
    }

    public static Bridge getBridge(String name) {
        return bridges.get(name);
    }

    public static void removeBridge(String name) {
        bridges.remove(name);
    }
}
