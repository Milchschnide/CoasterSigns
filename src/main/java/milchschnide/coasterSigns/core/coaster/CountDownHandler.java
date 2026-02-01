package milchschnide.coasterSigns.core.coaster;

import milchschnide.coasterSigns.CoasterSigns;
import org.bukkit.Bukkit;

public class CountDownHandler {
    private final Coaster coaster;
    private boolean isCountingDown = false;
    private int countdownTime;

    public CountDownHandler(Coaster coaster) {
        this.coaster = coaster;
    }

    public void startCountdown() {
        countdownTime = CoasterSigns.defaultCountDownTime + 1;
        countdownTick();
    }

    private void countdownTick() {
        countdownTime--;
        if(!isCountingDown) return;
        if (countdownTime <= 0) {
            closeGatesAndRestraints();
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, this::countdownTick, 20L);
    }

    private void closeGatesAndRestraints() {
        coaster.closeGates();
        coaster.closeRestraints();

        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, this::launchTrain, 60L);
    }

    private void launchTrain() {
        coaster.launchTrain();
        isCountingDown = false;
    }

    public void stopCountdown() {
        isCountingDown = false;
    }

    public boolean isRunning() {
        return isCountingDown;
    }
}
