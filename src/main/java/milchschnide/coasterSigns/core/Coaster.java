package milchschnide.coasterSigns.core;

import com.bergerkiller.bukkit.tc.controller.MinecartMember;

import java.util.ArrayList;
import java.util.List;

public class Coaster {
    private final String name;
    private final List<Block> blocks = new ArrayList<>();
    private MinecartMember<?> trainInStation;
    private boolean gatesClosed = false;
    private boolean restraintsClosed = false;

    public Coaster(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
