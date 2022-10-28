package org.ndenayer;

public class Constituents
{
	private int numConstituent; //num_csts
	private Constituent[] constituents;
	private int firstYear = 1970;
	private int numEpochs = 68; // num_nodes // num_epochs

	public Constituent[] getConstituents() { return constituents; }
	public void setConstituents(Constituent[] constituents)
	{
		this.constituents = constituents;
		numConstituent = constituents.length;
		if(numConstituent > 0)
			this.numEpochs = constituents[0].epochs.length;
	}
	public int getNumConstituent() { return numConstituent; }
	public int getFirstYear() { return firstYear; }
	public void setFirstYear(int firstYear) { this.firstYear = firstYear; }
	public int getNumEpochs() { return numEpochs; }
}
