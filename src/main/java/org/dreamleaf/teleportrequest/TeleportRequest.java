package org.dreamleaf.teleportrequest;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * TeleportRequest - TeleportRequest request plugin utilizing a BST
 * Copyright © 2022 DreamLeaf Inc.
 * @author bradd07
 *
 * This main class is part of TeleportRequest.
 * TeleportRequest is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TeleportRequest is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General
 * Public License along with TeleportRequest. If not, see
 * <https://www.gnu.org/licenses/>.
 *
 */
public final class TeleportRequest extends JavaPlugin {

    /**
     * TeleportRequest reference for plugin
     */
    private static TeleportRequest instance;

    /**
     * Root of TPRequest tree
     */
    private static TPRequest requestsRoot = null;

    @Override
    public void onDisable() {
        // notify shutdown
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[TeleportRequest] Plugin Disabled");
    }

    @Override
    public void onEnable() {
        // initialize instance
        instance = this;

        // notify startup
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[TeleportRequest] Plugin Enabled");

        // Get commands
        new TPCommands().getCommands();
    }

    /**
     * Adds a teleport request to the BST
     * @param data TPRequest data to be added to BST
     */
    public static void addRequest(TPRequest data)
    {
        // add data
        addRequest(requestsRoot, data);
    }

    /**
     * Overloaded method for BST insert
     * <p>
     * Note: Inserts by player UUID
     * <p>
     * Note: Uses "look-down" technique, links to current node;
     * handles special case of empty BST
     * @param root TPRequest tree root reference at the current
     * recursion level
     * @param data TPRequest item to be added to BST
     */
    private static void addRequest(TPRequest root, TPRequest data)
    {
        // check for empty
        if(root != null)
        {
            // compare uuids
            if(root.receiverUUID.compareTo(data.receiverUUID) > 0)
            {
                if(root.leftRef == null)
                {
                    root.leftRef = new TPRequest(data);
                }
                else
                {
                    addRequest(root.leftRef, data);
                }
            }
            else if(root.receiverUUID.compareTo(data.receiverUUID) < 0)
            {
                if(root.rightRef == null)
                {
                    root.rightRef = new TPRequest(data);
                }
                else
                {
                    addRequest(root.rightRef, data);
                }
            }
        }
        else
        {
            //  if empty, it is now the root
            requestsRoot = new TPRequest(data);
        }
    }

    /**
     * Get the instance
     * @return TeleportRequest instance
     */
    public static TeleportRequest getInstance()
    {
        return instance;
    }

    /**
     * Searches BST from given node to the maximum node value below it,
     * unlinks and returns found teleport request
     * @param parent TPRequest reference to current node
     * @param child TPRequest reference to child node to be tested
     * @return TPRequest reference containing removed node
     */
    private static TPRequest removeFromMax(TPRequest parent, TPRequest child)
    {
        // initialize variables
        TPRequest foundMax;

        // check for right child
        if(child.rightRef != null)
        {
            // continue recursion
            return removeFromMax(child, child.rightRef);
        }

        // if no right child found
        foundMax = child;
        parent.rightRef = child.leftRef;
        return foundMax;
    }

    /**
     * Removes request from BST using player UUID
     * <p>
     * Note: First verifies if the teleport request exists with the search method,
     * then if found, calls the overloaded method
     * @param data TPRequest data the includes the UUID to search for
     * @return TPRequest result of remove action
     */
    public static TPRequest removeRequest(TPRequest data)
    {
        // get request to remove
        TPRequest remRequest = searchRequests(data);

        // check for null
        if (remRequest != null)
        {
            // remove and begin recursion with overloaded method
            remRequest = new TPRequest(remRequest);
            requestsRoot = removeRequest(requestsRoot, data);
        }
        // return the teleport request that was removed (null if not found)
        return remRequest;
    }

    /**
     * Overloaded method for BST remove action for removing a
     * teleport request by a player's UUID
     * <p>
     * Note: Assumes removed node is available since it was
     * previously found in removeRequest with the search method
     * @param root TPRequest BST root reference at the current
     * recursion level
     * @param data TPRequest request that includes the player's UUID
     * @return TPRequest reference result of remove action
     */
    private static TPRequest removeRequest(TPRequest root, TPRequest data)
    {
        // initialize variables
        int compareResult;
        TPRequest tempReplacement;

        // compare UUIDs to start to find request in tree
        compareResult = root.receiverUUID.compareTo(data.receiverUUID);

        if(compareResult > 0)
        {
            // not found, must be left
            root.leftRef = removeRequest(root.leftRef, data);
        }
        else if(compareResult < 0)
        {
            // not found, must be right
            root.rightRef = removeRequest(root.rightRef, data);
        }
        else if(root.leftRef == null)
        {
            // found, has right child
            root = root.rightRef;
        }
        else if(root.rightRef == null)
        {
            root = root.leftRef;
        }
        else
        {
            // check for left node w/o right ref
            if(root.leftRef.rightRef == null)
            {
                root.setData(root.leftRef);
                root = root.leftRef.leftRef;
            }
            // assume left child has >= 1 right ref
            else
            {
                tempReplacement = removeFromMax(root, root.leftRef);
                root.setData(tempReplacement);
            }
        }
        return root;
    }

    /**
     * Searches for a teleport request in the BST with
     * a given TPRequest that contains necessary player UUID
     * @param data TPRequest object containing UUID
     * @return TPRequest reference to found teleport request in BST
     */
    public static TPRequest searchRequests(TPRequest data)
    {
        // begin recursion with overloaded method
        return searchRequests(requestsRoot, data);
    }

    /**
     * Overloaded method for BST search action
     * @param root TPRequest BST root reference at the current
     * recursion level
     * @param data TPRequest item containing player UUID
     * @return TPRequest teleport request found
     */
    private static TPRequest searchRequests(TPRequest root, TPRequest data)
    {
        // initialize variables
        int compareResult;

        // check for not null (no active requests)
        if(root != null)
        {
            // compare UUIDs to start to find request in tree
            compareResult = root.receiverUUID.compareTo(data.receiverUUID);

            if(compareResult > 0)
            {
                return searchRequests(root.leftRef, data);
            }
            else if(compareResult < 0)
            {
                return searchRequests(root.rightRef, data);
            }
            // return found item
            return root;
        }
        // player does not have a request
        return null;
    }
}
