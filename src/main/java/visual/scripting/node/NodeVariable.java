package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.Pin;

public class NodeVariable extends Node{
    public NodeVariable(Graph graph) {
        super(graph);
        setName("Variable");
        setHasTitleBar(false);
    }

    @Override
    public void init() {
        Pin pin = addOutputPin(Pin.DataType.Float, this);
        pin.setName("Variable");
    }
}
