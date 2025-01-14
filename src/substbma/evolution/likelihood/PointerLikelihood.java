package substbma.evolution.likelihood;


import beast.base.core.Description;
import beast.base.inference.Distribution;
import beast.base.inference.State;
import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;
import substbma.core.parameter.DPPointer;
import substbma.core.parameter.ParameterList;
import beast.base.inference.distribution.ParametricDistribution;

import java.util.Random;
import java.util.List;

/**
 * @author Chieh-Hsi Wu
 */
@Description("This class is probably used for testing.")
public class PointerLikelihood extends Distribution {
    public Input<DPPointer> pointersInput = new Input<DPPointer>(
            "pointers",
            "This pointer values with likelihood to be computed.",
            Input.Validate.REQUIRED
    );

    public Input<ParameterList> parameterListInput = new Input<ParameterList>(
            "parameterList",
            "This pointer values with likelihood to be computed.",
            Input.Validate.REQUIRED
    );
    public Input<ParametricDistribution> distrInput = new Input<ParametricDistribution>(
            "distr",
            "distribution used to calculate prior, e.g. normal, beta, gamma.",
            Input.Validate.REQUIRED
    );

	/** shadows m_distInput **/
	ParametricDistribution distr;
    DPPointer pointers;
    int pointersCount;

	@Override
	public void initAndValidate() {
		distr = distrInput.get();
        pointers = pointersInput.get();
        pointersCount = pointers.getDimension();
	}


	@Override
	public double calculateLogP() {
        logP = 0.0;
        for(int i = 0; i < pointersCount;i++){
            logP += distr.calcLogP(pointers.getParameter(i));

        }
        //System.out.println("logP: "+logP);
		return logP;
	}

    public double[] getLiks(RealParameter parameter){
        double[] liks = new double[pointers.getDimension()];
        try{
            for(int i = 0; i < pointers.getDimension();i++){
                liks[i] = Math.exp(distr.calcLogP(parameter));
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return liks;

    }




	@Override public void sample(State state, Random random) {}
	@Override public List<String> getArguments() {return null;}
	@Override public List<String> getConditions() {return null;}
}
