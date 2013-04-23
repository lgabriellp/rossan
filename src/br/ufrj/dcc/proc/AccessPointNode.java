package br.ufrj.dcc.proc;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.spot.peripheral.radio.AccessPoint;
import com.sun.spot.peripheral.radio.RoutingInterface;

public class AccessPointNode extends MIDlet implements AccessPoint {
	private RoutingInterface router;

	public AccessPointNode() {
		router = new RoutingInterface();
		router.setAccessPoint(this);
		
		router.setCycle(1);
		router.sendRoutingPacket(RoutingInterface.SYNC, RoutingInterface.BROADCAST);
	}
	
	protected void startApp() throws MIDletStateChangeException {
		router.log("Starting: Access Point");
		router.start();
	}

	protected void pauseApp() {
		router.log("Pausing: Access Point");
	}
	
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		router.interrupt();
		router.log("Terminating: Access Point");
	}
}
