package br.ufrj.dcc.wsn.main;

import com.sun.spot.util.IEEEAddress;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.network.proc.Message;
import br.ufrj.dcc.wsn.network.proc.NetworkInterface;
import br.ufrj.dcc.wsn.network.proc.Node;
import br.ufrj.dcc.wsn.network.proc.RoutingEntry;
import br.ufrj.dcc.wsn.profile.Profiler;
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
		
		RoutingEntry mySelf = getRoutingInterface().getState();
		
		log.log(Logger.DIGEST, 	"root digest "+
				mySelf.getCycle()+","+
                mySelf.getHops()+","+
                mySelf.isCoord()+","+
                mySelf.getEnergy()+","+
                Profiler.getInstance().getProcessingTimeMs()+","+
				IEEEAddress.toDottedHex(NetworkInterface.BROADCAST));
		
		return null;
	}
	
	protected boolean mainStep() {
		getRoutingInterface().startNewCycle(cycle++);
		return waitNotInterrupted(3*getInterval());
	}
}
