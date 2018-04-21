/**
 * Implementation of local server on Raspberry Pi.
 * Package contains beans for HTML pages and implementation of servlet for communication with Raspberry Pi {@link com.kuryshee.safehome.rpiserver.RpiServlet}.
 * It has an implementation of {@link javax.servlet.Filter} to prevent accessing pages for unauthorized users.
 * The server shares information about users with Raspberry Pi application via {@link com.kuryshee.safehome.rpiserver.UserConfigManager}.
 * 
 * @author Ekaterina Kurysheva
 */
package com.kuryshee.safehome.rpiserver;