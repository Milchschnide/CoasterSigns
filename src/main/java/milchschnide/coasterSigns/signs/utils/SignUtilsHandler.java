package milchschnide.coasterSigns.signs.utils;

import com.bergerkiller.bukkit.tc.Station;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import milchschnide.coasterSigns.utils.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SignUtilsHandler {

    public static boolean validateLaunch(String[] args, Player player) {
        try {
            Double.parseDouble(args[0]);
            final String p = args[1].trim();
            String numberPart = null;
            String unitPart = "";

            final Pattern pNumUnit = Pattern.compile("^([+-]?\\d*\\.?\\d+)\\s*(m/s\\^2|g|m)?$"
                    , Pattern.CASE_INSENSITIVE);
            final Pattern pUnitNum = Pattern.compile("^(m/s\\^2|g|m)\\s*([+-]?\\d*\\.?\\d+)$"
                    , Pattern.CASE_INSENSITIVE);
            Matcher m = pNumUnit.matcher(p);

            if (m.matches()) {
                numberPart = m.group(1);
                unitPart = m.group(2) == null ? "" : m.group(2);
            } else {
                m = pUnitNum.matcher(p);
                if (m.matches()) {
                    unitPart = m.group(1);
                    numberPart = m.group(2);
                } else {
                    sendMessage(player, " Invalid launch parameter format!");
                    return false;
                }
            }

            Double.parseDouble(numberPart);
            unitPart = unitPart.trim();
            if (!unitPart.isEmpty() && !unitPart.equalsIgnoreCase("g") &&
                    !unitPart.equalsIgnoreCase("m/s^2")) {
                sendMessage(player," Invalid unit for acceleration/distance! Use 'g' , 'm/s^2' or 'm'.");
                return false;
            }
        } catch (NumberFormatException e) {
            sendMessage(player, " Invalid launch speed or distance/acceleration value!");
            return false;
        }
        return true;
    }

    public static void checkPosibleLaunch(Player player, Station.StationConfig config, String[] line) {
        if (!line[0].isEmpty()) {
            if (line.length == 1) {
                try {
                    config.setLaunchSpeed(Double.parseDouble(line[0]));
                } catch (NumberFormatException e) {
                    SignUtilsHandler.sendMessage(player, "Invalid speed on line 6! " +
                            "Expected a numeric value for speed.");
                    return;
                }
            } else if (line.length == 2) {
                try {
                    config.setLaunchSpeed(Double.parseDouble(line[0]));
                } catch (NumberFormatException e) {
                    SignUtilsHandler.sendMessage(player, "Invalid speed on line 6! " +
                            "Expected a numeric value for speed.");
                    return;
                }
                if (!SignUtilsHandler.validateLaunch(line, player)) return;
                if (line[1].contains("m/s^2")) {
                    // acceleration mode
                    final String[] parts = line[1].split("m");
                    config.getLaunchConfig().setAcceleration(Double.parseDouble(parts[0]) / 400);
                } else if (line[1].contains("g")) {
                    // g-force mode
                    final String[] parts = line[1].split("g");
                    config.getLaunchConfig().setAcceleration(((Double.parseDouble(parts[0]) * 785) / 80) / 400);
                } else {
                    // distance mode
                    final String[] parts = line[1].split("m");
                    config.getLaunchConfig().setDistance(Double.parseDouble(parts[0]));
                }
            }
        } else {
            config.getLaunchConfig().setDistance(2);
        }
    }

    public static void sendMessage(Player player, String message) {
        if (player != null) {
            player.sendMessage(Component.text("[CoasterSigns]: ",Color.CYAN.color)
                    .append(Component.text(message, TextColor.color(255, 255, 255))));
        }

    }

}
