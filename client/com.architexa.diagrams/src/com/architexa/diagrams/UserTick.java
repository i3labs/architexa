package com.architexa.diagrams;

import org.apache.log4j.Logger;

/**
 * The big class for logging events. Things that happen infrequently but
 * consistently per user interaction go here. Examples are command execution and
 * selection events. Such places can be good to debug and are therefore
 * recommended to be added, but can cause noise during debugging and are hence
 * located via one central point (so that they can be disabled easily).
 */
public class UserTick {
    public static final Logger logger = Activator.getLogger(UserTick.class);
}
