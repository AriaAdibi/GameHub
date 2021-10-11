package util;

public class Chronometer {

	private long start;

	public void start() {
		start = System.currentTimeMillis();
	}

	public double stop() {
		return (double) (System.currentTimeMillis() - start) / 1000;
	}

}
