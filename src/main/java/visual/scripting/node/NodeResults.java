package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.Pin;

public class NodeResults extends Node{

    private Pin pin;

    public NodeResults(Graph graph) {
        super(graph);
        setName("Results");

        pin = addInputPin(Pin.DataType.Int, this);
    }
}
