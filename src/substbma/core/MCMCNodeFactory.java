package substbma.core;

import beast.base.core.Description;
import beast.base.inference.CalculationNode;

/**
 * @author Chieh-Hsi Wu
 */
@Description("This class is used so that internally initialized calculation nodes can be checked properly in the MCMC.")
public class MCMCNodeFactory {

    public static void checkDirtiness(CalculationNode calcNode){
        calcNode.isDirtyCalculation();
    }

//    public static void makeAccept(CalculationNode calcNode){
//        calcNode.accept();
//    }
}
