package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.Pin;
import visual.scripting.node.style.NodeStyle;

import java.util.ArrayList;

public class Node {

    private String CATEGORY = null;

    private final Graph graph;
    private int ID;
    private String name = "";
    private int linkID = 0;

    public ArrayList<Pin> outputPins = new ArrayList<>();
    public ArrayList<Pin> inputPins = new ArrayList<>();

    private NodeStyle style = new NodeStyle();
    private int r = 125;
    private int g = 125;
    private int b = 125;
    private int a = 125;

    public float width = -1;

    public Node(Graph graph){
        this.graph = graph;
    }

    public void init(){

    }

    public void setColor(int r, int g, int b, int a){
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int getRed(){
        return r;
    }

    public int getGreen(){
        return g;
    }

    public int getBlue(){
        return b;
    }

    public int getAlpha(){
        return a;
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

    /**
     * Used to fix alignment of output pins with an input pin of choice
     */
    public void addOutputSpacer(Node node){
        int id = Graph.getNextAvailablePinID();
        Pin pin = new Pin(node, id, null, Pin.PinType.Output, linkID++);
        outputPins.add(pin);
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

    public void setCategory(String category){
        this.CATEGORY = category;
    }

    public String getCategory(){
        return CATEGORY;
    }
}
