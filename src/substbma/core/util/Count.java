package substbma.core.util;


import java.io.PrintStream;

import beast.base.core.BEASTObject;
import beast.base.core.Description;
import beast.base.core.Function;
import beast.base.core.Input;
import beast.base.core.Loggable;
import beast.base.inference.CalculationNode;
import substbma.core.PluginList;

/**
 * @author Chieh-Hsi Wu
 */
@Description("Reports the number of items in a list.")
public class Count extends CalculationNode implements Loggable, Function{
    public Input<PluginList> listInput = new Input<PluginList>("list", "list of items to be counted", Input.Validate.REQUIRED);
    PluginList list;
    @Override
	public void initAndValidate() {
		list = listInput.get();
	}

    public int getDimension(){
        return 1;
    }

    public double getArrayValue(){
        return list.getDimension();
    }

    public double getArrayValue(int dim){
        return list.getDimension();
    }

    @Override
	public void init(PrintStream out) {
        out.print("count("+((BEASTObject)list).getID() + ")\t");
    }

    @Override
	public void log(long nSample, PrintStream out) {
    	out.print(list.getDimension() + "\t");
	}

    @Override
	public void close(PrintStream out) {
		// nothing to do
	}
}
