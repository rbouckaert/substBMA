package substbma.math.distributions;

import beast.base.core.Function;
import beast.base.inference.distribution.ParametricDistribution;
import beast.base.core.Description;

/**
 * @author Chieh-Hsi Wu
 *
 */
@Description("For testing only. Still don't laugh.")
public class ContinuousCategoricalDistribution2 extends ParametricDistribution{
    public void initAndValidate(){}
    public ParametricDistribution getDistribution(){
        return null;

    }

    public double calcLogP(Function val){
        //System.err.println("Hello?");
        if(val.getArrayValue() > 0.0 && val.getArrayValue()<0.5){
            return Math.log(1.0);
        }else if (val.getArrayValue()>0.5 && val.getArrayValue()<1.0){
            return Math.log(1.0);
        }else{
            return Double.NEGATIVE_INFINITY;
        }
      
    }
}
