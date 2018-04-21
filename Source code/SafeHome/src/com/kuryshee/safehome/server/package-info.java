/**
 * Provides classes necessary for server implementation in Safe Home project.
 * Contains servlets for processing HTTP requests from client application and Raspberry Pi:
 * {@link com.kuryshee.safehome.server.AndroidAppServlet} and {@link com.kuryshee.safehome.server.RpiCommunicationServlet}. 
 * Also has helper classes for gaining the responses for requests, including implementation of {@link com.kuryshee.safehome.database.DatabaseAccessInterface}:
 * {@link com.kuryshee.safehome.server.DatabaseAccessImpl}.
 * 
 * @author Ekaterina Kurysheva
 */
package com.kuryshee.safehome.server;