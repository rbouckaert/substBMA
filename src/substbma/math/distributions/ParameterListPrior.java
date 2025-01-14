package substbma.math.distributions;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.distribution.Prior;
import substbma.core.parameter.ParameterList;

/**
 * @author Chieh-Hsi Wu
 */
@Description("This class is a wrapper that provides prior to parameter list.")
public class ParameterListPrior extends Prior{

    public Input<ParameterList> xListInput = new Input<ParameterList>(
            "xList",
            "points at which the density is calculated",
            Input.Validate.REQUIRED
    );

    public Input<Boolean> applyToListInput = new Input<Boolean>(
            "applyToList",
            "Whether the prior is applied to the entire list",
            Input.Validate.REQUIRED
    );

    public ParameterListPrior(){
        m_x.setRule(Input.Validate.OPTIONAL);
    }

    boolean applyToList;
    public void initAndValidate() {
        //System.out.println(getID());

        applyToList = applyToListInput.get();
        super.initAndValidate();

    }

    @Override
	public double calculateLogP() {
        ParameterList parameterList = xListInput.get();
        if(applyToList){
            //System.err.println("logP: "+logP);
            logP = dist.calcLogP(parameterList);
        }else{
            logP = 0.0;

            int dimParam = parameterList.getDimension();
            for(int i = 0; i < dimParam; i ++){
                logP += dist.calcLogP(parameterList.getParameter(i));
            }

        }
        //System.out.println(getID()+" "+logP);
		return logP;
	}


 
}
