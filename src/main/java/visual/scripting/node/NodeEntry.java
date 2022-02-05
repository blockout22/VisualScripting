package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.Pin;

public class NodeEntry extends Node{

    private Pin output;

    public NodeEntry(Graph graph) {
        super(graph);
        setName("Start");
        setColor(255, 255, 255, 255);

        //setLanguages(new String[]{"java", "python", "c++"});
    }

    @Override
    public void init() {
        output = addOutputPin(Pin.DataType.Flow, this);
    }

    @Override
    public void execute() {
    }
}
