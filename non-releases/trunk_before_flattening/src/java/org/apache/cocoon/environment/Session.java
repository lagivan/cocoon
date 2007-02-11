/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.environment;

import java.util.Enumeration;
import java.util.Map;

/**
 *
 * Provides a way to identify a user across more than one page
 * request or visit to a Web site and to store information about that user.
 *
 * <p>Cocoon uses this interface to create a session
 * between a client and the "cocoon server". The session persists
 * for a specified time period, across more than one connection or
 * page request from the user. A session usually corresponds to one
 * user, who may visit a site many times. The server can maintain a
 * session in many ways such as using cookies or rewriting URLs.
 *
 * <p>This interface allows Cocoon to
 * <ul>
 * <li>View and manipulate information about a session, such as
 *     the session identifier, creation time, and last accessed time
 * <li>Bind objects to sessions, allowing user information to persist
 *     across multiple user connections
 * </ul>
 *
 * <p>Session information is scoped only to the current context
 * (<code>Context</code>), so information stored in one context
 * will not be directly visible in another.
 *
 * @version $Id$
 */

public interface Session {

    /**
     *
     * Returns the time when this session was created, measured
     * in milliseconds since midnight January 1, 1970 GMT.
     *
     * @return                                a <code>long</code> specifying
     *                                         when this session was created,
     *                                        expressed in
     *                                        milliseconds since 1/1/1970 GMT
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */
    long getCreationTime();

    /**
     *
     * Returns a string containing the unique identifier assigned
     * to this session. The identifier is assigned
     * by the context container and is implementation dependent.
     *
     * @return                                a string specifying the identifier
     *                                        assigned to this session
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */
    String getId();

    /**
     *
     * Returns the last time the client sent a request associated with
     * this session, as the number of milliseconds since midnight
     * January 1, 1970 GMT.
     *
     * <p>Actions that your application takes, such as getting or setting
     * a value associated with the session, do not affect the access
     * time.
     *
     * @return                                a <code>long</code>
     *                                        representing the last time
     *                                        the client sent a request associated
     *                                        with this session, expressed in
     *                                        milliseconds since 1/1/1970 GMT
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */

    long getLastAccessedTime();

    /**
     *
     * Specifies the time, in seconds, between client requests before the
     * contextcontainer will invalidate this session.  A negative time
     * indicates the session should never timeout.
     *
     * @param interval                An integer specifying the number
     *                                 of seconds
     *
     */
    void setMaxInactiveInterval(int interval);

   /**
    * Returns the maximum time interval, in seconds, that
    * the context container will keep this session open between
    * client accesses. After this interval, the context container
    * will invalidate the session.  The maximum time interval can be set
    * with the <code>setMaxInactiveInterval</code> method.
    * A negative time indicates the session should never timeout.
    *
    *
    * @return                an integer specifying the number of
    *                        seconds this session remains open
    *                        between client requests
    *
    * @see                #setMaxInactiveInterval(int)
    *
    *
    */
    int getMaxInactiveInterval();

    /**
     *
     * Returns the object bound with the specified name in this session, or
     * <code>null</code> if no object is bound under the name.
     *
     * @param name                a string specifying the name of the object
     *
     * @return                        the object with the specified name
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */
    Object getAttribute(String name);

    /**
     *
     * Returns an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of all the objects bound to this session.
     *
     * @return                        an <code>Enumeration</code> of
     *                                <code>String</code> objects specifying the
     *                                names of all the objects bound to
     *                                this session
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */
    Enumeration getAttributeNames();

    /**
     * Binds an object to this session, using the name specified.
     * If an object of the same name is already bound to the session,
     * the object is replaced.
     *
     *
     * @param name                        the name to which the object is bound;
     *                                        cannot be null
     *
     * @param value                        the object to be bound; cannot be null
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     *
     */
    void setAttribute(String name, Object value);

    /**
     *
     * Removes the object bound with the specified name from
     * this session. If the session does not have an object
     * bound with the specified name, this method does nothing.
     *
     *
     * @param name                                the name of the object to
     *                                                remove from this session
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        invalidated session
     */
    void removeAttribute(String name);

    /**
     * Utility method for getting a <code>Map</code> view of the request attributes.
     * Returns a <code>Map</code> with attributes.
     *
     * @return                a <code>Map</code> containing the request attributes.
     *
     * @since 2.2
     */
    Map getAttributes();

    /**
     *
     * Invalidates this session
     * to it.
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        already invalidated session
     *
     */
    void invalidate();

    /**
     *
     * Returns <code>true</code> if the client does not yet know about the
     * session or if the client chooses not to join the session.  For
     * example, if the server used only cookie-based sessions, and
     * the client had disabled the use of cookies, then a session would
     * be new on each request.
     *
     * @return                                 <code>true</code> if the
     *                                        server has created a session,
     *                                        but the client has not yet joined
     *
     * @exception IllegalStateException        if this method is called on an
     *                                        already invalidated session
     *
     */
    boolean isNew();

}

