package milchschnide.coasterSigns.core.coaster;

import java.util.ArrayList;
import java.util.List;

public class CoasterCACHE {

    private static final List<Coaster> coasterCACHE = new ArrayList<>();

    public static void addCoaster(Coaster coaster) {
        coasterCACHE.add(coaster);
    }

    public static void removeCoaster(Coaster coaster) {
        coasterCACHE.remove(coaster);
    }

    public static List<Coaster> getCoasterCACHE() {
        return new ArrayList<>(coasterCACHE);
    }

}
