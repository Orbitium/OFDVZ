package me.orbitium.ofdvz.classes.root;

import me.libraryaddict.disguise.DisguiseAPI;
import me.orbitium.ofdvz.OFDVZ;
import me.orbitium.ofdvz.OFEffect;
import me.orbitium.ofdvz.OFScoreboard;
import me.orbitium.ofdvz.classes.attacker.Zombie;
import me.orbitium.ofdvz.database.CCache;
import me.orbitium.ofdvz.event.Events;
import me.orbitium.ofdvz.lobby.Lobby;
import me.orbitium.ofdvz.util.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.function.Consumer;

public abstract class OFClass {
    public String name;
    public String displayName;
    private Material material;
    private int displaySlot;
    private int maxPlayer;
    public int maxHealth = 20;
    private int respawnCooldown = 3;
    public final List<String> lore = new ArrayList<>();
    public List<ItemStack> starterItems = new ArrayList<>();
    public final Set<Player> registeredPlayers = new HashSet<>();
    protected final Map<Class<? extends Event>, Consumer<Event>> events = new HashMap<>();
    String rootFilePath;
    ClassType classType;
    public Team team;

    private final Map<String, String> customConfigs = new LinkedHashMap<>();

    ItemStack displayItem;
    int unlockPercentage = 0;

    /* Use setters */
    public OFClass(String name, ClassType classType) {
        rootFilePath = OFDVZ.getInstance().getDataFolder().getPath() + "/" + classType.path + "/" + name + "/";
        this.name = name;
        this.classType = classType;
        loadStarterItems();
    }

    public void loadConfig() {
        for (Map.Entry<Class<? extends Event>, Consumer<Event>> entry : events.entrySet()) {
            CCache.registerNewClass(entry.getKey(), this);
        }

        try {
            File mainDir = new File(rootFilePath);
            if (!mainDir.exists())
                mainDir.mkdirs();
            File configFile = new File(rootFilePath + "/" + name + "Config.yml");
            if (!configFile.exists()) {
                configFile.createNewFile();
                FileWriter fileWriter = new FileWriter(configFile);
                fileWriter.write("# What's the name of class\n");
                fileWriter.write("# Under development, won't effect anything\n");
                fileWriter.write("name: " + name + "\n\n");
                fileWriter.write("# Maximum health of players in that class\n");
                fileWriter.write("maxHealth: 20\n\n");

                fileWriter.write("# If player which in that class the cooldown of respawn\n");
                fileWriter.write("respawnCooldown: 20\n\n");

                if (classType != ClassType.Heroes) {
                    fileWriter.write("# What's the material of class, it will be displayed on class select UI\n");
                    fileWriter.write("material: STONE\n\n");

                    fileWriter.write("# How much player can be the player of the class? (Can be overwritten by admin)\n");
                    fileWriter.write("maxPlayer: " + 5 + "\n\n");

                    fileWriter.write("# Which slot will show the class in class select UI?\n");
                    fileWriter.write("slotIndexInUI: " + 10 + "\n\n");

                    fileWriter.write("# What's the lore of the class? (It will show on the class select UI)\n");
                    fileWriter.write("# Don't forget put `-` on the start of the line\n");
                    fileWriter.write("itemLoreOnUI:\n");
                    fileWriter.write("\t- Test Lore\n");
                    fileWriter.write("\t- Test Lore2\n");
                    fileWriter.write("\t- Test Lore3\n\n");
                }

                if (classType == ClassType.Zombies) {
                    fileWriter.write("# What's the percentage of zombie for unlock that class\n");
                    fileWriter.write("unlockPercentage: 20\n\n");
                }

                for (Map.Entry<String, String> entry : customConfigs.entrySet()) {
                    if (!entry.getKey().startsWith("#"))
                        fileWriter.write(entry.getKey() + ": " + entry.getValue() + "\n\n");
                    else
                        fileWriter.write(entry.getKey() + "\n");
                }

                fileWriter.close();

            }

            String line;
            Scanner scanner = new Scanner(configFile);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.startsWith("#"))
                    continue;
                String[] s = line.split(":");
                if (line.replaceAll("\t", "").startsWith("-")) {
                    this.lore.add(ChatColor.translateAlternateColorCodes('&',
                            line.split("-")[1]));
                }
                if (s.length != 2)
                    continue;
                switch (s[0]) {
                    case "name" -> this.displayName = s[1];
                    case "material" -> this.material = Material.getMaterial(s[1].replaceAll(" ", ""));
                    case "maxPlayer" -> this.maxPlayer = Integer.parseInt(s[1].replaceAll(" ", ""));
                    case "slotIndexInUI" -> this.displaySlot = Integer.parseInt(s[1].replaceAll(" ", ""));
                    case "respawnCooldown" -> this.respawnCooldown = Integer.parseInt(s[1].replaceAll(" ", ""));
                    case "maxHealth" -> this.maxHealth = Integer.parseInt(s[1].replaceAll(" ", ""));
                    case "unlockPercentage" -> this.unlockPercentage = Integer.parseInt(s[1].replaceAll(" ", ""));
                    default -> {
                        if (customConfigs.containsKey(s[0].replaceAll(" ", ""))) {
                            customConfigs.put(s[0].replaceAll(" ", ""), s[1]);
                        }
                    }
                }
            }
            scanner.close();
            if (displayName.startsWith(" "))
                displayName = displayName.substring(1);

