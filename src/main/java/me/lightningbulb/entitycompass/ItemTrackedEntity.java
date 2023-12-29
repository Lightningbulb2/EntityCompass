package me.lightningbulb.entitycompass;


import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;


import java.io.Serializable;
import java.util.UUID;

public class ItemTrackedEntity extends TrackedEntity implements Serializable {

    // for some reason this is always null when it is saved and reloaded
    transient Plugin plugin = EntityCompass.getPlugin(EntityCompass.class);



    private UUID uuid;
    private EntityList entityListMembership;

    private UUID playerHoldingItem;

    private UUID lastPlayerHoldingItem;

    private String name;

    private String blockStorageLocation;

    //figure this out at some point to get rid of using items as chunk loaders
    private String lastGroundLocation;

    public ItemTrackedEntity(UUID uuid, EntityList entityListMembership , UUID playerHoldingItem) {

        this.uuid = uuid;
        this.entityListMembership = entityListMembership;
        this.playerHoldingItem = playerHoldingItem;
        this.lastPlayerHoldingItem = playerHoldingItem;


    }

    public UUID getUUID() {
        return uuid;
    }

    public EntityList getEntityListMembership() {
        return entityListMembership;
    }

    public void setBlockStorageLocation(Location location) {
        blockStorageLocation = loc2Str(location);
    }

    public void setLastGroundLocation(Location location) {lastGroundLocation = loc2Str(location);}

    public Location getLocation() {

        if (blockStorageLocation != null) {

            Location location = str2Loc(blockStorageLocation);

            World world = location.getWorld();

            Container container = (Container) world.getBlockAt(location).getState();

            ItemStack[] allContainerItems= null;



            if (container instanceof Chest) {

                //handle differently if it is a double chest
                Chest chest = ((Chest) container); // The Chest blockstate of one of them.
                InventoryHolder holder = chest.getInventory().getHolder();
                if (holder instanceof DoubleChest) {
                    DoubleChest doubleChest = ((DoubleChest) holder);
                    Chest leftChest = (Chest) doubleChest.getLeftSide();
                    Chest rightChest = (Chest) doubleChest.getRightSide();

                    allContainerItems = rightChest.getBlockInventory().getContents();

                    for (ItemStack itemStack : allContainerItems) {

                        if (itemStack != null && itemStack.hasItemMeta()) {
                            NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                            ItemMeta itemMeta = itemStack.getItemMeta();

                            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                            if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                                blockStorageLocation = loc2Str(rightChest.getLocation());
                                return str2Loc(blockStorageLocation);
                            }
                        }
                    }

                    allContainerItems = leftChest.getBlockInventory().getContents();

                    for (ItemStack itemStack : allContainerItems) {

                        if (itemStack != null && itemStack.hasItemMeta()) {
                            NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                            ItemMeta itemMeta = itemStack.getItemMeta();

                            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                            if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                                blockStorageLocation = loc2Str(leftChest.getLocation());
                                return str2Loc(blockStorageLocation);
                            }
                        }
                    }
                }

                //just do this if it is a single chest
                allContainerItems = ((Chest) container).getBlockInventory().getContents();
            }else {
                allContainerItems = container.getInventory().getContents();
            }

