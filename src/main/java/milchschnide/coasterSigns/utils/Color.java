package milchschnide.coasterSigns.utils;

import net.kyori.adventure.text.format.TextColor;

public enum Color {
    CYAN(TextColor.color(75, 105, 115));

    public final TextColor color;

    Color(TextColor color) {
        this.color = color;
    }
}

