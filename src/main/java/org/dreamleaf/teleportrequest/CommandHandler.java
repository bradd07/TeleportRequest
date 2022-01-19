package org.dreamleaf.teleportrequest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Class handles executed commands of all potential variations
 */
public abstract class CommandHandler extends BukkitCommand implements CommandExecutor {

    /**
     * List of players who are on delay
     */
    private List<String> delayedPlayers = null;

    /**
     * Default delay time (sec)
     */
    private int delay = 0;

    /**
     * Minimum number of arguments
     */
    private final int minArguments;

    /**
     * Maximum number of arguments
     */
    private final int maxArguments;

    /**
     * Whether the command requires a player to run it
     */
    private final boolean playerOnly;

    /**
     * Constructor with no arguments and no specification of player requirement
     * @param command String name of command
     */
    public CommandHandler(String command)
    {
        // run constructor
        this(command, 0);
    }

    /**
     * Constructor with no arguments
     * @param command String name of command
     * @param playerOnly boolean
     */
    public CommandHandler(String command, boolean playerOnly)
    {
        // run constructor
        this(command, 0, playerOnly);
    }

    /**
     * Constructor with no boolean
     * @param command String name of command
     * @param requiredArguments int number of arguments
     */
    public CommandHandler(String command, int requiredArguments)
    {
        // run constructor
        this(command, requiredArguments, requiredArguments);
    }

    /**
     * Constructor with a range of arguments and no boolean
     * @param command String name of command
     * @param minArguments int minimum number of arguments
     * @param maxArguments int maximum number of arguments
     */
    public CommandHandler(String command, int minArguments, int maxArguments)
    {
        // run constructor
        this(command, minArguments, maxArguments, false);
    }

    /**
     * Constructor with no range of arguments
     * @param command String name of command
     * @param requiredArguments int number of arguments
     * @param playerOnly boolean
     */
    public CommandHandler(String command, int requiredArguments, boolean playerOnly)
    {
        // run constructor
        this(command, requiredArguments, requiredArguments, playerOnly);
    }

    /**
     * Initialization constructor
     * @param command String name of command
     * @param minArguments int minimum  number of arguments
     * @param maxArguments int maximum number of arguments
     * @param playerOnly boolean
     */
    public CommandHandler(String command, int minArguments, int maxArguments, boolean playerOnly)
    {
        // initialize variables
        super(command);
        this.minArguments = minArguments;
        this.maxArguments = maxArguments;
        this.playerOnly = playerOnly;

        // register command map
        CommandMap commandMap = getCommandMap();
        if (commandMap != null)
        {
            commandMap.register(command, this);
        }
    }

    /**
     * Get the CommandMap for this plugin
     * @return CommandMap
     */
    public CommandMap getCommandMap()
    {
        try {
            if(Bukkit.getPluginManager() instanceof SimplePluginManager)
            {
                // get commandmap
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);

                // return
                return (CommandMap) field.get(Bukkit.getPluginManager());
            }
        } catch(NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        // failed
        return null;
    }

    /**
     * Enables the delay for a user for a command
     * @param delay int time in seconds
     * @return CommandHandler this
     */
    public CommandHandler enableDelay(int delay)
    {
        this.delay = delay;
        this.delayedPlayers = new ArrayList<>();
        return this;
    }

    /**
     * Removes the delay for a user for a command
     * @param player Player to remove the delay from
     */
    public void removeDelay(Player player)
    {
        this.delayedPlayers.remove(player.getName());
    }

    /**
     * Sends the proper command usage to the player if
     * there is a syntax error
     * @param sender CommandSender player who sent the command
     */
    public void sendUsage(CommandSender sender)
    {
        sender.sendMessage(getUsage());
    }

    /**
     * Executes the command
     * @param sender CommandSender sender player who sent the command
     * @param alias String alis for the command
     * @param args String[] arguments for the command
     * @return boolean true
     */
    public boolean execute(CommandSender sender, String alias, String[] args)
    {
        // check for correct arguments
        if(args.length < minArguments || args.length < maxArguments)
        {
            sendUsage(sender);
            return true;
        }

        // check for sender not a player
        if(playerOnly && !(sender instanceof Player))
        {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        // check for permission
        String permission = getPermission();
        if(permission != null && !sender.hasPermission(permission))
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        // check for delay on command
        if((delayedPlayers != null) && (sender instanceof Player) && (delay > 0))
        {
            Player player = (Player) sender;
            if (delayedPlayers.contains(player.getName()))
            {
                player.sendMessage(ChatColor.RED + "Please wait " + ChatColor.GRAY + delay +
                                ChatColor.RED +" seconds before using this command again.");
                return true;
            }

            // add player to delay list
            delayedPlayers.add(player.getName());
            Bukkit.getScheduler().scheduleSyncDelayedTask(TeleportRequest.getInstance(), () -> {
                delayedPlayers.remove(player.getName());
            }, 20L * delay);
        }

        // try to run command
        if(!onCommand(sender, args))
        {
            sendUsage(sender);
        }

        return true;
    }

    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
    {
        return this.onCommand(sender, args);
    }

    public abstract boolean onCommand(CommandSender sender, String[] args);
    public abstract String getUsage();
}
