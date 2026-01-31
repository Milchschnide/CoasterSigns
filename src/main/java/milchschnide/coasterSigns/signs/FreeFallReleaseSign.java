package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.Station;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import milchschnide.coasterSigns.CoasterSigns;
import org.bukkit.Bukkit;

public class FreeFallReleaseSign extends SignAction {
    @Override
    public boolean match(SignActionEvent event) {
        return event.isType("freefall");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.GROUP_ENTER)) {
            // Center the train on the track
            final Station station = new Station(event);
            station.centerTrain();

            // Ensure the train is not moving
            final TrainProperties properties = event.getGroup().getProperties();
            properties.setSlowingDown(false);
            properties.setSpeedLimit(5.0);

            // Set a delay before enabling free fall
            double waitDurationInSec = CoasterSigns.defaultWaitTime;

            if (!event.getLine(2).isEmpty()) {
                waitDurationInSec = Double.parseDouble(event.getLine(2));
            }

            // Set friction & gravity
            if (event.getLine(3).isEmpty()) {
                properties.setFriction(0);
                properties.setGravity(CoasterSigns.defaultGravity);
            } else {
                final String[] line4 = event.getLine(3).split(",");

                if (line4.length == 2) {
                    // set both friction and gravity if two parameters are given
                    properties.setGravity(Double.parseDouble(line4[0]));
                    properties.setFriction(Double.parseDouble(line4[1]));
                } else {
                    // set only friction if one parameter is given
                    properties.setGravity(Double.parseDouble(line4[0]));
                }
            }

            // Schedule task to enable free fall after wait duration
            Bukkit.getScheduler().scheduleSyncDelayedTask(CoasterSigns.instance, () ->
                    properties.setSlowingDown(true), (long) waitDurationInSec * 20);
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        final String line3 = event.getLine(2);
        if (!line3.isEmpty()) {
            try {
                Double.parseDouble(line3);
            } catch (NumberFormatException e) {
                event.getPlayer().sendMessage("Invalid Time value!");
                return false;
            }
        }
        if (!event.getLine(3).isEmpty()) {
            final String[] line4 = event.getLine(3).split(",");
            if (line4.length == 2) {
                try {
                    Double.parseDouble(line4[0]);
                    Double.parseDouble(line4[1]);
                } catch (NumberFormatException e) {
                    event.getPlayer().sendMessage("Invalid gravity or friction value!");
                    return false;
                }
            } else if (line4.length == 1) {
                try {
                    Double.parseDouble(line4[0]);
                } catch (NumberFormatException e) {
                    event.getPlayer().sendMessage("Invalid gravity value!");
                    return false;
                }
            } else if (line4.length > 2) {
                event.getPlayer().sendMessage("Invalid number of parameters!");
                return false;
            }
        }

        return SignBuildOptions.create()
                .setName("Free Fall Release Sign")
                .setDescription("Releases train into free fall after a delay")
                .handle(event.getPlayer());
    }
}
