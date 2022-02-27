package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.pin.PinFloat;
import visual.scripting.pin.PinFlow;

public class NodeEntry extends Node{

    private PinFlow input, output, output2;
    private PinFloat pinFloat;

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

        output2 = new PinFlow();
        output2.setNode(this);
        addCustomOutput(output2);

        input = new PinFlow();
        input.setNode(this);
        addCustomInput(input);

        pinFloat = new PinFloat();
        pinFloat.setNode(this);
        addCustomOutput(pinFloat);
    }

    @Override
    public void execute() {
    }
}
