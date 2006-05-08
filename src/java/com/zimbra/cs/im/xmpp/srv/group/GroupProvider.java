/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite Server.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2005 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.im.xmpp.srv.group;

import org.xmpp.packet.JID;

import java.util.Collection;
import java.util.Set;

/**
 * Provider interface for groups. Users that wish to integrate with
 * their own group system must implement this class and then register
 * the implementation with Wildfire in the <tt>wildfire.xml</tt>
 * file. An entry in that file would look like the following:
 *
 * <pre>
 *   &lt;provider&gt;
 *     &lt;group&gt;
 *       &lt;className&gt;com.foo.auth.CustomGroupProvider&lt;/className&gt;
 *     &lt;/group&gt;
 *   &lt;/provider&gt;</pre>
 *
 * @author Matt Tucker
 */
public interface GroupProvider {

    /**
     * Creates a group with the given name (optional operation).<p>
     *
     * The provider is responsible for setting the creation date and
     * modification date to the current date/time.
     *
     * @param name name of the group.
     * @return the newly created group.
     * @throws GroupAlreadyExistsException if a group with the same name already
     *      exists.
     * @throws UnsupportedOperationException if the provider does not
     *      support the operation.
     */
    Group createGroup(String name) throws UnsupportedOperationException,
            GroupAlreadyExistsException;

    /**
     * Deletes the group (optional operation).
     *
     * @param name the name of the group to delete.
     * @throws UnsupportedOperationException if the provider does not
     *      support the operation.
     */
    void deleteGroup(String name) throws UnsupportedOperationException;

    /**
     * Returns a group based on it's name.
     *
     * @param name the name of the group.
     * @return the group with the given name.
     * @throws GroupNotFoundException If no group with that ID could be found
     */
    Group getGroup(String name) throws GroupNotFoundException;

    /**
     * Sets the name of a group to a new name.
     *
     * @param oldName the current name of the group.
     * @param newName the desired new name of the group.
     * @throws GroupAlreadyExistsException if a group with the same name already
     *      exists.
     * @throws UnsupportedOperationException if the provider does not
     *      support the operation.
     */
    void setName(String oldName, String newName) throws UnsupportedOperationException,
            GroupAlreadyExistsException;

    /**
     * Updates the group's description.
     *
     * @param name the group name.
     * @param description the group description.
     * @throws GroupNotFoundException if no existing group could be found to update.
     */
    void setDescription(String name, String description)
            throws GroupNotFoundException;

    /**
     * Returns the number of groups in the system.
     *
     * @return the number of groups in the system.
     */
    int getGroupCount();

    /**
     * Returns the Collection of all groups in the system.
     *
     * @return the Collection of all groups.
     */
    Collection<Group> getGroups();

    /**
     * Returns the Collection of groups for the specified groups names.
     *
     * @return the Collection with the requested groups.
     */
    Collection<Group> getGroups(Set<String> groupNames);

    /**
     * Returns the Collection of all groups in the system.
     *
     * @param startIndex start index in results.
     * @param numResults number of results to return.
     * @return the Collection of all groups given the <tt>startIndex</tt> and <tt>numResults</tt>.
     */
    Collection<Group> getGroups(int startIndex, int numResults);

    /**
     * Returns the Collection of Groups that an entity belongs to.
     *
     * @param user the JID of the entity.
     * @return the Collection of groups that the user belongs to.
     */
    Collection<Group> getGroups(JID user);

    /**
     * Adds an entity to a group (optional operation).
     *
     * @param groupName the group to add the member to
     * @param user the JID of the entity to add
     * @param administrator True if the member is an administrator of the group
     * @throws UnsupportedOperationException if the provider does not
     *      support the operation.
     */
    void addMember(String groupName, JID user, boolean administrator)
            throws UnsupportedOperationException;

    /**
     * Updates the privileges of an entity in a group.
     *
     * @param groupName the group where the change happened
     * @param user the JID of the entity with new privileges
     * @param administrator True if the member is an administrator of the group
     * @throws UnsupportedOperationException if the provider does not
     *      support the operation.
     */
    void updateMember(String groupName, JID user, boolean administrator)
            throws UnsupportedOperationException;

    /**
     * Deletes an entity from a group (optional operation).
     *
     * @param groupName the group name.
     * @param user the JID of the entity to delete.
     * @throws UnsupportedOperationException if the provider does not
     *      support the operation.
     */
    void deleteMember(String groupName, JID user) throws UnsupportedOperationException;

    /**
     * Returns true if this GroupProvider is read-only. When read-only,
     * groups can not be created, deleted, or modified.
     *
     * @return true if the user provider is read-only.
     */
    public boolean isReadOnly();
}