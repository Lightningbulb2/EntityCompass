package me.lightningbulb.entitycompass;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;


import java.io.File;
import java.io.IOException;
import java.util.*;


public final class EntityCompass extends JavaPlugin implements Listener {

    EntityTrackingCommands entityTrackingMethods;

    BukkitTask voidItemChecker;
    
    Plugin plugin;

    boolean RelicRush = false;

    @Override
    public void onEnable() {
        // Plugin startup logic

        //register holding compass listener
        new CompassListener(this);

        plugin = this;

        entityTrackingMethods = new EntityTrackingCommands(this);

        getServer().getPluginCommand("EntityCompass").setExecutor(entityTrackingMethods);

        // Plugin startup logic
        System.out.println("EntityCompass has started");

        getServer().getPluginManager().registerEvents(this, this);

        //loading and directory validation
        ////////////////////
        File EntityListData = new File(this.getDataFolder(), "");

        if (!EntityListData.exists()) {
            EntityListData.mkdirs();
            System.out.println("[EntityCompass] storage file not found... creating");

        }

        File path = new File(this.getDataFolder(), "/entitylists.dat");

        ArrayList<EntityList> entityListArray = null;


        if (!path.exists()) {
            try {
                path.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
           entityListArray = FlatFile.load(this.getDataFolder() + "/entitylists.dat");
        }

        if (entityListArray != null) {
            entityTrackingMethods.setEntityListArray(entityListArray);
        }

        /////////////////////


        //autosave every 30 seconds
        new BukkitRunnable() {

            @Override
            public void run() {
                // save
                saveFile();
            }

        }.runTaskTimer(this, 0, 600);


        //check if tracked item is falling into the void
        voidItemChecker = new BukkitRunnable() {
            @Override
            public void run() {

                //search through all entities and find items
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof Item) {


                        ItemStack itemStack = ((Item) entity).getItemStack();

                        //check that it has meta and that it has a trackerUUID
                        if (itemStack.hasItemMeta()) {
                            NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                            ItemMeta itemMeta = itemStack.getItemMeta();

                            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                            //check if item is about to fall into the void
                            if (itemuuid != null && entity.getLocation().getY() < -55) {

                                //throw item up and turn gravity back on in 20 secs
                                entity.setGravity(false);
                                entity.setVelocity(new Vector(0, 2.45, 0));
                                entity.setGlowing(true);
                                World itemWorld = entity.getLocation().getWorld();
                                itemWorld.playSound(entity.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);
                                itemWorld.spawnParticle(Particle.FIREWORKS_SPARK, entity.getLocation(), 3);

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        entity.setGravity(true);
                                    }
                                }.runTaskLater(plugin, 400);
                            }
                        }
                    }
                }

            }

            }
        }.runTaskTimer(this, 0, 4);


        //if SpecialItem team exists then the minigame is active so give players compass
        for (EntityList entityList : this.entityTrackingMethods.getEntityListArray()) {
            if(entityList.getEntityListName().equals("SpecialItem")) {
                RelicRush = true;
            }
        }

    }

    public void saveFile() {
        FlatFile.save(this.getDataFolder() + "/entitylists.dat", entityTrackingMethods.getEntityListArray());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("[EntityCompass] " + "plugin has shut down");

        voidItemChecker.cancel();

        //save
        FlatFile.save(this.getDataFolder() + "/entitylists.dat",entityTrackingMethods.getEntityListArray());

    }


    //MINIGAME
    public void setRelicRushStatus(boolean bool) {
        this.RelicRush = bool;
    }
    public boolean getRelicRushStatus() {
        return this.RelicRush;
    }

}



class CompassListener implements Listener {

    private final EntityCompass plugin;

