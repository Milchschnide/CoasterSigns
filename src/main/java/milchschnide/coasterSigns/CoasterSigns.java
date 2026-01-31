package milchschnide.coasterSigns;

import com.bergerkiller.bukkit.tc.signactions.SignAction;
import milchschnide.coasterSigns.signs.BrakeSign;
import milchschnide.coasterSigns.signs.EnablePhysiksSign;
import milchschnide.coasterSigns.signs.FreeFallReleaseSign;
import milchschnide.coasterSigns.signs.HoldAndLaunchSign;
import milchschnide.coasterSigns.utils.ConfigHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class CoasterSigns extends JavaPlugin {

    public static CoasterSigns instance;

    public static double defaultFriction;

    public static double defaultGravity;

    public static double defaultTravelSpeed;

    public static double defaultBrakeDistance;

    public static double defaultLaunchSpeed;

    public static double defaultLaunchDistance;

    public static double defaultWaitTime;


    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration
        System.out.println("[CoasterSigns] Initializing configuration");
        ConfigHandler.initConfig(instance.getDataFolder());

        // Load default values from configuration
        System.out.println("[CoasterSigns] Loading default configuration values");
        ConfigHandler.loadDefaults();
        System.out.println("[CoasterSigns] Configuration initialized");

        System.out.println("defaultFriction: " + defaultFriction);
        System.out.println("defaultGravity: " + defaultGravity);
        System.out.println("defaultTravelSpeed: " + defaultTravelSpeed);
        System.out.println("defaultBrakeDistance: " + defaultBrakeDistance);
        System.out.println("defaultLaunchSpeed: " + defaultLaunchSpeed);
        System.out.println("defaultLaunchDistance: " + defaultLaunchDistance);
        System.out.println("defaultWaitTime: " + defaultWaitTime);

        // Register sign actions
        System.out.println("[CoasterSigns] Enabled");
        System.out.println("[CoasterSigns] Loading CoasterSigns");
        SignAction.register(new EnablePhysiksSign());
        SignAction.register(new BrakeSign());
        SignAction.register(new HoldAndLaunchSign());
        SignAction.register(new FreeFallReleaseSign());
        System.out.println("[CoasterSigns] Loaded CoasterSigns");
    }
}
