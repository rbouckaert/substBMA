package substbma.evolution.operators;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import substbma.core.parameter.DPPointer;
import substbma.core.parameter.DPValuable;
import substbma.core.parameter.ParameterList;
import substbma.core.parameter.QuietRealParameter;
import substbma.evolution.likelihood.CompoundTempTreeLikelihood;
import substbma.evolution.likelihood.DPSepTreeLikelihood;
import substbma.evolution.likelihood.DPTreeLikelihood;
import substbma.evolution.likelihood.SepTempTreeLikelihood;
import substbma.evolution.likelihood.SlowDPSepTreeLikelihood;
import substbma.evolution.likelihood.TempTreeLikelihood;
import substbma.evolution.sitemodel.DPNtdRateSepSiteModel;
import substbma.math.distributions.DirichletProcess;
import beast.base.inference.distribution.ParametricDistribution;
import beast.base.inference.parameter.RealParameter;
import beast.base.util.Randomizer;

/**
 * @author Chieh-Hsi Wu
 */
@Description("An attempt to speed up this sampler by using multithreading.")
public class FasterRateDPPGibbsSampler  extends Operator {
    public Input<DPPointer> ratePointersInput = new Input<DPPointer>(
            "ratesPointers",
            "array which points a set of unique parameter values",
            Input.Validate.REQUIRED
    );
    public Input<ParameterList> rateListInput = new Input<ParameterList>(
            "ratesList",
            "points at which the density is calculated",
            Input.Validate.REQUIRED
    );


    public Input<DirichletProcess> dpInput = new Input<DirichletProcess>(
            "dirichletProcess",
            "An object of Dirichlet Process",
            Input.Validate.REQUIRED
    );

    public Input<Integer> sampleSizeInput = new Input<Integer>(
            "sampleSize",
            "The number of prelimiary proposals",
            Input.Validate.REQUIRED
    );



    public Input<TempTreeLikelihood> tempLikelihoodInput = new Input<TempTreeLikelihood>(
            "tempLikelihood",
            "The temporary likelihood given the data at site i",
            Input.Validate.REQUIRED
    );

    public Input<DPValuable> dpValuableInput = new Input<DPValuable>(
            "dpVal",
            "reports the counts in each cluster",
            Input.Validate.REQUIRED
    );

    public Input<DPTreeLikelihood> dpTreeLikelihoodInput = new Input<DPTreeLikelihood>(
            "dpTreeLik",
            "Tree likelihood that handle DPP",
            Input.Validate.REQUIRED
    );

    public Input<CompoundTempTreeLikelihood> compoundTempTreeLikelihoodInput = new Input<CompoundTempTreeLikelihood>(
            "compoundTempLikelihood",
            "A list of temporary likelihoods given the data at site i",
            Input.Validate.REQUIRED
    );


    private DirichletProcess dp;
    private int sampleSize;
    private ParametricDistribution rateBaseDistr;
    private DPValuable dpVal;
    private DPTreeLikelihood dpTreeLikelihood;
    private CompoundTempTreeLikelihood compoundTempLik;
    public void initAndValidate(){
        dp = dpInput.get();

        sampleSize = sampleSizeInput.get();
        rateBaseDistr = dp.getBaseDistribution();

        dpVal = dpValuableInput.get();
        dpTreeLikelihood = dpTreeLikelihoodInput.get();
        compoundTempLik = compoundTempTreeLikelihoodInput.get();

    }


