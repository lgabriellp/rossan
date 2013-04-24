package com.sun.spot.peripheral.radio;

import com.sun.spot.peripheral.radio.proc.util.Tokenizer;

public class DataMessage {
	private int payload;
	private long source;
	
	public DataMessage(long source, Tokenizer tokenizer) {
		this.source = source;
		this.payload = Integer.parseInt(tokenizer.nextToken());
	}

	public long getSource() {
		return this.source;
	}
	
	public String getMessage() {
		return Integer.toString(payload);
	}

}
