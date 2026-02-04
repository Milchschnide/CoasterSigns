package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.Station;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import milchschnide.coasterSigns.CoasterSigns;
import milchschnide.coasterSigns.core.Block;
import milchschnide.coasterSigns.core.coaster.Coaster;
import milchschnide.coasterSigns.core.coaster.CoasterCHACHE;
import milchschnide.coasterSigns.signs.utils.SignUtilsHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BlockSign extends SignAction {
    @Override
    public boolean match(SignActionEvent event) {
        return CoasterCHACHE.getCoasterCHACHE().stream().anyMatch(coaster -> event.isType(coaster.name()));
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.GROUP_ENTER)) {
            final String[] line2 = event.getLine(1).split(",");
            final Coaster coaster = CoasterCHACHE.getCoasterCHACHE().stream().filter(coaster1 ->
                    coaster1.name().equals(line2[0])).findFirst().orElse(null);
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

            final Station station = new Station(event);
            block.setDirection(station.getNextDirectionFace());

            if (block.isTrainInBlock()) {
                block.setTrainWaitingToEnter(event.getMember());
                group.getActions().launchReset();
                station.centerTrain();
            } else {
                block.setTrainOnBlock(event.getMember());
                block.setTrainWaitingToEnter(null);
                block.setPreviousBlockFree();
            }

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

        System.out.println("Creating block for coaster: " + coaster.name() + " with index: " + line2[1]);

        int index;
        try {
            index = Integer.parseInt(line2[1]);
        } catch (NumberFormatException e) {
            SignUtilsHandler.sendMessage(player, " Invalid block index on line 3! It must be a number.");
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
