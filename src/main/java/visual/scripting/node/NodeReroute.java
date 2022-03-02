package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.pin.Pin;
import visual.scripting.pin.PinFlow;

public class NodeReroute extends Node{

    private Pin pinIn, pinOut;

    public NodeReroute(Graph graph) {
        super(graph);
        setName("Reroute");
        setHasTitleBar(false);
    }

    @Override
    public void init() {
        pinIn = new PinFlow();
        pinIn.setNode(this);
        addCustomInput(pinIn);

        pinOut = new PinFlow();
        pinOut.setNode(this);
        addCustomOutput(pinOut);
    }
}