            for (int i = 0; i < lore.size(); i++) {
                String s = lore.get(i);
                if (s.startsWith(" "))
                    s = s.substring(1);
                lore.set(i, s);
            }
            if (classType != ClassType.Heroes)
                loadDisplayItem();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadDisplayItem() {
        ItemBuilder itemBuilder = new ItemBuilder(material, name);
        itemBuilder.setLore(lore);
        itemBuilder.build();
        displayItem = itemBuilder.build();
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public String getCustomConfig(String key) {
        return customConfigs.get(key).replaceAll(" ", "");
    }

    public String getCustomConfig(String key, boolean replaceSpaces) {
        if (replaceSpaces)
            return customConfigs.get(key).replaceAll(" ", "");
        return customConfigs.get(key);
    }

    public void registerNewConfig(String label, String value) {
        customConfigs.put(label, value);
    }

    public void configCommend(String label) {
        customConfigs.put(label, "");
    }

    public void registerNewEvent(Class<? extends Event> eventClass, Consumer<Event> event) {
        events.put(eventClass, event);
    }

    public boolean addPlayer(Player player, boolean forceByAdmin) {
        if (registeredPlayers.size() + 1 > maxPlayer) {
            if (forceByAdmin)
                registeredPlayers.add(player);
        } else
            registeredPlayers.add(player);

        if (registeredPlayers.contains(player)) {
            player.getPersistentDataContainer().set(OFDVZ.classKey, PersistentDataType.INTEGER, 0);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            if (classType == ClassType.Zombies)
                player.teleport(Lobby.attackerStartLocation);
            if (classType == ClassType.Defender)
                player.teleport(Lobby.defenderStartLocation);
            if (classType == ClassType.Heroes)
                if (name.equals("Brood Mother"))
                    player.teleport(Lobby.attackerStartLocation);
                else
                    player.teleport(Lobby.defenderStartLocation);


            if (classType == ClassType.Zombies || name.equals("Brood Mother")) {
                player.getPersistentDataContainer().set(OFDVZ.zombieKey, PersistentDataType.INTEGER, 0);
                Lobby.zp.add(player);
            } else {
                DisguiseAPI.undisguiseToAll(player);
                player.getPersistentDataContainer().remove(OFDVZ.zombieKey);
                Lobby.zp.remove(player);
                if (classType == ClassType.Heroes)
                    ClassManager.builder.registeredPlayers.add(player);
            }
            giveItems(player);
            player.setScoreboard(OFScoreboard.scoreboard);
            team.addEntry(player.getName());
            player.setPlayerListName(team.getPrefix() + player.getName());
            return true;
        }

        return false;
    }

    public void removePlayer(Player player) {
        registeredPlayers.remove(player);
    }

    public void addStarterItem(ItemStack itemStack) {
        starterItems.add(itemStack);
    }

    public void giveItems() {
        for (Player registeredPlayer : registeredPlayers) {
            for (ItemStack starterItem : starterItems) {
                registeredPlayer.getInventory().addItem(starterItem);
            }
        }
    }

    public void die(Player player) {
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0.5, 0, 0.5), 50, ParticleUtil.dustOptions);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0.5, 1, 0.5), 50, ParticleUtil.dustOptions);
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(Lobby.respawnSpawnLocation);
        player.sendTitle(OFDVZ.deathTitle, OFDVZ.deathSubtitle.replace("[seconds]", respawnCooldown + ""),
                10, Math.min(90, respawnCooldown * 20), 10);
        Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
            if (classType == ClassType.Defender || classType == ClassType.Heroes && !name.equals("Brood Mother")) {
                Lobby.zp.add(player);
                if (Lobby.zp.size() >= Bukkit.getOnlinePlayers().size()) {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers())
                        MessageUtil.sendMessageToPlayer(onlinePlayer, "messages.allDefendersAreDeath");
                    Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), Lobby::end, 15 * 20);
                    return;
                }
                if (Events.isPlagueStarted) {
                    player.getPersistentDataContainer().remove(OFDVZ.classKey);
                    player.getPersistentDataContainer().set(OFDVZ.zombieKey, PersistentDataType.INTEGER, 0);
                    player.teleport(Lobby.attackerStartLocation);
                    Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                        ClassSelect.openZombieUI(player);
                    }, 1L);

                } else {
                    player.teleport(Lobby.eventWaitForEventLocation);
                    MessageUtil.sendMessageToPlayer(player, "messages.waitUntilPlagueStart");
                    Lobby.waitingPlayers.add(player);
                }
            } else if (classType == ClassType.Zombies || name.equals("Brood Mother")) {
                player.teleport(Lobby.attackerStartLocation);
            }

            for (ItemStack content : player.getInventory().getContents()) {
                if (content != null)
                    content.setAmount(0);
            }

            if (player.getPersistentDataContainer().has(OFDVZ.classKey, PersistentDataType.INTEGER))
                giveItems(player);

            if (Zombie.immunityCounter.containsValue(player)) {
                OFEffect d = null;
                for (Map.Entry<OFEffect, Player> entry : Zombie.immunityCounter.entrySet()) {
                    if (entry.getValue().equals(player)) {
                        d = entry.getKey();
                        break;
                    }
                }
                if (d != null)
                    Bukkit.getScheduler().cancelTask(d.id);
            }
            player.setGameMode(GameMode.SURVIVAL);
            Bukkit.getScheduler().runTaskLater(OFDVZ.getInstance(), () -> {
                player.setSneaking(true);
                player.setSneaking(false);
            }, 10L);
        }, respawnCooldown * 20L);
    }


    public void giveItems(Player player) {
        for (ItemStack starterItem : starterItems) {
            String itemName = starterItem.getType().name();
            String s[] = itemName.split("_");
            if (s.length == 2) {
                switch (s[1]) {
                    case "HELMET" -> player.getInventory().setHelmet(starterItem);
                    case "CHESTPLATE" -> player.getInventory().setChestplate(starterItem);
                    case "LEGGINGS" -> player.getInventory().setLeggings(starterItem);
                    case "BOOTS" -> player.getInventory().setBoots(starterItem);
                    default -> player.getInventory().addItem(starterItem);
                }
            } else
                player.getInventory().addItem(starterItem);

        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplaySlot(int displaySlot) {
        this.displaySlot = displaySlot;
    }

    public void setMaxPlayer(int maxPlayer) {
        this.maxPlayer = maxPlayer;
    }


    public void loadStarterItems() {
        try {
            List<Map<String, String>> data = ItemManager.readDataFromFile(rootFilePath + "items/");
            if (data != null)
                starterItems = ItemManager.generateItemsStacks(data);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void tryRunEvent(Player player, Event event) {
        if (registeredPlayers.contains(player))
            events.get(event.getClass()).accept(event);
    }

    public boolean isPlayerRegistered(Player player) {
        return registeredPlayers.contains(player);
    }

    public int getDisplaySlot() {
        return displaySlot;
    }

    public int getMaxPlayer() {
        return maxPlayer;
    }

    public Set<Player> getRegisteredPlayers() {
        return registeredPlayers;
    }
}
