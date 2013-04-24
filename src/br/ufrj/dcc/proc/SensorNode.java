package br.ufrj.dcc.proc;

public class SensorNode extends Node {
	private int counter;
	
	public SensorNode() {
		super("SensorNode");
		counter = 0;
	}

	protected void mainStep() {
		send(Integer.toString(counter++));
		waitNotInterrupted(1000);
	}
}
