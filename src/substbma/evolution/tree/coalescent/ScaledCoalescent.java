package substbma.evolution.tree.coalescent;


import beast.base.inference.CalculationNode;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.inference.State;
import beast.base.inference.parameter.RealParameter;
import beast.base.evolution.tree.IntervalList;
import beast.base.evolution.tree.IntervalType;
import beast.base.evolution.tree.TreeDistribution;
import beast.base.evolution.tree.TreeIntervals;
import beast.base.evolution.tree.coalescent.PopulationFunction;
import beast.base.util.Binomial;
import substbma.evolution.tree.Scaler;

import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * @author Alexei Drummond
 */

@Description("Calculates the probability of a beast.tree conditional on a population size function. " +
        "Note that this does not take the number of possible tree interval/tree topology combinations " +
        "in account, in other words, the constant required for making this a proper distribution that integrates " +
        "to unity is not calculated (partly, because we don't know how for sequentially sampled data).")
public class ScaledCoalescent extends TreeDistribution {
    public Input<PopulationFunction> popSizeInput = new Input<PopulationFunction>("populationModel", "A population size model", Validate.REQUIRED);
     public Input<RealParameter> scalerInput = new Input<RealParameter>(
            "scaler",
            "A component that scales the tree intervals",
            Input.Validate.REQUIRED
    );

    TreeIntervals intervals;

    @Override
    public void initAndValidate() {
        intervals = treeIntervalsInput.get();
        if (intervals == null) {
            throw new RuntimeException("Expected treeIntervals to be specified");
        }
        calculateLogP();
    }


    /**
     * do the actual calculation *
     */
    @Override
    public double calculateLogP() {

        logP = calculateLogLikelihood(intervals, popSizeInput.get());

        if (Double.isInfinite(logP)) {
        	logP = Double.NEGATIVE_INFINITY;
        }

        return logP;
    }

    @Override
    public void sample(State state, Random random) {
        // TODO this should eventually sample a coalescent tree conditional on population size function
        throw new UnsupportedOperationException("This should eventually sample a coalescent tree conditional on population size function.");
    }

    /**
     * @return a list of unique ids for the state nodes that form the argument
     */
    @Override
    public List<String> getArguments() {
        return Collections.singletonList(treeIntervalsInput.get().getID());
    }

    /**
     * @return a list of unique ids for the state nodes that make up the conditions
     */
    @Override
    public List<String> getConditions() {
        return popSizeInput.get().getParameterIds();
    }


    /**
     * Calculates the log likelihood of this set of coalescent intervals,
     * given a demographic model.
     *
     * @param intervals       the intervals whose likelihood is computed
     * @param popSizeFunction the population size function
     * @return the log likelihood of the intervals given the population size function
     */
    public double calculateLogLikelihood(IntervalList intervals, PopulationFunction popSizeFunction) {
        return calculateLogLikelihood(intervals, popSizeFunction, 0.0);
    }

    /**
     * Calculates the log likelihood of this set of coalescent intervals,
     * given a population size function.
     *
     * @param intervals       the intervals whose likelihood is computed
     * @param popSizeFunction the population size function
     * @param threshold       the minimum allowable coalescent interval size; negative infinity will be returned if
     *                        any non-zero intervals are smaller than this
     * @return the log likelihood of the intervals given the population size function
     */
    public double calculateLogLikelihood(IntervalList intervals, PopulationFunction popSizeFunction, double threshold) {

        double logL = 0.0;

        double startTime = 0.0;
        final int n = intervals.getIntervalCount();
        for (int i = 0; i < n; i++) {

            final double duration = intervals.getInterval(i)*scalerInput.get().getValue();
            final double finishTime = startTime + duration;

            final double intervalArea = popSizeFunction.getIntegral(startTime, finishTime);
            if (intervalArea == 0 && duration != 0) {
                return Double.NEGATIVE_INFINITY;
            }
            final int lineageCount = intervals.getLineageCount(i);

            final double kChoose2 = Binomial.choose2(lineageCount);
            // common part
            logL += -kChoose2 * intervalArea;

            if (intervals.getIntervalType(i) == IntervalType.COALESCENT) {

                final double demographicAtCoalPoint = popSizeFunction.getPopSize(finishTime);

                // if value at end is many orders of magnitude different than mean over interval reject the interval
                // This is protection against cases where ridiculous infinitesimal
                // population size at the end of a linear interval drive coalescent values to infinity.

                if (duration == 0.0 || demographicAtCoalPoint * (intervalArea / duration) >= threshold) {
                    //                if( duration == 0.0 || demographicAtCoalPoint >= threshold * (duration/intervalArea) ) {
                    logL -= Math.log(demographicAtCoalPoint);
                } else {
                    // remove this at some stage
                    //  System.err.println("Warning: " + i + " " + demographicAtCoalPoint + " " + (intervalArea/duration) );
                    return Double.NEGATIVE_INFINITY;
                }
            }
            startTime = finishTime;
        }

        return logL;
    }

    @Override
    protected boolean requiresRecalculation() {
        return ((CalculationNode) popSizeInput.get()).isDirtyCalculation() || super.requiresRecalculation();
    }
}
