package br.ufrj.dcc.wsn.main;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.network.proc.Message;
import br.ufrj.dcc.wsn.network.proc.Node;


public class HeatSensorNode extends Node {
	private HeatMessage message;
	private short temperature;
	
	public HeatSensorNode() {
		super("HeatSensorNode");
		message = new HeatMessage();
		temperature = 0;
	}

	public Message processDataMessage(PacketReader reader) {
		message.readFrom(reader);
		log.info("ProcessDataMessage");
		return message;
	}
	
	protected boolean mainStep() {
		message.setTemperature(temperature++);
		message.setOrigin(getAddress());
		send(message);
		return waitNotInterrupted(getInterval());
	}
}
