package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import milchschnide.coasterSigns.core.CoasterCHACHE;

public class BlockSign extends SignAction {
    @Override
    public boolean match(SignActionEvent event) {
        return CoasterCHACHE.getCoasterCHACHE().stream().anyMatch(coaster -> event.isType(coaster.name()));
    }

    @Override
    public void execute(SignActionEvent event) {

    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        return false;
    }
}
