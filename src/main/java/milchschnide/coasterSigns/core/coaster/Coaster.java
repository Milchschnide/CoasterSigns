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

    public Coaster(String name, StationConfig stationConfig, BlockFace direction) {
        this.name = name;
        this.stationConfig = stationConfig;
        this.direction = direction;
    }

    public Coaster init() {
        CoasterCHACHE.addCoaster(this);
        return this;
    }

    public void deleteCoaster() {
        blocks.clear();
        CoasterCHACHE.removeCoaster(this);
    }

    public String name() {
        return name;
    }

    public boolean isTrainInStation() {
        return trainInStation != null;
    }

    public void openGates() {
        if (block == null) return;
        block.setType(Material.TARGET);
    }

    public void closeGates() {
        if (block == null) return;
        block.setType(Material.REDSTONE_BLOCK);
    }

    public void openRestraints(MinecartGroup group) {
        final TrainProperties properties = group.getProperties();
        properties.setPlayersEnter(false);
        properties.setPlayersExit(false);

        group.playNamedAnimation("open_restraints");
    }

    public void closeRestraints(MinecartGroup group) {
        final TrainProperties properties = group.getProperties();
        properties.setPlayersEnter(false);
        properties.setPlayersExit(false);

        group.playNamedAnimation("close_restraints");
    }

    public void launchTrain() {
        if (trainInStation == null) throw new IllegalStateException("Train has not been set");
        trainInStation.getActions()
                .addActionLaunch(direction, stationConfig.getLaunchConfig(), stationConfig.getLaunchSpeed());
        lastDispatchedTrain = trainInStation;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance,
                this::clearPreviousBlocks, CoasterSigns.defaultPreviousBlockLaunchDelay);
        trainInStation = null;
    }

    private void clearPreviousBlocks() {
        final Block previousBlock = blocks.getFirst();
        if (previousBlock == null) return;
        if (previousBlock.isTrainWaitingToEnter()){
            previousBlock.launchTrain();
        } else {
            previousBlock.setPreviousBlockFree();
            previousBlock.setTrainOnBlock(null);
        }
    }

    public void startCountDown(MinecartGroup group, boolean forceStart, int i) {
            if(lastDispatchedTrain != null) {
                countDownHandler.sendActionBarMessage(group,
                        Component.text(CoasterSigns.defaultNextBlockIsOccupiedMessage));
                if(!forceStart) return;
                final int finalI = i++;
                if(finalI > 180) {
                    System.out.println("Countdown stopped for coaster " + name() +
                            " because the next block did not free up in time.");
                    return;
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance,() -> {
                    startCountDown(group, forceStart, finalI);
                });
                return;
            }
        if (!isTrainInStation()) return;
        if(forceStart) {
            countDownHandler.closeGatesAndRestraints(trainInStation.getGroup());
            System.out.println("Force start count down");
            return;
        }
        System.out.println("Start Countdown");
        countDownHandler.startCountdown(group);
    }

    public void stopCountDown() {
        countDownHandler.stopCountdown();
    }

    public void setPassThroughs(int passThroughs) {
        if (passThroughHandler == null) {
            passThroughHandler = new PassThroughHandler(this, passThroughs);
        }
    }

    public void addBlock(Block block) {
        if(blocks.stream().anyMatch(block1 -> block1.getIndex() == block.getIndex())) return;
        blocks.add(block);
        blocks.sort(Comparator.comparingInt(Block::getIndex));
    }

}
