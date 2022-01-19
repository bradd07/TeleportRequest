package org.dreamleaf.teleportrequest;
import org.bukkit.entity.Player;
import java.util.UUID;

/**
 * Class handles the storing of teleport request information in a BST structure
 */
public class TPRequest {

    /**
     * Player who sent the tpa request
     */
    public Player sender;

    /**
     * Player who received the tpa request
     */
    public Player receiver;

    /**
     * UUID of player who received the tpa request
     */
    public UUID receiverUUID;

    /**
     * Reference to the left child
     */
    public TPRequest leftRef;

    /**
     * Reference to the right child
     */
    public TPRequest rightRef;

    /**
     * Identifier for request type (tpa/tpahere)
     * 1001 = TPA
     * 2002 = TPAHERE
     */
    public int type;

    /**
     * Initialization constructor, handles TP request info
     * @param sender String name of the player who sent the tpa request
     * @param receiver String name of the player who received the tpa reuqest
     */
    public TPRequest(Player sender, Player receiver, UUID uuid, int type)
    {
        // initialize variables
        this.receiver = receiver;
        this.sender = sender;
        this.receiverUUID = uuid;
        this.type = type;
        leftRef = rightRef = null;
    }

    /**
     * Copy constructor, handles copied TP request info
     * @param copied TPRequest
     */
    public TPRequest(TPRequest copied)
    {
        // initialize variables
        this.receiver = copied.receiver;
        this.sender = copied.sender;
        this.receiverUUID = copied.receiverUUID;
        this.type = copied.type;
        leftRef = rightRef = null;
    }

    /**
     * Used to update the information of a teleport request
     * @param newData TPRequest data containing new information
     */
    public void setData(TPRequest newData)
    {
        // initialize variables
        this.receiver = newData.receiver;
        this.sender = newData.sender;
        this.receiverUUID = newData.receiverUUID;
        this.type = newData.type;
    }

    /**
     * Get the name of the player who sent the tpa request
     * @return String name of player
     */
    public Player getSender()
    {
        return sender;
    }

    /**
     * Get the name of the player who received the tpa request
     * @return String name of player
     */
    public Player getReceiver()
    {
        return receiver;
    }

    /**
     * Get the UUID of the player who received the tpa request
     * @return UUID of player
     */
    public UUID getReceiverUUID()
    {
        return receiverUUID;
    }
}
