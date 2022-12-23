package me.orbitium.oflobby.listener;

import me.orbitium.oflobby.OFDVZ;
import me.orbitium.oflobby.beacon.BeaconListener;
import me.orbitium.oflobby.classes.root.ClassManager;
import me.orbitium.oflobby.database.CCache;
import me.orbitium.oflobby.lobby.Lobby;
import me.orbitium.oflobby.util.ItemBuilder;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlayerListener implements Listener {


    @EventHandler
    public void d(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (event.getEntity().getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER))
                if (event.getDamager().getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER))
                    event.setCancelled(true);
            if (!event.getEntity().getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER))
                if (!event.getDamager().getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER))
                    event.setCancelled(true);
        }

        if (event.getDamager() instanceof Player player && event.getEntity() instanceof Monster monster) {
            if (!player.getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER)) {
                monster.setTarget(player);
            }
        }

        Player player = null;

        if (event.getDamager() instanceof Player p)
            player = p;
        else if (event.getEntity() instanceof Player p)
            player = p;

        if (player == null)
            return;

        CCache.runClassOnEvent(player, event);

    }


    @EventHandler
    public void i(PlayerDropItemEvent event) {
        PersistentDataContainer pdc = event.getItemDrop().getItemStack().getItemMeta().getPersistentDataContainer();

        if (pdc.has(ItemBuilder.key, PersistentDataType.INTEGER))
            event.setCancelled(true);

    }

    @EventHandler
    public void q(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player))
            return;

        if (player.getPersistentDataContainer().has(OFDVZ.zombieKey, PersistentDataType.INTEGER)) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void j(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.getPersistentDataContainer().set(OFDVZ.compassData, PersistentDataType.INTEGER, 0);
        if (Lobby.playing) {
            ClassManager.alchemist.die(event.getPlayer());
            BeaconListener.bossBar.addPlayer(player);
        } else
            player.teleport(Lobby.lobbyLocation);

    }

}
