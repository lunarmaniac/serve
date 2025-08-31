package com.github.dorocha.serve;

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

public final class Serve extends JavaPlugin {

    private HttpServer server;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int port = getConfig().getInt("port", 8080);
        String filePath = getConfig().getString("file", "resourcepack.zip");

        File resourcePack = new File(filePath);

        if (!resourcePack.exists()) {
            getLogger().warning("The resource pack file does not exist: " + resourcePack.getAbsolutePath());
            getLogger().warning("Please place the resource pack in the correct location and restart the server.");
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/resourcepack.zip", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    byte[] bytes = Files.readAllBytes(resourcePack.toPath());
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                }
            });
            server.start();

            InetAddress localAddress = InetAddress.getLocalHost();
            String ip = localAddress.getHostAddress();

            getLogger().info("");
            getLogger().info("Resource Pack Web Server is now running!");
            getLogger().info("Hosting: " + resourcePack.getAbsolutePath());
            getLogger().info("Suggested server.properties line:");
            getLogger().info("resource-pack=http://" + ip + ":" + port + "/resourcepack.zip");
            getLogger().info("");


        } catch (IOException e) {
            getLogger().severe("Failed to start the resource pack server:");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop(0);
            getLogger().info("Resource Pack Web Server stopped.");
        }
    }
}
