package me.orbitium.ofdvz.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleUtil {

    public static Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
    public static void spawnRedParticle(Player player, Location location) {
        player.spawnParticle(Particle.REDSTONE, location.add(0.5, 0, 0.5), 50, dustOptions);
    }

    public static void spawnGreenParticle(Player player, Location location) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1.0F);
        player.spawnParticle(Particle.REDSTONE, location.add(0.5, 0, 0.5), 50, dustOptions);
    }

    public static void spawnRedParticle(Location location) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
        location.getWorld().spawnParticle(Particle.REDSTONE, location, 3, dustOptions);
    }
}
