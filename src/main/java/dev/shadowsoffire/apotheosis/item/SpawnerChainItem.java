package dev.shadowsoffire.apotheosis.item;

/**
 * Spawner Chains are dropped when a Rogue Spawner is broken, which often happens in hostile surroundings.
 * <p>
 * To avoid the drop being destroyed before it can be picked up, the item entity is immune to fire and explosion damage.
 */
public class SpawnerChainItem extends TooltipItem {

    public SpawnerChainItem(Properties properties) {
        super(properties);
    }

}
