package com.sun.spot.peripheral.radio;

import com.sun.spot.peripheral.radio.proc.util.Tokenizer;

public class RoutingEntry {
	public long source;
	public int cycle;
	public int hops;
	public boolean coord;
	public int energy;
	
	public RoutingEntry() {
		source = -1;
		cycle = 0;
		hops = 0;
		coord = false;
		energy = 100;
	}
	
	public RoutingEntry(long source, Tokenizer tokenizer) {
		this.source = source;
		this.cycle = Integer.parseInt(tokenizer.nextToken());
		this.hops = Integer.parseInt(tokenizer.nextToken());
		this.coord = tokenizer.nextToken().equalsIgnoreCase("true") ? true : false;
		this.energy = Integer.parseInt(tokenizer.nextToken());
	}
	
	public String toString() {
		return cycle + "," + hops + "," + coord + "," + energy + ",";
	}
}

