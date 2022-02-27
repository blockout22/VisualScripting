package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.pin.Pin;
import visual.scripting.pin.PinFloat;

public class NodeVariable extends Node{

    private Pin pin;
    public NodeVariable(Graph graph) {
        super(graph);
        setName("Variable");
        setHasTitleBar(false);
    }

    @Override
    public void init() {
        pin = new PinFloat();
        pin.setNode(this);
//        Pin pin = addOutputPin(Pin.DataType.Float, this);
        pin.setName("Variable");
    }
}
