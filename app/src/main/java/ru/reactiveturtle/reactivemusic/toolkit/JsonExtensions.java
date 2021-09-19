package ru.reactiveturtle.reactivemusic.toolkit;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public final class JsonExtensions {
    private JsonExtensions() {
    }

    public static String serializeList(List<String> strings) {
        JSONArray jsonArray = new JSONArray();
        for (String str : strings) {
            jsonArray.put(str);
        }
        return jsonArray.toString();
    }

    public static ReactiveList<String> deserializeList(String serialized) {
        try {
            JSONArray jsonArray = new JSONArray(serialized);
            ReactiveList<String> list = new ReactiveList<>();
            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                list.add(jsonArray.getString(i));
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }
}
