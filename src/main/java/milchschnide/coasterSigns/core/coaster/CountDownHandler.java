package milchschnide.coasterSigns.core.coaster;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import milchschnide.coasterSigns.CoasterSigns;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CountDownHandler {
    private final Coaster coaster;
    private boolean isCountingDown = false;
    private boolean isForcedToStop = false;
    private int countdownTime;

    public CountDownHandler(Coaster coaster) {
        this.coaster = coaster;
    }

    /**
     * Starts the countdown for the given minecart group.
     * If there is a train currently on the next block, it will wait until the block is free before starting the countdown.
     *
     * @param group The minecart group for which to start the countdown.
     */
    public void startCountdown(MinecartGroup group) {
        isCountingDown = true;
        countdownTime = CoasterSigns.defaultCountDownTime + 1;
        if (coaster.getLastDispatchedTrain() != null) {
            waitTillNextBlockIsFree(group, 0);
            return;
        }
        countdownTick(group);
    }

    /**
     * Waits until the next block is free before starting the countdown. If the block does not free up within a certain time, it will stop waiting and open the gates and restraints.
     *
     * @param group         The minecart group for which to wait.
     * @param maxTimeToWait The maximum time to wait in seconds.
     */
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

    /**
     * Handles each tick of the countdown,
     * sending action bar messages to the passengers and checking if the countdown has reached zero to close gates and restraints.
     *
     * @param group The minecart group for which to handle the countdown tick.
     */
    private void countdownTick(MinecartGroup group) {
        countdownTime--;
        sendActionBarMessage(group, getCountdownMessage(countdownTime));
        if (!isCountingDown || isForcedToStop) return;
        if (countdownTime <= 0) {
            closeGatesAndRestraints(group);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, () -> countdownTick(group), 20L);
    }

    // Generates the countdown message to be sent to the passengers, including the time left and the appropriate singular/plural form of "second".
    private Component getCountdownMessage(int timeLeft) {
        final String[] text = CoasterSigns.defaultCountDownMessagePartTwo.split("/");
        return Component.text(CoasterSigns.defaultCountDownMessagePartOne + " " + timeLeft + " "
                + (timeLeft < 2 ? text[0] : text[0] + text[1]));
    }

    /**
     * Sends an action bar message to all passengers of the minecart group.
     *
     * @param group     The minecart group to which the message should be sent.
     * @param component The message component to send.
     */
    public void sendActionBarMessage(MinecartGroup group, Component component) {
        group.forEach((minecartMember) -> minecartMember.getEntity().getPassengers()
                .forEach(passenger -> {
                    if (!(passenger instanceof Player)) return;
                    passenger.sendActionBar(component);
                }));
    }

    /**
     * Closes the gates and restraints for the given minecart group, sends an announcement message, and schedules the train to launch after a short delay.
     *
     * @param group The minecart group for which to close gates and restraints.
     */
    public void closeGatesAndRestraints(MinecartGroup group) {
        coaster.closeGates();
        coaster.closeRestraints(group);

        isForcedToStop = true;

        sendActionBarMessage(group, Component.text(CoasterSigns.defaultAnnouncementMessage));

        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, this::launchTrain, 60L);
    }

    /**
     * Launches the train currently in the station and schedules the countdown to stop after a short delay.
     *
     * @throws IllegalStateException if no train is set in the station.
     */
    private void launchTrain() {
        coaster.launchTrain();
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, this::stopCountdown, 20L);
    }

    /**
     * Stops the countdown, allowing gates and restraints to be opened again and preventing any further countdown ticks or waiting for the next block to free up.
     */
    public void stopCountdown() {
        isCountingDown = false;
        isForcedToStop = false;
    }

    /**
     * Checks if the countdown is currently running.
     *
     * @return true if the countdown is running, false otherwise.
     */
    public boolean isRunning() {
        return isCountingDown;
    }
}
