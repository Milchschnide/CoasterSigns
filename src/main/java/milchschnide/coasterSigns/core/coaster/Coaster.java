package milchschnide.coasterSigns.core.coaster;

import com.bergerkiller.bukkit.tc.Station.StationConfig;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import lombok.Getter;
import lombok.Setter;
import milchschnide.coasterSigns.core.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class Coaster {
    private final String name;
    private final List<Block> blocks = new ArrayList<>();
    private final StationConfig stationConfig;
    private final BlockFace direction;
    @Getter
    private final CountDownHandler countDownHandler = new CountDownHandler(this);
    @Setter
    private MinecartMember<?> trainInStation;
    private boolean gatesClosed = false;
    private boolean restraintsClosed = false;

    public Coaster(String name, StationConfig stationConfig, BlockFace direction) {
        this.name = name;
        this.stationConfig = stationConfig;
        this.direction = direction;
    }

    public String name() {
        return name;
    }

    public boolean isTrainInStation() {
        return trainInStation != null;
    }

    public void closeGates() {
        //close gates logic
    }

    public void closeRestraints() {
        //close restraints logic
    }

    public void launchTrain() {
        if (trainInStation == null) throw new IllegalStateException("Train has not been set");
        trainInStation.getActions().addActionLaunch(direction, stationConfig.getLaunchConfig(),stationConfig.getLaunchSpeed());
    }

}
