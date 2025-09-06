package com.github.dorocha.serve;

import com.github.dorocha.serve.commands.ReloadCommand;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class Serve extends JavaPlugin {

    private HttpServer server;
    private File resourcePack;

    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private int port;
    private String bindAddress;
    private String filePath;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();
        startServer();

        this.getCommand("serve").setExecutor(new ReloadCommand(this));
    }

    @Override
    public void onDisable() {
        stopServer();
    }

    public void loadConfigValues() {
        port = getConfig().getInt("port", 8080);
        filePath = getConfig().getString("file", "resourcepack.zip");
        bindAddress = getConfig().getString("bind-address", "0.0.0.0");
        resourcePack = new File(filePath);
    }

    public void startServer() {
        stopServer();

        if (!resourcePack.exists()) {
            getLogger().warning(RED + "The resource pack file does not exist: " + resourcePack.getAbsolutePath() + RESET);
            getLogger().warning(RED + "Please place the resource pack in the correct location." + RESET);
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(bindAddress, port), 0);
            server.createContext("/resourcepack.zip", new ResourcePackHandler());
            server.start();

            String ip = InetAddress.getLocalHost().getHostAddress();
            String sha1 = calculateSHA1(resourcePack);

            getLogger().info("");
            getLogger().info(GREEN + "Resource Pack Web Server is now running!" + RESET);
            getLogger().info(YELLOW + "Hosting: " + resourcePack.getAbsolutePath() + RESET);
            getLogger().info("Suggested server.properties lines:");
            getLogger().info(YELLOW + "resource-pack=http://" + ip + ":" + port + "/resourcepack.zip" + RESET);
            if (sha1 != null) {
                getLogger().info(YELLOW + "resource-pack-sha1=" + sha1 + RESET);
            }
            getLogger().info("");

        } catch (IOException e) {
            getLogger().severe(RED + "Failed to start the resource pack server:" + RESET);
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            getLogger().info(GREEN + "Resource Pack Web Server stopped." + RESET);
        }
    }

    private class ResourcePackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!resourcePack.exists()) {
                String response = "Resource pack not found.";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            try {
                byte[] bytes = Files.readAllBytes(resourcePack.toPath());
                exchange.getResponseHeaders().add("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, bytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (IOException e) {
                getLogger().severe(RED + "Error serving resource pack: " + e.getMessage() + RESET);
                String response = "Internal server error.";
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    private String calculateSHA1(File file) {
        try {
            MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] hashBytes = sha1Digest.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format(Locale.ROOT, "%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            getLogger().warning(RED + "Failed to calculate SHA-1 for resource pack: " + e.getMessage() + RESET);
            return null;
        }
    }
}
