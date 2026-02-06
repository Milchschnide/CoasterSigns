package milchschnide.coasterSigns.utils;

import com.bergerkiller.bukkit.coasters.tracks.TrackNode;
import com.google.common.collect.Lists;
import milchschnide.coasterSigns.CoasterSigns;
import milchschnide.coasterSigns.core.coaster.CoasterCACHE;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public final class MineCartRemovalHandler {

    /**
     * Init method to remove all MineCarts from the tracks
     */
    public static void init() {
        final World world = CoasterSigns.world;
        getTrackNodes(world).forEach(trackNode -> removeMineCartForNode(trackNode, world));
    }

    /**
     * Get all track nodes from the world
     *
     * @param world the world
     * @return the track nodes
     */
    private static List<TrackNode> getTrackNodes(World world) {
        final List<TrackNode> trackNodes = new ArrayList<>();
        CoasterSigns.tcCoasters.getCoasterWorld(world).getTracks().getCoasters().stream()
                .filter(trackCoaster -> CoasterCACHE.getCoasterCACHE().stream()
                        .anyMatch(coaster -> coaster.getName().equals(trackCoaster.getName())))
                .forEach(trackCoaster -> trackNodes.addAll(trackCoaster.getNodes()));
        if (trackNodes.isEmpty()) throw new RuntimeException("No tracks found to clear in MineCartRemovalHandler");
        return trackNodes;
    }

    /**
     * Remove all MineCarts nearby the track node
     *
     * @param trackNode the track node
     * @param world     the world
     */
    public static void removeMineCartForNode(TrackNode trackNode, World world) {
        final Block pos = trackNode.getPositionBlock().toBlock(world);
        if (!pos.getChunk().isLoaded()) pos.getChunk().load();
        Lists.newArrayList(world.getNearbyEntities(pos.getLocation(), 1, 1, 1)).forEach(Entity::remove);
    }
}
