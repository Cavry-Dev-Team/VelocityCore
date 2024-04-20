package dev.necr.velocitycore;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.necr.velocitycore.dependency.DependencyManager;
import dev.necr.velocitycore.utils.MiniMessageUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Plugin(
        id = "velocitycore",
        name = "VelocityCore",
        version = BuildConstants.VERSION,
        description = "Velocity Utility Plugin.",
        authors = { "necr" },
        dependencies = {
                @Dependency(id = "luckperms", optional = false)
        }
)
public class VelocityCore {

    @Getter
    private static VelocityCore instance;

    @Getter
    private final ProxyServer server;
    @Getter
    private final Logger logger;
    @Getter
    private final Path dataDirectory;

    /*
     * Constructor
     */
    @Inject
    public VelocityCore(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        long millis = System.currentTimeMillis();

        instance = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        logger.info("VelocityCore v" + BuildConstants.VERSION + " construction process done in {}ms.", System.currentTimeMillis() - millis);
    }

    /*
     * Plugin initialization
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        long millis = System.currentTimeMillis();

        try {
            loadDependencies();
        } catch (IOException e) {
            logger.error("Failed to load dependencies", e);
        }

        startupMessage();
        logger.info("VelocityCore v" + BuildConstants.VERSION + " initialization process done in {}ms.", System.currentTimeMillis() - millis);
    }

    /**
     * Downloads and/or loads dependencies
     */
    private void loadDependencies() throws IOException {
        long millis = System.currentTimeMillis();

        this.getLogger().info("Loading and injecting dependencies...");
        this.getLogger().info("(If this is the first time it may take a while to download all the dependencies)");
        Map<String, String> dependencyMap = new HashMap<>();

        try (InputStream stream = VelocityCore.class.getClassLoader().getResourceAsStream("dependencies.json")) {
            if (stream == null) {
                logger.error("Failed to find dependencies.json");
                return;
            }

            try (InputStreamReader reader = new InputStreamReader(stream)) {
                JsonArray dependencies = JsonParser.parseReader(reader).getAsJsonArray();

                if (dependencies.isEmpty()) {
                    return;
                }

                for (JsonElement element : dependencies) {
                    JsonObject dependency = element.getAsJsonObject();
                    dependencyMap.put(
                            dependency.get("name").getAsString(),
                            dependency.get("url").getAsString()
                    );
                }
            } catch (Exception e) {
                logger.error("Failed to parse dependencies.json", e);
            }
        }

        DependencyManager dependencyManager = new DependencyManager(VelocityCore.class);
        Path dirPath = Paths.get(dataDirectory + "/libs");
        Files.createDirectories(dirPath);
        try {
            dependencyManager.download(dependencyMap, dirPath);
            dependencyManager.loadDir(dirPath);
        } catch (IOException e) {
            logger.error("Failed to load dependencies", e);
        }

        this.getLogger().info("Loaded/injected and/or downloaded all dependencies in {}ms!", System.currentTimeMillis() - millis);
    }

    private void startupMessage() {
        MiniMessage miniMessage = MiniMessageUtil.getMiniMessage();

        String message = "<gradient:blue:white>" +
                " __     __         _               _____                        \n" +
                " \\ \\   / /        | |             / ____|                      \n" +
                "  \\ \\_/ /__  _   _| | ___  _ __  | |     ___  _ __ ___          \n" +
                "   \\   / _ \\| | | | |/ _ \\| '__| | |    / _ \\| '__/ _ \\    \n" +
                "    | | (_) | |_| | | (_) | |    | |___| (_) | | |  __/         \n" +
                "    |_|\\___/ \\__,_|_|\\___/|_|     \\_____\\___/|_|  \\___|  \n" +
                "                                                         \n" +
                "                                                         \n" +
                "</gradient>" +
                "\n" +
                "    <blue>Velocity Core <green>v" + BuildConstants.VERSION + " <yellow>by <blue>" +  Arrays.stream(VelocityCore.class.getAnnotation(Plugin.class).authors()).findFirst().orElse("Unknown") +
                "\n" +
                "    <blue>Running on " + server.getVersion();

        Component component = miniMessage.deserialize(message);
        server.getConsoleCommandSource().sendMessage(component);
    }
}
