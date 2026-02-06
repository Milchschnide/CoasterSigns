package milchschnide.coasterSigns.utils;

import lombok.Getter;
import milchschnide.coasterSigns.CoasterSigns;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class ConfigHandler {

    @Getter
    private static YamlConfiguration configuration;

    /**
     * Initializes the configuration file.
     *
     * @param dataFolder the data folder of the plugin
     */
    public static void initConfig(@Nonnull File dataFolder) {
        final File configPath = new File(dataFolder.getPath(), "settings.yml");
        if (configPath.exists()) {
            configuration = YamlConfiguration.loadConfiguration(configPath);
            return;
        }
        configuration = createConfig(configPath);
        if (configuration != null) return;
        CoasterSigns.instance.getLogger().log(Level.SEVERE, () -> "[CoasterSigns] Konnte die settings.yml Datei nicht erstellen!");
        Bukkit.getPluginManager().disablePlugin(CoasterSigns.instance);
    }

    /**
     * Creates the configuration file.
     *
     * @param configPath the path to the configuration file
     * @return the created configuration
     */
    private static YamlConfiguration createConfig(@Nonnull File configPath) {
        final YamlConfiguration conf = new YamlConfiguration();
        conf.addDefault("defaults.friction", 0.3);
        conf.addDefault("defaults.gravity", 1.5);
        conf.addDefault("defaults.brakes.speed", 14);
        conf.addDefault("defaults.brakes.distance", 15);
        conf.addDefault("defaults.launch.speed", 50);
        conf.addDefault("defaults.launch.distance", 20);
        conf.addDefault("defaults.launch.waitTime", 3);
        conf.addDefault("defaults.station.countDownTime", 10);
        conf.addDefault("defaults.station.countDown.messagePartOne", "The train will depart in");
        conf.addDefault("defaults.station.countDown.messagePartTwo", "second/s");
        conf.addDefault("defaults.station.announcement.message", "Have a nice ride!");
        conf.addDefault("defaults.station.announcement.nextBlockIsOccupied", "Next block is still occupied.");
        conf.addDefault("defaults.block.previousBlockLaunchDelay", 1);
        conf.options().copyDefaults(true);
        try {
            conf.save(configPath);
        } catch (IOException ex) {
            return null;
        }
        return conf;
    }

    /**
     * Loads the default values from the configuration into the CoasterSigns class.
     */
    public static void loadDefaults() {
        CoasterSigns.defaultFriction = configuration.getDouble("defaults.friction");
        CoasterSigns.defaultGravity = configuration.getDouble("defaults.gravity");
        CoasterSigns.defaultTravelSpeed = (configuration.getDouble("defaults.brakes.speed") / 3.6) / 20;
        CoasterSigns.defaultBrakeDistance = configuration.getDouble("defaults.brakes.distance");
        CoasterSigns.defaultLaunchSpeed = (configuration.getDouble("defaults.launch.speed") / 3.6) / 20;
        CoasterSigns.defaultLaunchDistance = configuration.getDouble("defaults.launch.distance");
        CoasterSigns.defaultWaitTime = configuration.getDouble("defaults.launch.waitTime");
        CoasterSigns.defaultCountDownTime = configuration.getInt("defaults.station.countDownTime");
        CoasterSigns.defaultCountDownMessagePartOne = configuration.getString("defaults.station.countDown.messagePartOne");
        CoasterSigns.defaultCountDownMessagePartTwo = configuration.getString("defaults.station.countDown.messagePartTwo");
        CoasterSigns.defaultAnnouncementMessage = configuration.getString("defaults.station.announcement.message");
        CoasterSigns.defaultNextBlockIsOccupiedMessage = configuration.getString("defaults.station.announcement.nextBlockIsOccupied");
        CoasterSigns.defaultPreviousBlockLaunchDelay = configuration.getInt("defaults.block.previousBlockLaunchDelay") * 20;
    }

}
