package milchschnide.coasterSigns.signs.utils;

import com.bergerkiller.bukkit.tc.Station;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import milchschnide.coasterSigns.utils.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUtilsHandler {

    /**
     * Validates the launch parameters provided in the sign.
     * It checks if the first parameter is a valid number and if the second parameter is in the correct format for acceleration or distance.
     *
     * @param args   The array of arguments from the sign.
     * @param player The player to send error messages to if validation fails.
     * @return true if the parameters are valid, false otherwise.
     */
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
                sendMessage(player, " Invalid unit for acceleration/distance! Use 'g' , 'm/s^2' or 'm'.");
                return false;
            }
        } catch (NumberFormatException e) {
            sendMessage(player, " Invalid launch speed or distance/acceleration value!");
            return false;
        }
        return true;
    }

    /**
     * Checks the possible launch parameters from the sign and sets the corresponding values in the station configuration.
     * It handles different formats for acceleration and distance, and sends error messages to the player if any validation fails.
     *
     * @param player The player to send error messages to if validation fails.
     * @param config The station configuration to set the launch parameters on.
     * @param line   The array of launch parameters from the sign.
     */
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

    /**
     * Sends a formatted message to the player with a prefix and the specified message content.
     *
     * @param player  The player to send the message to.
     * @param message The content of the message to send.
     */
    public static void sendMessage(Player player, String message) {
        if (player != null) {
            player.sendMessage(Component.text("[CoasterSigns]: ", Color.CYAN.color)
                    .append(Component.text(message, TextColor.color(255, 255, 255))));
        } else {
            System.out.println(message);
        }

    }

}
