package visual.scripting.node;

import imgui.type.ImFloat;
import imgui.type.ImLong;
import visual.scripting.Graph;
import visual.scripting.NodeData;
import visual.scripting.Pin;

public class NodeLongToFloat extends Node{

    private Pin in, out;

    public NodeLongToFloat(Graph graph) {
        super(graph);
        setName("Long To Float");
    }

    @Override
    public void init() {
        in = addInputPin(Pin.DataType.Long, this);
        out = addOutputPin(Pin.DataType.Float, this);
    }

    @Override
    public void execute() {
        NodeData<ImLong> inData = in.getData();
        NodeData<ImFloat> outData = out.getData();

        outData.value.set(Float.valueOf(inData.value.get()));
    }
}
