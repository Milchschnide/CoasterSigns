package milchschnide.coasterSigns.utils;

import com.bergerkiller.bukkit.coasters.signs.power.NamedPowerChannel;
import milchschnide.coasterSigns.CoasterSigns;
import org.bukkit.Bukkit;

public final class PowerStateHandler {

    /**
     * Power the power channel
     *
     * @param powerChannelName the power channel name
     */
    public static void power(String powerChannelName) {
        final NamedPowerChannel powerChannel = CoasterSigns.tcCoasters.findGlobalPowerState(powerChannelName);
        powerChannel.setPowered(true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, () -> {
            powerChannel.setPowered(false);
        }, 10);
    }
}
