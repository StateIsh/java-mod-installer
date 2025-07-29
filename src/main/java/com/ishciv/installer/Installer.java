package com.ishciv.installer;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.Base64;

public class Installer {

    private static final String VERSION_ID = "state-parkour";
    private static final String VERSION_JAR_URL = "https://dl.ishciv.com/modpacks/state-parkour.jar";
    private static final String VERSION_JSON_URL = "https://dl.ishciv.com/modpacks/state-parkour.json";
    private static final String PROFILE_KEY = VERSION_ID;
    private static final String PROFILE_NAME = "State Parkour";

    public static void downloadAndCopyMod(String urlStr, String fileName) throws IOException {
        Path modsDir = getModsPath();
        Files.createDirectories(modsDir);
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, modsDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void downloadToGameDir(String urlStr, String fileName) throws IOException {
        Path gameDir = getGameDir();
        Files.createDirectories(gameDir);
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, gameDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static Path getModsPath() {
        return getGameDir().resolve("mods");
    }

    public static void addLauncherProfile() throws IOException {
        ensureCustomVersion();
        Path launcherJson = getMinecraftDir().resolve("launcher_profiles.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root = Files.exists(launcherJson) ? gson.fromJson(Files.newBufferedReader(launcherJson), JsonObject.class) : new JsonObject();
        JsonObject profiles = root.has("profiles") && root.get("profiles").isJsonObject() ? root.getAsJsonObject("profiles") : new JsonObject();
        root.add("profiles", profiles);
        if (!profiles.has(PROFILE_KEY)) {
            JsonObject p = new JsonObject();
            p.addProperty("name", PROFILE_NAME);
            p.addProperty("type", "custom");
            p.addProperty("created", Instant.now().toString());
            p.addProperty("lastUsed", Instant.now().toString());
            p.addProperty("gameDir", getGameDir().toString());
            p.addProperty("lastVersionId", VERSION_ID);
            p.addProperty("icon", "data:image/png;base64," + logoBase64());
            profiles.add(PROFILE_KEY, p);
            try (Writer w = Files.newBufferedWriter(launcherJson)) {
                gson.toJson(root, w);
            }
        }
    }

    private static void ensureCustomVersion() throws IOException {
        Path versionFolder = getMinecraftDir().resolve("versions").resolve(VERSION_ID);
        Files.createDirectories(versionFolder);
        Path jarPath = versionFolder.resolve(VERSION_ID + ".jar");
        Path jsonPath = versionFolder.resolve(VERSION_ID + ".json");
        if (Files.notExists(jarPath)) download(VERSION_JAR_URL, jarPath);
        if (Files.notExists(jsonPath)) download(VERSION_JSON_URL, jsonPath);
    }

    private static void download(String url, Path target) throws IOException {
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String logoBase64() throws IOException {
        try (InputStream in = Installer.class.getResourceAsStream("/logo.png")) {
            if (in == null) throw new FileNotFoundException();
            return Base64.getEncoder().encodeToString(in.readAllBytes());
        }
    }

    public static Path getMinecraftDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        if (os.contains("win")) return Paths.get(System.getenv("APPDATA"), ".minecraft");
        if (os.contains("mac")) return Paths.get(home, "Library", "Application Support", "minecraft");
        return Paths.get(home, ".minecraft");
    }

    public static Path getGameDir() {
        return getMinecraftDir().resolve("profiles").resolve(VERSION_ID);
    }
}
