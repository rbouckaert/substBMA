package substbma.evolution.tree;



import java.util.List;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import beast.base.inference.StateNode;
import beast.base.inference.StateNodeInitialiser;

import java.util.ArrayList;

/**
 * @author Chieh-Hsi Wu
 */
@Description("This class is used to scale the tree to a certain height.")
public class TreeScaler extends Tree implements StateNodeInitialiser{
    public Input<Scaler> scaleFactorInput = new Input<Scaler>(
            "scaleFactor",
            "Scale the tree by the scale factor."
    );

    public Input<Double> scaleToHeightInput = new Input<Double>(
            "scaleToHeight",
            "The height to which the tree is to be scaled",
            Input.Validate.XOR,
            scaleFactorInput
    );


    //private double scaleToHeight;
    private Tree tree;
    public void initAndValidate() {

        super.initAndValidate();
        tree = m_initial.get();
        System.err.print("Scaling the root of "+tree.getID()+" from "+m_initial.get().getRoot().getHeight()+" to ");
        double scaleFactor;
        if(scaleFactorInput.get() != null){
            scaleFactor = scaleFactorInput.get().getScaleFactor();
        }else{
            double scaleToHeight = scaleToHeightInput.get();
            double rootHeight = tree.getRoot().getHeight();
            scaleFactor = scaleToHeight/rootHeight;

        }

        scaleNodeHeight(tree.getRoot(), scaleFactor);
        //scaleTree();
        System.err.println(m_initial.get().getRoot().getHeight()+".");

    }


    /*public void scaleTree(){
        double rootHeight = tree.getRoot().m_fHeight;
        double scaleFactor = scaleToHeight/rootHeight;
        scaleNodeHeight(tree.getRoot(), scaleFactor);
    }*/


    /** scale height of this node and all its descendants
     * @param fScale scale factor
     **/
    public void scaleNodeHeight(Node node, double fScale){
        if (!node.isLeaf()) {
            //System.err.println(node.m_fHeight);
            node.setHeight(node.getHeight() * fScale);
            //System.err.println(node.m_fHeight);
            scaleNodeHeight(node.getLeft(),fScale);
            if (node.getRight() != null) {
                scaleNodeHeight(node.getRight(),fScale);
            }
            if (node.getHeight() <node.getLeft().getHeight() || 
            		node.getHeight() < node.getRight().getHeight()) {
                throw new RuntimeException("Scale gives negative branch length");
            }
        }
    }


    @Override
	public void initStateNodes() {}

	@Override
	public void getInitialisedStateNodes(List<StateNode> stateNodes) { }
}