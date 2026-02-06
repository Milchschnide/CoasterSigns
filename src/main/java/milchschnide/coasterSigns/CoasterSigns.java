package milchschnide.coasterSigns;

import com.bergerkiller.bukkit.coasters.TCCoasters;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import lombok.Getter;
import milchschnide.coasterSigns.signs.*;
import milchschnide.coasterSigns.utils.ConfigHandler;
import milchschnide.coasterSigns.utils.MineCartRemovalHandler;
import milchschnide.coasterSigns.utils.PowerStateHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class CoasterSigns extends JavaPlugin {

    public static CoasterSigns instance;

    public static TCCoasters tcCoasters;

    public static World world;

    public static double defaultFriction;

    public static double defaultGravity;

    public static double defaultTravelSpeed;

    public static double defaultBrakeDistance;

    public static double defaultLaunchSpeed;

    public static double defaultLaunchDistance;

    public static double defaultWaitTime;

    public static int defaultCountDownTime;

    public static int defaultPreviousBlockLaunchDelay;

    public static String defaultCountDownMessagePartOne;

    public static String defaultCountDownMessagePartTwo;

    public static String defaultAnnouncementMessage;

    public static String defaultNextBlockIsOccupiedMessage;

    public static StationSign stationSign = new StationSign();

    public static BlockSign blockSign = new BlockSign();

    public static boolean collectblocks = true;

    @Override
    public void onEnable() {
        tcCoasters = TCCoasters.getPlugin(TCCoasters.class);

        System.out.println("Load Blocks");
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> {
            BlockSign.posibleBlocks.forEach(block -> {
                if (!blockSign.isblockSign(block)) return;
                blockSign.loadedChanged(block, false);
            });
        }, 5);
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> collectblocks = false, 40);
        System.out.println("Loaded Blocks");


        world = Bukkit.getWorld("world");
        if (world == null) {
            System.out.println("World 'world' not found! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register sign actions
        System.out.println("Loading CoasterSigns");
        SignAction.register(new EnablePhysiksSign());
        SignAction.register(new BrakeSign());
        SignAction.register(new HoldAndLaunchSign());
        SignAction.register(new FreeFallReleaseSign());
        SignAction.register(stationSign);
        SignAction.register(blockSign);
        System.out.println("Loaded CoasterSigns");

        System.out.println("spawn trains in 3 seconds");
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> PowerStateHandler.power("spawnTrains"),60);
    }

    @Override
    public void onLoad() {
        instance = this;
        // Queue initialisieren

        // Initialize configuration
        System.out.println("Initializing configuration");
        ConfigHandler.initConfig(instance.getDataFolder());

        // Load default values from configuration
        System.out.println("Loading default configuration values");
        ConfigHandler.loadDefaults();
        System.out.println("Configuration initialized");

        System.out.println("Pre-loading StationSign");
        SignAction.register(stationSign);
        SignAction.register(blockSign);
        System.out.println("Pre-loaded StationSign");
    }

    @Override
    public void onDisable() {
        SignAction.unregister(stationSign);
        SignAction.unregister(blockSign);
        MineCartRemovalHandler.init();
    }

}