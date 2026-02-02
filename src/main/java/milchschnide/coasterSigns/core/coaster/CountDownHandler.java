package milchschnide.coasterSigns.core.coaster;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import milchschnide.coasterSigns.CoasterSigns;
import org.bukkit.Bukkit;

public class CountDownHandler {
    private final Coaster coaster;
    private boolean isCountingDown = false;
    private int countdownTime;

    public CountDownHandler(Coaster coaster) {
        this.coaster = coaster;
    }

    public void startCountdown(MinecartGroup group) {
        isCountingDown = true;
        countdownTime = CoasterSigns.defaultCountDownTime + 1;
        countdownTick(group);
    }

    private void countdownTick(MinecartGroup group) {
        countdownTime--;
        if(!isCountingDown) return;
        if (countdownTime <= 0) {
            closeGatesAndRestraints(group);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance,() -> countdownTick(group), 20L);
    }

    private void closeGatesAndRestraints(MinecartGroup group) {
        coaster.closeGates();
        coaster.closeRestraints(group);

        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, this::launchTrain, 60L);
    }

    private void launchTrain() {
        coaster.launchTrain();
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, this::stopCountdown, 20L);
    }

    public void stopCountdown() {
        isCountingDown = false;
    }

    public boolean isRunning() {
        return isCountingDown;
    }
}
