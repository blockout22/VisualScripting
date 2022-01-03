package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.Pin;

import java.util.ArrayList;

public class Node {

    private final Graph graph;
    private int ID;
    private String name = "";
    private int linkID = 0;

    public ArrayList<Pin> outputPins = new ArrayList<>();
    public ArrayList<Pin> inputPins = new ArrayList<>();

    public Node(Graph graph){
        this.graph = graph;
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
