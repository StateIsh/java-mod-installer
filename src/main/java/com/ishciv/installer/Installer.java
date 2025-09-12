package com.ishciv.installer;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Base64;

public class Installer {

    private static final String VERSION_ID        = "ishciv-purge";
    private static final String VERSION_JAR_URL   = "https://dl.ishciv.com/modpacks/ishciv-purge.jar";
    private static final String VERSION_JSON_URL  = "https://dl.ishciv.com/modpacks/ishciv-purge.json";
    private static final String PROFILE_KEY       = VERSION_ID;
    private static final String PROFILE_NAME      = "ish Purge Civilization";

    /* ---------- download helpers ---------- */

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

    private static void download(String url, Path target) throws IOException {
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /* ---------- launcher profile ---------- */

    public static void addLauncherProfile() throws IOException {
        ensureCustomVersion();

        Path launcherJson = getMinecraftDir().resolve("launcher_profiles.json");
        Gson gson         = new GsonBuilder().setPrettyPrinting().create();

        /* Java 8‑compatible reader */
        JsonObject root;
        if (Files.exists(launcherJson)) {
            try (Reader r = Files.newBufferedReader(launcherJson, StandardCharsets.UTF_8)) {
                root = gson.fromJson(r, JsonObject.class);
            }
        } else {
            root = new JsonObject();
        }

        JsonObject profiles = root.has("profiles") && root.get("profiles").isJsonObject()
                ? root.getAsJsonObject("profiles")
                : new JsonObject();
        root.add("profiles", profiles);

        if (!profiles.has(PROFILE_KEY)) {
            JsonObject p = new JsonObject();
            p.addProperty("name",           PROFILE_NAME);
            p.addProperty("type",           "custom");
            p.addProperty("created",        Instant.now().toString());
            p.addProperty("lastUsed",       Instant.now().toString());
            p.addProperty("gameDir",        getGameDir().toString());
            p.addProperty("lastVersionId",  VERSION_ID);
            p.addProperty("icon",           "data:image/png;base64," + logoBase64());
            profiles.add(PROFILE_KEY, p);

            /* Java 8‑compatible writer */
            try (Writer w = Files.newBufferedWriter(launcherJson,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                gson.toJson(root, w);
            }
        }
    }

    private static void ensureCustomVersion() throws IOException {
        Path versionFolder = getMinecraftDir().resolve("versions").resolve(VERSION_ID);
        Files.createDirectories(versionFolder);

        Path jarPath  = versionFolder.resolve(VERSION_ID + ".jar");
        Path jsonPath = versionFolder.resolve(VERSION_ID + ".json");

        if (Files.notExists(jarPath))  download(VERSION_JAR_URL,  jarPath);
        if (Files.notExists(jsonPath)) download(VERSION_JSON_URL, jsonPath);
    }

    /* ---------- utility paths ---------- */

    public static Path getModsPath() {
        return getGameDir().resolve("mods");
    }

    public static Path getGameDir() {
        return getMinecraftDir().resolve("profiles").resolve(VERSION_ID);
    }

    public static Path getMinecraftDir() {
        String os   = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");

        if (os.contains("win")) {                       // Windows
            String appData = System.getenv("APPDATA");
            return Paths.get(appData != null ? appData : home, ".minecraft");
        }
        if (os.contains("mac")) {                       // macOS
            return Paths.get(home, "Library", "Application Support", "minecraft");
        }
        return Paths.get(home, ".minecraft");           // Linux / other
    }

    /* ---------- resource helpers ---------- */

    private static String logoBase64() throws IOException {
        try (InputStream in = Installer.class.getResourceAsStream("/logo.png")) {
            if (in == null) throw new FileNotFoundException("Resource /logo.png not found.");

            /* InputStream.readAllBytes() is Java 9+, so copy manually */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }
}
