package visual.scripting.node;

import imgui.type.ImFloat;
import imgui.type.ImLong;
import visual.scripting.Graph;
import visual.scripting.NodeData;
import visual.scripting.Pin;

public class NodeTime extends Node{

    private Pin pin, pinDivide;

    public NodeTime(Graph graph) {
        super(graph);
        setName("Time");
        setColor(50, 255, 50, 255);
    }

    @Override
    public void init() {
        pin = addOutputPin(Pin.DataType.Long, this);
        pin.setName("System time");

        pinDivide = addInputPin(Pin.DataType.Float, this);
        pinDivide.setName("Modifier");
    }

    @Override
    public void execute() {
        NodeData<ImLong> data = pin.getData();
        long time = System.currentTimeMillis();

        NodeData<ImFloat> dataPinDivive = pinDivide.getData();
        if(dataPinDivive.value.get() > 0){
            time =  time / (long)dataPinDivive.value.get();
        }
        data.value.set(time);
    }
}
