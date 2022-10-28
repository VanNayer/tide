
package org.ndenayer;

public class Constituent
{
	public String name;

	// Speed is in degrees per solar hour.
	public float speeds; // cst_speeds

	// The following table gives loc_amp arguments for each year that
	// we can predict tides for.  The loc_amp argument is in degrees for
	// the meridian of Greenwich, at the beginning of each year.
	public float[] epochs; // cst_epochs

	// Now come the node factors for the middle of each year that we can
	// predict tides for.
	public float[] nodes; // cst_nodes
}
