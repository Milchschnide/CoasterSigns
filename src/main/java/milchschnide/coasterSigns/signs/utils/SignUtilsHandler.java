package milchschnide.coasterSigns.signs.utils;

import milchschnide.coasterSigns.utils.Color;
import net.kyori.adventure.text.Component;
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
                    sendMessage(player, Component.text("[CoasterSigns]", Color.CYAN.color)
                            .append(Component.text(" Invalid launch parameter format!")));
                    return false;
                }
            }

            Double.parseDouble(numberPart);
            unitPart = unitPart.trim();
            if (!unitPart.isEmpty() && !unitPart.equalsIgnoreCase("g") &&
                    !unitPart.equalsIgnoreCase("m/s^2")) {
                sendMessage(player, Component.text("[CoasterSigns]", Color.CYAN.color)
                        .append(Component.text(" Invalid unit for acceleration/distance! Use 'g' , 'm/s^2' or 'm'.")));
                return false;
            }
        } catch (NumberFormatException e) {
            sendMessage(player, Component.text("[CoasterSigns]", Color.CYAN.color)
                    .append(Component.text(" Invalid launch speed or distance/acceleration value!")));
            return false;
        }
        return true;
    }

    public static void sendMessage(Player player, Component message) {
        if (player != null) {
            player.sendMessage(message);
        } else {
            System.out.println(message);
        }
    }

}
