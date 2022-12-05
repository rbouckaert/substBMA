package beast.evolution.likelihood;

import beast.base.core.Description;
import beast.base.evolution.alignment.Alignment;
import beast.base.evolution.branchratemodel.BranchRateModel;
import beast.base.evolution.substitutionmodel.SubstitutionModel;
import beast.base.evolution.tree.Tree;
import beast.base.evolution.sitemodel.SiteModel;
import beast.evolution.substitutionmodel.SwitchingNtdBMA;

/**
 * @author Chieh-Hsi Wu
 */
@Description("Used to compute the tree likelihood for operators that samples the partitioning space.")
public class TempSiteTreeLikelihood extends QuietTreeLikelihood{

    public TempSiteTreeLikelihood(Alignment data,
                               Tree tree,
                               boolean useAmbiguities,
                               SiteModel siteModel,
                               BranchRateModel.Base branchRateModel){
        super(data,tree,useAmbiguities,siteModel,branchRateModel);

    }

    
    double m_fScale = 1.01;
    int m_nScale = 0;
    int X = 100;

    @Override
    public double calculateLogP() {

        //System.out.println("rate ID: "+((SiteModel)m_siteModel).getRateParameter().getIDNumber());
        //System.out.println("model ID: "+((SwitchingNtdBMA)((SiteModel)m_siteModel).getSubstitutionModel()).getIDNumber());
        substitutionModel = m_siteModel.substModelInput.get();
        hasDirt = Tree.IS_FILTHY;



       	traverse(tree.getRoot());
        calcLogP();



        m_nScale++;
        if (logP > 0 || (likelihoodCore.getUseScaling() && m_nScale > X)) {
            //System.err.println("Switch off scaling");
            likelihoodCore.setUseScaling(1.0);
            likelihoodCore.unstore();
            hasDirt = Tree.IS_FILTHY;
            X *= 2;
           	traverse(tree.getRoot());
            calcLogP();
        } else if (logP == Double.NEGATIVE_INFINITY && m_fScale < 10) { // && !m_likelihoodCore.getUseScaling()) {
        	m_nScale = 0;
        	m_fScale *= 1.01;
            //System.err.println("Turning on scaling to prevent numeric instability " + m_fScale);
            likelihoodCore.setUseScaling(m_fScale);
            likelihoodCore.unstore();
            hasDirt = Tree.IS_FILTHY;
           	traverse(tree.getRoot());
            calcLogP();
        }
        /*if(Double.isNaN(logP)){
            ((BeerLikelihoodCore)m_likelihoodCore).printDetails();
            ((SwitchingNtdBMA)m_substitutionModel).printDetails();
            tree.log(-1,System.out);
            System.out.println();
            printPatternLogLik();
            System.out.println(((SiteModel)m_siteModel).getRateParameter());
            throw new RuntimeException("Prob: "+logP);
        }*/
        return logP;

    }


    

}
