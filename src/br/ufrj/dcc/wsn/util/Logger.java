package br.ufrj.dcc.wsn.util;

public class Logger {
	public static final int NONE = 0;
	public static final int INFO = 1 << 0;
	public static final int WARNING = 1 << 1;
	public static final int DEBUG = 1 << 2;
	public static final int APP = 1 << 3;
	public static final int NET = 1 << 4;
	public static final int LINK = 1 << 5;
	public static final int DIGEST = 1 << 6;

	private String prefix;
	private int level;

	public Logger(String prefix) {
		this.prefix = prefix;
		this.level = NONE;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return this.level;
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
