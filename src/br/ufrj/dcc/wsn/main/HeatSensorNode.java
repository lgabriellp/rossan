package br.ufrj.dcc.wsn.main;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.network.proc.Message;
import br.ufrj.dcc.wsn.network.proc.Node;


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
		return waitNotInterrupted(7500);
	}
}