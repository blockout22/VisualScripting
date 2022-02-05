package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.Pin;
import visual.scripting.ui.Button;
import visual.scripting.ui.listeners.ClickListener;

public class NodeSplitFlow extends Node{

    private Pin input, output1, output2;

    public NodeSplitFlow(Graph graph) {
        super(graph);
        setName("Split Flow");
        setColor(255, 0, 0, 255);
    }

    @Override
    public void init() {
        Button button = addButton("Add Output Pin");
        button.addClickListener(new ClickListener() {
            @Override
            public void onClicked() {
                Pin pin = addOutputPin(Pin.DataType.Flow, getSelf());
                pin.setCanDelete(true);
            }
        });
        input = addInputPin(Pin.DataType.Flow, this);
        output1 = addOutputPin(Pin.DataType.Flow, this);
        output2 = addOutputPin(Pin.DataType.Flow, this);
    }
}
