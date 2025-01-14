package substbma.evolution.likelihood;


import beast.base.core.Description;
import beast.base.evolution.alignment.Alignment;
import beast.base.evolution.branchratemodel.BranchRateModel;
import beast.base.evolution.substitutionmodel.SubstitutionModel;
import substbma.core.parameter.QuietRealParameter;
import substbma.evolution.sitemodel.DummySiteModel;
import substbma.evolution.sitemodel.QuietSiteModel;
import substbma.evolution.substitutionmodel.SwitchingNtdBMA;
import beast.base.evolution.tree.Tree;
import beast.base.evolution.sitemodel.SiteModel;
import beast.base.core.Input;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.RealParameter;

/**
 * @author Chieh-Hsi Wu
 */
@Description("Used to compute the tree likelihood for operators that samples the partitioning space. Used when there is only one partitioning. Pattern weights can be modified.")
public class TempWVTreeLikelihood extends NewWVTreeLikelihood{
    public Input<IntegerParameter> patternWeightInput = new Input<IntegerParameter>(
            "patternWeights",
            "The weight of each pattern as an integer"
    );

    public TempWVTreeLikelihood(){}

    public TempWVTreeLikelihood(int[] patternWeights,
                               Alignment data,
                               Tree tree,
                               boolean useAmbiguities,
                               SiteModel siteModel,
                               BranchRateModel.Base branchRateModel){
        super(patternWeights, data, tree, useAmbiguities, siteModel, branchRateModel);
    }



    public void initAndValidate() {

        //setup weights
        if(patternWeightInput.get() == null){
            patternWeights = dataInput.get().getWeights();

        }else{
            IntegerParameter weightsParameter = patternWeightInput.get();
            Integer[] tempPatternWeights = weightsParameter.getValues();
            patternWeights = new int [tempPatternWeights.length];
            for(int i = 0; i < tempPatternWeights.length;i++){
                patternWeights[i] = tempPatternWeights[i];
            }
           
        }



        storedPatternWeights = new int[patternWeights.length];

        super.initAndValidate();

    }

        @Override
    public double calculateLogP() {
        /*boolean[] unmasked = m_likelihoodCore.getUnmasked();
        for(int i = 0; i < patternWeights.length; i++){
            System.out.print(unmasked[i]+" ");
        }
            System.out.println();*/

        Tree tree = (Tree) treeInput.get();
        //System.err.println("m_nHasDirt: "+m_nHasDirt);
        hasDirt = Tree.IS_FILTHY;
       	traverse(tree.getRoot());


        calcLogP();

        m_nScale++;
        if (logP > 0 || (m_likelihoodCore.getUseScaling() && m_nScale > X)) {
            //System.err.println("Switch off scaling");
            m_likelihoodCore.setUseScaling(1.0);
            m_likelihoodCore.unstore();
            hasDirt = Tree.IS_FILTHY;
            X *= 2;
           	traverse(tree.getRoot());
            calcLogP();
            return logP;
        } else if (logP == Double.NEGATIVE_INFINITY && m_fScale < 10) { // && !m_likelihoodCore.getUseScaling()) {
        	m_nScale = 0;
        	m_fScale *= 1.01;
            System.err.println("Turning on scaling to prevent numeric instability " + m_fScale);
            m_likelihoodCore.setUseScaling(m_fScale);
            m_likelihoodCore.unstore();
            hasDirt = Tree.IS_FILTHY;
           	traverse(tree.getRoot());
            calcLogP();
            //System.err.println(getID()+": "+logP);
            return logP;
        }

        //System.err.println(getID()+": "+logP);
        return logP;
    }

    public double[] calculateLogP (
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
            RealParameter rate,
            int[] sites) throws Exception{
        double[] siteLogP = new double[sites.length];
        calculateLogP(modelParameters,modelCode,freqs,rate);
        for(int i = 0; i < sites.length;i++){
            siteLogP[i] = patternLogLikelihoods[dataInput.get().getPatternIndex(sites[i])];
        }
        /*for(int i = 0; i < m_fPatternLogLikelihoods.length;i++){
            System.out.println("m_fPatternLogLikelihoods: "+m_fPatternLogLikelihoods[i]);
        }*/
        return siteLogP;
    }

