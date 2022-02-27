package visual.scripting.node;

import imgui.type.ImFloat;
import imgui.type.ImLong;
import visual.scripting.Graph;
import visual.scripting.NodeData;
import visual.scripting.pin.Pin;
import visual.scripting.pin.PinFloat;
import visual.scripting.pin.PinLong;

public class NodeLongToFloat extends Node{

    private Pin in, out;

    public NodeLongToFloat(Graph graph) {
        super(graph);
        setName("Long To Float");
    }

    @Override
    public void init() {
        in = new PinLong();
        in.setNode(this);
        addCustomInput(in);
//        in = addInputPin(Pin.DataType.Long, this);
        out = new PinFloat();
        out.setNode(this);
        addCustomOutput(out);
//        out = addOutputPin(Pin.DataType.Float, this);
    }

    @Override
    public void execute() {
        NodeData<ImLong> inData = in.getData();
        NodeData<ImFloat> outData = out.getData();

        if(in.connectedTo != -1){
            Pin pin = getGraph().findPinById(in.connectedTo);
            NodeData<ImLong> pinData = pin.getData();
            inData.value.set(pinData.getValue());
        }

        outData.value.set(Float.valueOf(inData.value.get()));
    }
}
