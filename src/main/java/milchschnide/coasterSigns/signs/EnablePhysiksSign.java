package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import milchschnide.coasterSigns.CoasterSigns;
import milchschnide.coasterSigns.utils.Color;
import net.kyori.adventure.text.Component;

public class EnablePhysiksSign extends SignAction {
    @Override
    public boolean match(SignActionEvent event) {
        return event.isType("enablephysiks");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.GROUP_ENTER)) {
            // Enable physiks & set speed limit
            final TrainProperties properties = event.getGroup().getProperties();
            properties.setSlowingDown(true);
            properties.setSpeedLimit(5.0);

            // Set friction & gravity if
            final String line3 = event.getLine(2).toLowerCase();
            final String[] paras = line3.split(",");

            if (paras.length == 0 || event.getLine(2).isEmpty()) {
                // set default values if no parameters are given
                properties.setFriction(CoasterSigns.defaultFriction);
                properties.setGravity(CoasterSigns.defaultGravity);
            } else if (paras.length == 2) {
                // set both friction and gravity if two parameters are given
                properties.setFriction(Double.parseDouble(paras[0]));
                properties.setGravity(Double.parseDouble(paras[1]));
            } else {
                // set only friction if one parameter is given
                properties.setFriction(Double.parseDouble(paras[0]));
            }
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
                    event.getPlayer().sendMessage(Component.text("[CoasterSigns]", Color.CYAN.color)
                            .append(Component.text("Invalid friction or gravity value!")));
                    return false;
                }
            } else if (line3.length == 1) {
                try {
                    Double.parseDouble(line3[0]);
                } catch (NumberFormatException e) {
                    event.getPlayer().sendMessage(Component.text("[CoasterSigns]", Color.CYAN.color)
                            .append(Component.text("Invalid friction or gravity value!")));
                    return false;
                }
            } else if (line3.length > 2) {
                event.getPlayer().sendMessage(Component.text("[CoasterSigns]", Color.CYAN.color)
                        .append(Component.text("Invalid number of parameters!")));
                return false;
            }
        }
        return SignBuildOptions.create()
                .setName("Enable Physiks Sign")
                .setDescription("Enables physiks for the train")
                .handle(event.getPlayer());
    }
}