    public CompassListener(EntityCompass plugin) {
        this.plugin = plugin;

        this.plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {


        //run the compass updater when a player joins the server
        new BukkitRunnable() {

            final Player player = event.getPlayer();

            @Override
            public void run() {

                // What you want to schedule goes here
                checkHand(player);

                if (!player.isOnline()) {
                    this.cancel();
                }

            }

        }.runTaskTimer(plugin, 0, 1);



        //get rid of item data if previous trackedEntity associated with it was deleted
        for (ItemStack itemStack : event.getPlayer().getInventory().getContents()) {

            if (itemHasTrackerUUID(itemStack)) {

                NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                PersistentDataContainer data = itemStack.getItemMeta().getPersistentDataContainer();

                if (plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(data.get(uuidKey, PersistentDataType.STRING))) == null) {


                    NamespacedKey relicKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "isRelic");
                    NamespacedKey lastNetherCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "lastNetherCoordinates");
                    NamespacedKey lastNormalCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "lastNormalCoordinates");
                    NamespacedKey lastEndCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "lastEndCoordinates");
                    NamespacedKey firstNetherCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "firstNetherCoordinates");
                    NamespacedKey firstNormalCoordinatesKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "firstNormalCoordinates");




                    ItemMeta itemMeta = itemStack.getItemMeta();

                    data = itemMeta.getPersistentDataContainer();

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

                }
            }
        }

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        if (plugin.RelicRush) {
            event.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
        }
    }

    //toggle lists and entities that someone is tracking
    @EventHandler
    public void compassChanger(PlayerInteractEvent event) {

        ArrayList<EntityList> entityListArray = plugin.entityTrackingMethods.getEntityListArray();

        TrackedEntity trackedEntity = plugin.entityTrackingMethods.getTrackedEntity(event.getPlayer().getUniqueId());

        //cancel if current player is not on an entity list
        if (trackedEntity == null) {
            return;
        }

        //cancel if tracked entity is null
        if (trackedEntity.getPointedTrackedEntity() == null) {
            return;
        }

        EntityList entityListMembership = trackedEntity.getEntityListMembership();

        boolean compassHeld = trackedEntity.getCompassHeld();

        boolean validHand = false;

        if (event.getHand() == EquipmentSlot.HAND) {
            validHand = true;
        }
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            validHand = true;
        }


        if (compassHeld && validHand) {

            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

                //swap entity list pointing
                if (event.getPlayer().isSneaking()) {
                    int index = 0;
                    for (EntityList entityList : entityListArray) {


                        if (entityList == trackedEntity.getPointedEntityList()) {
                            int startingIndex = index;
                            int searchingIndex = index;


                            do {
                                //reset to zero when it reaches the end of the array
                                if (searchingIndex + 1 >= entityListArray.size()) {
                                    searchingIndex = 0;
                                } else {
                                    searchingIndex++;
                                }

                                if (entityListArray.get(searchingIndex).getTrackedEntityArray().size() != 0 && entityListArray.get(searchingIndex) != entityListMembership) {
                                    trackedEntity.setPointedEntityList(entityListArray.get(searchingIndex));
                                    trackedEntity.setPointedTrackedEntity(trackedEntity.getPointedEntityList().getTrackedEntityArray().get(0));
                                    return;
                                }

                                if (entityListArray.get(searchingIndex) == entityListMembership && entityListArray.get(searchingIndex).getTrackedEntityArray().size() > 1) {

                                    for (TrackedEntity comparedTrackedEntity : entityListArray.get(searchingIndex).getTrackedEntityArray()) {
                                        if (trackedEntity != comparedTrackedEntity) {
                                            trackedEntity.setPointedEntityList(entityListMembership);
                                            trackedEntity.setPointedTrackedEntity(comparedTrackedEntity);
                                            return;
                                        }
                                    }

                                    return;
                                }


                            } while (searchingIndex != startingIndex);

                            return;

                        }
                        index++;
                    }

                }
                //swap player pointing
                else {
                    TrackedEntity pointedTrackedEntity = trackedEntity.getPointedTrackedEntity();

                    ArrayList<TrackedEntity> pointedTrackedEntityList = trackedEntity.getPointedEntityList().getTrackedEntityArray();

                    int index = 0;
                    for (TrackedEntity comparedPointedTrackedEntity : pointedTrackedEntityList) {

                        if (pointedTrackedEntity == comparedPointedTrackedEntity) {
                            int startingIndex = index;
                            int searchingIndex = index;

                            do {

                                //reset to zero when it reaches the end of the array
                                if (searchingIndex + 1 >= pointedTrackedEntityList.size()) {
                                    searchingIndex = 0;
                                } else {
                                    searchingIndex++;
                                }


                                if (pointedTrackedEntityList.get(searchingIndex) != pointedTrackedEntity && pointedTrackedEntityList.get(searchingIndex) != trackedEntity) {
                                    trackedEntity.setPointedTrackedEntity(pointedTrackedEntityList.get(searchingIndex));
                                    return;
                                }


                            } while (searchingIndex != startingIndex);

                            return;
                        }
                        index++;
                    }

                }
            }


        }

    }

    //executes all persistent updates to compass and ui
    public void checkHand(Player player) {

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));

        TrackedEntity trackedEntity = plugin.entityTrackingMethods.getTrackedEntity(player.getUniqueId());

        if (trackedEntity == null) {
            return;
        }

        TrackedEntity pointedTrackedEntity = trackedEntity.getPointedTrackedEntity();

        //tries to find default player if none was found already tracked, and verifies that the current pointed is on the team you think they are
        if (pointedTrackedEntity == null || pointedTrackedEntity.getEntityListMembership() == null) {
            plugin.entityTrackingMethods.defaultPointer(player);
            return;
        }

        if (!trackedEntity.getPointedEntityList().isMember(pointedTrackedEntity)) {
            plugin.entityTrackingMethods.defaultPointer(player);
            player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "TrackedEntity has swapped lists or been removed.");
            return;
        }



        //check that player hand is not empty and check if they are holding compass
        Material materialInMainHand = player.getInventory().getItemInMainHand().getType();
        Material materialInOffHand = player.getInventory().getItemInOffHand().getType();


        if ((materialInMainHand == Material.COMPASS) ||
                materialInOffHand == Material.COMPASS) {

            trackedEntity.setCompassHeld(true);
            updateCompass(player);

            //base tracking text
            String actionBarMessage = "Tracking " + pointedTrackedEntity.getName();

            World.Environment trackedEntityDimension = World.Environment.NORMAL;

            try {
                trackedEntityDimension = pointedTrackedEntity.getLocation().getWorld().getEnvironment();
            }catch (NullPointerException e) {
                // Can't track offline player dimensions so just have it to normal
            }

            //set color depending on tracked dimension
            if (trackedEntityDimension == World.Environment.NORMAL) {
                actionBarMessage = ChatColor.GREEN + actionBarMessage;
            }
            if (trackedEntityDimension == World.Environment.NETHER) {
                actionBarMessage = ChatColor.RED + actionBarMessage;
            }
            if (trackedEntityDimension == World.Environment.THE_END) {
                actionBarMessage = ChatColor.DARK_AQUA + actionBarMessage;
            }

            //tell if player is online or not
            if (!pointedTrackedEntity.isOnline()) {
                actionBarMessage += " (Offline)";
            }


            // add team relation
            if (trackedEntity.getEntityListMembership() == pointedTrackedEntity.getEntityListMembership()) {
                actionBarMessage += ChatColor.BLUE + " Same EntityList";
            }else {
                actionBarMessage += ChatColor.BLUE + " EntityList: " + trackedEntity.getPointedEntityList().getEntityListName();
            }




            //display on the action bar
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));

        }
        else {
            //if there is no compass in hand
            trackedEntity.setCompassHeld(false);
        }


    }

    //this decides what data to apply to the compass when its held
    public void updateCompass(Player player) {


        TrackedEntity trackedEntity = plugin.entityTrackingMethods.getTrackedEntity(player.getUniqueId());

        TrackedEntity pointedTrackedEntity = trackedEntity.getPointedTrackedEntity();

        if (pointedTrackedEntity == null) {
            return;
        }

        if (!pointedTrackedEntity.isOnline()) {
            return;
        }

        CompassMeta meta = null;
        ItemStack handItem = null;


        Material materialInMainHand = player.getInventory().getItemInMainHand().getType();
        Material materialInOffHand = player.getInventory().getItemInOffHand().getType();

        if (materialInMainHand == Material.COMPASS) {
            meta = (CompassMeta) player.getInventory().getItemInMainHand().getItemMeta();
            handItem = player.getInventory().getItemInMainHand();
        }
        else if (materialInOffHand == Material.COMPASS) {
            meta = (CompassMeta) player.getInventory().getItemInOffHand().getItemMeta();
            handItem = player.getInventory().getItemInOffHand();
        }

        World.Environment nether = World.Environment.NETHER;
        World.Environment overworld = World.Environment.NORMAL;
        World.Environment theEnd = World.Environment.THE_END;

        World.Environment compassHolderDimension = player.getWorld().getEnvironment();

        World.Environment pointedEntityDimension = pointedTrackedEntity.getLocation().getWorld().getEnvironment();

        if (compassHolderDimension == nether && pointedEntityDimension == nether) {
            meta.setLodestone(pointedTrackedEntity.getLocation());
            handItem.setItemMeta(meta);
        }


        if (compassHolderDimension == overworld && pointedEntityDimension == overworld) {
            meta.setLodestone(pointedTrackedEntity.getLocation());
            handItem.setItemMeta(meta);
        }


        if (compassHolderDimension == nether &&
                (pointedEntityDimension == overworld || pointedEntityDimension == theEnd)) {

            //try tracked player's last coordinates
            try {
                meta.setLodestone(pointedTrackedEntity.getLastNetherLocation());

            }catch (Exception e) {

                //try current player's first coordinates entering this dimension
                try {
                    meta.setLodestone(trackedEntity.getFirstNetherLocation());

                    //if all else fails then the plugin doesn't have enough information
                }catch (Exception ex) {
                    player.sendMessage(ChatColor.DARK_RED + "[EntityCompass] "+ "Entity untraceable");
                }
            }

            handItem.setItemMeta(meta);
        }


        if (compassHolderDimension == overworld && pointedEntityDimension == nether) {
            try {
                meta.setLodestone(pointedTrackedEntity.getLastOverworldLocation());
                handItem.setItemMeta(meta);

            }catch (Exception e) {

                try{
                    meta.setLodestone(trackedEntity.getFirstOverworldLocation());
                    handItem.setItemMeta(meta);
                }catch (Exception ex) {
                    player.sendMessage(ChatColor.DARK_RED + "[EntityCompass] "+ "Entity untraceable");
                }

            }
        }


        if (compassHolderDimension == overworld && pointedEntityDimension == theEnd) {

            meta.setLodestone(pointedTrackedEntity.getLastOverworldLocation());
            handItem.setItemMeta(meta);
        }
        if (compassHolderDimension == theEnd && pointedEntityDimension == theEnd) {

            meta.setLodestone(pointedTrackedEntity.getLocation());
            handItem.setItemMeta(meta);
        }

        if (compassHolderDimension == theEnd &&
                (pointedEntityDimension == overworld || pointedEntityDimension == nether)) {

            try {
                meta.setLodestone(pointedTrackedEntity.getLastEndLocation());
            }

            catch (Exception e) {
                meta.setLodestone(new Location(Bukkit.getServer().getWorlds().get(2), 0, 0, 0));
            }

            handItem.setItemMeta(meta);
        }


    }


    //stores the last coordinates of a previous dimension when a player leaves it
    @EventHandler
    public void portalEvent(PlayerPortalEvent event) {

        NamespacedKey lastNetherCoordinatesKey = new NamespacedKey(plugin, "lastNetherCoordinates");
        NamespacedKey lastNormalCoordinatesKey = new NamespacedKey(plugin, "lastNormalCoordinates");
        NamespacedKey lastEndCoordinatesKey = new NamespacedKey(plugin, "lastEndCoordinates");

        PersistentDataContainer data = event.getPlayer().getPersistentDataContainer();


        //find tracked items in player inventory to set their data

        UUID playerUUID = event.getPlayer().getUniqueId();
        ItemStack[] playerInventory = Bukkit.getPlayer(playerUUID).getInventory().getContents();

        ArrayList<ItemStack> itemStackArray = new ArrayList<>();

        for (ItemStack itemStack : playerInventory) {

            if (itemStack != null) {
                NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                ItemMeta itemMeta = itemStack.getItemMeta();
                PersistentDataContainer itemdata = itemMeta.getPersistentDataContainer();

                String itemuuid = itemdata.get(uuidKey, PersistentDataType.STRING);

                if (itemuuid != null) {
                    itemStackArray.add(itemStack);
                }
            }
        }


        World.Environment playerEnvironment = event.getPlayer().getWorld().getEnvironment();

        if (playerEnvironment == World.Environment.NETHER) {

            String netherCoordinates = loc2Str(event.getPlayer().getLocation());
            data.set(lastNetherCoordinatesKey, PersistentDataType.STRING, netherCoordinates);

            //set dimension change data to tracked items in inventory
            for (ItemStack itemStack : itemStackArray) {

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

                    itemData.set(lastNetherCoordinatesKey, PersistentDataType.STRING, netherCoordinates);

                    itemStack.setItemMeta(itemMeta);
            }
        }

        if (playerEnvironment == World.Environment.NORMAL) {

            String normalCoordinates = loc2Str(event.getPlayer().getLocation());
            data.set(lastNormalCoordinatesKey, PersistentDataType.STRING, normalCoordinates);

            //set dimension change data to tracked items in inventory
            for (ItemStack itemStack : itemStackArray) {

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

                    itemData.set(lastNormalCoordinatesKey, PersistentDataType.STRING, normalCoordinates);

                    itemStack.setItemMeta(itemMeta);

            }
        }

        if (playerEnvironment == World.Environment.THE_END) {

            String endCoordinates = loc2Str(event.getPlayer().getLocation());
            data.set(lastEndCoordinatesKey, PersistentDataType.STRING, endCoordinates);

            //set dimension change data to tracked items in inventory
            for (ItemStack itemStack : itemStackArray) {

                NamespacedKey relicKey = new NamespacedKey(plugin, "isRelic");

                ItemMeta itemMeta = itemStack.getItemMeta();
                PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

                itemData.set(lastEndCoordinatesKey, PersistentDataType.STRING, endCoordinates);

                itemStack.setItemMeta(itemMeta);

                String isRelic = itemData.get(relicKey, PersistentDataType.STRING);

                    ///MINIGAME FUNCTIONALITY
                if (isRelic != null) {
                    Bukkit.broadcastMessage(ChatColor.BLUE + event.getPlayer().getName() + " has thrown the item into the portal!");
                }
            }
        }


    }

    //stores the location of a player when they just enter a new dimension
    @EventHandler
    public void portalExitEvent(PlayerChangedWorldEvent event) {


        Player player = event.getPlayer();

        NamespacedKey firstNetherCoordinatesKey = new NamespacedKey(plugin, "firstNetherCoordinates");
        NamespacedKey firstNormalCoordinatesKey = new NamespacedKey(plugin, "firstNormalCoordinates");
        NamespacedKey lastEndCoordinatesKey = new NamespacedKey(plugin, "lastEndCoordinates");

        PersistentDataContainer data = player.getPlayer().getPersistentDataContainer();

        //find tracked items in player inventory to set their data
        ItemStack[] playerInventory = Bukkit.getPlayer(event.getPlayer().getUniqueId()).getInventory().getContents();

        ArrayList<ItemStack> itemStackArray = new ArrayList<>();

        //set dimension change data to tracked items in inventory
        for (ItemStack itemStack : playerInventory) {

            if (itemStack != null) {
                NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                ItemMeta itemMeta = itemStack.getItemMeta();
                PersistentDataContainer itemdata = itemMeta.getPersistentDataContainer();

                String itemuuid = itemdata.get(uuidKey, PersistentDataType.STRING);

                if (itemuuid != null) {
                    itemStackArray.add(itemStack);
                }
            }
        }



        if (player.getPlayer().getWorld().getEnvironment() == World.Environment.NETHER) {

            String firstNetherCoordinates = loc2Str(player.getPlayer().getLocation());
            data.set(firstNetherCoordinatesKey, PersistentDataType.STRING, firstNetherCoordinates);

            //set dimension change data to tracked items in inventory
            for (ItemStack itemStack : itemStackArray) {

                ItemMeta itemMeta = itemStack.getItemMeta();
                PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

                itemData.set(firstNormalCoordinatesKey, PersistentDataType.STRING, firstNetherCoordinates);

                itemStack.setItemMeta(itemMeta);
            }

        }

        if (player.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL) {

            String firstNormalCoordinates = loc2Str(player.getPlayer().getLocation());
            data.set(firstNormalCoordinatesKey, PersistentDataType.STRING, firstNormalCoordinates);

            //set dimension change data to tracked items in inventory
            for (ItemStack itemStack : itemStackArray) {

                ItemMeta itemMeta = itemStack.getItemMeta();
                PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

                itemData.set(firstNormalCoordinatesKey, PersistentDataType.STRING, firstNormalCoordinates);

                itemStack.setItemMeta(itemMeta);
            }
        }

        if (event.getFrom().getEnvironment().equals(World.Environment.THE_END)) {
            String lastEndCoordinates = loc2Str(player.getPlayer().getLocation());
            data.set(lastEndCoordinatesKey, PersistentDataType.STRING, lastEndCoordinates);

            //set dimension change data to tracked items in inventory
            for (ItemStack itemStack : itemStackArray) {

                NamespacedKey relicKey = new NamespacedKey(plugin, "isRelic");

                ItemMeta itemMeta = itemStack.getItemMeta();
                PersistentDataContainer itemData = itemMeta.getPersistentDataContainer();

                itemData.set(firstNormalCoordinatesKey, PersistentDataType.STRING, lastEndCoordinates);

                itemStack.setItemMeta(itemMeta);

                ///MINIGAME FUNCTIONALITY

                String isRelic = itemData.get(relicKey, PersistentDataType.STRING);

                if (isRelic != null) {
                    Bukkit.broadcastMessage(ChatColor.BLUE + event.getPlayer().getName() + " has thrown the item into the portal!");
                }

            }
        }


    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {

        if (event.getEntity() instanceof Player) {
            NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

            Player player = ((Player) event.getEntity()).getPlayer();

            ItemMeta itemMeta = event.getItem().getItemStack().getItemMeta();
            PersistentDataContainer itemdata = itemMeta.getPersistentDataContainer();

            String itemuuid = itemdata.get(uuidKey, PersistentDataType.STRING);

            if (itemuuid != null) {
                TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));
                itemTrackedEntity.setPlayerHoldingItem(player.getUniqueId());
            }
        }

    }


    @EventHandler
    public void onItemDrop(ItemSpawnEvent event) {


        if (event.getEntity().getItemStack().hasItemMeta()) {

            NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

            ItemMeta itemMeta = event.getEntity().getItemStack().getItemMeta();

            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

            if (itemuuid != null) {
                event.getEntity().setInvulnerable(true);

                TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                itemTrackedEntity.setBlockStorageLocation(null);
                itemTrackedEntity.setPlayerHoldingItem(null);

            }

        }

    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {

        if (event.getEntity().getItemStack().hasItemMeta()) {

            NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

            ItemMeta itemMeta = event.getEntity().getItemStack().getItemMeta();

            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

            if (itemuuid != null) {
                event.setCancelled(true);
            }

        }
    }


    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {


        if (event.getEntity() instanceof Item) {

            if (((Item) event.getEntity()).getItemStack().hasItemMeta()) {

                NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                ItemMeta itemMeta = ((Item) event.getEntity()).getItemStack().getItemMeta();

                PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                if (itemuuid != null) {
                    event.setCancelled(true);

                    if (event.getEntity().getLocation().getY() < -64) {
                        event.getEntity().setGravity(false);
                        event.getEntity().setVelocity(new Vector(0, 2.5, 0));


                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                event.getEntity().setGravity(true);
                            }
                        }.runTaskLater(plugin, 400);
                    }
                }

            }
        }
    }


    @EventHandler
    public void onItemPortalEnter(EntityPortalEnterEvent event) {
        if (event.getEntity() instanceof Item) {

            if (((Item) event.getEntity()).getItemStack().hasItemMeta()) {

                NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                ItemMeta itemMeta = ((Item) event.getEntity()).getItemStack().getItemMeta();

                PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                if (itemuuid != null) {

                    NamespacedKey lastNetherCoordinatesKey = new NamespacedKey(plugin, "lastNetherCoordinates");
                    NamespacedKey lastNormalCoordinatesKey = new NamespacedKey(plugin, "lastNormalCoordinates");
                    NamespacedKey lastEndCoordinatesKey = new NamespacedKey(plugin, "lastEndCoordinates");

                    World.Environment EntityEnvironment = event.getEntity().getWorld().getEnvironment();

                    if (EntityEnvironment == World.Environment.NETHER) {

                        String netherCoordinates = loc2Str(event.getEntity().getLocation());
                        data.set(lastNetherCoordinatesKey, PersistentDataType.STRING, netherCoordinates);
                    }

                    if (EntityEnvironment == World.Environment.NORMAL) {
                        String normalCoordinates = loc2Str(event.getEntity().getLocation());
                        data.set(lastNormalCoordinatesKey, PersistentDataType.STRING, normalCoordinates);
                    }

                    if (EntityEnvironment == World.Environment.THE_END) {

                        String endCoordinates = loc2Str(event.getEntity().getLocation());
                        data.set(lastEndCoordinatesKey, PersistentDataType.STRING, endCoordinates);



                        ///MINIGAME FUNCTIONALITY
                        NamespacedKey relicKey = new NamespacedKey(plugin, "isRelic");

                        String isRelic = data.get(relicKey, PersistentDataType.STRING);

                        TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                        Player lastPlayerHoldingItem = Bukkit.getPlayer(itemTrackedEntity.getLastPlayerHoldingItem());
                        if (isRelic != null) {
                            if (lastPlayerHoldingItem == null) {

                                Bukkit.broadcastMessage(ChatColor.BLUE + Bukkit.getOfflinePlayer(itemTrackedEntity.getLastPlayerHoldingItem()).getName() + " has thrown the item into the portal!");

                            } else {
                                Bukkit.broadcastMessage(ChatColor.BLUE + lastPlayerHoldingItem.getName() + " has thrown the item into the portal!");
                            }
                        }
                    }

                    ((Item) event.getEntity()).getItemStack().setItemMeta(itemMeta);

                }

            }
        }
    }

    @EventHandler
    public void onItemPortalExit(EntityPortalExitEvent event) {

        if (event.getEntity() instanceof Item) {

            if (((Item) event.getEntity()).getItemStack().hasItemMeta()) {

                NamespacedKey uuidKey = new NamespacedKey(EntityCompass.getPlugin(EntityCompass.class), "trackerUUID");

                ItemStack item = ((Item) event.getEntity()).getItemStack();

                ItemMeta itemMeta = item.getItemMeta();

                PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                if (itemuuid != null) {

                    NamespacedKey firstNetherCoordinatesKey = new NamespacedKey(plugin, "firstNetherCoordinates");
                    NamespacedKey firstNormalCoordinatesKey = new NamespacedKey(plugin, "firstNormalCoordinates");


                    if (event.getEntity().getWorld().getEnvironment() == World.Environment.NETHER) {
                        String firstNetherCoordinates = loc2Str(event.getEntity().getLocation());
                        data.set(firstNetherCoordinatesKey, PersistentDataType.STRING, firstNetherCoordinates);

                    }

                    if (event.getEntity().getWorld().getEnvironment() == World.Environment.NORMAL) {
                        String firstNormalCoordinates = loc2Str(event.getEntity().getLocation());
                        data.set(firstNormalCoordinatesKey, PersistentDataType.STRING, firstNormalCoordinates);
                    }

                    item.setItemMeta(itemMeta);

                }

            }
        }
    }

    @EventHandler
    public void handleMovementOfTrackedItemsInContainers(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) {
            return;
        }

        //cancel when item is dropped into an invalid area (left or right click)



        if (event.isLeftClick() || event.isRightClick()) {

            if (invalidInventoryType(event.getClickedInventory().getType())) {

                if (itemHasTrackerUUID(event.getCursor())) {

                    event.setCancelled(true);
                }
            }

            //store item in container
            if (storageInventoryType(event.getClickedInventory().getType())) {
                if (itemHasTrackerUUID(event.getCursor())) {

                    Location location = event.getView().getTopInventory().getLocation();
                    NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                    ItemMeta itemMeta = event.getCursor().getItemMeta();

                    PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                    String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                    TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                    itemTrackedEntity.setBlockStorageLocation(location);
                    itemTrackedEntity.setPlayerHoldingItem(null);

                }
            }

            //pull item out of container
            if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
                if (itemHasTrackerUUID(event.getCursor())) {

                    NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                    ItemMeta itemMeta = event.getCursor().getItemMeta();

                    PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                    String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                    TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                    itemTrackedEntity.setBlockStorageLocation(null);
                    itemTrackedEntity.setPlayerHoldingItem(event.getWhoClicked().getUniqueId());

                }
            }

            ///pull item to cursor (player owned)
            if (storageInventoryType(event.getClickedInventory().getType())) {
                if (itemHasTrackerUUID(event.getCurrentItem())) {

                    NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                    ItemMeta itemMeta = event.getCurrentItem().getItemMeta();

                    PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                    String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                    TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                    itemTrackedEntity.setBlockStorageLocation(null);
                    itemTrackedEntity.setPlayerHoldingItem(event.getWhoClicked().getUniqueId());

                }
            }


        }

        //cancel shift clicks into invalid areas for tracked items (shift click items)
        if (event.getClick() == ClickType.SHIFT_LEFT ||
                event.getClick() == ClickType.SHIFT_RIGHT) {


            //shift clicking behaves different in player inventory so exclude that
            if (event.getView().getTopInventory().getType() == InventoryType.CRAFTING) {
                if (itemHasTrackerUUID(event.getCurrentItem())) {
                    return;
                }
            }




            if (invalidInventoryType(event.getView().getTopInventory().getType())) {

                if (itemHasTrackerUUID(event.getCurrentItem())) {
                    event.setCancelled(true);
                }
            }

            new BukkitRunnable() {
                @Override
                public void run() {


                   for (ItemStack itemStack2 : event.getView().getTopInventory().getContents()) {
                       if (itemHasTrackerUUID(itemStack2)) {

                           if (storageInventoryType(event.getView().getTopInventory().getType())) {

                               Location location = event.getView().getTopInventory().getLocation();
                               NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                               ItemMeta itemMeta = itemStack2.getItemMeta();

                               PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                               String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                               TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                               itemTrackedEntity.setBlockStorageLocation(location);
                               itemTrackedEntity.setPlayerHoldingItem(null);
                           }
                       }
                   }


                    for (ItemStack itemStack : event.getView().getBottomInventory().getContents()) {
                        if (itemHasTrackerUUID(itemStack)) {

                            NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                            ItemMeta itemMeta2 = itemStack.getItemMeta();

                            PersistentDataContainer data2 = itemMeta2.getPersistentDataContainer();

                            String itemuuid = data2.get(uuidKey, PersistentDataType.STRING);

                            TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(itemuuid);

                            if (itemTrackedEntity != null) {
                                itemTrackedEntity.setBlockStorageLocation(null);
                                itemTrackedEntity.setPlayerHoldingItem(event.getWhoClicked().getUniqueId());
                            }
                        }
                    }
                }

            }.runTaskLater(plugin, 0);

        }


        //cancel hotkeying for tracked items into invalid areas (hotkey)
        if (event.getClick() == ClickType.NUMBER_KEY) {

            ItemStack itemStack = event.getView().getBottomInventory().getItem(event.getHotbarButton());

            if (itemHasTrackerUUID(itemStack)) {

                if (invalidInventoryType(event.getClickedInventory().getType())) {
                    event.setCancelled(true);
                }

                if (storageInventoryType(event.getClickedInventory().getType())) {

                    Location location = event.getView().getTopInventory().getLocation();
                    NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                    ItemMeta itemMeta = itemStack.getItemMeta();

                    PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                    String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                    TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                    itemTrackedEntity.setBlockStorageLocation(location);
                    itemTrackedEntity.setPlayerHoldingItem(null);
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {

                    ItemStack itemStack = event.getView().getBottomInventory().getItem(event.getHotbarButton());

                    if (itemHasTrackerUUID(itemStack)) {

                        NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                        ItemMeta itemMeta = itemStack.getItemMeta();

                        PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                        String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                        TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                        itemTrackedEntity.setBlockStorageLocation(null);
                        itemTrackedEntity.setPlayerHoldingItem(event.getView().getPlayer().getUniqueId());
                    }
                }
            }.runTaskLater(plugin, 0);



        }
    }

    @EventHandler
    public void cancelInventoryDragOfTrackedItems(InventoryDragEvent event) {


        //cancel drag into crafting table for tracked items (drag items)
        if (itemHasTrackerUUID(event.getOldCursor())) {

            boolean affectedSlotsAreInInventory = true;

            for (int i : event.getRawSlots()) {

                if (i < event.getView().getTopInventory().getSize()) {
                    affectedSlotsAreInInventory = false;
                }
            }

            if (!affectedSlotsAreInInventory && invalidInventoryType(event.getView().getTopInventory().getType())) {
                event.setCancelled(true);
            }

            if (!affectedSlotsAreInInventory && storageInventoryType(event.getView().getTopInventory().getType())) {
                Location location = event.getView().getTopInventory().getLocation();
                NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                ItemMeta itemMeta = event.getOldCursor().getItemMeta();

                PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                itemTrackedEntity.setBlockStorageLocation(location);
                itemTrackedEntity.setPlayerHoldingItem(null);
            }

            if (affectedSlotsAreInInventory && event.getView().getBottomInventory().getType() == InventoryType.PLAYER) {
                NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                ItemMeta itemMeta = event.getOldCursor().getItemMeta();

                PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                itemTrackedEntity.setBlockStorageLocation(null);
                itemTrackedEntity.setPlayerHoldingItem(event.getWhoClicked().getUniqueId());
            }

        }



    }

    //handles hoppers moving tracked items
    @EventHandler
    public void handleTrackedItemHopperSwap(InventoryMoveItemEvent event) {
        if (itemHasTrackerUUID(event.getItem())) {

            ItemStack itemStack = event.getItem();

            NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

            ItemMeta itemMeta = itemStack.getItemMeta();

            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

            TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

            itemTrackedEntity.setBlockStorageLocation(event.getDestination().getLocation());
            itemTrackedEntity.setPlayerHoldingItem(null);

        }
    }

    //handles hoppers picking up tracked items
    @EventHandler
    public void handleTrackedItemHopperPickup(InventoryPickupItemEvent event) {
        if (itemHasTrackerUUID(event.getItem().getItemStack())) {

            ItemStack itemStack = event.getItem().getItemStack();

            NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

            ItemMeta itemMeta = itemStack.getItemMeta();

            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

            TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

            itemTrackedEntity.setBlockStorageLocation(event.getInventory().getLocation());
            itemTrackedEntity.setPlayerHoldingItem(null);

        }
    }


    //store location tracked item is in when a chunk is unloaded
    @EventHandler
    public void trackItemOnChunkUnload(ChunkUnloadEvent event) {
        boolean hasTrackedItem = false;

        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Item) {
                ItemStack itemStack = (ItemStack) ((Item) entity).getItemStack();

                if (itemHasTrackerUUID(itemStack)) {

                    hasTrackedItem = true;

                    NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

                    ItemMeta itemMeta = itemStack.getItemMeta();

                    PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                    String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

                    TrackedEntity itemTrackedEntity = plugin.entityTrackingMethods.getTrackedEntity(UUID.fromString(itemuuid));

                    itemTrackedEntity.setLastGroundLocation(entity.getLocation());
                }
            }
        }

    }


    public boolean invalidInventoryType(InventoryType type) {

        InventoryType[] validInventoryType = {
                InventoryType.CRAFTING,
                InventoryType.WORKBENCH,
                InventoryType.BEACON,
                InventoryType.CARTOGRAPHY,
                InventoryType.GRINDSTONE,
                InventoryType.ENCHANTING,
                InventoryType.LOOM,
                InventoryType.STONECUTTER,
                InventoryType.SMITHING,
                InventoryType.ENDER_CHEST,
                InventoryType.BREWING,
                InventoryType.MERCHANT,
                InventoryType.SHULKER_BOX

        };
        for (InventoryType inventoryType : validInventoryType) {
            if (type == inventoryType) {
                return true;
            }
        }
        return false;
    }

    public boolean storageInventoryType(InventoryType type) {

        InventoryType[] validInventoryType = {
                InventoryType.CHEST,
                InventoryType.FURNACE,
                InventoryType.BARREL,
                InventoryType.HOPPER,
                InventoryType.DROPPER,
                InventoryType.DISPENSER,
                InventoryType.SMOKER,

        };
        for (InventoryType inventoryType : validInventoryType) {
            if (type == inventoryType) {
                return true;
            }
        }
        return false;
    }

    public boolean itemHasTrackerUUID(ItemStack itemStack) {

        if (itemStack != null && itemStack.hasItemMeta()) {
            NamespacedKey uuidKey = new NamespacedKey(plugin, "trackerUUID");

            ItemMeta itemMeta = itemStack.getItemMeta();

            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

            String itemuuid = data.get(uuidKey, PersistentDataType.STRING);

            if (itemuuid != null) {
                return true;
            }
        }
        return false;
    }



    //convert locations to a format that can be reconstructed by str2Loc
    public String loc2Str (Location location) {

        World world = location.getWorld();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return world + "," + x + "," + y + "," + z;
    }

    //reconstruct locations from strings provided by the loc2Str method
    public Location str2Loc(String str) {

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
