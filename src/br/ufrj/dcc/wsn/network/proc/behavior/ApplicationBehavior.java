package br.ufrj.dcc.wsn.network.proc.behavior;

import java.util.Vector;

import br.ufrj.dcc.wsn.network.proc.RoutingEntry;

public interface ApplicationBehavior {
	int getRoutingRules(Vector neighbors, RoutingEntry parent);
	void startRoutingCycle(int cycle, boolean coord);
	void joinedBackbone();
}
