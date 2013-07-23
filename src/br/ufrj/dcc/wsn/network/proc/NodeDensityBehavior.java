package br.ufrj.dcc.wsn.network.proc;

import java.util.Vector;

public class NodeDensityBehavior implements ApplicationBehavior {

	public int getRoutingRules(Vector neighbors, RoutingEntry parent) {
		return NetworkInterface.ROUTING_RULES_MAX / neighbors.size();
	}

	public void startRoutingCycle(int cycle, boolean coord) {
		// TODO Auto-generated method stub
		
	}

	public void joinedBackbone() {
		// TODO Auto-generated method stub
		
	}

}
