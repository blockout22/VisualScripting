package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.pin.Pin;
import visual.scripting.pin.PinUnset;

public class NodeReroute extends Node{

    private Pin pinIn, pinOut;

    //state 0 = unset / state 1 = connected / state 2 = waiting for reset
    private int state = 0;

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

    @Override
    public void execute() {
        if(inputPins.size() > 0 && outputPins.size() > 0) {
            if (outputPins.get(0).connectedTo != -1) {
                state = 1;
            }
            if (inputPins.get(0).connectedTo != -1) {
                state = 1;
                Pin c = getGraph().findPinById(inputPins.get(0).connectedTo);
                if(c.getData() != null) {
                    inputPins.get(0).setData(c.getData());
                    outputPins.get(0).setData(inputPins.get(0).getData());
                }
            }

            if (inputPins.get(0).connectedTo == -1 && outputPins.get(0).connectedTo == -1 && state == 1) {
                //reset pins ONCE
                state = 2;
            }

            if (state == 2) {
                inputPins.remove(0);
                outputPins.remove(0);
                init();
                state = 0;
            }
        }
    }
}
