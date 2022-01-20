package org.dreamleaf.teleportrequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

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
        tpdelay();
    }

    /**
     * Handles creating a new TPRequest and verifies
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
     * Accepts a teleport request that a user has received - Finds request, determines delay,
     * and teleports based off request type (tpa/tpahere)
     * Usage: /tpaccept
     */
    public void tpaccept() {
        new CommandHandler("tpaccept", true) {
            @Override
            public boolean onCommand(CommandSender sender, String[] args) {
                // initialize variables
                Player teleportPlayer;
                Location location;
                int delay = TeleportRequest.getInstance().getConfig().getInt("delay");

                // get sender as player object
                Player sendPlayer = (Player) sender;

                // get request
                TPRequest data = new TPRequest(null, null, sendPlayer.getUniqueId(), 0);
                TPRequest request = TeleportRequest.searchRequests(data);

                // check that request exists
                if (request != null)
                {
                    // notify command sender
                    sendPlayer.sendMessage(ChatColor.GREEN + "Teleport request accepted.");

                    // check request type, was this tpa or tpahere?
                    // handle tpa request
                    if (request.type == TPA)
                    {
                        // get info from player to tp to
                        teleportPlayer = request.receiver;
                        location = teleportPlayer.getLocation();

                        // check for no active delay or OP user
                        if (delay == 0 || request.sender.hasPermission("TeleportRequest.admin"))
                        {
                            // begin teleportation
                            request.sender.sendMessage(ChatColor.GREEN + "Teleport commencing...");
                            request.sender.teleport(location);
                        }
                        else
                        {
                            // notify player of delay
                            request.sender.sendMessage(ChatColor.GREEN + "Teleport commencing in " + ChatColor.GRAY +
                                                                delay + ChatColor.GREEN + " seconds... Don't move!");

                            // check for already on delay
                            if(TeleportRequest.onDelay.containsKey(request.sender.getUniqueId()))
                            {
                                // cancel old delay
                                TeleportRequest.onDelay.get(request.sender.getUniqueId()).cancel();
                                TeleportRequest.onDelay.remove(request.sender.getUniqueId());
                            }

                            // put user on delay
                            TeleportRequest.onDelay.put(request.sender.getUniqueId(), new BukkitRunnable() {
                                @Override
                                public void run() {
                                    request.sender.teleport(location);
                                    TeleportRequest.onDelay.remove(request.sender.getUniqueId());
                                }
                            }.runTaskLater(TeleportRequest.getInstance(), 20L * delay));
                        }
                    }
                    // handle tpahere request
                    else
                    {
                        // get info from player to tp to
                        teleportPlayer = request.sender;
                        location = teleportPlayer.getLocation();

                        // check for no active delay or OP user
                        if (delay == 0 || sendPlayer.hasPermission("TeleportRequest.admin"))
                        {
                            // begin teleportation
                            sendPlayer.sendMessage(ChatColor.GREEN + "Teleport commencing...");
                            sendPlayer.teleport(location);
                        }
                        else
                        {
                            // notify player of delay
                            sendPlayer.sendMessage(ChatColor.GREEN + "Teleport commencing in " + ChatColor.GRAY +
                                                            delay + ChatColor.GREEN + " seconds... Don't move!");

                            // check for already on delay
                            if(TeleportRequest.onDelay.containsKey(sendPlayer.getUniqueId()))
                            {
                                // cancel old delay
                                TeleportRequest.onDelay.get(sendPlayer.getUniqueId()).cancel();
                                TeleportRequest.onDelay.remove(sendPlayer.getUniqueId());
                            }

                            // put user on delay
                            TeleportRequest.onDelay.put(sendPlayer.getUniqueId(), new BukkitRunnable() {
                                @Override
                                public void run() {
                                    sendPlayer.teleport(location);
                                    TeleportRequest.onDelay.remove(sendPlayer.getUniqueId());
                                }
                            }.runTaskLater(TeleportRequest.getInstance(), 20L * delay));
                        }
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
        }.setDescription("Send a teleport request to a player");
    }

    /**
     * Tpdelay command
     * Sets the delay for teleport commencement
     * Usage: /tpdelay <seconds>
     */
    public void tpdelay() {
        new CommandHandler("tpdelay", 1, false) {

            @Override
            public boolean onCommand(CommandSender sender, String[] args) {
                // initialize variables
                int newDelay;

                // confirm player entered an integer
                try {
                    newDelay = Integer.parseInt(args[0]);
                } catch (NumberFormatException nfe) {
                    // if not, notify player
                    sender.sendMessage(ChatColor.RED + "Delay must be an integer in seconds");
                    return true;
                }

                // set new delay and return success
                TeleportRequest.getInstance().getConfig().set("delay", newDelay);
                TeleportRequest.getInstance().saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Delay has been set to " + ChatColor.GRAY +
                                                        args[0] + ChatColor.GREEN + " seconds.");
                return true;
            }

            @Override
            public @NotNull String getUsage() {
                return "/tpdelay <seconds>";
            }
        }.setDescription("Set the delay for teleport commencement").setPermission("TeleportRequest.admin");
    }
}
