package com.sun.spot.peripheral.radio;

import java.util.Vector;

public interface Application {

	public int getRoutingRules(Vector neighbors, RoutingEntry parent);

	public void startNewCycle(int cycle, boolean coord);

	public String prepareRoutingPacket(String message, long address);

	public void forcedCoord();

	public String forwardData(String message);

}
