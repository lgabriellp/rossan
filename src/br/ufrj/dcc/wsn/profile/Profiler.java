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
	
	public long getSpentEnergy() {
		return 0;
	}
	
	public long getProcessingTimeMs() {
		return processingTimeMs;
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
