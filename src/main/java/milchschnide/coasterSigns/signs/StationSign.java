package milchschnide.coasterSigns.signs;

import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.signactions.TrainCartsSignAction;
import milchschnide.coasterSigns.core.coaster.Coaster;
import milchschnide.coasterSigns.core.coaster.CoasterCHACHE;

public class StationSign extends TrainCartsSignAction {

    public StationSign() {
        super("coaster");
    }

    @Override
    public void execute(SignActionEvent event) {
        final Coaster coaster = CoasterCHACHE.getCoasterCHACHE().stream().filter(coaster1 ->
                coaster1.name().equals(event.getLine(2))).findFirst().orElse(null);
        if (coaster == null) throw new RuntimeException("Big stress, pls report!");
        if (event.isAction(SignActionType.GROUP_ENTER)) {
            coaster.setTrainInStation(event.getMember());

        } else if (event.isAction(SignActionType.GROUP_UPDATE)) {
            if (event.getGroup().hasPassenger()) {
                if (coaster.getCountDownHandler().isRunning()) return;
                coaster.getCountDownHandler().startCountdown();
            } else {
                coaster.getCountDownHandler().stopCountdown();
            }
        }
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        return true;
    }

    @Override
    public void loadedChanged(SignActionEvent event, boolean loaded) {
        super.loadedChanged(event, loaded);
        System.out.println("Station sign loadedChanged called");
    }
}
