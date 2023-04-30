package me.fullpage.manticlib;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.fullpage.manticlib.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

public class Versionator {

    public static String LATEST_VERSION;
    private static boolean NOT_CLEANED;

    static {
        NOT_CLEANED = true;
        try {
            LATEST_VERSION = getLatestVersion();
        } catch (Exception e) {
            LATEST_VERSION = null;
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(ManticLib.get(), () -> {
            try {
                String latestVersion = getLatestVersion();
                if (latestVersion != null) {
                    LATEST_VERSION = latestVersion;
                }
                updateToLatest(ManticLib.get());
            } catch (Exception ignored) {
            }
        }, 12000, 12000);
    }

    public static void updateToLatest(Plugin plugin) {
        final File directory = new File("plugins");
        if (directory == null || !directory.isDirectory()) {
            throw new IllegalArgumentException("Directory is null or not a directory");
        }

        if (NOT_CLEANED) {
            cleanOldFiles(plugin);
            NOT_CLEANED = false;
        }

        if (LATEST_VERSION == null) {
            return;
        }


        final Integer current = convertVersion(plugin.getDescription().getVersion());
        if (current >= convertVersion(LATEST_VERSION)) {
            return;
        }

        if (new File(directory, "ManticLib-" + LATEST_VERSION + ".jar").exists()) {
            return;
        }

        plugin.getLogger().info("Downloading the latest version " + LATEST_VERSION);

        String url = String.format("https://github.com/Mantic-Development/ManticLib/releases/latest/download/ManticLib-%s.jar", LATEST_VERSION);
        File targetFile = new File(directory, String.format("ManticLib-%s.jar", LATEST_VERSION));

        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            targetFile.delete();
            plugin.getLogger().warning("An error occurred while downloading the latest version (" + LATEST_VERSION + ")");
            return;
        }

        plugin.getLogger().info("Downloaded the latest version " + LATEST_VERSION);
        plugin.getLogger().warning("Please restart the server to use the new version");

        try {
            Method getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
            getFileMethod.setAccessible(true);
            File file = (File) getFileMethod.invoke(plugin);
            if (file != null) {
                file.renameTo(new File(file.getAbsolutePath() + ".old"));
            }
        } catch (Throwable ignored) {
        }

    }

    private static String getLatestVersion() {
        try {
            URL url = new URL("https://api.github.com/repos/Mantic-Development/ManticLib/releases/latest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");


            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connection.disconnect();

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
            return jsonObject.get("tag_name").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private static void cleanOldFiles(Plugin plugin) {
        final File folder = new File("plugins");
        if (!folder.isDirectory()) {
            return;
        }
        final Integer current = convertVersion(plugin.getDescription().getVersion());
        for (File f : folder.listFiles()) {
            if (f.isFile()) {
                String lowerCase = f.getName().toLowerCase();

                if (lowerCase.startsWith("manticlib")) {
                    if (lowerCase.endsWith(".jar")) {
                        plugin.getLogger().info("Found jar file: " + f.getName());
                        String version = lowerCase.substring(lowerCase.indexOf("-") + 1, lowerCase.indexOf(".jar"));
                        final Integer oldVersion = convertVersion(version);
                        if (oldVersion != 0) {
                            if (current > oldVersion) {
                                plugin.getLogger().info("Deleting old file " + f.getName());
                                try {
                                    f.delete();
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (lowerCase.endsWith(".old")) {
                        plugin.getLogger().info("Deleting old file " + f.getName());
                        try {
                            f.delete();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static Integer convertVersion(String v) {
        v = v.replaceAll("[^\\d.]", "");
        int version = 0;
        if (v.contains(".")) {
            StringBuilder lVersion = new StringBuilder();
            for (String s : v.split("\\.")) {
                if (s.length() == 1) {
                    s = "0" + s;
                }
                lVersion.append(s);
            }

            if (Utils.isInt(lVersion.toString())) {
                version = Integer.parseInt(lVersion.toString());
            }
        } else {
            if (Utils.isInt(v)) {
                version = Integer.parseInt(v);
            }
        }
        return version;
    }


}
