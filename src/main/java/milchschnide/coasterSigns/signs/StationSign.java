package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import milchschnide.coasterSigns.core.Coaster;
import milchschnide.coasterSigns.core.CoasterCHACHE;

public class StationSign extends SignAction {
    @Override
    public boolean match(SignActionEvent event) {
        return event.isType("coaster");
    }

    @Override
    public void execute(SignActionEvent event) {
        if(event.isAction(SignActionType.GROUP_ENTER)){

        } else if(event.isAction(SignActionType.GROUP_LEAVE)){

        } else if(event.isAction(SignActionType.GROUP_UPDATE)){

        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if(!event.getLine(2).isEmpty()){
            CoasterCHACHE.addCoaster(new Coaster(event.getLine(2)));
        }
        return true;
    }

    @Override
    public void loadedChanged(SignActionEvent info, boolean loaded) {
        super.loadedChanged(info, loaded);
        System.out.println("Test sign station loaded changed");
    }
}
