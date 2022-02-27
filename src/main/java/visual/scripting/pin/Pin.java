package visual.scripting.pin;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImVec4;
import imgui.type.*;
import visual.scripting.NodeData;
import visual.scripting.node.Node;

public class Pin {

    public enum PinType{
        Output,
        Input
    }

    public enum DataType{
        Flow,
        Bool,
        Int,
        Float,
        Double,
        Long,
        String,
        Object,
        Function,
        SPACER
    }

    private Node node;
    private int ID;
    private PinType pinType;
    private DataType dataType;
    private String name = "";
    public int connectedTo = -1;

//    private ImBoolean Boolean = new ImBoolean();
//    private ImInt Int = new ImInt();
//    private ImFloat Float = new ImFloat();
//    private ImString String = new ImString();
//    private ImDouble Double = new ImDouble();
//    private ImLong Long = new ImLong();
    //this doesn't exist but possibly a Object<T> kind of class might work
//    private ImObject Object = new ImObject()

    private NodeData data;

    private String variable;
    private boolean canDelete = false;

    public ImVec4 color = new ImVec4();

//    public ImVec2 spacing;

    public Pin(Node node, int ID, DataType dataType, PinType pinType){
        this.node = node;
        this.ID = ID;
        this.dataType = dataType;
        this.pinType = pinType;
        data = new NodeData();
//        setupData();
    }

    public void draw(ImDrawList windowDrawList, float posX, float posY, boolean isConnected, boolean pinDragSame){

    }

    public void drawDefaultCircle(ImDrawList windowDrawList, float posX, float posY, boolean isConnected, boolean pinDragSame){
        float size = 10f;
        int doubleGrey = pinDragSame ? rgbToInt(49, 102, 80, 255) : rgbToInt(50, 50, 50, 255);
        if(isConnected) {
            windowDrawList.addCircleFilled(posX + (size / 2), posY + (size / 2), size / 2, doubleGrey);
        }else{
            windowDrawList.addCircle(posX + (size / 2), posY + (size / 2), size / 2, doubleGrey);
        }
    }

    public void UI(){

    }

    public int rgbToInt(int r, int g, int b, int a){
        return ImColor.intToColor(r, g, b, a);
    }

    public Pin(){
    }

//    private void setupData(){
//        if(dataType == null){
//            return;
//        }
//        switch (dataType){
//            case Flow:
//                break;
//            case Bool:
//                data.setValue(Boolean);
//                break;
//            case Int:
//                data.setValue(Int);
//                break;
//            case Float:
//                data.setValue(Float);
//                break;
//            case Double:
//                data.setValue(Double);
//                break;
//            case Long:
//                data.setValue(Long);
//                break;
//            case String:
//                data.setValue(String);
//                break;
//            case Object:
////                data.setValue();
//                break;
//            case Function:
//                break;
//        }
//    }

    public void setVariable(String var){
        this.variable = var;
    }

    public String getVariable(){
        return variable;
    }

    /**
     * called when pin is loaded from file, used to convert string value to NodeData
     * @param value
     */
    public void loadValue(String value){

    }

    public void setID(int id){
        this.ID = id;
        getNode().getGraph().setHighestPinID(++id);
    }

    public int getID(){
        return ID;
    }

    public void setPinType(PinType pinType){
        this.pinType = pinType;
    }

    public PinType getPinType() {
        return pinType;
    }

//    public DataType getDataType() {
//        return dataType;
//    }

    public Pin setName(String name){
        if(name == null){
            name = "";
        }
        this.name = name;

        return this;
    }

    public boolean connect(Pin targetPin){
        //remove old connections
        if (connectedTo != -1) {
            Pin oldPin = getNode().getGraph().findPinById(connectedTo);
            oldPin.connectedTo = -1;
        }

        if (targetPin.connectedTo != -1) {
            Pin oldPin = getNode().getGraph().findPinById(targetPin.connectedTo);
            oldPin.connectedTo = -1;
        }
        if (connectedTo != targetPin.connectedTo || (targetPin.connectedTo == -1 || connectedTo == -1)) {
            connectedTo = targetPin.getID();
            targetPin.connectedTo = getID();
            return true;
        }
        return false;
    }

    public String getName(){
        return name;
    }

    public int getConnectedTo() {
        return connectedTo;
    }

//    public ImBoolean getBoolean() {
//        return Boolean;
//    }
//
//    public ImInt getInt() {
//        return Int;
//    }
//
//    public ImFloat getFloat() {
//        return Float;
//    }
//
//    public ImString getString() {
//        return String;
//    }
//
//    public ImDouble getDouble() {
//        return Double;
//    }
//
//    public ImLong getLong() {
//        return Long;
//    }
//
//    public void setLong(ImLong aLong) {
//        Long = aLong;
//    }

    public NodeData getData() {
        return data;
    }

    public void setData(NodeData data) {
        this.data = data;
    }

    public void setNode(Node node){
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }
}

