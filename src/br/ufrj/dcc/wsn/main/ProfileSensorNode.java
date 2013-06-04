package br.ufrj.dcc.wsn.main;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.network.proc.Message;
import br.ufrj.dcc.wsn.network.proc.Node;
import br.ufrj.dcc.wsn.profile.ProfileMessage;
import br.ufrj.dcc.wsn.profile.Profiler;

public class ProfileSensorNode extends Node {
	private ProfileMessage message;
	
	public ProfileSensorNode() {
		super("ProfileSensorNode");
		message = new ProfileMessage();
	}

	public Message processDataMessage(PacketReader reader) {
		message.readFrom(reader);
		return message;
	}

	protected boolean mainStep() {
		message.setProfileState(Profiler.getInstance());
		message.setRoutingState(getRoutingInterface().getState());
		return waitNotInterrupted(7500);
	}

}
