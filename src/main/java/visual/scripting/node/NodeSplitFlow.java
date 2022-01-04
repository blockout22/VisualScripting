package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.Pin;

public class NodeSplitFlow extends Node{

    private Pin input, output1, output2;

    public NodeSplitFlow(Graph graph) {
        super(graph);
        setName("Split Flow");
    }

    @Override
    public void init() {
        System.out.println("Init Called");
        input = addInputPin(Pin.DataType.Flow, this);
        output1 = addOutputPin(Pin.DataType.Flow, this);
        output2 = addOutputPin(Pin.DataType.Flow, this);
    }
}
