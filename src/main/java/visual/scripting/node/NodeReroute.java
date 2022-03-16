package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.pin.Pin;
import visual.scripting.pin.PinFlow;
import visual.scripting.pin.PinUnset;

public class NodeReroute extends Node{

    private Pin pinIn, pinOut;

    public NodeReroute(Graph graph) {
        super(graph);
        setName("Reroute");
        setHasTitleBar(false);
    }

    @Override
    public void init() {
        pinIn = new PinUnset();
        pinIn.setNode(this);
        addCustomInput(pinIn);

        pinOut = new PinUnset();
        pinOut.setNode(this);
        addCustomOutput(pinOut);
    }
}
