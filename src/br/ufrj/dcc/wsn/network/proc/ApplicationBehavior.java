package br.ufrj.dcc.wsn.network.proc;

import java.util.Vector;

public interface ApplicationBehavior {
	int getRoutingRules(Vector neighbors, RoutingEntry parent);
	void startRoutingCycle(int cycle, boolean coord);
	void joinedBackbone();
}
