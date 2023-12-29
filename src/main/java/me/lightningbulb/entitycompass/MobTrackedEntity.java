package me.lightningbulb.entitycompass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.Serializable;
import java.util.UUID;

public class MobTrackedEntity implements Serializable {

    transient Plugin plugin = EntityCompass.getPlugin(EntityCompass.class);

    private UUID uuid;
    private EntityList entityListMembership;

    public MobTrackedEntity(UUID uuid, EntityList entityListMembership) {

        this.uuid = uuid;
        this.entityListMembership = entityListMembership;

    }


    public UUID getUUID() {
        return uuid;
    }

    public EntityList getEntityListMembership() {
        return entityListMembership;
    }

    public Location getLocation() {
        return null;
    }

    public Location getFirstOverworldLocation() {
        /*

        NamespacedKey firstNormalCoordinatesKey = new NamespacedKey(plugin, "firstNormalCoordinates");

        PersistentDataContainer data = Bukkit.getPlayer(playerHoldingItem).getPersistentDataContainer();

        String stringLocation = data.get(firstNormalCoordinatesKey, PersistentDataType.STRING);

        Location location = str2Loc(stringLocation);
*/
        return null;
    }

    public Location getLastOverworldLocation() {

        /*
        NamespacedKey lastNormalCoordinatesKey = new NamespacedKey(plugin, "lastNormalCoordinates");

        PersistentDataContainer data = Bukkit.getPlayer(playerHoldingItem).getPersistentDataContainer();

        String stringLocation = data.get(lastNormalCoordinatesKey, PersistentDataType.STRING);

        Location location = str2Loc(stringLocation);
*/
        return null;
    }

    public Location getFirstNetherLocation() {

        /*
        NamespacedKey firstNetherCoordinatesKey = new NamespacedKey(plugin, "firstNetherCoordinates");

        PersistentDataContainer data = Bukkit.getPlayer(playerHoldingItem).getPersistentDataContainer();

        String stringLocation = data.get(firstNetherCoordinatesKey, PersistentDataType.STRING);

        Location location = str2Loc(stringLocation);
*/
        return null;
    }

    public Location getLastNetherLocation() {

        /*
        NamespacedKey lastNetherCoordinatesKey = new NamespacedKey(plugin, "lastNetherCoordinates");

        PersistentDataContainer data = Bukkit.getPlayer(playerHoldingItem).getPersistentDataContainer();

        String stringLocation = data.get(lastNetherCoordinatesKey, PersistentDataType.STRING);

        Location location = str2Loc(stringLocation);
*/
        return null;
    }

    public Location getLastEndLocation() {

        /*
        NamespacedKey lastEndCoordinatesKey = new NamespacedKey(plugin, "lastEndCoordinates");

        PersistentDataContainer data = Bukkit.getPlayer(playerHoldingItem).getPersistentDataContainer();

        String stringLocation = data.get(lastEndCoordinatesKey, PersistentDataType.STRING);

        Location location = str2Loc(stringLocation);
*/
        return null;
    }


}
