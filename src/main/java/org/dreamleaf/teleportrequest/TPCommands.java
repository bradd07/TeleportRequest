package org.dreamleaf.teleportrequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Class contains all commands for the plugin
 */
public class TPCommands {

    /**
     * Constant for tpa identifier
     */
    private static final int TPA = 1001;

    /**
     * Constant for tpahere identifier
     */
    private static final int TPA_HERE = 2002;

    /**
     * Initializes the commands
     */
    public void getCommands()
    {
        tpa();
        tpahere();
        tpaccept();
        tpdeny();
    }

    /**
     * Handles creating a new TPRequest based off type and verifies
     * that there is not an existing request with the player
     * @param sender Player who sent the teleport request
     * @param receiver Player who received the teleport request
     * @param type int identifier for type of request (tpa/tpahere)
     * @return boolean result of creating request
     */
    public boolean handleRequests(Player sender, Player receiver, int type)
    {
        // check for real online player
        if (receiver == null || !receiver.isOnline())
        {
            // notify and return
            sender.sendMessage(ChatColor.RED + "That player is currently offline!");
            return true;
        }
        else if (sender.getName().equals(receiver.getName()))
        {
            sender.sendMessage(ChatColor.RED + "You can not send a teleport request to yourself.");
            return true;
        }

        // create request
        TPRequest data = new TPRequest(sender, receiver, receiver.getUniqueId(), type);

        // search for an existing request the receiver already has
        TPRequest existingData = TeleportRequest.searchRequests(data);
        if(existingData == null)
        {
            // if not found, add it
            TeleportRequest.addRequest(data);
        }
        else
        {
            // if found, check if it is to the same person
            if(existingData.sender == sender)
            {
                // notify that they have an existing request and return
                sender.sendMessage(ChatColor.RED +
                        "You already have an existing teleport request with this player.");
                return true;
            }
            // if found and not from same sender, update the teleport request info with latest data
            existingData.setData(data);
        }

        // notify player based on request type
        if(type == TPA)
        {
            receiver.sendMessage(ChatColor.GRAY + sender.getName() + ChatColor.GREEN +
                    " has sent you a teleport request.");
        }
        else
        {
            receiver.sendMessage(ChatColor.GRAY + sender.getName() + ChatColor.GREEN +
                    " has requested that you teleport to them.");
        }
        receiver.sendMessage(ChatColor.GRAY + "/tpaccept " + ChatColor.GREEN + "to accept");
        receiver.sendMessage(ChatColor.GRAY + "/tpdeny " + ChatColor.GREEN + "to deny");

        // return success
        sender.sendMessage(ChatColor.GREEN + "Teleport request sent to player!");

        return true;
    }

    /**
     * Tpaccept command
     * Accepts a teleport request that a user has received
     * Usage: /tpaccept
     */
    public void tpaccept() {
        new CommandHandler("tpaccept", true) {
            @Override
            public boolean onCommand(CommandSender sender, String[] args) {
                // initialize variables
                Player teleportPlayer;
                Location location;

                // get sender as player object
                Player sendPlayer = (Player) sender;

                // get request
                TPRequest data = new TPRequest(null, null, sendPlayer.getUniqueId(), 0);
                TPRequest request = TeleportRequest.searchRequests(data);

                // check that request exists
                if (request != null)
                {
                    // check request type, was this tpa or tpahere?
                    if (request.type == TPA)
                    {
                        // get info from player to tp to
                        teleportPlayer = request.receiver;
                        location = teleportPlayer.getLocation();

                        // begin teleportation
                        request.sender.sendMessage(ChatColor.GREEN + "Teleport commencing...");
                        request.sender.teleport(location);
                    }
                    else
                    {
                        // get info from player to tp to
                        teleportPlayer = request.sender;
                        location = teleportPlayer.getLocation();

                        // begin teleportation
                        sendPlayer.sendMessage(ChatColor.GREEN + "Teleport commencing...");
                        sendPlayer.teleport(location);
                    }
                    // remove request
                    TeleportRequest.removeRequest(request);
                }
                else
                {
                    // notify player of not having active requests
                    sendPlayer.sendMessage(ChatColor.RED + "You do not have any active teleport requests.");
                }
                // end command
                return true;
            }
            @Override
            public @NotNull String getUsage() {
                return "/tpaccept";
            }
        }.setDescription("Accept a teleport request");
    }

    /**
     * Tpdeny command
     * Denies a teleport request that a user has received
     * Usage: /tpdeny
     */
    public void tpdeny() {
        new CommandHandler("tpdeny", true) {
            @Override
            public boolean onCommand(CommandSender sender, String[] args) {
                // get sender as player object
                Player sendPlayer = (Player) sender;

                // get request
                TPRequest data = new TPRequest(null, null, sendPlayer.getUniqueId(), 0);
                TPRequest request = TeleportRequest.searchRequests(data);

                // check that request exists
                if (request != null)
                {
                    // remove request
                    TeleportRequest.removeRequest(request);

                    // notify
                    sendPlayer.sendMessage(ChatColor.GREEN + "Teleport request has been removed.");
                }
                else
                {
                    // notify player of not having active requests
                    sendPlayer.sendMessage(ChatColor.RED + "You do not have any active teleport requests.");
                }
                // end command
                return true;
            }

            @Override
            public @NotNull String getUsage() {
                return "/tpdeny";
            }
        }.setDescription("Deny a teleport request");
    }

    /**
     * Tpahere command
     * Sends a request to another player to teleport them to you
     * Usage: /tpahere <player>
     */
    public void tpahere() {
        new CommandHandler("tpahere", 1, true) {

            @Override
            public boolean onCommand(CommandSender sender, String[] args) {
                // get sender as player object
                Player sendPlayer = (Player) sender;

                // get player to send request to
                Player player = Bukkit.getServer().getPlayer(args[0]);

                // try to create request
                return handleRequests(sendPlayer, player, TPA_HERE);
            }

            @Override
            public @NotNull String getUsage() {
                return "/tpahere <player>";
            }
        }.setDescription("Request that a player teleport to you");
    }

    /**
     * Tpa command
     * Sends a teleport request to a specific player
     * Usage: /tpa <player>
     */
    public void tpa() {
        new CommandHandler("tpa", 1, true) {
            @Override
            public boolean onCommand(CommandSender sender, String[] args) {
                // get sender as player object
                Player sendPlayer = (Player) sender;
                
                // get player to send request to
                Player player = Bukkit.getServer().getPlayer(args[0]);

                // try to create request
                return handleRequests(sendPlayer, player, TPA);
            }

            @Override
            public @NotNull String getUsage() {
                return "/tpa <player>";
            }
        }.setDescription("Send a teleport request to a player");//.setName("tpa");
    }
}
