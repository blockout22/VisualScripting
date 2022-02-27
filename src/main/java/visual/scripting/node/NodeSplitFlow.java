package visual.scripting.node;

import imgui.type.ImFloat;
import visual.scripting.Graph;
import visual.scripting.NodeData;
import visual.scripting.pin.Pin;
import visual.scripting.pin.PinFloat;
import visual.scripting.pin.PinFlow;

public class NodeSplitFlow extends Node{

    private PinFlow input, output1, output2;
    private PinFloat pinFloat;

    public NodeSplitFlow(Graph graph) {
        super(graph);
        setName("Split Flow");
        setColor(255, 0, 0, 255);
    }

    @Override
    public void init() {
//        Button button = addButton("Add Output Pin");
//        button.addLeftClickListener(new LeftClickListener() {
//            @Override
//            public void onClicked() {
//                Pin pin = addOutputPin(Pin.DataType.Flow, getSelf());
//                pin.setCanDelete(true);
//            }
//        });

        input = new PinFlow();
        input.setNode(this);

        output1 = new PinFlow();
        output1.setNode(this);

        output2 = new PinFlow();
        output2.setNode(this);

        pinFloat = new PinFloat();
        pinFloat.setNode(this);

        addCustomInput(input);
        addCustomOutput(output1);
        addCustomOutput(output2);
        addCustomInput(pinFloat);
        //        input = addInputPin(Pin.DataType.Flow, this);
//        output1 = addOutputPin(Pin.DataType.Flow, this);
//        output2 = addOutputPin(Pin.DataType.Flow, this);
    }

    @Override
    public void execute() {
        NodeData<ImFloat> input = pinFloat.getData();

        if(pinFloat.connectedTo != -1){
            Pin pin = getGraph().findPinById(pinFloat.connectedTo);
            NodeData<ImFloat> conPin = pin.getData();
            input.getValue().set(conPin.value.get());
        }
    }

    @Override
    public String printSource(StringBuilder sb) {
        return "";
    }
}
