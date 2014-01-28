package org.mobicents.media.core.ice;

/**
 * A factory to produce ICE agents.<br>
 * The user can decide whether to create agents for full or lite ICE
 * implementations.
 * 
 * @author Henrique Rosa
 * 
 */
public class IceFactory {

	/**
	 * Produces a new agent that implements ICE-lite.<br>
	 * As such, this agent won't keep states nor attempt connectivity checks.<br>
	 * It will only listen to incoming connectivity checks.
	 * 
	 * @return The ice-lite agent
	 */
	public static IceLiteAgent createLiteAgent() {
		return new IceLiteAgent();
	}
}
