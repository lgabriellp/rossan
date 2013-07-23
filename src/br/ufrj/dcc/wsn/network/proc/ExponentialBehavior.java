package br.ufrj.dcc.wsn.network.proc;

import java.util.Vector;

public class ExponentialBehavior implements ApplicationBehavior {
	private int lastCycle;
	private int lastCoordCycle;
	
	public int getRoutingRules(Vector neighbors, RoutingEntry parent) {
		int exponent = lastCycle - lastCoordCycle;
		if (exponent > NetworkInterface.ROUTING_RULES_MAX_EXP)
			exponent = NetworkInterface.ROUTING_RULES_MAX_EXP;
		
		return 1 << exponent;
	}

	public void startRoutingCycle(int cycle, boolean coord) {
		lastCycle = cycle;
		if (coord)
			joinedBackbone();
	}

	public void joinedBackbone() {
		lastCoordCycle = lastCycle;
	}

}
