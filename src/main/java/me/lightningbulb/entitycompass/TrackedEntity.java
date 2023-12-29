package me.lightningbulb.entitycompass;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.UUID;


public class TrackedEntity implements Serializable {

    private EntityList pointedEntityList;

    private TrackedEntity pointedTrackedEntity;

    private UUID trackedEntityUUID;

    private UUID uuid;

    //private Location location;

    private TrackedEntityType trackedEntityType;

    private String name;

    private boolean compassHeld;

    private EntityList entityListMembership;

    public enum TrackedEntityType {
        PLAYER,
        ITEM,
        MOB
    }

    private PlayerTrackedEntity playerTrackedEntity;
    private ItemTrackedEntity itemTrackedEntity;
    private MobTrackedEntity mobTrackedEntity;

    /*
    TrackedEntityType - list of supported entity categories to use appropriate object
    UUID - player uuid or new randomized one for entities and mobs
    EntityList - just gives the reference of the EntityList that the Tracked player is on (for tracking controls)
     */

    public TrackedEntity() {

    }

    public TrackedEntity(UUID uuid, TrackedEntityType trackedEntityType, EntityList entityListMembership) {

        this.trackedEntityType = trackedEntityType;


        switch (trackedEntityType) {
            case PLAYER:
                this.playerTrackedEntity = new PlayerTrackedEntity(uuid, entityListMembership);
                break;
            case ITEM:
                System.out.println("[EntityCompass] " + "CANNOT CREATE ITEM TRACKED ENTITY WITHOUT TrackedEntity IN THE CONSTRUCTOR");
                break;
            case MOB:
                this.mobTrackedEntity = new MobTrackedEntity(uuid, entityListMembership);
                break;
        }
    }
    public TrackedEntity(UUID uuid, TrackedEntityType trackedEntityType, EntityList entityListMembership, UUID playerHoldingItem) {

        this.trackedEntityType = trackedEntityType;

        switch (trackedEntityType) {
            case PLAYER:
                this.playerTrackedEntity = new PlayerTrackedEntity(uuid, entityListMembership);
                break;
            case ITEM:
                this.itemTrackedEntity = new ItemTrackedEntity(uuid, entityListMembership, playerHoldingItem);
                break;
            case MOB:
                this.mobTrackedEntity = new MobTrackedEntity(uuid, entityListMembership);
                break;
        }
    }


    public void setCompassHeld(boolean compassHeld) {
        if (trackedEntityType == TrackedEntityType.PLAYER) {
            playerTrackedEntity.setCompassHeld(compassHeld);
        }
    }
    public boolean getCompassHeld() {

        if (trackedEntityType == TrackedEntityType.PLAYER) {
           return playerTrackedEntity.getCompassHeld();
        }
        return false;
    }

    public UUID getUUID() {

        switch (trackedEntityType) {
            case PLAYER:
                return playerTrackedEntity.getUUID();
            case ITEM:
                return itemTrackedEntity.getUUID();
            case MOB:
                return mobTrackedEntity.getUUID();
            default:
                return null;
        }
    }

    public void setBlockStorageLocation(Location location) {
        if (trackedEntityType == TrackedEntityType.ITEM) {
            itemTrackedEntity.setBlockStorageLocation(location);
        }
    }

    public void setLastGroundLocation(Location location) {
        if (trackedEntityType == TrackedEntityType.ITEM) {
            itemTrackedEntity.setLastGroundLocation(location);
        }
    }

    public Location getLocation() {

        switch (trackedEntityType) {
            case PLAYER:
                return playerTrackedEntity.getLocation();
            case ITEM:
                return itemTrackedEntity.getLocation();
            case MOB:
                return mobTrackedEntity.getLocation();
            default:
                return null;
        }
    }

    public EntityList getPointedEntityList() {
        if (trackedEntityType == TrackedEntityType.PLAYER) {
            return playerTrackedEntity.getPointedEntityList();
        }
        return null;
    }

    public void setPointedEntityList(EntityList entityList) {
        if (trackedEntityType == TrackedEntityType.PLAYER) {
            playerTrackedEntity.setPointedEntityList(entityList);
        }
    }

    public TrackedEntity getPointedTrackedEntity() {
        if (trackedEntityType == TrackedEntityType.PLAYER) {
            return playerTrackedEntity.getPointedTrackedEntity();
        }
        return null;
    }

    public void setPointedTrackedEntity(TrackedEntity trackedEntity) {
        if (trackedEntityType == TrackedEntityType.PLAYER) {
            playerTrackedEntity.setPointedTrackedEntity(trackedEntity);
        }
    }

    public void setEntityListMembership(EntityList entityListMembership) {
        this.entityListMembership = entityListMembership;
    }

    public EntityList getEntityListMembership() {

        switch (trackedEntityType) {
            case PLAYER:
                return playerTrackedEntity.getEntityListMembership();
            case ITEM:
                return itemTrackedEntity.getEntityListMembership();
            case MOB:
                return mobTrackedEntity.getEntityListMembership();
            default:
                return null;
        }
    }

    public String getName() {
        if (trackedEntityType == TrackedEntityType.PLAYER) {
            return playerTrackedEntity.getName();
        }
        if (trackedEntityType == TrackedEntityType.ITEM) {
            return itemTrackedEntity.getName();
        }

        return null;
    }