    public double proposal(){
        //double smallestVal = Double.NaN;
        //Get the pointer and the list of unique values
        DPPointer ratePointers = ratePointersInput.get();
        ParameterList rateList = rateListInput.get();

        //Randomly pick an index to update, gets it's current value and its position in the parameter list
        int dimPointer = ratePointers.getDimension();
        int index = Randomizer.nextInt(dimPointer);
        int listIndex = ratePointers.indexInList(index,rateList);
        RealParameter currRateVal = rateList.getParameter(listIndex);

        TempTreeLikelihood tempLik = tempLikelihoodInput.get();

        //Get the dimension of the parameter
        int rateDimValue = rateList.getParameterDimension();

        RealParameter curr = ratePointers.getParameter(index);

        //Count the number of items in each cluster but excluding the one about to be updated
        int[] clusterCounts = dpVal.getClusterCounts();
        clusterCounts[listIndex] =  clusterCounts[listIndex]-1;

        QuietRealParameter[] existingRateVals = new QuietRealParameter[clusterCounts.length];
        int[] existingCluster = new int[clusterCounts.length];




        int counter = 0;
        int zeroCount = -1;

        for(int i = 0; i < clusterCounts.length;i++){
            //System.out.println("clusterCounts[i]: "+clusterCounts[i]);
            if(clusterCounts[i]>0){
                clusterCounts[counter] = clusterCounts[i];
                existingRateVals[counter] = rateList.getParameter(i);
                existingCluster[counter] = i;
                counter++;
                //System.out.println("non-zero count: "+rateList.getParameter(i).getIDNumber());
            }else{
                zeroCount = i;
                //System.out.println("zero count: "+rateList.getParameter(i).getIDNumber());
            }
        }

        try{

            //Generate a sample of proposals
//            QuietRealParameter[] ratePreProposals = getSamples(rateBaseDistr,currRateVal); // line 325
            QuietRealParameter[] ratePreProposals = QuietRealParameter.getSamples(rateBaseDistr,
                    sampleSize, currRateVal.getUpper(), currRateVal.getLower());

            //System.err.println("zero count: "+zeroCount);
            //If the a singleton has been picked
            if(zeroCount > -1){
                ratePreProposals[0] = rateList.getParameter(zeroCount);
            }
            //int dimList = paramList.getDimension();
            int i;
            double concVal =dp.getConcParameter();

            double[] logFullCond = new double[counter+sampleSize];

            for(i = 0; i < counter; i++){

                logFullCond[i] = Math.log(clusterCounts[i]/(dimPointer - 1 + concVal));
                /*double temp1 =tempLik.calculateLogP(
                        existingRateVals[i],
                        index
                );

                double temp2 = getSiteLogLikelihood(
                        existingRateVals[i],
                        existingCluster[i],
                        index,
                        tempLik
                );
                if(temp1 != temp2){
                    throw new RuntimeException(temp1+" "+temp2);
                }
                logFullCond[i] = logFullCond[i]+temp2;*/


                logFullCond[i] = logFullCond[i]+getSiteLogLikelihood(
                        existingRateVals[i],
                        existingCluster[i],
                        index,
                        tempLik
                );
                if(Double.isNaN(logFullCond[i])){
                    return Double.NEGATIVE_INFINITY;
                }
            }

            if(zeroCount > -1){
                logFullCond[i] = Math.log(concVal/sampleSize/(dimPointer - 1 + concVal));


                /*double temp1 = tempLik.calculateLogP(
                        ratePreProposals[0],
                        index
                );

                double temp2 = getSiteLogLikelihood(
                        ratePreProposals[0],
                        zeroCount,
                        index,
                        tempLik
                );
                if(temp1 != temp2){
                    System.out.println(logFullCond[i]+" "+i);
                    System.out.println(ratePreProposals[0]);
                    throw new RuntimeException(temp1+" "+temp2);
                }
                logFullCond[i] = logFullCond[i]+temp1;*/
                logFullCond[i] = logFullCond[i]+getSiteLogLikelihood(
                        ratePreProposals[0],
                        zeroCount,
                        index,
                        tempLik
                );
                i++;
            }

            double[] tempLogPs = compoundTempLik.calculateLogP(
                    ratePreProposals,
                    index
            );
            for(; i < logFullCond.length; i++){
                logFullCond[i] = Math.log(concVal/sampleSize/(dimPointer - 1 + concVal))+tempLogPs[i-counter];
                if(Double.isNaN(logFullCond[i])){
                    return Double.NEGATIVE_INFINITY;
                }

            }

            /*smallestVal = logFullCond[0];
            for(i = 1; i < logFullCond.length;i++){
                if(smallestVal > logFullCond[i])
                    smallestVal = logFullCond[i];
            }
            if(smallestVal == Double.NEGATIVE_INFINITY){
                smallestVal = 0.0;
            } */
            //System.err.println("smallestVal2: "+smallestVal);

            double[] fullConditional = new double[logFullCond.length];
            for(i = 0; i < fullConditional.length;i++){
                fullConditional[i] = Math.exp(logFullCond[i]);//-smallestVal);
                //System.err.println("fullConditional[i]: "+fullConditional[i]);
                if(Double.isNaN(fullConditional[i])){
                    //System.err.println("smallestVal: "+smallestVal);
                    for(int j = 0;j <logFullCond.length;j++){
                        System.err.println("logFullCond[i]: "+logFullCond[j]);

                    }
                    for(int j = 0; j < existingRateVals.length;j++){
                        System.err.println(existingRateVals[j]);

                    }
                    for(int j = 0; j < ratePreProposals.length;j++){
                        System.err.println(ratePreProposals[j]);

                    }
                    return Double.NEGATIVE_INFINITY;
                }
                if(Double.POSITIVE_INFINITY == fullConditional[i]){
                    //System.err.println("smallestVal: "+smallestVal);
                    System.err.println("logFullCond[i]: "+logFullCond[i]);
                }
            }


            int proposedIndex = Randomizer.randomChoicePDF(fullConditional);



            QuietRealParameter rateProposal;

            if(proposedIndex < counter){
                rateProposal = existingRateVals[proposedIndex];
            }else{
                rateProposal = ratePreProposals[proposedIndex-counter];

            }

            if(curr != rateProposal){//Takes a different value from the current

                if(proposedIndex >= counter && zeroCount > -1){//Singleton takes new value

                    rateList = rateListInput.get();

                    int paramListIndex = ratePointers.indexInList(index,rateList);
                    //System.err.println("paramListIndex: "+paramListIndex);
                    rateList.setValue(paramListIndex,0,rateProposal.getValue());
                    zeroCount = -1;

                    //System.err.println("Singleton take new value");

                }else{
                    //Singleton takes existing value or
                    //non-singleton takes new or existing value
                    ratePointers = ratePointersInput.get();
                    ratePointers.point(index, rateProposal);

                    //Non singleton takes new value
                    if(proposedIndex >= counter){
                        rateList = rateListInput.get();
                        rateList.addParameter(rateProposal);
                        //System.out.println("add cluster");
                    }

                }

                //If any cluster has no member then it is removed.
                if(zeroCount > -1){
                    rateList = rateListInput.get();
                    rateList.removeParameter(zeroCount);
                    //System.err.println("remove cluster");
                }

            }/*else{
                System.out.println("no change");

            } */


        }catch(Exception e){
                //System.err.println("smallestVal2: "+smallestVal);

                throw new RuntimeException(e);
            }
        return Double.POSITIVE_INFINITY;
    }


//    public QuietRealParameter[] getSamples(ParametricDistribution distr, RealParameter example) throws Exception{
//        QuietRealParameter[] samples = new QuietRealParameter[sampleSize];
//        Double[][] sampleVals = distr.sample(sampleSize);
//        for(int i = 0; i < samples.length;i++){
//            samples[i] = new QuietRealParameter(sampleVals[i]);
//            samples[i].setUpper(example.getUpper());
//            samples[i].setLower(example.getLower());
//        }
//        return samples;
//    }

