package br.ufrj.dcc.wsn.profile;

public class Profiler {
	private static Profiler instance;
	
	private long processingTimeMs;
	private long lastProcessingStartTime;
	private long receivedBytes;
	private long transmitedBytes;
	
	private Profiler() {
		processingTimeMs = 0;
		receivedBytes = 0;
		transmitedBytes = 0;
	}
	
	public static Profiler getInstance() {
		if (instance == null)
			instance = new Profiler();
		return instance;
	}
	
	public void startProcessing() {
		lastProcessingStartTime = now();
	}
	
	public void stopProcessing() {
		processingTimeMs += now() - lastProcessingStartTime;
	}
	
	public void receiving(long bytes) {
		receivedBytes += bytes;
	}
	
	public void transmiting(long bytes) {
		transmitedBytes += bytes;
	}
	
	public short getSpentEnergy() {
		long energy = transmitedBytes + receivedBytes;
		
		if (energy > Short.MAX_VALUE)
            energy = Short.MAX_VALUE;
        else if (energy < 0)
            energy = 0;
		
		return (short)(100 * (Short.MAX_VALUE - energy)/Short.MAX_VALUE);
	}
	
	public short getProcessingTimeMs() {
		if (processingTimeMs > Short.MAX_VALUE)
			processingTimeMs = Short.MAX_VALUE;
		else if (processingTimeMs < 0)
			processingTimeMs = 0;
		
		return (short)(100 * processingTimeMs / Short.MAX_VALUE);
	}

	public long getReceivedBytes() {
		return receivedBytes;
	}

	public long getTransmitedBytes() {
		return transmitedBytes;
	}

	private long now() {
		return System.currentTimeMillis();
	}
}