    public Location getFirstOverworldLocation () {

        switch (trackedEntityType) {
            case PLAYER:
                return playerTrackedEntity.getFirstOverworldLocation();
            case ITEM:
                return itemTrackedEntity.getFirstOverworldLocation();
            case MOB:
                return mobTrackedEntity.getFirstOverworldLocation();
            default:
                return null;
        }
    }
    public Location getLastOverworldLocation () {
        switch (trackedEntityType) {
            case PLAYER:
                return playerTrackedEntity.getLastOverworldLocation();
            case ITEM:
                return itemTrackedEntity.getLastOverworldLocation();
            case MOB:
                return mobTrackedEntity.getLastOverworldLocation();
            default:
                return null;
        }
    }
    public Location getFirstNetherLocation () {
        switch (trackedEntityType) {
            case PLAYER:
                return playerTrackedEntity.getFirstNetherLocation();
            case ITEM:
                return itemTrackedEntity.getFirstNetherLocation();
            case MOB:
                return mobTrackedEntity.getFirstNetherLocation();
            default:
                return null;
        }
    }
    public Location getLastNetherLocation () {
        switch (trackedEntityType) {
            case PLAYER:
                return playerTrackedEntity.getLastNetherLocation();
            case ITEM:
                return itemTrackedEntity.getLastNetherLocation();
            case MOB:
                return mobTrackedEntity.getLastNetherLocation();
            default:
                return null;
        }
    }
    public Location getLastEndLocation () {
        switch (trackedEntityType) {
            case PLAYER:
                return playerTrackedEntity.getLastEndLocation();
            case ITEM:
                return itemTrackedEntity.getLastEndLocation();
            case MOB:
                return mobTrackedEntity.getLastEndLocation();
            default:
                return null;
        }
    }

    public boolean isOnline() {

        if (trackedEntityType == TrackedEntityType.PLAYER && Bukkit.getPlayer(playerTrackedEntity.getUUID()) == null) {
            return false;
        }
        if (trackedEntityType == TrackedEntityType.ITEM) {
            return itemTrackedEntity.isOnline();
        }

        return true;
    }

    public void setPlayerHoldingItem(UUID playerHoldingItem) {
        if (trackedEntityType == TrackedEntityType.ITEM) {
            itemTrackedEntity.setPlayerHoldingItem(playerHoldingItem);
        }
    }

    public UUID getPlayerHoldingItem() {
        if (trackedEntityType == TrackedEntityType.ITEM) {
            return itemTrackedEntity.getPlayerHoldingItem();
        }
        return null;
    }

    public UUID getLastPlayerHoldingItem() {
        if (trackedEntityType == TrackedEntityType.ITEM) {
            return itemTrackedEntity.getLastPlayerHoldingItem();
        }
        return null;
    }

    public void removeSelf() {
        switch (trackedEntityType) {
            case ITEM:
                itemTrackedEntity.removeSelf();
        }
    }

    public ItemStack getItemStack() {
        if (trackedEntityType == TrackedEntityType.ITEM) {
            return itemTrackedEntity.getItemReference();
        }
        return null;
    }


    //convert locations to a format that can be reconstructed by str2Loc
    public String loc2Str (Location location) {

        if (location == null) {
            return null;
        }

        World world = location.getWorld();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return world + "," + x + "," + y + "," + z;
    }

    //reconstruct locations from strings provided by the loc2Str method
    public Location str2Loc(String str) {

        if (str == null) {
            return null;
        }

        String[] arg = str.split(",");
        double[] parsed = new double[3];
        for (int a = 0; a < 3; a++) {
            parsed[a] = Double.parseDouble(arg[a+1]);
        }

        Location location = new Location (Bukkit.getWorld(arg[0]), parsed[0], parsed[1], parsed[2]);

        //if bukkit ever changes how the world part of a location is printed then this will break
        if (arg[0].equals("CraftWorld{name=world}")) {
            location.setWorld(Bukkit.getServer().getWorlds().get(0));

        }
        if (arg[0].equals("CraftWorld{name=world_nether}")) {
            location.setWorld(Bukkit.getServer().getWorlds().get(1));

        }
        if (arg[0].equals("CraftWorld{name=world_the_end}")) {
            location.setWorld(Bukkit.getServer().getWorlds().get(2));

        }

        return location;
    }

}


/*
    TrackedEntity(UUID, ENTITYLISTMEMBERSHIP , EntityType) {

        getLocation() {
        }


    }





    TrackedEntity(Params..., EntityType) {


        ItemTrackedEntity itemTrackedEntity;
        PlayerTracked

        if (EntityType = item) {
            ItemTrackedEntity itemTrackedEntity= new itemTrackedEntity(Params...);
        }
        if (EntityType = player) {
            PlayerTrackedEntity playerTrackedEntity = new playerTrackedEntity(Params...);
        }
        if(EntityType = mob) {
           MobTrackedEntity mobTrackedEntity = new
        }


        getLocation() {
            if (EntityType = item) {
                itemTrackedEntity.getLocation
        }
        if (EntityType = player) {
            entityAbstraction = new playerTrackedEntity(Params...);
        }
        if(EntityType = mob) {
           MobTrackedEntity mobTrackedEntity = new
        }
        }
        getLocation() {
            if(Type = player) {
                new playerTrackedEntity();
            }

            if(type = item) {

            }

        }

        getName() {
            return entityAbstraction.getName();
        }

    }




 */



