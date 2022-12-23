package me.orbitium.oflobby.classes.attacker;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.classes.root.ClassType;
import me.orbitium.oflobby.classes.root.OFClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Silverfish extends OFClass {

    PotionEffect speed;
    int maxHealth;

    public Silverfish() {
        super("Silverfish", ClassType.Zombies);

        configCommend("# Silverfish's max health");
        registerNewConfig("maxHealth", "10");



        configCommend("# What's the amplifier of speed effect on silverfish players");
        registerNewConfig("speedAmplifier", "1");

        loadConfig();

        maxHealth = Integer.parseInt(getCustomConfig("maxHealth"));
        int a = Integer.parseInt(getCustomConfig("speedAmplifier"));
        speed = new PotionEffect(PotionEffectType.SPEED, 20, a);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(OFDVZ.getInstance(), () -> {
            for (Player registeredPlayer : registeredPlayers) {
                registeredPlayer.addPotionEffect(speed);
            }
        }, 10L, 10L);
    }

    @Override
    public boolean addPlayer(Player player, boolean forceByAdmin) {
        MobDisguise mobDisguise = new MobDisguise(DisguiseType.SILVERFISH);
        DisguiseAPI.disguiseEntity(player, mobDisguise);
        return super.addPlayer(player, forceByAdmin);
    }
}
