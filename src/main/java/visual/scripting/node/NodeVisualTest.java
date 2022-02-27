package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.pin.Pin;
import visual.scripting.pin.PinFloat;
import visual.scripting.pin.PinFlow;

public class NodeVisualTest extends Node {

    private Pin flowIn, floatIn, flowOut, floatOut;
    public NodeVisualTest(Graph graph) {
        super(graph);
        setName("Visual Test Node");
        setCategory("Test.category");
        setColor(100, 200, 100, 255);
        setLanguages(new String[]{"java"});
    }

    @Override
    public void init() {
//        addInputPin(Pin.DataType.Flow, this);
        flowIn = new PinFlow();
        flowIn.setNode(this);
        addCustomInput(flowIn);

        floatIn = new PinFloat();
        floatIn.setNode(this);
        addCustomInput(floatIn);

//        addInputPin(Pin.DataType.Bool, this).setName("Boolean");
//        addInputPin(Pin.DataType.Int, this).setName("Integer");
//        addInputPin(Pin.DataType.Float, this).setName("Float");
//        addInputPin(Pin.DataType.Double, this).setName("Double");
//        addInputPin(Pin.DataType.String, this).setName("String");


        flowOut = new PinFlow();
        flowOut.setNode(this);
        addCustomOutput(flowOut);

        floatOut = new PinFloat();
        flowOut.setNode(this);
        addCustomInput(floatOut);
//        addOutputPin(Pin.DataType.Flow, this);
//        addOutputSpacer(this);
//        addOutputPin(Pin.DataType.Bool, this);
//        addOutputPin(Pin.DataType.Int, this);
//        addOutputPin(Pin.DataType.Float, this);
//        addOutputPin(Pin.DataType.Double, this);
//        addOutputPin(Pin.DataType.String, this);
    }
}
