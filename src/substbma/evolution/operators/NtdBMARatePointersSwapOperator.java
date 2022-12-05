package substbma.evolution.operators;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.evolution.alignment.Alignment;
import beast.base.util.Randomizer;
import substbma.core.parameter.DPPointer;
import substbma.core.parameter.DPValuable;

/**
 * @author Chieh-Hsi
 */
@Description("This operator swaps sites from different substitution model and rate categories.")
public class NtdBMARatePointersSwapOperator extends Operator {
    public Input<DPPointer> paramPointersInput = new Input<DPPointer>(
            "paramPointers",
            "array which points a set of unique parameter values",
            Input.Validate.REQUIRED
    );

    public Input<DPPointer> modelPointersInput = new Input<DPPointer>(
            "modelPointers",
            "array which points a set of unique model",
            Input.Validate.REQUIRED
    );

    public Input<DPPointer> freqPointersInput = new Input<DPPointer>(
            "freqPointers",
            "array which points a set of unique model",
            Input.Validate.REQUIRED
    );

    public Input<DPPointer> ratesPointersInput = new Input<DPPointer>(
            "ratesPointers",
            "Pointers that records the assignment of sites to rate",
            Input.Validate.REQUIRED
    );

    public Input<DPValuable> dpValInput = new Input<DPValuable>("dpVal", "A valuable that stores information about the category assignments", Input.Validate.REQUIRED);
    public Input<Alignment> alignmentInput = new Input<Alignment>("alignment", "A valuable that stores information about the category assignments", Input.Validate.REQUIRED);

    private DPValuable dpVal;
    private Alignment alignment;

    public void initAndValidate(){
        dpVal = dpValInput.get();
        alignment = alignmentInput.get();
    }

    public double proposal(){
        int categoryCount = dpVal.getCategoryCount();
        if(categoryCount == 1){
            return Double.NEGATIVE_INFINITY;
        }

        int categoryIndex1 = (int)(Randomizer.nextDouble()*categoryCount);
        int categoryIndex2 = categoryIndex1;
        while(categoryIndex1 == categoryIndex2){
            int something = (int)(Randomizer.nextDouble()*categoryCount);
            //System.out.println("Hi! "+categoryCount+" "+something);
            categoryIndex2 = something;
        }

        int[] sitesInCategoryIndex1 = dpVal.getClusterSites(categoryIndex1);
        int[] sitesInCategoryIndex2 = dpVal.getClusterSites(categoryIndex2);
        int site1 = sitesInCategoryIndex1[(int)(sitesInCategoryIndex1.length*Randomizer.nextDouble())];
        int site2 = sitesInCategoryIndex2[(int)(sitesInCategoryIndex2.length*Randomizer.nextDouble())];
        /*if(alignment.getPatternIndex(site1) == alignment.getPatternIndex(site2)){
            return Double.NEGATIVE_INFINITY;
        }*/


        DPPointer paramPointers = paramPointersInput.get();
        paramPointers.swapPointers(site1, site2);
        DPPointer modelPointers = modelPointersInput.get();
        modelPointers.swapPointers(site1, site2);
        DPPointer freqPointers = freqPointersInput.get();
        freqPointers.swapPointers(site1, site2);
        DPPointer ratesPointers = ratesPointersInput.get();
        ratesPointers.swapPointers(site1, site2);

        return 0.0;
    }
}
