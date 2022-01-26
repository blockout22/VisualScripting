package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.Pin;

public class NodeVisualTest extends Node {
    public NodeVisualTest(Graph graph) {
        super(graph);
        setName("Visual Test Node");
        setCategory("Test.category");
    }

    @Override
    public void init() {
        addInputPin(Pin.DataType.Flow, this);
        addInputPin(Pin.DataType.Bool, this);
        addInputPin(Pin.DataType.Int, this);
        addInputPin(Pin.DataType.Float, this);
        addInputPin(Pin.DataType.Double, this);
        addInputPin(Pin.DataType.String, this);

        addOutputPin(Pin.DataType.Flow, this);
        addOutputPin(Pin.DataType.Bool, this);
        addOutputPin(Pin.DataType.Int, this);
        addOutputPin(Pin.DataType.Float, this);
        addOutputPin(Pin.DataType.Double, this);
        addOutputPin(Pin.DataType.String, this);

        System.out.println(getCategory());
    }
}
