package com.sun.spot.peripheral.radio.proc.util;

public class Tokenizer {
	private char[] buffer;
	private int tokenBegin = 0;
	private int tokenEnd = 0;
	
	public Tokenizer(char[] buffer) {
		this.buffer = buffer;
	}
	
	public String nextToken() {
		tokenEnd = tokenBegin;
		while (tokenEnd < buffer.length && buffer[tokenEnd++] != ',');
		
		String token = new String(buffer, tokenBegin, tokenEnd - tokenBegin - 1);
		tokenBegin = tokenEnd;
		
		return token.trim();
	}
}
