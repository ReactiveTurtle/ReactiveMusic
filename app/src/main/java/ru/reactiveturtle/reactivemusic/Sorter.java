package ru.reactiveturtle.reactivemusic;

import java.io.File;
import java.util.Comparator;

public class Sorter {
    public static class NameComaparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            String name1 = f1.getName().toLowerCase();
            String name2 = f2.getName().toLowerCase();
            if (name1.equals(name2))
                return 0;

            int length = name1.length() > name2.length() ? name2.length() : name1.length();
            for (int i = 0; i < length; i++) {
                if (name1.charAt(i) != name2.charAt(i))
                    return name1.charAt(i) - name2.charAt(i);
            }
            return name1.length() > name2.length() ? 1 : -1;
        }
    }

    public static class FileComaparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            if (f1.isDirectory()) return f2.isDirectory() ? 0 : -1;
            else return f2.isDirectory() ? 1 : 0;
        }
    }
}