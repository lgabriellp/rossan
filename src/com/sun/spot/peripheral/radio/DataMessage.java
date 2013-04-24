package com.sun.spot.peripheral.radio;

import com.sun.spot.peripheral.radio.proc.util.Tokenizer;

public class DataMessage {
	private int payload;
	private long source;
	
	public DataMessage(long source, Tokenizer tokenizer) {
		this.source = source;
		this.payload = Integer.parseInt(tokenizer.nextToken());
	}

	public DataMessage(int message) {
		payload = message;
	}

	public long getSource() {
		return this.source;
	}
	
	public String toString() {
		return Integer.toString(payload) + ",";
	}

}
