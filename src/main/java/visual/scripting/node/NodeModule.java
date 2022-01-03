package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.Pin;

import static imgui.ImGui.button;

public class NodeModule extends Node{

    private Pin pin1;
    public NodeModule(Graph graph) {
        super(graph);
        setName("Module Test");

        pin1 = addInputPin(Pin.DataType.Float, this);
    }

    @Override
    public void init() {

    }

    @Override
    public void execute() {
    }
}
