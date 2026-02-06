package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.Station;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.properties.standard.type.SlowdownMode;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import milchschnide.coasterSigns.CoasterSigns;
import milchschnide.coasterSigns.core.block.Block;
import milchschnide.coasterSigns.core.coaster.Coaster;
import milchschnide.coasterSigns.core.coaster.CoasterCHACHE;
import milchschnide.coasterSigns.signs.utils.SignUtilsHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BlockSign extends SignAction {
    public static final List<SignActionEvent> posibleBlocks = new ArrayList<>();

    @Override
    public boolean match(SignActionEvent event) {
        if(CoasterSigns.collectblocks) posibleBlocks.add(event);
        return isblockSign(event);
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.GROUP_ENTER)) {
            final String[] line2 = event.getLine(1).split(",");
            final Coaster coaster = CoasterCHACHE.getCoasterCHACHE().stream().filter(coaster1 ->
                    coaster1.name().equals(line2[0])).findFirst().orElse(null);
            //Error handling if coaster or block does not exist, should never happen
            if (coaster == null) {
                final Location location = event.getBlock().getLocation();
                System.out.println("Train entered block of coaster that does not exist! x: "
                        + location.getX() + " y: " + location.getY() + " z: " + location.getZ());
                return;
            }

            final Block block = coaster.getBlocks().stream().filter(block1 -> block1.getIndex() ==
                    Integer.parseInt(line2[1])).findFirst().orElse(null);
            if (block == null) {
                System.out.println("Train entered block that does not exist in coaster "
                        + coaster.name() + "!");
                return;
            }

            final MinecartGroup group = event.getGroup();
            final TrainProperties properties = group.getProperties();

            if(properties.isSlowingDown(SlowdownMode.FRICTION)
            || properties.isSlowingDown(SlowdownMode.GRAVITY)) block.setHasSlowdownBeenEnabled(true);

            // Set the block direction to the station's next direction if it hasn't been set yet
            final Station station = new Station(event);
            block.setDirection(station.getNextDirectionFace());

            // if the last dispatched train enters the fitst block again,
            // reset the last dispatched train to allow further trains to be dispatched from the station
            if(event.getMember() == coaster.getLastDispatchedTrain()) coaster.setLastDispatchedTrain(null);

            if (block.isTrainInBlock()) {
                // If a train is already in the block, set the waiting train
                block.setTrainWaitingToEnter(event.getMember());
                group.getActions().launchReset();
                station.centerTrain();
                properties.setSlowingDown(false);
            } else {
                // If no train is in the block, set the current train as the train in the block and reset the previous block
                block.setTrainOnBlock(event.getMember());
                block.setTrainWaitingToEnter(null);
                block.setPreviousBlockFree();
            }

            // If the train contains a passenger and the coaster has a train in the station, start the station countdown to launch the next train
            if (group.hasPassenger() && coaster.isTrainInStation()) coaster.startCountDown(group, true, 0);
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!initBlock(event, event.getPlayer())) return false;
        return SignBuildOptions.create()
                .setName("Block Sign")
                .setDescription("Defines a block in a coaster")
                .handle(event);
    }

    @Override
    public void loadedChanged(SignActionEvent event, boolean loaded) {
        super.loadedChanged(event, initBlock(event, null));
    }

    /**
     * Checks if the sign is a block sign by comparing the first line of the sign to the names of the coasters in the cache.
     *
     * @param event The SignActionEvent containing the sign information.
     * @return true if the sign is a block sign, false otherwise.
     */
    public boolean isblockSign(SignActionEvent event) {
        return CoasterCHACHE.getCoasterCHACHE().stream().anyMatch(coaster -> event.isType(coaster.name()));
    }

    /**
     * Initializes a block based on the information provided in the sign.
     *
     * @param event  The SignActionEvent containing the sign information.
     * @param player The player who is building the sign, can be null if called from loadedChanged.
     * @return true if the block was successfully initialized, false otherwise.
     */
    private boolean initBlock(SignActionEvent event, Player player) {
        if (event.getLine(1).isEmpty()) {
            SignUtilsHandler.sendMessage(player, " You must specify a caoster and block index on line 3!" +
                    " Example: 'CoasterName,1'");
            return false;
        }
        final String[] line2 = event.getLine(1).split(",");
        if (line2.length != 2) {
            SignUtilsHandler.sendMessage(player, " Invalid format on line 3! Expected format: 'CoasterName,BlockIndex'");
            return false;
        }

        final Station.StationConfig config = new Station.StationConfig();
        config.setLaunchSpeed(CoasterSigns.defaultTravelSpeed);

        final String coasterName = line2[0];

        if (CoasterCHACHE.getCoasterCHACHE().stream().noneMatch(coaster -> coaster.name().equals(coasterName))) {
            SignUtilsHandler.sendMessage(player, " Coaster with name '" + coasterName + "' does not exist!");
            return false;
        }

        final Coaster coaster = CoasterCHACHE.getCoasterCHACHE().stream()
                .filter(coaster1 -> coaster1.name().equals(coasterName)).findFirst().orElseThrow();

        int index;
        try {
            index = Integer.parseInt(line2[1]);
        } catch (NumberFormatException e) {
            SignUtilsHandler.sendMessage(player, " Invalid block index on line 3! It must be a number.");
            return false;
        }
        if(coaster.getBlocks().stream().anyMatch(block -> block.getIndex() == index)) {
            return false;
        }
        if (coaster.getBlocks().stream().anyMatch(block1 -> block1.getIndex() == index)) {
            SignUtilsHandler.sendMessage(player, " Block with index " + index + " already exists in coaster '" +
                    coasterName + "'!");
            return false;
        }

        SignUtilsHandler.checkPosibleLaunch(player, config, event.getLine(2).split(","));

        new Block(coaster, index, config).init();
        return true;
    }

}
