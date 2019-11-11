package cs555.chiba.util;

public class Metric {
	private int numResults;
	private int numHops;

	public Metric(int numResults, int numHops) {
		this.numResults = numResults;
		this.numHops = numHops;
	}

	public int getNumResults() {
		return numResults;
	}
	
	public int getNumHops() {
		return numHops;
	}
}
