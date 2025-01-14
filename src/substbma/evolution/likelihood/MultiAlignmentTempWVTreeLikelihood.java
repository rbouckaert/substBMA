package substbma.evolution.likelihood;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.RealParameter;
import beast.base.evolution.alignment.Alignment;
import beast.base.evolution.sitemodel.SiteModel;
import substbma.evolution.sitemodel.DPMultiAlignSiteModel;
import substbma.evolution.sitemodel.DummySiteModel;
import substbma.evolution.sitemodel.QuietSiteModel;
import substbma.evolution.substitutionmodel.SwitchingNtdBMA;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Chieh-Hsi Wu
 */
@Description("Temporary likelihood calculation class that is able to take up multiple alignments.")
public class MultiAlignmentTempWVTreeLikelihood extends TempWVTreeLikelihood{


    public Input<List<TempWVTreeLikelihood>> tempWVLikelihoodsInput = new Input<List<TempWVTreeLikelihood>>(
            "tempWVTreeLikelihood",
            "Temporary weight variable treeLikelihood",
            new ArrayList<TempWVTreeLikelihood>(),
            Input.Validate.REQUIRED
    );

    public Input<DPMultiAlignSiteModel> dpMultiAlignSiteModelInput = new Input<DPMultiAlignSiteModel>(
            "dpMultiAlignSiteModel",
            "Site model for multiple alignments",
            Input.Validate.REQUIRED
    );



    public MultiAlignmentTempWVTreeLikelihood(){
        dataInput.setRule(Input.Validate.OPTIONAL);
        treeInput.setRule(Input.Validate.OPTIONAL);
        siteModelInput.setRule(Input.Validate.OPTIONAL);
    }

    private TempWVTreeLikelihood[] tempWVTreeLikelihoods;
    private DPMultiAlignSiteModel dpMultiAlignSiteModel;
    private int[] siteIndexWithinAlignment;

    public void initAndValidate() {
        List<TempWVTreeLikelihood> tempWVTreeLikelihoodList = tempWVLikelihoodsInput.get();
        tempWVTreeLikelihoods = new TempWVTreeLikelihood[tempWVTreeLikelihoodList.size()];
        for(int i = 0; i < tempWVTreeLikelihoods.length; i++){
            tempWVTreeLikelihoods[i] = tempWVTreeLikelihoodList.get(i);
        }
        dpMultiAlignSiteModel = dpMultiAlignSiteModelInput.get();


        int[] prevAlignEndIndex = new int[tempWVTreeLikelihoods.length];
        prevAlignEndIndex[0] = 0;
        for(int i = 1; i < prevAlignEndIndex.length; i++){
            prevAlignEndIndex[i] = prevAlignEndIndex[i - 1] +
                    tempWVTreeLikelihoods[i - 1].dataInput.get().getSiteCount();
        }

        int siteCount = prevAlignEndIndex[prevAlignEndIndex.length - 1] +
                tempWVTreeLikelihoods[tempWVTreeLikelihoods.length - 1].dataInput.get().getSiteCount();

        siteIndexWithinAlignment = new int[siteCount];
        for(int i = 0; i < siteIndexWithinAlignment.length; i++){
            siteIndexWithinAlignment[i] = i - prevAlignEndIndex[dpMultiAlignSiteModel.getAlignmentIndex(i)];

        }

    }

    private ArrayList<Integer> alignmentPatternWeightChanged;
    public void setupPatternWeightsFromSites(int[] sites){

        int[][] tempWeights = new int[tempWVTreeLikelihoods.length][];
        int alignmentIndex = -1;
        int patIndex;
        for(int i = 0; i < sites.length; i++){

            alignmentIndex = dpMultiAlignSiteModel.getAlignmentIndex(sites[i]);
            patIndex = tempWVTreeLikelihoods[alignmentIndex].dataInput.get().getPatternIndex(
                    siteIndexWithinAlignment[sites[i]]
            );

            if(tempWeights[alignmentIndex] == null){
                int patternCount = tempWVTreeLikelihoods[alignmentIndex].dataInput.get().getPatternCount();
                tempWeights[alignmentIndex] = new int[patternCount];
            }

            tempWeights[alignmentIndex][patIndex] = 1;


        }


        alignmentPatternWeightChanged = new ArrayList<Integer>();
        for(int i = 0; i < tempWeights.length; i++){
            if(tempWeights[i] != null){
                alignmentPatternWeightChanged.add(i);
                tempWVTreeLikelihoods[i].setPatternWeights(tempWeights[i]);
            }
        }

    }


    public double[] calculateLogP (
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
            RealParameter rate,
            int[] sites) throws Exception{
        throw new RuntimeException("Not yet properly implemented");
    }

    public double[] calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
            RealParameter rate,
            int[] sites,
            int exceptSite) {
        throw new RuntimeException("Note yet properly implemented");
    }

    public double calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
            RealParameter rate) throws Exception{


        throw new RuntimeException("Not yet properly implemented");



    }

    public double[] calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
            int[] sites,
            int exceptSite) throws Exception{
            double[] siteLogP = new double[sites.length-1];
        throw new RuntimeException("Not yet properly implemented");
    }

    public double[] calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs,
        int[] sites) throws Exception{

        throw new RuntimeException("Not yet properly implemented");
    }

    public double calculateLogP(
            RealParameter modelParameters,
            RealParameter modelCode,
            RealParameter freqs) throws Exception{

        throw new RuntimeException("Not yet properly implemented");

    }





    public double[] calculateLogP(
            RealParameter rateParameter,
            int[] sites) throws Exception{

        double[] siteLogP = new double[sites.length];
        try{

            setRateAndCalculateLogP(rateParameter);
            int alignmentIndex;
            int patternIndex;
            for(int i = 0; i < sites.length;i++){
                alignmentIndex = dpMultiAlignSiteModel.getAlignmentIndex(sites[i]);
                patternIndex =  tempWVTreeLikelihoods[alignmentIndex].dataInput.get().getPatternIndex(siteIndexWithinAlignment[sites[i]]);
                siteLogP[i] = tempWVTreeLikelihoods[alignmentIndex].getPatternLogLikelihood(patternIndex);
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
            setRateAndCalculateLogP(rateParameter);


            int k = 0;
            int alignmentIndex;
            int patternIndex;
            for(int i = 0; i < sites.length;i++){
                if(sites[i] != exceptSite){
                    alignmentIndex = dpMultiAlignSiteModel.getAlignmentIndex(sites[i]);
                    patternIndex =  tempWVTreeLikelihoods[alignmentIndex].dataInput.get().getPatternIndex(siteIndexWithinAlignment[sites[i]]);
                    siteLogP[k++] = tempWVTreeLikelihoods[alignmentIndex].getPatternLogLikelihood(patternIndex);
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

    private void setRateAndCalculateLogP(RealParameter rateParameter) throws Exception{

        for(int i = 0; i < alignmentPatternWeightChanged.size(); i++){
            DummySiteModel siteModel = (DummySiteModel) tempWVTreeLikelihoods[alignmentPatternWeightChanged.get(i)].getSiteModel();
            siteModel.getRateParameter().setValueQuietly(0,rateParameter.getValue());
            tempWVTreeLikelihoods[alignmentPatternWeightChanged.get(i)].calculateLogP();
        }

    }



}
