package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.pin.PinFlow;

public class NodeEntry extends Node{

    private PinFlow output;

    public NodeEntry(Graph graph) {
        super(graph);
        setName("Start");
        setColor(255, 255, 255, 255);

        //setLanguages(new String[]{"java", "python", "c++"});
    }

    @Override
    public void init() {
        output = new PinFlow();
        output.setNode(this);
        addCustomOutput(output);
    }

    @Override
    public void execute() {
    }
}
