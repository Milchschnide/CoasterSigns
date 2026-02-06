package milchschnide.coasterSigns.core.coaster;

import com.bergerkiller.bukkit.tc.Station.StationConfig;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import lombok.Getter;
import lombok.Setter;
import milchschnide.coasterSigns.CoasterSigns;
import milchschnide.coasterSigns.core.block.Block;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Coaster {
    private final String name;
    @Getter
    private final List<Block> blocks = new ArrayList<>();
    private final StationConfig stationConfig;
    @Getter
    @Setter
    private BlockFace direction;
    @Getter
    private final CountDownHandler countDownHandler = new CountDownHandler(this);
    private PassThroughHandler passThroughHandler = null;
    @Setter
    private MinecartMember<?> trainInStation;
    @Setter
    @Getter
    private MinecartMember<?> lastDispatchedTrain;
    private boolean gatesClosed = false;
    @Setter
    private org.bukkit.block.Block block;
    private boolean restraintsClosed = false;
    @Setter
    private boolean slowdownWhenEnter = false;
    @Setter
    private boolean slowdownWhenExit = false;
    @Getter
    @Setter
    private boolean cooldown = false;

    public Coaster(String name, StationConfig stationConfig, BlockFace direction) {
        this.name = name;
        this.stationConfig = stationConfig;
        this.direction = direction;
    }

    /**
     * Initializes the coaster by adding it to the global coaster cache.
     *
     * @return The initialized coaster.
     */
    public Coaster init() {
        System.out.println("Initializing coaster " + name);
        CoasterCACHE.addCoaster(this);
        return this;
    }

    /**
     * Deletes the coaster by clearing its blocks and removing it from the global coaster cache.
     */
    public void deleteCoaster() {
        blocks.clear();
        CoasterCACHE.removeCoaster(this);
    }

    /**
     * Gets the name of the coaster.
     *
     * @return The name of the coaster.
     */
    public String name() {
        return name;
    }

    /**
     * Checks if there is a train currently in the station.
     *
     * @return true if a train is in the station, false otherwise.
     */
    public boolean isTrainInStation() {
        return trainInStation != null;
    }

    /**
     * Opens the station gates by changing the block type to TARGET.
     */
    public void openGates() {
        if (block == null) return;
        block.setType(Material.TARGET);
    }

    /**
     * Closes the station gates by changing the block type to REDSTONE_BLOCK.
     */
    public void closeGates() {
        if (block == null) return;
        block.setType(Material.REDSTONE_BLOCK);
    }

    /**
     * Opens the restraints of the given minecart group and prevents players from entering or exiting.
     *
     * @param group The minecart group whose restraints are to be opened.
     */
    public void openRestraints(MinecartGroup group) {
        final TrainProperties properties = group.getProperties();
        properties.setPlayersEnter(false);
        properties.setPlayersExit(false);

        group.playNamedAnimation("open");
    }

    /**
     * Closes the restraints of the given minecart group and prevents players from entering or exiting.
     *
     * @param group The minecart group whose restraints are to be closed.
     */
    public void closeRestraints(MinecartGroup group) {
        final TrainProperties properties = group.getProperties();
        properties.setPlayersEnter(false);
        properties.setPlayersExit(false);

        group.playNamedAnimation("close");
    }

    /**
     * Launches the train currently in the station.
     * automatically clears previous blocks after a delay.
     *
     * @throws IllegalStateException if no train is set in the station.
     */
    public void launchTrain() {
        if (trainInStation == null) return;
        trainInStation.getActions()
                .addActionLaunch(direction, stationConfig.getLaunchConfig(), stationConfig.getLaunchSpeed());
        if(!blocks.isEmpty()) lastDispatchedTrain = trainInStation;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance,
                this::clearPreviousBlocks, CoasterSigns.defaultPreviousBlockLaunchDelay);
        trainInStation.getGroup().getProperties().setSlowingDown(slowdownWhenExit);
        trainInStation = null;
    }

    /**
     * Clears the previous blocks by launching any train waiting to enter
     * or setting the previous block free if a train is on it.
     */
    private void clearPreviousBlocks() {
        Block previousBlock = null;
        if (!blocks.isEmpty()) previousBlock = blocks.getFirst();
        if (previousBlock == null) return;
        if (previousBlock.isTrainWaitingToEnter()) {
            previousBlock.launchTrain();
        } else {
            previousBlock.setPreviousBlockFree();
            previousBlock.setTrainOnBlock(null);
        }
    }

    /**
     * Starts the countdown for the given minecart group.
     *
     * @param group      The minecart group for which to start the countdown.
     * @param forceStart Whether to force start the countdown even if the next block is occupied.
     * @param i          The current iteration count for checking block occupancy.
     */
    public void startCountDown(MinecartGroup group, boolean forceStart, int i) {
        if (lastDispatchedTrain != null) {
            countDownHandler.sendActionBarMessage(group,
                    Component.text(CoasterSigns.defaultNextBlockIsOccupiedMessage));
            if (!forceStart) return;
            final int finalI = i++;
            if (finalI > 180) {
                System.out.println("Countdown stopped for coaster " + name() +
                        " because the next block did not free up in time.");
                return;
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, () -> {
                startCountDown(group, forceStart, finalI);
            },20);
            return;
        }
        if (!isTrainInStation()) return;
        if (forceStart) {
            countDownHandler.closeGatesAndRestraints(trainInStation.getGroup());
            return;
        }
        countDownHandler.startCountdown(group);
    }

    /**
     * Stops the countdown if it is running.
     */
    public void stopCountDown() {
        countDownHandler.stopCountdown();
    }

    /**
     * Sets the number of pass-throughs for the coaster.
     *
     * @param passThroughs The number of pass-throughs to set.
     */
    public void setPassThroughs(int passThroughs) {
        if (passThroughHandler == null) {
            passThroughHandler = new PassThroughHandler(passThroughs);
        }
    }

    /**
     * Adds a pass-through to the coaster. If the coaster has reached the maximum number of pass-throughs, it resets the count.
     * This method also implements a cooldown to prevent rapid pass-throughs.
     */
    public void addPassThrough() {
        if (passThroughHandler == null) return;
        passThroughHandler.addPassThrough();
    }

    /**
     * Checks if the coaster can pass through the station without triggering the station's effects.
     *
     * @return true if the coaster can pass through, false otherwise.
     */
    public boolean passThroughStation() {
        if (passThroughHandler == null) return false;
        return passThroughHandler.passThroughStation();
    }

    /**
     * Adds a block to the coaster if it does not already exist.
     *
     * @param block The block to add.
     */
    public void addBlock(Block block) {
        if (blocks.stream().anyMatch(block1 -> block1.getIndex() == block.getIndex())) return;
        blocks.add(block);
        blocks.sort(Comparator.comparingInt(Block::getIndex));
    }

    /**
     * @return true if slowdown should be activated when a train exits the station, false otherwise.
     */
    public boolean slowdownWhenEnter() {
        return slowdownWhenEnter;
    }

}
