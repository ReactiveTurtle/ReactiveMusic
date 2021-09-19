package ru.reactiveturtle.reactivemusic.toolkit.lambda;

public class LambdaExtensions {
    public static void startNewThread(EmptyVoid v) {
        new Thread(v::call).start();
    }
}
