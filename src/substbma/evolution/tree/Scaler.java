package substbma.evolution.tree;

import beast.base.inference.CalculationNode;
import beast.base.core.Description;

/**
 * @author Chieh-Hsi Wu
 */
@Description("This class is used to scale things.")
public abstract class Scaler extends CalculationNode {
    public abstract double getScaleFactor();
}
