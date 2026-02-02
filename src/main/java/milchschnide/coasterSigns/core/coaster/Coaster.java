package milchschnide.coasterSigns.core.coaster;

import com.bergerkiller.bukkit.tc.Station.StationConfig;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class Coaster {
    private final String name;
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

    public String name() {
        return name;
    }

    public boolean isTrainInStation() {
        return trainInStation != null;
    }

    public void openGates() {
        //open gates logic
    }

    public void closeGates() {
        //close gates logic
    }

    public void openRestraints(MinecartGroup group) {
        //open restraints logic
    }

    public void closeRestraints(MinecartGroup group) {
        //close restraints logic
    }

    public void launchTrain() {
        if (trainInStation == null) throw new IllegalStateException("Train has not been set");
        trainInStation.getActions().addActionLaunch(direction, stationConfig.getLaunchConfig(), stationConfig.getLaunchSpeed());
    }

    public void startCountDown(MinecartGroup group) {
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

}
