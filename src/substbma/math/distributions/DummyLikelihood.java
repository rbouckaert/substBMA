package substbma.math.distributions;

import beast.base.core.BEASTObject;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Distribution;
import beast.base.inference.State;


import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Chieh-Hsi Wu
 */
@Description("A class that is well uselful for testing things.")
public class DummyLikelihood extends Distribution {
    public Input<List<BEASTObject>> calcNodes = new Input<>("plugin", "Some sort of input", new ArrayList<>());

	@Override
	public void initAndValidate() {}

	@Override
	public double calculateLogP() {
		return 0.0;
	}

	@Override
	public boolean requiresRecalculation() {
		return true;
	}

	@Override public void sample(State state, Random random) {}
	@Override public List<String> getArguments() {return null;}
	@Override public List<String> getConditions() {return null;}

}
