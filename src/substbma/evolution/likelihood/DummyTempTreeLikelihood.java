package substbma.evolution.likelihood;

import beast.base.core.Description;
import beast.base.inference.parameter.RealParameter;
import substbma.evolution.substitutionmodel.NtdBMA;

/**
 * @author Chieh-Hsi Wu
 */
@Description("This class is well useful for testing purpose.")
public class DummyTempTreeLikelihood extends TempTreeLikelihood{

    public double calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
            int site){
        return 0.0;
    }


    public double calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
            RealParameter rate,
            int site){

        return 0.0;
    }


    public double calculateLogP(
            RealParameter rateParameter,
            int site){

        return 0.0;
    }

}
