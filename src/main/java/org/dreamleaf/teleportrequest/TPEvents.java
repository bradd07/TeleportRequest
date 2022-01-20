package org.dreamleaf.teleportrequest;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public class TPEvents implements Listener {
    /**
     * Checks if a player is moving while on delay for teleporting
     * @param pme PlayerMoveEvent
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent pme) {
        if (pme.getFrom().getBlockX() != pme.getTo().getBlockX()
                || pme.getFrom().getBlockY() != pme.getTo().getBlockY()
                || pme.getFrom().getBlockZ() != pme.getTo().getBlockZ()) {
            BukkitTask task = TeleportRequest.onDelay.get(pme.getPlayer().getUniqueId());
            if (task != null) {
                task.cancel();
                TeleportRequest.onDelay.remove(pme.getPlayer().getUniqueId());
                pme.getPlayer().sendMessage(ChatColor.RED + "Teleportation cancelled!");
            }
        }
    }

    /**
     * Checks if a player logs out while on delay for teleporting
     * @param pqe PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent pqe) {
        BukkitTask task = TeleportRequest.onDelay.get(pqe.getPlayer().getUniqueId());
        if (task != null && !pqe.getPlayer().isOnline()) {
            task.cancel();
            TeleportRequest.onDelay.remove(pqe.getPlayer().getUniqueId());
        }
    }
}
