package me.lightningbulb.entitycompass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class PlayerTrackedEntity extends TrackedEntity implements Serializable {


    transient Plugin plugin = EntityCompass.getPlugin(EntityCompass.class);


    private UUID uuid;
    private EntityList entityListMembership;
    private EntityList pointedEntityList;

    private TrackedEntity pointedTrackedEntity;

    private boolean compassHeld;

    public PlayerTrackedEntity(UUID uuid, EntityList entityListMembership) {

        this.uuid = uuid;
        this.entityListMembership = entityListMembership;

    }


    public UUID getUUID() {
        return uuid;
    }


    public EntityList getEntityListMembership() {
        return entityListMembership;
    }


    public Location getLocation () {
        return Bukkit.getPlayer(uuid).getLocation();
    }


    public String getName() {
        try {
            return Bukkit.getPlayer(uuid).getName();
        }catch (NullPointerException e) {
            return Bukkit.getOfflinePlayer(uuid).getName();
        }
    }

    public void setCompassHeld(boolean compassHeld) {
        this.compassHeld = compassHeld;
    }


    public boolean getCompassHeld() {
        return compassHeld;
    }


    public void setPointedTrackedEntity(TrackedEntity pointedTrackedEntity) {
        this.pointedTrackedEntity =pointedTrackedEntity;
    }

    public TrackedEntity getPointedTrackedEntity() {

        try {
            return pointedTrackedEntity;
        }catch (NullPointerException e) {
            return null;
        }
    }

    public void setPointedEntityList(EntityList entityList) {
        this.pointedEntityList = entityList ;
    }

    public EntityList getPointedEntityList() {

        try {
            return pointedEntityList;
        }catch (NullPointerException e) {
            return null;
        }
    }


    public Location getFirstOverworldLocation() {

        NamespacedKey firstNormalCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "firstNormalCoordinates");

        PersistentDataContainer data = Bukkit.getPlayer(uuid).getPersistentDataContainer();

        String stringLocation = data.get(firstNormalCoordinatesKey, PersistentDataType.STRING);

        Location location = str2Loc(stringLocation);

        return location;
    }

    public Location getLastOverworldLocation() {

        NamespacedKey lastNormalCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "lastNormalCoordinates");

        PersistentDataContainer data = Bukkit.getPlayer(uuid).getPersistentDataContainer();

        String stringLocation = data.get(lastNormalCoordinatesKey, PersistentDataType.STRING);

        Location location = str2Loc(stringLocation);

        return location;
    }

    public Location getFirstNetherLocation() {

        NamespacedKey firstNetherCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "firstNetherCoordinates");

        PersistentDataContainer data = Bukkit.getPlayer(uuid).getPersistentDataContainer();

        String stringLocation = data.get(firstNetherCoordinatesKey, PersistentDataType.STRING);

        Location location = str2Loc(stringLocation);

        return location;
    }

    public Location getLastNetherLocation() {

        NamespacedKey lastNetherCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "lastNetherCoordinates");

        PersistentDataContainer data = Bukkit.getPlayer(uuid).getPersistentDataContainer();

        String stringLocation = data.get(lastNetherCoordinatesKey, PersistentDataType.STRING);

        Location location = str2Loc(stringLocation);

        return location;
    }

    public Location getLastEndLocation() {

        NamespacedKey lastEndCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "lastEndCoordinates");

        PersistentDataContainer data = Bukkit.getPlayer(uuid).getPersistentDataContainer();

        String stringLocation = data.get(lastEndCoordinatesKey, PersistentDataType.STRING);

        Location location = str2Loc(stringLocation);

        return location;
    }


}
