package com.sun.spot.peripheral.radio.proc.util;

public class Log {
	public static final int NONE = 0;
	public static final int INFO = 1 << 0;
	public static final int WARNING = 1 << 1;
	public static final int DEBUG = 1 << 2;
	
	private String prefix;
	private int level;

	public Log(String prefix) {
		this.prefix = prefix;
		this.level = NONE;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public void log(int level, String message) {
		if ((level & this.level) == 0)
			return;
		
		System.out.println("[" + this.prefix + "] " + message);
	}
	
	public void info(String message) {
		log(INFO, message);
	}
	
	public void warning(String message) {
		log(WARNING, message);
	}
	
	public void debug(String message) {
		log(DEBUG, message);
	}
}