        public double getSiteLogLikelihood(
                QuietRealParameter parameter,
                int clusterIndex,
                int siteIndex,
                TempTreeLikelihood tempLik){

            double siteLogLik;
            if(dpTreeLikelihood instanceof DPSepTreeLikelihood){
                //System.out.println("?????"+parameter.getIDNumber());
                //if(clusterIndex > -1){
                    siteLogLik = ((DPSepTreeLikelihood)dpTreeLikelihood).getSiteLogLikelihood(
                            DPNtdRateSepSiteModel.RATES,
                            parameter.getIDNumber(),
                            siteIndex
                    );
                //}

                if(Double.isNaN(siteLogLik)){
                    //System.out.println(getID()+" flag 2");
                    siteLogLik =  ((SepTempTreeLikelihood)tempLik).calculateLogP(
                            parameter,
                            siteIndex
                    );//todo need a new temp likelihood

                }
            }else if(dpTreeLikelihood instanceof SlowDPSepTreeLikelihood){
                siteLogLik = ((SlowDPSepTreeLikelihood)dpTreeLikelihood).getSiteLogLikelihood(
                            DPNtdRateSepSiteModel.RATES,
                            parameter.getIDNumber(),
                            siteIndex
                    );
                //}

                if(Double.isNaN(siteLogLik)){
                    //System.out.println(getID()+" flag 2");
                    siteLogLik =  ((SepTempTreeLikelihood)tempLik).calculateLogP(
                            parameter,
                            siteIndex
                    );//todo need a new temp likelihood

                }
           }else{
               siteLogLik =  dpTreeLikelihood.getSiteLogLikelihood(clusterIndex,siteIndex);
                if(Double.isNaN(siteLogLik)){
                    //System.out.println(getID()+" flag 2");
                    siteLogLik =  tempLik.calculateLogP(
                            parameter,
                            siteIndex
                    );//todo need a new temp likelihood

                }
           }
           return siteLogLik;
       }
}
