package com.sun.spot.peripheral.radio.proc.util;

public class Tokenizer {
	private char[] buffer;
	private int tokenBegin;
	private int tokenEnd;
	private int offset;
	private int length;
	
	public Tokenizer(char[] buffer, int offset, int length) {
		this.offset = offset;
		this.length = length;
		this.tokenBegin = offset;
		this.tokenEnd = offset;
		this.buffer = buffer;
	}
	
	public String nextToken() {
		tokenEnd = tokenBegin;
		while (tokenEnd < (this.offset + this.length) && buffer[tokenEnd++] != ',');
		
		String token = new String(buffer, tokenBegin, tokenEnd - tokenBegin - 1);
		tokenBegin = tokenEnd;
		
		return token.trim();
	}
}
