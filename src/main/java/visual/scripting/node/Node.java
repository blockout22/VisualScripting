package visual.scripting.node;

import imgui.internal.ImRect;
import visual.scripting.Graph;
import visual.scripting.Pin;
import visual.scripting.node.style.NodeStyle;

import java.util.ArrayList;

import static imgui.ImGui.getItemRectMax;
import static imgui.ImGui.getItemRectMin;

public class Node {

    private final Graph graph;
    private int ID;
    private String name = "";
    private int linkID = 0;

    public ArrayList<Pin> outputPins = new ArrayList<>();
    public ArrayList<Pin> inputPins = new ArrayList<>();

    private NodeStyle style = new NodeStyle();

    public float width = -1;

    public Node(Graph graph){
        this.graph = graph;
    }

    public void init(){

    }

    public void setID(int id){
        this.ID = id;
    }

    public Pin addOutputPin(Pin.DataType dataType, Node node){
        int id = Graph.getNextAvailablePinID();
        Pin pin = new Pin(node, id, dataType, Pin.PinType.Output, linkID++);
        outputPins.add(pin);
        return pin;
    }

    public Pin addInputPin(Pin.DataType dataType, Node node){
        int id = Graph.getNextAvailablePinID();
        Pin pin = new Pin(node, id, dataType, Pin.PinType.Input, linkID++);
        inputPins.add(pin);
        return pin;
    }

    public void execute(){

    }

    public void setName(String name){
        if(name == null){
            name = "";
        }
        this.name = name;
    }

    public NodeStyle getStyle() {
        return style;
    }

    public void setStyle(NodeStyle style) {
        this.style = style;
    }

    public String printSource(StringBuilder sb){
        return "";
    }

    public String getName()
    {
        return name;
    }

    public int getID()
    {
        return ID;
    }

    public Graph getGraph()
    {
        return graph;
    }
}
