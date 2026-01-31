package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.Station;
import com.bergerkiller.bukkit.tc.Station.StationConfig;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import milchschnide.coasterSigns.CoasterSigns;
import org.bukkit.Bukkit;

public class HoldAndLaunchSign extends SignAction {
    @Override
    public boolean match(SignActionEvent event) {
        return event.isType("holdandlaunch");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.GROUP_ENTER)) {
            final StationConfig config = new StationConfig();
            final Station station = new Station(event, config);
            final TrainProperties properties = event.getGroup().getProperties();
            final String[] paras = event.getLine(3).split(",");

            station.centerTrain();

            // Disable physiks also set friction & gravity to 0
            properties.setSlowingDown(false);
            properties.setGravity(0);
            properties.setFriction(0);

            if (paras.length == 0 || event.getLine(3).isEmpty()) {
                // set default values if no parameters are given
                config.setLaunchSpeed(CoasterSigns.defaultLaunchSpeed);
                config.getLaunchConfig().setDistance(CoasterSigns.defaultLaunchDistance);
            } else if (paras.length == 2) {
                // set both launch speed and distance if two parameters are given
                config.setLaunchSpeed((Double.parseDouble((paras[0])) / 3.6) / 20);

                if (paras[1].contains("m/s^2")) {
                    // acceleration mode
                    final String[] parts = paras[1].split("m");
                    config.getLaunchConfig().setAcceleration(Double.parseDouble(parts[0]) / 400);
                } else if (paras[1].contains("g")) {
                    // g-force mode
                    final String[] parts = paras[1].split("g");
                    config.getLaunchConfig().setAcceleration(((Double.parseDouble(parts[0]) * 785) / 80) / 400);
                } else {
                    // distance mode
                    config.getLaunchConfig().setDistance(Double.parseDouble(paras[1]));
                }
            } else {
                final double speed = (Double.parseDouble((paras[0])) / 3.6) / 20;
                config.setLaunchSpeed(speed);
                config.getLaunchConfig().setDistance(speed * 50);
            }

            //warte bis der zug steht
            final int[] task = {0};
            task[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(CoasterSigns.instance, () -> {
                if (event.getGroup().head().getRealSpeed() == 0) {
                    double time = CoasterSigns.defaultWaitTime;
                    if (!event.getLine(2).isEmpty()) {
                        time = Double.parseDouble(event.getLine(2));
                    }

                    //wenn der zug steht, dann starten
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, () -> {
                        //Launchen
                        event.getMember().getActions()
                                .addActionLaunch(station.getNextDirectionFace()
                                        , config.getLaunchConfig(), config.getLaunchSpeed()
                                        , 5.0);

                        //Physik wieder aktivieren und speed limit setzen
                        properties.setSlowingDown(true);
                        if (properties.getFriction() == 0) properties.setFriction(CoasterSigns.defaultFriction);
                        if (properties.getGravity() == 0) properties.setGravity(CoasterSigns.defaultGravity);
                    }, (long) (time * 20));

                    //Task beenden
                    Bukkit.getScheduler().cancelTask(task[0]);
                }
            }, 0, 5);
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!event.getLine(2).isEmpty()) {
            final String line3 = event.getLine(2);
            try {
                Double.parseDouble(line3);
            } catch (NumberFormatException e) {
                event.getPlayer().sendMessage("§b[CoasterSigns]§r Invalid hold time value!");
                return false;
            }
        }
        if (!event.getLine(3).isEmpty()) {
            final String[] line4 = event.getLine(3).split(",");
            if (line4.length == 2) {
                if (!validateLaunchParameters(event, line4)) return false;
            } else if (line4.length == 1) {
                try {
                    Double.parseDouble(line4[0]);
                } catch (NumberFormatException e) {
                    event.getPlayer().sendMessage("§b[CoasterSigns]§r Invalid launch speed value!");
                    return false;
                }
            } else if (line4.length >= 3) {
                event.getPlayer().sendMessage("§b[CoasterSigns]§r Invalid number of parameters!");
                return false;
            }
        }
        return SignBuildOptions.create()
                .setName("HoldAndLaunch")
                .setDescription("Holds the train for a specified time and then launches it")
                .handle(event);
    }

    private static boolean validateLaunchParameters(SignChangeActionEvent event, String[] line4) {
        try {
            Double.parseDouble(line4[0]);
            final String p = line4[1].trim();
            String numberPart = null;
            String unitPart = "";

            final java.util.regex.Pattern pNumUnit =
                    java.util.regex.Pattern.compile("^([+-]?\\d*\\.?\\d+)\\s*(m/s\\^2|g|m)?$"
                            , java.util.regex.Pattern.CASE_INSENSITIVE);
            final java.util.regex.Pattern pUnitNum =
                    java.util.regex.Pattern.compile("^(m/s\\^2|g|m)\\s*([+-]?\\d*\\.?\\d+)$"
                            , java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = pNumUnit.matcher(p);

            if (m.matches()) {
                numberPart = m.group(1);
                unitPart = m.group(2) == null ? "" : m.group(2);
            } else {
                m = pUnitNum.matcher(p);
                if (m.matches()) {
                    unitPart = m.group(1);
                    numberPart = m.group(2);
                } else {
                    event.getPlayer().sendMessage("§b[CoasterSigns]§r Invalid launch parameter format!");
                    return false;
                }
            }

            Double.parseDouble(numberPart);
            unitPart = unitPart.trim();
            if (!unitPart.isEmpty() && !unitPart.equalsIgnoreCase("g") &&
                    !unitPart.equalsIgnoreCase("m/s^2")) {
                event.getPlayer().sendMessage("§b[CoasterSigns]§r Invalid unit for acceleration/distance! " +
                        "Use 'g' , 'm/s^2' or 'm'.");
                return false;
            }
        } catch (NumberFormatException e) {
            event.getPlayer().sendMessage("§b[CoasterSigns]§r Invalid launch speed or distance/acceleration value!");
            return false;
        }
        return true;
    }
}