    public double[] calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
            RealParameter rate,
            int[] sites,
            int exceptSite) {
            double[] siteLogP = new double[sites.length-1];
        try{

            calculateLogP(modelParameters,modelCode,freqs,rate);
            int k = 0;
            for(int i = 0; i < sites.length;i++){
                if(sites[i] != exceptSite){
                    //System.out.println(sites[i] +" "+exceptSite);
                    siteLogP[k++] = patternLogLikelihoods[dataInput.get().getPatternIndex(sites[i])];
                }
            }


        }catch(Exception e){
            throw new RuntimeException(e);

        }
        return siteLogP;
    }

    public double calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
            RealParameter rate) throws Exception{


        SwitchingNtdBMA  substModel = (SwitchingNtdBMA)substitutionModel;
        (substModel.getLogKappa()).setValueQuietly(0, modelParameters.getValue(0));
        (substModel.getLogTN()).setValueQuietly(0,modelParameters.getValue(1));
        (substModel.getLogAC()).setValueQuietly(0,modelParameters.getValue(2));
        (substModel.getLogAT()).setValueQuietly(0,modelParameters.getValue(3));
        (substModel.getLogGC()).setValueQuietly(0,modelParameters.getValue(4));
        (substModel.getModelChoose()).setValueQuietly(0,modelCode.getValue());
        (substModel.getFreqs()).setValueQuietly(0,freqs.getValue(0));
        (substModel.getFreqs()).setValueQuietly(1,freqs.getValue(1));
        (substModel.getFreqs()).setValueQuietly(2,freqs.getValue(2));
        (substModel.getFreqs()).setValueQuietly(3,freqs.getValue(3));
        substModel.setUpdateMatrix(true);
        ((QuietSiteModel)m_siteModel).getRateParameter().setValueQuietly(0,rate.getValue());
        return calculateLogP();



    }



    public double[] calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
            int[] sites,
            int exceptSite) throws Exception{
            double[] siteLogP = new double[sites.length-1];
        try{

            calculateLogP(modelParameters,modelCode,freqs);
            int k = 0;
            for(int i = 0; i < sites.length;i++){
                if(sites[i] != exceptSite){
                    //System.out.println(sites[i] +" "+exceptSite);
                    siteLogP[k++] = patternLogLikelihoods[dataInput.get().getPatternIndex(sites[i])];
                }
            }


        }catch(Exception e){
            throw new RuntimeException(e);

        }
        return siteLogP;
    }

    public double[] calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
        int[] sites) throws Exception{
        double[] siteLogP = new double[sites.length];
        calculateLogP(modelParameters,modelCode,freqs);
        for(int i = 0; i < sites.length;i++){
            siteLogP[i] = patternLogLikelihoods[dataInput.get().getPatternIndex(sites[i])];
        }
        return siteLogP;
    }

    public double calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs) throws Exception{


                SwitchingNtdBMA  substModel = (SwitchingNtdBMA)substitutionModel;
                (substModel.getLogKappa()).setValueQuietly(0,modelParameters.getValue(0));
                (substModel.getLogTN()).setValueQuietly(0,modelParameters.getValue(1));
                (substModel.getLogAC()).setValueQuietly(0,modelParameters.getValue(2));
                (substModel.getLogAT()).setValueQuietly(0,modelParameters.getValue(3));
                (substModel.getLogGC()).setValueQuietly(0,modelParameters.getValue(4));
                (substModel.getModelChoose()).setValueQuietly(0,modelCode.getValue());
                (substModel.getFreqs()).setValueQuietly(0,freqs.getValue(0));
                (substModel.getFreqs()).setValueQuietly(1,freqs.getValue(1));
                (substModel.getFreqs()).setValueQuietly(2,freqs.getValue(2));
                (substModel.getFreqs()).setValueQuietly(3,freqs.getValue(3));
                substModel.setUpdateMatrix(true);
                return calculateLogP();



    }





    public double[] calculateLogP(
            RealParameter rateParameter,
            int[] sites) throws Exception{

        double[] siteLogP = new double[sites.length];
        try{
            ((DummySiteModel)m_siteModel).getRateParameter().setValueQuietly(0,rateParameter.getValue());
            calculateLogP();
            for(int i = 0; i < sites.length;i++){
                siteLogP[i] = patternLogLikelihoods[dataInput.get().getPatternIndex(sites[i])];
                //System.out.println(siteLogP[i]);
            }
        }catch(Exception e){
            throw new RuntimeException(e);

        }
        return siteLogP;
    }

    public double[] calculateLogP(
            RealParameter rateParameter,
            int[] sites,
            int exceptSite) throws Exception{
        double[] siteLogP = new double[sites.length-1];
        try{
            ((DummySiteModel)m_siteModel).getRateParameter().setValueQuietly(0,rateParameter.getValue());
            calculateLogP();
            int k = 0;
            for(int i = 0; i < sites.length;i++){
                if(sites[i] != exceptSite){                    
                    siteLogP[k++] = patternLogLikelihoods[dataInput.get().getPatternIndex(sites[i])];
                }
            }
        }catch(Exception e){
            throw new RuntimeException(e);

        }
        return siteLogP;
    }

    public double calculateLogP(
            RealParameter rateParameter) throws Exception{

        try{
            ((DummySiteModel)m_siteModel).getRateParameter().setValueQuietly(0,rateParameter.getValue());


        }catch(Exception e){
            throw new RuntimeException(e);

        }
        return calculateLogP();

    }


    public void setupPatternWeightsFromSites(int[] sites){
        int[] tempWeights = new int[dataInput.get().getPatternCount()];
        int patIndex;
        for(int i = 0; i < sites.length; i++){
            patIndex = dataInput.get().getPatternIndex(sites[i]);
            tempWeights[patIndex] = 1;
        }

        setPatternWeights(tempWeights);
    }

    public double getCurrSiteLikelihood(int siteIndex){
        return patternLogLikelihoods[dataInput.get().getPatternIndex(siteIndex)];

    }
   


}
