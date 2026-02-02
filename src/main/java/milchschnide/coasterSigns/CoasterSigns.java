package milchschnide.coasterSigns;

import com.bergerkiller.bukkit.tc.signactions.SignAction;
import milchschnide.coasterSigns.signs.*;
import milchschnide.coasterSigns.utils.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLOutput;

public final class CoasterSigns extends JavaPlugin {

    public static CoasterSigns instance;

    public static World world;

    public static double defaultFriction;

    public static double defaultGravity;

    public static double defaultTravelSpeed;

    public static double defaultBrakeDistance;

    public static double defaultLaunchSpeed;

    public static double defaultLaunchDistance;

    public static double defaultWaitTime;

    public static int defaultCountDownTime;

    public static StationSign stationSign = new StationSign();


    @Override
    public void onEnable() {
        instance = this;
        world = Bukkit.getWorld("world");
        if(world == null) {
            System.out.println("World 'world' not found! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize configuration
        System.out.println("Initializing configuration");
        ConfigHandler.initConfig(instance.getDataFolder());

        // Load default values from configuration
        System.out.println("Loading default configuration values");
        ConfigHandler.loadDefaults();
        System.out.println("Configuration initialized");

        // Register sign actions
        System.out.println("Enabled");
        System.out.println("Loading CoasterSigns");
        SignAction.register(new EnablePhysiksSign());
        SignAction.register(new BrakeSign());
        SignAction.register(new HoldAndLaunchSign());
        SignAction.register(new FreeFallReleaseSign());
        System.out.println("Loaded CoasterSigns");

        //TODO: Experimental
        SignAction.register(stationSign);
    }

    @Override
    public void onLoad() {
        SignAction.register(stationSign);
    }

    @Override
    public void onDisable() {
        SignAction.unregister(stationSign);
    }
}
