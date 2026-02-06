package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.Station;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.signactions.TrainCartsSignAction;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import milchschnide.coasterSigns.CoasterSigns;
import milchschnide.coasterSigns.core.coaster.Coaster;
import milchschnide.coasterSigns.core.coaster.CoasterCACHE;
import milchschnide.coasterSigns.signs.utils.SignUtilsHandler;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class StationSign extends TrainCartsSignAction {

    public StationSign() {
        super("coaster");
    }

    @Override
    public void execute(SignActionEvent event) {
        //Error handling if coaster does not exist, should never happen
        final Coaster coaster = CoasterCACHE.getCoasterCACHE().stream().filter(coaster1 ->
                coaster1.name().equals(event.getLine(2))).findFirst().orElse(null);
        if (coaster == null) throw new RuntimeException("Big stress, pls report!");

        final MinecartGroup group = event.getGroup();
        group.getActions().launchReset();
        if (event.isAction(SignActionType.GROUP_ENTER)) {
            // Set the train in station to prevent other trains from entering the station
            coaster.setTrainInStation(event.getMember());
            coaster.addPassThrough();

            // Set cooldown to prevent that players get countdown messages when they enter the station
            coaster.setCooldown(true);

            // If the train should pass through the station without stopping
            if (coaster.passThroughStation()) {
                return;
            }

            final Station station = new Station(event);
            station.centerTrain();

            // Disable physiks
            group.getProperties().setSlowingDown(coaster.slowdownWhenEnter());

            // Set the block direction to the station's next direction if it hasn't been set yet
            if (coaster.getDirection() == null) coaster.setDirection(station.getNextDirectionFace());

            //warte bis der zug steht
            final int[] task = {0};
            task[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(CoasterSigns.instance, () -> {
                if (group.head().getRealSpeed() == 0) {
                    if (group.hasPassenger()) checkPreviouseBlock(coaster, group);

                    coaster.openGates();
                    coaster.openRestraints(group);
                    group.eject();

                    //Task beenden
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, () -> {
                        coaster.setCooldown(false);
                    },5);
                    Bukkit.getScheduler().cancelTask(task[0]);
                }
            }, 0, 5);

        } else if (event.isAction(SignActionType.GROUP_UPDATE)) {
            if (group.hasPassenger() && !coaster.isCooldown()) {
                if (coaster.getCountDownHandler().isRunning()) return;
                coaster.startCountDown(group, false, 0);
            } else {
                coaster.stopCountDown();
            }
        }
    }

    /**
     * Checks if there is a train waiting to enter the first block of the coaster and starts the station countdown if there is.
     *
     * @param coaster The coaster to check for waiting trains.
     * @param group   The minecart group that just entered the station.
     */
    private void checkPreviouseBlock(Coaster coaster, MinecartGroup group) {
        if (!coaster.getBlocks().isEmpty()) {
            if (coaster.getBlocks().getFirst().isTrainWaitingToEnter()) {
                coaster.startCountDown(group, true, 0);
            }
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!initCoaster(event, event.getPlayer())) return false;
        return SignBuildOptions.create()
                .setName("Coaster Station Sign")
                .setDescription("Acts as an advanced station for coasters")
                .handle(event);
    }

    @Override
    public void destroy(SignActionEvent event) {
        final Coaster coaster = CoasterCACHE.getCoasterCACHE().stream().filter(coaster1 ->
                coaster1.name().equals(event.getLine(2))).findFirst().orElse(null);
        if (coaster == null) {
            SignUtilsHandler.sendMessage(null, "A coaster with the name '"
                    + event.getLine(2) + "' does not exist!");
            return;
        }
        if (coaster.isTrainInStation()) {
            SignUtilsHandler.sendMessage(null, "Cannot delete the station sign while a train is in the station!");
            return;
        }
        coaster.deleteCoaster();
    }

    @Override
    public void loadedChanged(SignActionEvent event, boolean loaded) {
        super.loadedChanged(event, initCoaster(event, null));
    }

    /**
     * Initializes a coaster based on the sign's configuration.
     *
     * @param event  The sign action event containing the sign's information.
     * @param player The player who is building the sign (can be null when loading).
     * @return true if the coaster was successfully initialized, false otherwise.
     */
    private boolean initCoaster(SignActionEvent event, Player player) {
        String name = null;
        org.bukkit.block.Block block = null;
        BlockFace direction = null;
        int passThrow = 0;

        final Station.StationConfig config = new Station.StationConfig();
        config.setLaunchSpeed(CoasterSigns.defaultTravelSpeed);

        if (event.getLine(2).isEmpty()) {
            SignUtilsHandler.sendMessage(player, "You must specify a name for the coaster on line 3!");
            return false;
        }
        name = event.getLine(2);

        final String finalName = name;
        if (CoasterCACHE.getCoasterCACHE().stream().anyMatch(coaster -> coaster.name().equals(finalName))) {
            SignUtilsHandler.sendMessage(player, "A coaster with the name '" + name + "' already exists!");
            return false;
        }

        if (!event.getLine(3).isEmpty()) {
            final String[] line4 = event.getLine(3).split(",");
            if (line4.length > 0 && !line4[0].isEmpty()) {
                if (line4.length == 3) {
                    try {
                        final int x = Integer.parseInt(line4[0]);
                        final int y = Integer.parseInt(line4[1]);
                        final int z = Integer.parseInt(line4[2]);
                        block = event.getWorld().getBlockAt(x, y, z);
                    } catch (NumberFormatException e) {
                        SignUtilsHandler.sendMessage(player, "Invalid coordinates on line 4! Expected format: x,y,z");
                        return false;
                    }
                } else {
                    SignUtilsHandler.sendMessage(player, "Invalid coordinates on line 4! Expected format: x,y,z");
                    return false;
                }
            }
        }

        final String[] extraLinesBelow = event.getExtraLinesBelow();
        if (extraLinesBelow != null && extraLinesBelow.length >= 1 && !extraLinesBelow[0].isEmpty()) {
            final String[] line5 = event.getExtraLinesBelow()[0].split(",");
            if (line5.length > 0 && !line5[0].isEmpty()) {
                if (line5.length == 1) {
                    try {
                        direction = BlockFace.valueOf(line5[0].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        SignUtilsHandler.sendMessage(player, "Invalid direction on line 5! " +
                                "Expected a valid BlockFace value. [Bsp: north, south, east, west]");
                        return false;
                    }
                } else if (line5.length == 2) {
                    try {
                        direction = BlockFace.valueOf(line5[0].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        SignUtilsHandler.sendMessage(player, "Invalid direction on line 5! " +
                                "Expected a valid BlockFace value. [Bsp: north, south, east, west]");
                        return false;
                    }
                    try {
                        passThrow = Integer.parseInt(line5[1]);
                    } catch (NumberFormatException e) {
                        SignUtilsHandler.sendMessage(player, "Invalid speed on line 5! " +
                                "Expected a numeric value for pass throughs.");
                        return false;
                    }
                } else {
                    SignUtilsHandler.sendMessage(player, "Invalid parameters on line 5! Expected format:" +
                            " direction or direction,speed");
                    return false;
                }
            }
        }

        if (extraLinesBelow != null && extraLinesBelow.length >= 2 && extraLinesBelow[1] != null && !extraLinesBelow[1].isEmpty()) {
            SignUtilsHandler.checkPosibleLaunch(player, config, extraLinesBelow[1].split(","));
        }

        final Coaster coaster = new Coaster(name, config, direction).init();

        if (extraLinesBelow != null && extraLinesBelow.length >= 3 && extraLinesBelow[2] != null && !extraLinesBelow[2].isEmpty()) {
            final String[] line7 = extraLinesBelow[2].split(",");
            if (line7.length == 1) {
                int enableSlowdownWhenEnter = 0;
                try {
                    enableSlowdownWhenEnter = Integer.parseInt(line7[0]);
                } catch (NumberFormatException e) {
                    SignUtilsHandler.sendMessage(player, "Invalid value for slowdown on line 7! " +
                            "Expected a numeric value (0 or 1).");
                    return false;
                }
                if (enableSlowdownWhenEnter == 1) {
                    coaster.setSlowdownWhenEnter(true);
                    coaster.setSlowdownWhenExit(true);
                }
            } else if (line7.length == 2) {
                int enableSlowdownWhenEnter = 0;
                int enableSlowdownWhenExit = 0;
                try {
                    enableSlowdownWhenEnter = Integer.parseInt(line7[0]);
                } catch (NumberFormatException e) {
                    SignUtilsHandler.sendMessage(player, "Invalid value for slowdown when enter on line 7! " +
                            "Expected a numeric value (0 or 1).");
                    return false;
                }
                try {
                    enableSlowdownWhenExit = Integer.parseInt(line7[1]);
                } catch (NumberFormatException e) {
                    SignUtilsHandler.sendMessage(player, "Invalid value for slowdown when exit on line 7! " +
                            "Expected a numeric value (0 or 1).");
                    return false;
                }
                if (enableSlowdownWhenEnter == 1) coaster.setSlowdownWhenEnter(true);
                if (enableSlowdownWhenExit == 1) coaster.setSlowdownWhenExit(true);
            } else {
                SignUtilsHandler.sendMessage(player, "Invalid parameters on line 7! Expected format: " +
                        "EnalbeSlowdownWhenEnter or EnableSlowdownWhenEnter,EnableSlowdownWhenExit");
                return false;
            }

        }

        if (block != null) coaster.setBlock(block);
        if (passThrow != 0) coaster.setPassThroughs(passThrow);
        return true;
    }
}
