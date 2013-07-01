package br.ufrj.dcc.wsn.main;

import com.sun.spot.util.IEEEAddress;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.network.proc.Message;
import br.ufrj.dcc.wsn.network.proc.Node;
import br.ufrj.dcc.wsn.util.Logger;


public class BaseStation extends Node {
	private HeatMessage message;
	private int cycle;
	
	public BaseStation() {
		super("AccessPoint");
		message = new HeatMessage();
		cycle = 1;
	}
	
	public Message processDataMessage(PacketReader reader) {
		message.readFrom(reader);
		log.log(Logger.NET, "arrived from "+IEEEAddress.toDottedHex(reader.getSourceAddress())+" data"+message);
		return null;
	}
	
	protected boolean mainStep() {
		getRoutingInterface().startNewCycle(cycle++);
		return waitNotInterrupted(3*getInterval());
	}
}
