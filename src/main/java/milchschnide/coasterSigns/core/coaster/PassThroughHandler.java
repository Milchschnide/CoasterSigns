package milchschnide.coasterSigns.core.coaster;

public class PassThroughHandler {
    private final Coaster coaster;
    private final int passThroughs;
    private int currentPassThroughs = 0;

    public PassThroughHandler(Coaster coaster, int passThroughs) {
        this.coaster = coaster;
        this.passThroughs = passThroughs;
    }
}
