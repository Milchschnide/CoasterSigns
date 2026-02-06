package milchschnide.coasterSigns.core.coaster;

import milchschnide.coasterSigns.CoasterSigns;
import org.bukkit.Bukkit;

public class PassThroughHandler {
    private final int passThroughs;
    private int currentPassThroughs;
    private boolean isCooledDown = true;

    public PassThroughHandler(int passThroughs) {
        this.passThroughs = passThroughs;
        currentPassThroughs = passThroughs;
    }

    /**
     * Adds a pass-through to the coaster. If the coaster has reached the maximum number of pass-throughs, it resets the count.
     * This method also implements a cooldown to prevent rapid pass-throughs.
     */
    public void addPassThrough() {
        if(!isCooledDown) return;
        Bukkit.getScheduler().runTaskLater(CoasterSigns.instance,() -> isCooledDown = true,20L);
        isCooledDown = false;
        currentPassThroughs++;
        if (currentPassThroughs > passThroughs) {
            currentPassThroughs = 0;
        }
    }

    /**
     * Checks if the coaster can pass through the station without triggering the station's effects.
     *
     * @return true if the coaster can pass through, false otherwise.
     */
    public boolean passThroughStation() {
        return currentPassThroughs != 0;
    }
}
