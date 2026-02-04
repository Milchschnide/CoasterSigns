package milchschnide.coasterSigns.core.coaster;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import milchschnide.coasterSigns.CoasterSigns;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        if (coaster.getLastDispatchedTrain() != null) {
            waitTillNextBlockIsFree(group, 0);
            return;
        }
        countdownTick(group);
    }

    private void waitTillNextBlockIsFree(MinecartGroup group, int maxTimeToWait) {
        int finalMaxTimeToWait = maxTimeToWait++;
        if (finalMaxTimeToWait >= 180) {
            System.out.println("Countdown stopped for coaster " + coaster.name() +
                    " because the next block did not free up in time.");
            coaster.openRestraints(group);
            coaster.openGates();
            stopCountdown();
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, () -> {
            if (!isCountingDown) return;
            if (coaster.getLastDispatchedTrain() != null) {
                waitTillNextBlockIsFree(group, finalMaxTimeToWait);
            } else {
                countdownTick(group);
            }
        }, 20L);
    }

    private void countdownTick(MinecartGroup group) {
        countdownTime--;
        sendActionBarMessage(group, getCountdownMessage(countdownTime));
        if (!isCountingDown) return;
        if (countdownTime <= 0) {
            closeGatesAndRestraints(group);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, () -> countdownTick(group), 20L);
    }

    private Component getCountdownMessage(int timeLeft) {
        return Component.text(CoasterSigns.defaultCountDownMessagePartOne + timeLeft
                + (timeLeft < 2
                ? CoasterSigns.defaultCountDownMessagePartTwo.split("/")[0]
                : CoasterSigns.defaultCountDownMessagePartTwo));
    }

    public void sendActionBarMessage(MinecartGroup group, Component component) {
        group.forEach((minecartMember) -> minecartMember.getEntity().getPassengers()
                .forEach(passenger -> {
                    if (!(passenger instanceof Player)) return;
                    passenger.sendActionBar(component);
                }));
    }

    public void closeGatesAndRestraints(MinecartGroup group) {
        coaster.closeGates();
        coaster.closeRestraints(group);

        sendActionBarMessage(group, Component.text(CoasterSigns.defaultAnnouncementMessage));

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
