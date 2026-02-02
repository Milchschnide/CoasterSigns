package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.Station;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.signactions.TrainCartsSignAction;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import milchschnide.coasterSigns.CoasterSigns;
import milchschnide.coasterSigns.core.coaster.Coaster;
import milchschnide.coasterSigns.core.coaster.CoasterCHACHE;
import milchschnide.coasterSigns.signs.utils.SignUtilsHandler;
import milchschnide.coasterSigns.utils.Color;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class StationSign extends TrainCartsSignAction {

    public StationSign() {
        super("coaster");
    }

    @Override
    public void execute(SignActionEvent event) {
        final Coaster coaster = CoasterCHACHE.getCoasterCHACHE().stream().filter(coaster1 ->
                coaster1.name().equals(event.getLine(2))).findFirst().orElse(null);
        if (coaster == null) throw new RuntimeException("Big stress, pls report!");

        final MinecartGroup group = event.getGroup();
        if (event.isAction(SignActionType.GROUP_ENTER)) {
            coaster.setTrainInStation(event.getMember());

            final Station station = new Station(event);
            station.centerTrain();
            if (coaster.getDirection() == null) coaster.setDirection(station.getNextDirectionFace());

            //warte bis der zug steht
            final int[] task = {0};
            task[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(CoasterSigns.instance, () -> {
                if (group.head().getRealSpeed() == 0) {
                    coaster.openGates();
                    coaster.openRestraints(group);

                    //Task beenden
                    Bukkit.getScheduler().cancelTask(task[0]);
                }
            }, 0, 5);

        } else if (event.isAction(SignActionType.GROUP_UPDATE)) {
            if (group.hasPassenger()) {
                if (coaster.getCountDownHandler().isRunning()) return;
                coaster.startCountDown(group);
            } else {
                coaster.stopCountDown();
            }
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if(!initCoaster(event,event.getPlayer())) return false;
        return SignBuildOptions.create()
                .setName("Coaster Station Sign")
                .setDescription("Acts as an advanced station for coasters")
                .handle(event);
    }

    @Override
    public void loadedChanged(SignActionEvent event, boolean loaded) {
        super.loadedChanged(event, initCoaster(event, null));
    }

    private boolean initCoaster(SignActionEvent event, Player player) {
        String name = null;
        Block block = null;
        BlockFace direction = null;
        int passThrow = 0;

        final Station.StationConfig config = new Station.StationConfig();
        config.setLaunchSpeed(CoasterSigns.defaultLaunchSpeed);


        if (!event.getLine(2).isEmpty()) name = event.getLine(2);

        final String[] line4 = event.getLine(3).split(",");
        if (line4.length > 0 && !line4[0].isEmpty()) {
            if (line4.length == 3) {
                try {
                    final int x = Integer.parseInt(line4[0]);
                    final int y = Integer.parseInt(line4[1]);
                    final int z = Integer.parseInt(line4[2]);
                    block = event.getWorld().getBlockAt(x, y, z);
                } catch (NumberFormatException e) {
                    SignUtilsHandler.sendMessage(player,Component.text("[CoasterSigns]", Color.CYAN.color)
                            .append(Component.text("Invalid coordinates on line 4! Expected format: x,y,z")));
                    return false;
                }
            } else {
                SignUtilsHandler.sendMessage(player,Component.text("[CoasterSigns]", Color.CYAN.color)
                        .append(Component.text("Invalid coordinates on line 4! Expected format: x,y,z")));
                return false;
            }
        }

        final String[] line5 = event.getLine(4).split(",");
        if(line5.length > 0 && !line5[0].isEmpty()) {
            if(line5.length == 1) {
                try {
                    direction = BlockFace.valueOf(line5[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    SignUtilsHandler.sendMessage(player,Component.text("[CoasterSigns]", Color.CYAN.color)
                            .append(Component.text("Invalid direction on line 5! " +
                                    "Expected a valid BlockFace value. [Bsp: north, south, east, west]")));
                    return false;
                }
            } else if(line5.length == 2) {
                try {
                    direction = BlockFace.valueOf(line5[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    SignUtilsHandler.sendMessage(player,Component.text("[CoasterSigns]", Color.CYAN.color)
                            .append(Component.text("Invalid direction on line 5! " +
                                    "Expected a valid BlockFace value. [Bsp: north, south, east, west]")));
                    return false;
                }
                try {
                    passThrow = Integer.parseInt(line5[1]);
                } catch (NumberFormatException e) {
                    SignUtilsHandler.sendMessage(player,Component.text("[CoasterSigns]", Color.CYAN.color)
                            .append(Component.text("Invalid speed on line 5! " +
                                    "Expected a numeric value for pass throughs.")));
                    return false;
                }
            } else {
                SignUtilsHandler.sendMessage(player,Component.text("[CoasterSigns]", Color.CYAN.color)
                        .append(Component.text("Invalid parameters on line 5! Expected format: direction or direction,speed")));
                return false;
            }
        }

        String[] line6 = event.getLine(5).split(",");
        if(line6.length > 0 && !line6[0].isEmpty()) {
            if(line6.length == 1) {
                try {
                    config.setLaunchSpeed(Double.parseDouble(line6[0]));
                } catch (NumberFormatException e) {
                    SignUtilsHandler.sendMessage(player,Component.text("[CoasterSigns]", Color.CYAN.color)
                            .append(Component.text("Invalid speed on line 6! " +
                                    "Expected a numeric value for speed.")));
                    return false;
                }
            } else if(line6.length == 2) {
                try {
                    config.setLaunchSpeed(Double.parseDouble(line6[0]));
                } catch (NumberFormatException e) {
                    SignUtilsHandler.sendMessage(player,Component.text("[CoasterSigns]", Color.CYAN.color)
                            .append(Component.text("Invalid speed on line 6! " +
                                    "Expected a numeric value for speed.")));
                    return false;
                }
                if(!SignUtilsHandler.validateLaunch(line6,player)) return false;
                if (line6[1].contains("m/s^2")) {
                    // acceleration mode
                    final String[] parts = line6[1].split("m");
                    config.getLaunchConfig().setAcceleration(Double.parseDouble(parts[0]) / 400);
                } else if (line6[1].contains("g")) {
                    // g-force mode
                    final String[] parts = line6[1].split("g");
                    config.getLaunchConfig().setAcceleration(((Double.parseDouble(parts[0]) * 785) / 80) / 400);
                } else {
                    // distance mode
                    final String[] parts = line6[1].split("m");
                    config.getLaunchConfig().setDistance(Double.parseDouble(parts[0]));
                }
            }
        }
        if(line6.length == 0 || line6[0].isEmpty() || line6.length == 1) {
            config.getLaunchConfig().setDistance(2);
        }

        final Coaster coaster = new Coaster(name,config,direction).init();
        if(block != null) coaster.setBlock(block);
        if(passThrow != 0) coaster.setPassThroughs(passThrow);
        return true;
    }
}
