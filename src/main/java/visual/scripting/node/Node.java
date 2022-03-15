package visual.scripting.node;

import imgui.ImVec2;
import visual.scripting.Graph;
import visual.scripting.node.style.NodeStyle;
import visual.scripting.pin.Pin;
import visual.scripting.pin.PinSpacer;
import visual.scripting.ui.UiComponent;

import java.util.ArrayList;

public class Node {

    private Node self;
    private String CATEGORY = null;

    private String[] languages = new String[0];
    private boolean hasTitleBar = true;
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


    public float posX = 0;
    public float posY = 0;
    public float width = -1;
    private ImVec2 loadedPosition = new ImVec2();

    public ArrayList<UiComponent> uiComponents = new ArrayList<>();

    private String error = "";

    public Node(Graph graph){
        this.self = this;
        this.graph = graph;
    }

    public Node getSelf()
    {
        return self;
    }

    public void init(){

    }

    public void setError(String error){
        this.error = error;
    }

    public String getError(){
        return error;
    }

    public void setColor(int r, int g, int b, int a){
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    /**
     * Only used during loading phase to set the positions of the nodes when the graph is loaded from a save file
     * @param x
     * @param y
     */
    public void setLoadedPosition(float x, float y){
        this.loadedPosition.set(x, y);
    }

    /**
     * Only used during loading phase to set the positions of the nodes when the graph is loaded from a save file
     * @return
     */
    public ImVec2 getLoadedPosition(){
        return loadedPosition;
    }

    /**
     * if no languages are set we will assume this node can be used on any language
     */
    public void setLanguages(String[] languages){
        this.languages = languages;
    }

    public String[] getLanguages(){
        return languages;
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

//    public Pin addOutputPin(Pin.DataType dataType, Node node){
//        int id = Graph.getNextAvailablePinID();
////        Pin pin = new Pin(node, id, dataType, Pin.PinType.Output, linkID++);
//        Pin pin = new Pin(node, id, dataType, Pin.PinType.Output);
//        outputPins.add(pin);
//        return pin;
//    }

    public void addCustomInput(Pin pin){
        int id = Graph.getNextAvailablePinID();
        pin.setID(id);
        pin.setPinType(Pin.PinType.Input);
        inputPins.add(pin);
    }

    public void addCustomOutput(Pin pin){
        int id = Graph.getNextAvailablePinID();
        pin.setID(id);
        pin.setPinType(Pin.PinType.Output);
        outputPins.add(pin);
    }

    /**
     * Used to fix alignment of output pins with an input pin of choice
     */
    public void addOutputSpacer(Node node){
        Pin pin = new PinSpacer(node);
        addCustomOutput(pin);
//        int id = Graph.getNextAvailablePinID();
////        Pin pin = new Pin(node, id, Pin.DataType.SPACER, Pin.PinType.Output, linkID++);
//        Pin pin = new Pin(node, id, Pin.DataType.SPACER, Pin.PinType.Output);
//        outputPins.add(pin);
    }

//    public Pin addInputPin(Pin.DataType dataType, Node node){
//        int id = Graph.getNextAvailablePinID();
////        Pin pin = new Pin(node, id, dataType, Pin.PinType.Input, linkID++);
//        Pin pin = new Pin(node, id, dataType, Pin.PinType.Input);
//        inputPins.add(pin);
//        return pin;
//    }

    public void addUiComponent(UiComponent uiComponent){
        uiComponents.add(uiComponent);
    }

    public boolean removePinById(int id){
        boolean found = false;
        for (int i = 0; i < inputPins.size(); i++) {
            Pin targetPin = inputPins.get(i);
            if(targetPin.getID() == id){
                found = true;
                if(targetPin.connectedTo != -1){
                    Pin connection = getGraph().findPinById(targetPin.connectedTo);
                    connection.connectedTo = -1;
                }
                inputPins.remove(i);
                break;
            }
        }

        if(!found){
            for (int i = 0; i < outputPins.size(); i++) {
                Pin targetPin = outputPins.get(i);
                if(targetPin.getID() == id){
                    found = true;
                    if(targetPin.connectedTo != -1){
                        Pin connection = getGraph().findPinById(targetPin.connectedTo);
                        connection.connectedTo = -1;
                    }
                    outputPins.remove(i);
                    break;
                }
            }
        }

        return found;
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

    public boolean hasTitleBar() {
        return hasTitleBar;
    }

    public void setHasTitleBar(boolean hasTitleBar) {
        this.hasTitleBar = hasTitleBar;
    }
}
