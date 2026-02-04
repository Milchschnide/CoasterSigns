package milchschnide.coasterSigns.core;

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

    public Block(Coaster coaster, int index, Station.StationConfig config) {
        this.coaster = coaster;
        this.index = index;
        this.stationConfig = config;
    }

    public Block init() {
        coaster.addBlock(this);
        return this;
    }

    public boolean isTrainInBlock() {
        return trainOnBlock != null;
    }

    public boolean isTrainWaitingToEnter() {
        return trainWaitingToEnter != null;
    }

    public void launchTrain() {
        if (trainWaitingToEnter == null) throw new IllegalStateException("Train has not been set");
        trainWaitingToEnter.getActions()
                .addActionLaunch(direction, stationConfig.getLaunchConfig(), stationConfig.getLaunchSpeed());
        trainOnBlock = trainWaitingToEnter;
        trainWaitingToEnter = null;
        setPreviousBlockFree();
    }

    public void setPreviousBlockFree() {
        setPreviousBlockFree(this);
    }

    public void setPreviousBlockFree(Block block) {
        Block previous = coaster.getBlocks().stream()
                .filter(b -> b.getIndex() > block.getIndex())
                .min(Comparator.comparingInt(Block::getIndex))
                .orElse(null);
        if (previous == null) return;
        if (previous.isTrainWaitingToEnter()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance,
                    previous::launchTrain, CoasterSigns.defaultPreviousBlockLaunchDelay);
        } else {
            previous.setTrainOnBlock(null);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, () ->
                    setPreviousBlockFree(previous), CoasterSigns.defaultPreviousBlockLaunchDelay);
        }
    }

}
