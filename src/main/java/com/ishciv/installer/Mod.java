package com.ishciv.installer;

public class Mod {
    public final String name;
    public final String url;
    public final String fileName;

    public Mod(String name, String url, String fileName) {
        this.name = name;
        this.url = url;
        this.fileName = fileName;
    }
}