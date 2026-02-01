package milchschnide.coasterSigns.core;

import java.util.ArrayList;
import java.util.List;

public class CoasterCHACHE {

    private static final List<Coaster> coasterCHACHE = new ArrayList<>();

    public static void addCoaster(Coaster coaster) {
        coasterCHACHE.add(coaster);
    }

    public static void removeCoaster(Coaster coaster) {
        coasterCHACHE.remove(coaster);
    }

    public static List<Coaster> getCoasterCHACHE() {
        return new ArrayList<>(coasterCHACHE);
    }

}