            for (ItemStack itemStack : allContainerItems) {

                if (itemStack != null && itemStack.hasItemMeta()) {
                    NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                    ItemMeta itemMeta = itemStack.getItemMeta();

                    PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                    String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                    if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                        return location;
                    }
                }
            }

            blockStorageLocation = null;

        }

        if (playerHoldingItem != null) {
            return Bukkit.getPlayer(playerHoldingItem).getLocation();
        }

        else {

            PersistentDataContainer data;

            for (World world : Bukkit.getWorlds()) {

                for (Entity entity : world.getEntities()) {


                    if (entity instanceof Item) {

                        if (((Item) entity).getItemStack().hasItemMeta()) {

                            NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                            ItemMeta itemMeta = ((Item) entity).getItemStack().getItemMeta();

                            data = itemMeta.getPersistentDataContainer();

                            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                            if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                                return entity.getLocation();
                            }

                        }
                    }

                }
            }


        }


        if (lastGroundLocation != null) {
            Location location = str2Loc(lastGroundLocation);

                for (Entity entity : location.getChunk().getEntities()) {
                    if (entity instanceof Item) {
                        ItemStack itemStack = ((Item) entity).getItemStack();
                        if (itemStack.hasItemMeta()) {

                            NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                            ItemMeta itemMeta = ((Item) entity).getItemStack().getItemMeta();

                            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                            if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                                return entity.getLocation();
                            }
                        }
                    }
                }
        }


        return null;
    }

    public void setPlayerHoldingItem(UUID playerHoldingItem) {
        this.playerHoldingItem = playerHoldingItem;

        //MINIGAME FUNCTIONALITY
        if (playerHoldingItem != null) {
            this.lastPlayerHoldingItem = playerHoldingItem;
        }
    }

    //MINIGAME FUNCTIONALITY
    public UUID getLastPlayerHoldingItem() {
        return lastPlayerHoldingItem;
    }

    public UUID getPlayerHoldingItem() {
        return playerHoldingItem;
    }

    public String getName() {

        ItemStack itemStack = getItemReference();

        if (itemStack != null) {
        ItemMeta itemMeta = itemStack.getItemMeta();

            if (!itemMeta.getDisplayName().equals("")) {
                this.name = itemMeta.getDisplayName();
                return itemMeta.getDisplayName();
            }
            this.name = itemStack.getType().toString();
            return itemStack.getType().toString();
        }


        return name;
    }

    public Location getFirstOverworldLocation() {
        return getLocationByKey("firstNormalCoordinates");
    }

    public Location getLastOverworldLocation() {
        return getLocationByKey("lastNormalCoordinates");
    }

    public Location getFirstNetherLocation() {
        return getLocationByKey("firstNetherCoordinates");
    }

    public Location getLastNetherLocation() {

       return getLocationByKey("lastNetherCoordinates");
    }

    public Location getLastEndLocation() {
        return getLocationByKey("lastEndCoordinates");
    }

    public boolean isOnline() {
        if (playerHoldingItem != null && Bukkit.getPlayer(playerHoldingItem) == null) {
            return false;
        }
        return true;
    }

    public void removeSelf() {

        NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");
        NamespacedKey relicKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "isRelic");
        NamespacedKey lastNetherCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "lastNetherCoordinates");
        NamespacedKey lastNormalCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "lastNormalCoordinates");
        NamespacedKey lastEndCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "lastEndCoordinates");
        NamespacedKey firstNetherCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "firstNetherCoordinates");
        NamespacedKey firstNormalCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "firstNormalCoordinates");



        ItemStack itemStack = getItemReference();

        if (itemStack != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

            data.remove(uuidKey);
            data.remove(relicKey);
            data.remove(lastNetherCoordinatesKey);
            data.remove(lastNormalCoordinatesKey);
            data.remove(lastEndCoordinatesKey);
            data.remove(firstNormalCoordinatesKey);
            data.remove(firstNetherCoordinatesKey);

            itemStack.setItemMeta(itemMeta);

            itemMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);

            itemStack.setItemMeta(itemMeta);

            itemStack.removeEnchantment(Enchantment.LUCK);

            entityListMembership.removeTrackedEntity(this);
        }
    }

    public Location getLocationByKey (String key) {

        ItemStack itemStack = getItemReference();

        NamespacedKey CoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), key);

        ItemMeta itemMeta = itemStack.getItemMeta();

        PersistentDataContainer data = itemMeta.getPersistentDataContainer();

        return str2Loc(data.get(CoordinatesKey, PersistentDataType.STRING));
    }

    public ItemStack getItemReference() {

        if (blockStorageLocation != null) {

            Location location = str2Loc(blockStorageLocation);

            World world = location.getWorld();

            Container container = (Container) world.getBlockAt(location).getState();

            ItemStack[] allContainerItems= null;



            if (container instanceof Chest) {

                //handle differently if it is a double chest
                Chest chest = ((Chest) container); // The Chest blockstate of one of them.
                InventoryHolder holder = chest.getInventory().getHolder();
                if (holder instanceof DoubleChest) {
                    DoubleChest doubleChest = ((DoubleChest) holder);
                    Chest leftChest = (Chest) doubleChest.getLeftSide();
                    Chest rightChest = (Chest) doubleChest.getRightSide();

                    allContainerItems = rightChest.getBlockInventory().getContents();

                    for (ItemStack itemStack : allContainerItems) {

                        if (itemStack != null && itemStack.hasItemMeta()) {
                            NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                            ItemMeta itemMeta = itemStack.getItemMeta();

                            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                            if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                                blockStorageLocation = loc2Str(rightChest.getLocation());
                                return itemStack;
                            }
                        }
                    }

                    allContainerItems = leftChest.getBlockInventory().getContents();

                    for (ItemStack itemStack : allContainerItems) {

                        if (itemStack != null && itemStack.hasItemMeta()) {
                            NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                            ItemMeta itemMeta = itemStack.getItemMeta();

                            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                            if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                                blockStorageLocation = loc2Str(leftChest.getLocation());
                                return itemStack;
                            }
                        }
                    }
                }

                //just do this if it is a single chest
                allContainerItems = ((Chest) container).getBlockInventory().getContents();
            }else {
                allContainerItems = container.getInventory().getContents();
            }

            for (ItemStack itemStack : allContainerItems) {

                if (itemStack != null && itemStack.hasItemMeta()) {
                    NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                    ItemMeta itemMeta = itemStack.getItemMeta();

                    PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                    String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                    if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                        return itemStack;
                    }
                }
            }

            blockStorageLocation = null;

        }

        if (playerHoldingItem != null) {

            if (Bukkit.getPlayer(playerHoldingItem) == null) {
                return null;
            }

            ItemStack cursorItemStack = Bukkit.getPlayer(playerHoldingItem).getItemOnCursor();

            if (cursorItemStack != null && cursorItemStack.hasItemMeta()) {

                    NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                    ItemMeta itemMeta = cursorItemStack.getItemMeta();

                    PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                    String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                    if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                        return cursorItemStack;
                    }

            }

            ItemStack[] playerInventory = Bukkit.getPlayer(playerHoldingItem).getInventory().getContents();


            for (ItemStack itemStack : playerInventory) {

                if (itemStack != null) {
                    NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                    ItemMeta itemMeta = itemStack.getItemMeta();

                    PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                    String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                    if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                       return itemStack;
                    }
                }
            }

            //#getItemOnCursor() only works in survival because the server doesn't care to authenticate creative players
            if (Bukkit.getPlayer(playerHoldingItem).getItemOnCursor().getType() != Material.AIR) {

                NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                ItemStack itemStack = Bukkit.getPlayer(playerHoldingItem).getItemOnCursor();

                if (itemStack.hasItemMeta()) {
                    ItemMeta itemMeta = itemStack.getItemMeta();

                    PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                    String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                    if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                        if (!itemMeta.getDisplayName().equals("")) {
                            return itemStack;
                        }

                    }
                }
            }

        }

        else {

            PersistentDataContainer data;

            for (World world : Bukkit.getWorlds()) {

                for (Entity entity : world.getEntities()) {


                    if (entity instanceof Item) {

                        if (((Item) entity).getItemStack().hasItemMeta()) {

                            NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                            ItemMeta itemMeta = ((Item) entity).getItemStack().getItemMeta();

                            data = itemMeta.getPersistentDataContainer();

                            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                            if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                                return ((Item) entity).getItemStack();
                            }

                        }
                    }

                }
            }


        }


        if (lastGroundLocation != null) {
            Location location = str2Loc(lastGroundLocation);
            Chunk chunk = location.getChunk();
            chunk.load();
            for (Entity entity : location.getChunk().getEntities()) {
                if (entity instanceof Item) {
                    ItemStack itemStack = ((Item) entity).getItemStack();
                    if (itemStack.hasItemMeta()) {

                        NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                        ItemMeta itemMeta = ((Item) entity).getItemStack().getItemMeta();

                        PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                        String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                        if (itemuuid != null && itemuuid.equals(uuid.toString())) {
                            return itemStack;
                        }
                    }
                }
            }
        }


        return null;
    }



}
