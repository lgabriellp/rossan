package br.ufrj.dcc.wsn.network.proc;

import java.util.Vector;

public class DummyBehavior implements ApplicationBehavior {

	public int getRoutingRules(Vector neighbors, RoutingEntry parent) {
		return 0;
	}

	public void startRoutingCycle(int cycle, boolean coord) {
		
	}

	public void joinedBackbone() {

	}

}
