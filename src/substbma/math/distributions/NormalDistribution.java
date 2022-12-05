package substbma.math.distributions;

import beast.base.core.Input;
import beast.base.inference.distribution.Normal;
import beast.base.inference.parameter.RealParameter;

/**
 * Created by IntelliJ IDEA.
 * User: cwu080
 * Date: 9/07/13
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class NormalDistribution extends Normal {
    public Input<RealParameter> precisionInput = new Input<RealParameter>("precision", "variance of the normal distribution, defaults to 1");

    void refresh() {
        double fMean;
        double fSigma;
        if (meanInput.get() == null) {
            fMean = 0;
        } else {
            fMean = meanInput.get().getArrayValue();
        }
        if (sigmaInput.get() != null) {
            fSigma = sigmaInput.get().getArrayValue();
        }else if(precisionInput.get() != null){
            fSigma = 1.0/precisionInput.get().getValue();

        } else {
            fSigma = 1;
        }

        ((org.apache.commons.math.distribution.NormalDistribution)getDistribution()).setMean(fMean);
        ((org.apache.commons.math.distribution.NormalDistribution)getDistribution()).setStandardDeviation(fSigma);
    }
}
