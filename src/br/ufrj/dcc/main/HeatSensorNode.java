package br.ufrj.dcc.main;

import br.ufrj.dcc.mac.PacketReader;
import br.ufrj.dcc.routing.proc.Message;
import br.ufrj.dcc.routing.proc.Node;


public class HeatSensorNode extends Node {
	private short temperature;
	
	public HeatSensorNode() {
		super("SensorNode");
		temperature = 0;
	}

	public Message processDataMessage(PacketReader reader) {
		log.info("PrepareDataMessage");
		return new HeatMessage(reader);
	}
	
	protected boolean mainStep() {
		send(new HeatMessage(temperature++));
		return waitNotInterrupted(2500);
	}
}