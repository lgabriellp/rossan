package br.ufrj.dcc.wsn.network.proc.behavior;

import java.util.Vector;

import br.ufrj.dcc.wsn.network.proc.NetworkInterface;
import br.ufrj.dcc.wsn.network.proc.RoutingEntry;

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
