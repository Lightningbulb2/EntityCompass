package me.lightningbulb.entitycompass;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class EntityTrackingCommands implements TabExecutor {



    private ArrayList<EntityList> entityListArray = new ArrayList<>();

    private final EntityCompass main;

    public EntityTrackingCommands(EntityCompass main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String Label, String[] args) {


        if (sender instanceof Player) {
            Player player = (Player) sender;

            TrackedEntity playerTrackedEntity = getTrackedEntity(player.getUniqueId());

            //verify command is complete
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED+"[EntityCompass] " + "You must specify an action.");
                return true;
            }

            //create EntityList command
            if (args.length > 1 && args[0].equals("create")) {

                //check for reserved name
                if (args[1].equals("SpecialItem")) {
                    player.sendMessage(ChatColor.RED + "[EntityCompass] " + "Sorry that is a reserved EntityList name");
                    return true;
                }

                //check for duplicated
                for (EntityList entityList : entityListArray) {
                    if (args[1].equals(entityList.getEntityListName())) {
                        player.sendMessage(ChatColor.RED + "[EntityCompass] " + "EntityList name is already taken");
                        return true;
                    }
                }

                //if there is no duplicate
                entityListArray.add(new EntityList(args[1]));
                player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "EntityList \"" + args[1] + "\" has been created.");
                return true;
            }


            //delete EntityList command
            if (args[0].equals("delete")) {

                if (args[1].equals("")) {
                    player.sendMessage(ChatColor.RED + "[EntityCompass] " + "Must specify EntityList name to delete");
                    return true;
                }

                for (EntityList entityList : entityListArray) {

                    //find correct entityList to delete
                    if (args[1].equals(entityList.getEntityListName())) {

                        //clear Entity list membership for all entities
                        entityList.setToNull();
                        entityListArray.remove(entityList);
                        entityList = null;

                        player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "EntityList \"" + args[1] + "\" has been deleted.");
                        return true;
                    }
                }
                //error message
                player.sendMessage(ChatColor.RED + "[EntityCompass] " + "EntityList \"" + args[1] + "\" not found.");
                return true;
            }

            //join an entityList as player
            if (args[0].equals("join")) {

                if (args[1].equals("")) {
                    player.sendMessage(ChatColor.RED + "An entityList name must be specified");
                    return true;
                }

                for (EntityList entityList : entityListArray) {

                    if (args[1].equals(entityList.getEntityListName())) {

                        //remove TrackedEntity from previous EntityList (if it exists)
                        if (playerTrackedEntity != null) {
                            EntityList parentEntityList = playerTrackedEntity.getEntityListMembership();
                            parentEntityList.removeTrackedEntity(playerTrackedEntity);
                        }

                        entityList.addTrackedEntity(new TrackedEntity(player.getUniqueId(), TrackedEntity.TrackedEntityType.PLAYER, entityList));
                        player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Successfully added to \"" + args[1] + "\" EntityList");
                        return true;
                    }
                }

                player.sendMessage(ChatColor.RED + "[EntityCompass] " + "Could not find EntityList to be added to.");
                return true;

            }

            //leave entityList completely
            if (args[0].equals("leave")) {
                if (playerTrackedEntity != null) {
                    for (EntityList entityList : entityListArray) {

                        if (entityList == playerTrackedEntity.getEntityListMembership()) {


                            entityList.removeTrackedEntity(playerTrackedEntity);
                            player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Successfully removed from EntityList");
                            return true;
                        }
                    }
                }
                player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Not on an EntityList");
                return true;
            }


            if (args[0].equals("list")) {

                player.sendMessage(ChatColor.GREEN + "EntityCompass list");

                //print out EntityList along with entities
                for (EntityList entityList : entityListArray) {

                    //print empty ENTITY LIST
                    if (entityList.getTrackedEntityArray().size() == 0) {
                        player.sendMessage(ChatColor.GOLD + "List: " + entityList.getEntityListName() + " (EMPTY)");
                    }

                    //print teams and players
                    else {
                        player.sendMessage(ChatColor.GOLD + "List: " + entityList.getEntityListName());

                        for (TrackedEntity trackedEntity : entityList.getTrackedEntityArray()) {
                            player.sendMessage(ChatColor.BLUE + "-" + trackedEntity.getName());
                        }
                    }

                }

                if (entityListArray.size() == 0) {
                    player.sendMessage(ChatColor.GREEN + "No EntityLists have been created");
                }
                return true;
            }

            if (args[0].equals("additem")) {

                ItemStack itemInPlayerHand = player.getInventory().getItemInMainHand();

                if (itemInPlayerHand.getType() != Material.AIR) {

                    //verify item name isn't duplicate
                    for (EntityList entityList : entityListArray) {
                        for (TrackedEntity trackedEntity : entityList.getTrackedEntityArray()) {
                            if (itemInPlayerHand.hasItemMeta() && !itemInPlayerHand.getItemMeta().getDisplayName().equals("")) {
                                if (itemInPlayerHand.getItemMeta().getDisplayName().equals(trackedEntity.getName())) {

                                    player.sendMessage(ChatColor.RED + "[EntityCompass] " + "Item with the same name is already on an entity list");
                                    return true;
                                }
                            }else if (itemInPlayerHand.getType().toString().equals(trackedEntity.getName())) {
                                player.sendMessage(ChatColor.RED + "[EntityCompass] " + "Item with the same name is already on an entity list");
                                return true;
                            }

                        }
                    }

                    for (EntityList entityList : entityListArray) {
                        if (args[1].equals(entityList.getEntityListName())) {

                            NamespacedKey uuidKey = new NamespacedKey(main, "trackerUUID");

                            ItemStack itemStack = player.getPlayer().getInventory().getItemInMainHand();

                            if (itemStack.getAmount() > 1) {
                                player.sendMessage(ChatColor.RED + "[EntityCompass] " + "Cannot add multiple items to entity list (no stacks just one item)");
                                return true;
                            }

                            ItemMeta itemMeta = itemStack.getItemMeta();

                            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                            PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                            UUID uuid = UUID.randomUUID();
                            data.set(uuidKey, PersistentDataType.STRING, uuid.toString());

                            itemStack.setItemMeta(itemMeta);

                            itemStack.addUnsafeEnchantment(Enchantment.LUCK, 0);

                            TrackedEntity itemTrackedEntity = new TrackedEntity(uuid, TrackedEntity.TrackedEntityType.ITEM, getEntityListByName(args[1]), player.getUniqueId());

                            entityList.addTrackedEntity(itemTrackedEntity);

                            player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Added item to \"" + args[1] + "\" Entity List");
                            return true;
                        }
                    }
                    player.sendMessage(ChatColor.RED + "[EntityCompass] " + "List name \"" + args[1] + "\" not found");
                    return true;
                }
                player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "No item in main hand");
                return true;
            }

            if (args[0].equals("remove")) {

                for (EntityList entityList : entityListArray) {
                    for (TrackedEntity trackedEntity : entityList.getTrackedEntityArray()) {
                        if (args[1].equals(trackedEntity.getName())) {
                            trackedEntity.removeSelf();
                            trackedEntity.getEntityListMembership().removeTrackedEntity(trackedEntity);
                            player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Successfully removed \"" + trackedEntity.getName() + "\"");
                            return true;
                        }
                    }
                }

                player.sendMessage(ChatColor.RED + "[EntityCompass] " + "No tracked Entity with that name");
                return true;
            }

            if (args[0].equals("add")) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (args[1].equals(onlinePlayer.getName())) {

                        for (EntityList entityList : entityListArray) {
                            if (args[2].equals(entityList.getEntityListName())) {

                                TrackedEntity addedTrackedEntity = getTrackedEntity(onlinePlayer.getUniqueId());

                                //remove TrackedEntity from previous EntityList (if it exists)
                                if (addedTrackedEntity != null) {
                                    EntityList parentEntityList = addedTrackedEntity.getEntityListMembership();
                                    parentEntityList.removeTrackedEntity(addedTrackedEntity);
                                }

                                entityList.addTrackedEntity(new TrackedEntity(onlinePlayer.getUniqueId(), TrackedEntity.TrackedEntityType.PLAYER, entityList));
                                player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Successfully added " + onlinePlayer.getName() + " to \"" + entityList.getEntityListName() + "\" EntityList");
                                return true;
                            }
                        }

                        player.sendMessage(ChatColor.RED + "[EntityCompass] " + "No entity list by that name");
                        return true;

                    }
                }

                player.sendMessage(ChatColor.RED + "[EntityCompass] " + "No player online by that name");
                return true;
            }


            if (args[0].equals("indestructible")) {

                ItemStack item = player.getInventory().getItemInMainHand();
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.setUnbreakable(true);

                item.setItemMeta(itemMeta);

                player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Held item is now indestructible");
                return true;
            }

            if (args[0].equals("getTrackedEntityData(Debug)")) {

                if (playerTrackedEntity == null) {
                    return true;
                }
                player.sendMessage(ChatColor.GREEN+"Name: "+playerTrackedEntity.getName());
                player.sendMessage(ChatColor.BLUE+"UUID: "+playerTrackedEntity.getUUID());
                if (playerTrackedEntity.getPointedTrackedEntity() != null) {
                    player.sendMessage(ChatColor.GREEN + "pointedEntity: " + playerTrackedEntity.getPointedTrackedEntity().getName());
                }else {
                    player.sendMessage(ChatColor.GREEN + "pointedEntity: " + "null");
                }
                if (playerTrackedEntity.getPointedEntityList() != null) {
                    player.sendMessage(ChatColor.GOLD + "pointedEntityList: " + playerTrackedEntity.getPointedEntityList().getEntityListName());
                }else {
                    player.sendMessage(ChatColor.GOLD + "pointedEntityList: " + "null");
                }
                player.sendMessage(ChatColor.BLUE+"EntityListMembership: "+playerTrackedEntity.getEntityListMembership().getEntityListName());
                player.sendMessage(ChatColor.GREEN+"CompassHeld: " + playerTrackedEntity.getCompassHeld());
                player.sendMessage(ChatColor.BLUE+"FirstOverworldLocation: "+playerTrackedEntity.getFirstOverworldLocation());
                player.sendMessage(ChatColor.GREEN+"LastOverworldLocation: "+playerTrackedEntity.getLastOverworldLocation());
                player.sendMessage(ChatColor.BLUE+"FirstNetherLocation: "+playerTrackedEntity.getFirstNetherLocation());
                player.sendMessage(ChatColor.GREEN+"LastNetherLocation: "+playerTrackedEntity.getLastNetherLocation());
                player.sendMessage(ChatColor.BLUE+"LastEndLocation: "+playerTrackedEntity.getLastEndLocation());



                return true;

            }

            if (args[0].equals("relicrush")) {

                if (args.length > 1 && args[1].equals("end")) {

                    if (!main.getRelicRushStatus()) {

                        player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "RelicRush is not active");
                        return true;
                    }


                    for (EntityList entityList : entityListArray) {
                        if (entityList.getEntityListName().equals("SpecialItem")) {

                            entityList.setToNull();
                            entityListArray.remove(entityList);


                                World normal = Bukkit.getWorlds().get(0);
                                World nether = Bukkit.getWorlds().get(1);

                                WorldBorder overWorldBorder = normal.getWorldBorder();
                                WorldBorder netherWorldBorder = nether.getWorldBorder();

                                overWorldBorder.setSize(59999968);
                                netherWorldBorder.setSize(59999968);

                                main.setRelicRushStatus(false);

                                player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Relic Rush has been ended");

                                return true;
                        }
                    }
                }


                if (args.length > 1 && args[1].equals("start")) {

                    if (main.getRelicRushStatus()) {
                        player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "RelicRush is already active");
                        return true;
                    }

                    if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {


                        NamespacedKey relicKey = new NamespacedKey(main, "isRelic");

                        //check for duplicated
                        for (EntityList entityList : entityListArray) {
                            if (entityList.getEntityListName().equals("SpecialItem")) {
                                entityListArray.remove(entityList);
                            }
                        }

                        entityListArray.add(new EntityList("SpecialItem"));
                        player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "EntityList \"SpecialItem\" has been created.");


                        Bukkit.dispatchCommand(sender, "EntityCompass additem SpecialItem");
                        Bukkit.dispatchCommand(sender, "EntityCompass indestructible");

                        ItemStack itemStack = player.getInventory().getItemInMainHand();
                        ItemMeta itemMeta = itemStack.getItemMeta();

                        PersistentDataContainer data = itemMeta.getPersistentDataContainer();

                        data.set(relicKey, PersistentDataType.STRING, "yes");

                        itemMeta.setDisplayName("Relic");

                        itemStack.setItemMeta(itemMeta);

                        World normal = Bukkit.getWorlds().get(0);
                        World nether = Bukkit.getWorlds().get(1);

                        WorldBorder overWorldBorder = normal.getWorldBorder();
                        WorldBorder netherWorldBorder = nether.getWorldBorder();

                        overWorldBorder.setSize(16000);
                        netherWorldBorder.setSize(overWorldBorder.getSize() / 8);

                        main.setRelicRushStatus(true);

                        player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Relic Rush has been started");

                        return true;
                    }

                    player.sendMessage(ChatColor.RED + "[EntityCompass] " + "Valid item must be held");
                    return true;
                }

                player.sendMessage(ChatColor.RED + "[EntityCompass] " + "Please type start or end after relic rush");
                return true;
            }


            player.sendMessage(ChatColor.RED + "[EntityCompass] " + "Invalid Command");



        }

        //if instance was not a player run this
        main.getLogger().info("[TeamsCompass] " + "ONLY PLAYERS CAN EXECUTE ENTITYCOMPASS COMMANDS");
        return true;

    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String Label, String[] args) {

        if (args.length == 1) {
            List<String> arguments = new ArrayList<>();
            arguments.add("create");
            arguments.add("delete");
            arguments.add("join");
            arguments.add("leave");
            arguments.add("add");
            arguments.add("remove");
            arguments.add("additem");
            arguments.add("list");
            arguments.add("getTrackedEntityData(Debug)");
            arguments.add("compass");
            return arguments;
        }

        if ((args.length == 2 && (args[0].equals("join") || args[0].equals("delete") || args[0].equals("additem"))) ||
        args.length == 3 && args[0].equals("add")) {
            List<String> arguments = new ArrayList<>();

            for (int i = 0; i < entityListArray.size(); i++) {
                arguments.add(entityListArray.get(i).getEntityListName());
            }
            return arguments;
        }


        return null;
    }

    public void defaultPointer(Player player) {

        TrackedEntity trackedEntity = getTrackedEntity(player.getUniqueId());

        if (trackedEntity == null) {
           return;
        }

        for (EntityList entityList : entityListArray) {

            if (entityList != trackedEntity.getEntityListMembership() && entityList.getTrackedEntityArray().size() > 0) {

                trackedEntity.setPointedEntityList(entityList);
                trackedEntity.setPointedTrackedEntity(entityList.getTrackedEntityArray().get(0));
                player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Now pointing to " + entityList.getTrackedEntityArray().get(0).getName());

                return;
            }

        }

        //checks current list if another entityList isnt found
        if (trackedEntity.getEntityListMembership().getTrackedEntityArray().size() > 1) {

            for (TrackedEntity sameListTrackedEntity : trackedEntity.getEntityListMembership().getTrackedEntityArray()) {
                if (sameListTrackedEntity != trackedEntity) {

                    trackedEntity.setPointedEntityList(trackedEntity.getEntityListMembership());
                    trackedEntity.setPointedTrackedEntity(sameListTrackedEntity);

                    player.sendMessage(ChatColor.GREEN + "[EntityCompass] " + "Now pointing to " + sameListTrackedEntity.getName());
                    return;
                }
            }
        }

        trackedEntity.setPointedTrackedEntity(null);
        trackedEntity.setPointedEntityList(null);


    }

    public ArrayList<EntityList> getEntityListArray() {
        return entityListArray;
    }

    public void setEntityListArray (ArrayList<EntityList> entityListArray) {
        this.entityListArray = entityListArray;
    }

    public TrackedEntity getTrackedEntity(String UUID) {

        //HUGE nested loop to search all tracked entities to find one by UUID
        for (EntityList entityList : entityListArray) {

            for (TrackedEntity trackedEntity : entityList.getTrackedEntityArray()) {

                if (trackedEntity.getUUID().toString().equals(UUID)) {
                    return trackedEntity;
                }

            }

        }
        return null;
    }

    //overloaded for String and non string UUID compatibility
    public TrackedEntity getTrackedEntity(UUID UUID) {

        //HUGE nested loop to search all tracked entities to find one by UUID
        for (EntityList entityList : entityListArray) {

            for (TrackedEntity trackedEntity : entityList.getTrackedEntityArray()) {

                if (trackedEntity.getUUID().toString().equals(UUID.toString())) {
                    return trackedEntity;
                }

            }

        }
        return null;
    }


    public EntityList getEntityListByName (String entitylistname) {

        for (EntityList entityList : entityListArray) {
            if (entityList.getEntityListName().equals(entitylistname)) {
                return entityList;
            }
        }
        return null;
    }

}
