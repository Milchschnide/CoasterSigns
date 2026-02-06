package milchschnide.coasterSigns.core.block;

import com.bergerkiller.bukkit.tc.Station;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import lombok.Getter;
import lombok.Setter;
import milchschnide.coasterSigns.CoasterSigns;
import milchschnide.coasterSigns.core.coaster.Coaster;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;

import java.util.Comparator;

public class Block {
    private final Coaster coaster;
    @Getter
    private final int index;
    private final Station.StationConfig stationConfig;
    @Setter
    @Getter
    private MinecartMember<?> trainOnBlock = null;
    @Setter
    @Getter
    private MinecartMember<?> trainWaitingToEnter = null;
    @Setter
    private BlockFace direction;
    @Setter
    @Getter
    private boolean hasSlowdownBeenEnabled = false;

    public Block(Coaster coaster, int index, Station.StationConfig config) {
        this.coaster = coaster;
        this.index = index;
        this.stationConfig = config;
    }

    /**
     * Initializes the block by adding it to the coaster's block list.
     *
     * @return The initialized block.
     */
    public Block init() {
        coaster.addBlock(this);
        return this;
    }

    /**
     * Checks if there is a train currently on the block.
     *
     * @return true if a train is on the block, false otherwise.
     */
    public boolean isTrainInBlock() {
        return trainOnBlock != null;
    }

    /**
     * Checks if there is a train waiting to enter the block.
     *
     * @return true if a train is waiting to enter, false otherwise.
     */
    public boolean isTrainWaitingToEnter() {
        return trainWaitingToEnter != null;
    }

    /**
     * Launches the train that is waiting to enter the block.
     * this automatically sets the previous block to free after launching.
     *
     * @throws IllegalStateException if no train is set to enter.
     */
    public void launchTrain() {
        if (trainWaitingToEnter == null) throw new IllegalStateException("Train has not been set");
        trainWaitingToEnter.getActions()
                .addActionLaunch(direction, stationConfig.getLaunchConfig(), stationConfig.getLaunchSpeed());
        trainOnBlock = trainWaitingToEnter;
        if(hasSlowdownBeenEnabled) trainOnBlock.getGroup().getProperties().setSlowingDown(true);
        trainWaitingToEnter = null;
        setPreviousBlockFree();
    }

    /**
     * Sets the previous block in the coaster to free.
     * If the previous block has a train waiting to enter, it launches that train after a delay.
     * If the previous block has a train on it, it clears that train after a delay and recursively checks the block before it.
     */
    public void setPreviousBlockFree() {
        setPreviousBlockFree(this);
    }

    /**
     * Helper method to set the previous block free recursively.
     *
     * @param block The current block to check for the previous block.
     */
    public void setPreviousBlockFree(Block block) {
        Block previous = coaster.getBlocks().stream()
                .filter(b -> b.getIndex() > block.getIndex())
                .min(Comparator.comparingInt(Block::getIndex))
                .orElse(null);
        if (previous == null) return;
        if (previous.isTrainWaitingToEnter()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance,
                    previous::launchTrain, CoasterSigns.defaultPreviousBlockLaunchDelay);
            System.out.println("Previous block launched train after delay.");
        } else {
            previous.setTrainOnBlock(null);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, () ->
                    setPreviousBlockFree(previous), CoasterSigns.defaultPreviousBlockLaunchDelay);
            System.out.println("Previous block cleared after delay.");
        }
    }

}
