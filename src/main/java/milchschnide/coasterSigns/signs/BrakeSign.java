package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.Station.StationConfig;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import milchschnide.coasterSigns.CoasterSigns;

public class BrakeSign extends SignAction {
    @Override
    public boolean match(SignActionEvent event) {
        return event.isType("brake");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.GROUP_ENTER)) {
            final StationConfig config = new StationConfig();

            // Parse launch distance and speed from sign & yaml
            final String[] line3 = event.getLine(2).split(",");
            double targetSpeed = CoasterSigns.defaultTravelSpeed;

            if (line3.length == 0 || event.getLine(2).isEmpty()) {
                // set default values if no parameters are given
                config.getLaunchConfig().setDistance(CoasterSigns.defaultBrakeDistance);
                config.setLaunchSpeed(targetSpeed);
            } else if (line3.length == 2) {
                // set both launch distance and speed if two parameters are given
                config.getLaunchConfig().setDistance(Double.parseDouble(line3[0]));
                targetSpeed = (Double.parseDouble(line3[1]) / 20) / 3.6;
                config.setLaunchSpeed(targetSpeed);
            } else {
                // set only launch distance if one parameter is given
                config.getLaunchConfig().setDistance(Double.parseDouble(line3[0]));
            }

            // Launch the train
            event.getMember().getActions().addActionLaunch(event.getMember().getDirectionTo(), config.getLaunchConfig()
                    , config.getLaunchSpeed(), targetSpeed);

            // Set friction & gravity to 0 & disable physiks
            final TrainProperties properties = event.getGroup().getProperties();
            properties.setSlowingDown(false);
            properties.setFriction(0);
            properties.setGravity(0);
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!event.getLine(2).isEmpty()) {
            final String[] line3 = event.getLine(2).split(",");
            if (line3.length == 2) {
                try {
                    Double.parseDouble(line3[0]);
                    Double.parseDouble(line3[1]);
                } catch (NumberFormatException e) {
                    event.getPlayer().sendMessage("§b[CoasterSigns]§r Invalid launch distance or speed value!");
                    return false;
                }
            } else if (line3.length == 1) {
                try {
                    Double.parseDouble(line3[0]);
                } catch (NumberFormatException e) {
                    event.getPlayer().sendMessage("§b[CoasterSigns]§r Invalid launch distance value!");
                    return false;
                }
            } else if (line3.length >= 2) {
                event.getPlayer().sendMessage("§b[CoasterSigns]§r Invalid number of parameters!");
                return false;
            }
        }
        return SignBuildOptions.create()
                .setName("Brake Sign")
                .setDescription("Brakes the train down")
                .handle(event.getPlayer());
    }
}
