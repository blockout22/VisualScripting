package visual.scripting;

import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesContext;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.extension.texteditor.TextEditor;
import imgui.flag.*;
import imgui.type.*;
import visual.scripting.node.Node;
import visual.scripting.node.style.NodeColor;
import visual.scripting.node.NodeEntry;
import visual.scripting.node.NodeSplitFlow;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static imgui.ImGui.*;
import static imgui.ImGui.getMainViewport;
import static imgui.extension.imnodes.ImNodes.*;

public class GraphWindow {

    private ImBoolean closable = new ImBoolean(true);

    private ImGuiWindow window;
    private String id;
    private Graph graph;
    private ImNodesContext context;
    private boolean windowFocused;

//    private ArrayList<NodeBuilder> nodesTypes = new ArrayList<>();
    private ArrayList<Class<? extends Node>> nodeList = new ArrayList<>();

    private static final ImInt LINK_A = new ImInt();
    private static final ImInt LINK_B = new ImInt();

    private NodeCompiler nodeCompiler = new NodeCompiler();
    private TextEditor EDITOR = new TextEditor();

    public Map<Integer, ImVec2> nodeQPos = new HashMap<>();

    public GraphWindow(ImGuiWindow window){
        this.window = window;
        //id will be changed to file name
        this.id = "new" + new Random().nextInt(100);
        graph = new Graph();
        context = ImNodes.editorContextCreate();

        //add a node to allow more than one flow
        addNodeToList(NodeSplitFlow.class);
        //add a starter node to the graph
        graph.addNode(new NodeEntry(graph));

        for(VisualScriptingPlugin plugin : ImGuiWindow.pluginManager.getExtensions(VisualScriptingPlugin.class)){
            plugin.init(this);
        }
    }

    public int ToNodeColor(NodeColor nodeColor){
        int Red = (nodeColor.r << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        int Green = (nodeColor.g << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        int Blue = nodeColor.b & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    /**
     *  Shows the Graphs window
     */
    public void show(float menuBarHeight){
        graph.update();
        setNextWindowSize(GLFWWindow.getWidth(), GLFWWindow.getHeight() - menuBarHeight, ImGuiCond.Once);
        setNextWindowPos(getMainViewport().getPosX(), getMainViewport().getPosY() + menuBarHeight, ImGuiCond.Once);

        if(begin(id, closable, ImGuiWindowFlags.NoCollapse)){
            //checks is value has been changed from clicking the close button
            if(closable.get() == false){
                //should init save
                window.removeGraphWindow(this);
//                editorContextFree(context);
            }

            //used to call the method used to convert the nodes to the nodes output text
            if(button("Compile")){
                EDITOR.setText(nodeCompiler.compile(graph));
                GraphSaver.save(graph);
            }

            //loads the nodes into the graph from a save file
            if(button("load")){
//                GraphSaver.load(this, graph);
            }

            //clears all nodes from the graph and resets the graph
            //this may cause an ConcurrentModificationException, needs testing
            if(button("Clear Graph")){
                graph.getNodes().clear();
                graph.addNode(new NodeEntry(graph));
            }

            if(beginTabBar("TabBar")) {
                if(beginTabItem("NodeEditor")) {
                    if(beginChild("SideBar", 100, 250)){
                        for (Node node : graph.getNodes().values()) {
                            if(button(node.getName())){
                            }
                        }
                    }
                    endChild();

                    sameLine();

                    editorContextSet(context);
                    beginNodeEditor();
                    {
                        for (Node node : graph.getNodes().values()) {
                            ImNodes.pushColorStyle(ImNodesColorStyle.TitleBar, ToNodeColor(node.getStyle().TitleBar));
                            ImNodes.pushColorStyle(ImNodesColorStyle.TitleBarSelected, ToNodeColor(node.getStyle().TitleBarSelected));
                            ImNodes.pushColorStyle(ImNodesColorStyle.TitleBarHovered, ToNodeColor(node.getStyle().TitleBarHovered));
                            ImNodes.pushColorStyle(ImNodesColorStyle.NodeBackground, ToNodeColor(node.getStyle().NodeBackground));
                            ImNodes.pushColorStyle(ImNodesColorStyle.NodeBackgroundSelected, ToNodeColor(node.getStyle().NodeBackgroundSelected));
                            ImNodes.pushColorStyle(ImNodesColorStyle.NodeBackgroundHovered, ToNodeColor(node.getStyle().NodeBackgroundHovered));
                            beginNode(node.getID());
                            {
                                beginNodeTitleBar();
                                text(node.getName());
                                endNodeTitleBar();


                                //add node pins
                                int max = Math.max(node.outputPins.size(), node.inputPins.size());
                                for (int i = 0; i < max; i++) {

                                    if (node.inputPins.size() > i) {
                                        Pin inPin = node.inputPins.get(i);
                                        addPin(inPin);
                                    }

                                    dummy(150, 0);

                                    if (node.outputPins.size() > i) {
                                        Pin outPin = node.outputPins.get(i);
                                        addPin(outPin);
                                    }
                                    newLine();
                                }
                            }
                            endNode();

                            ImNodes.popColorStyle();
                            ImNodes.popColorStyle();
                            ImNodes.popColorStyle();
                            ImNodes.popColorStyle();
                            ImNodes.popColorStyle();
                            ImNodes.popColorStyle();

                            //calculate connect pins values
                            for (int i = 0; i < node.outputPins.size(); i++) {
                                Pin pin = node.outputPins.get(i);

                                if (pin.connectedTo != -1) {
                                    //find the input pin that is connect to this output pin
                                    Pin otherPin = graph.findPinById(pin.connectedTo);

                                    switch (pin.getDataType()) {
                                        case Bool:
                                            NodeData<ImBoolean> boolOutData = otherPin.getData();
                                            NodeData<ImBoolean> boolInData = pin.getData();
                                            boolOutData.getValue().set(boolInData.value.get());
                                            break;
                                        case Int:
                                            NodeData<ImInt> intOutData = otherPin.getData();
                                            NodeData<ImInt> intInData = pin.getData();
                                            intOutData.getValue().set(intInData.value.get());
                                            break;
                                        case Float:
                                            NodeData<ImFloat> floatOutData = otherPin.getData();
                                            NodeData<ImFloat> floatInData = pin.getData();
                                            floatOutData.getValue().set(floatInData.value.get());
                                            break;
                                        case Double:
                                            NodeData<ImDouble> doubleOutData = otherPin.getData();
                                            NodeData<ImDouble> doubleInData = pin.getData();
                                            doubleOutData.getValue().set(doubleInData.value.get());
                                            break;
                                        case String:
                                            NodeData<ImString> stringOutData = otherPin.getData();
                                            NodeData<ImString> stringInData = pin.getData();
                                            stringOutData.getValue().set(stringInData.value.get());
                                            break;
                                    }
                                }
                            }

                            node.execute();
                        }

                        //link node pins together
                        int uniqueLinkId = 1;
                        for (Node node : graph.getNodes().values()) {
                            for (Pin pin : node.outputPins) {
                                if (pin.connectedTo != -1) {
                                    link(uniqueLinkId++, pin.getID(), pin.connectedTo);
                                }
                            }
                        }
                    }
                    windowFocused = isWindowHovered();
                    endNodeEditor();
                    endTabItem();
                }

                if(beginTabItem("Output")){
                    EDITOR.render("output");
                    EDITOR.setReadOnly(true);
                    endTabItem();
                }
            }
            endTabBar();
        }


        //set nodes position to the current graphs pos
        for(Integer id : nodeQPos.keySet()){
            ImVec2 pos = nodeQPos.get(id);
            getNodeEditorSpacePos(id, pos);
            setNodeGridSpacePos(id, -pos.x + 300, -pos.y + 300);

        }
        nodeQPos.clear();

        end();

        checkPinConnections();

        if(isMouseClicked(ImGuiMouseButton.Right)){
            final int hoveredNode = getHoveredNode();
            if(hoveredNode != -1){
                openPopup("node_menu" + id);
                getStateStorage().setInt(getID("delete_node_id"), hoveredNode);
            }else{
                if(windowFocused) {
                    openPopup("context_menu" + id);
                }
            }
        }

        if(isPopupOpen("node_menu" + id)){
            final int targetNode = getStateStorage().getInt(getID("delete_node_id"));
            if(beginPopup("node_menu" + id)){
                if(menuItem("Delete " + graph.getNodes().get(targetNode).getName()))
                {
                    graph.removeNode(targetNode);
                    closeCurrentPopup();
                }
            }
            endPopup();
        }

        if(isPopupOpen("context_menu" + id)){
            if(beginPopup("context_menu" + id)){
                //get all loaded nodes and show them in the right click menu
                for(Class<? extends Node> node : nodeList){
                    Constructor<? extends Node> nodeClass = null;
                    Node instance = null;
                    try {
                        nodeClass = node.getDeclaredConstructor(Graph.class);
                        //not very good, this creates a new Object each frame
                        //an alternative should be used to get Objects set Variables without creating 100s of instances while context menu is open
                        //(Maybe create an array the same size as the nodeList array and store an instance there (destroy instances after menu is closed))
                        instance = nodeClass.newInstance(graph);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(menuItem(instance.getName()))
                    {
                        try {
                            graph.addNode(instance);
                            instance.init();
                            nodeQPos.put(instance.getID(), new ImVec2());
//                            setNodeScreenSpacePos(instance.getID(), getMousePosX(), getMousePosX());
                            } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        closeCurrentPopup();
                    }

                }
                endPopup();
            }
        }
    }

    /**
     * Called by Plugins to a node to the context menu and allow it to be created in the graph
     */
    public void addNodeToList(Class<? extends Node> node){
        nodeList.add(node);
    }

    /**
     * checks if 2 pins should be connected and checks if the pin should be disconnected
     */
    private void checkPinConnections(){
        if(isLinkCreated(LINK_A, LINK_B)){
            final Pin sourcePin = graph.findPinById(LINK_A.get());
            final Pin targetPin = graph.findPinById(LINK_B.get());

            if(!(sourcePin.getDataType() == targetPin.getDataType())){
                System.out.println("Types are not the same");
            }else{
                if(sourcePin.connectedTo != -1){
                    Pin oldPin = graph.findPinById(sourcePin.connectedTo);
                    oldPin.connectedTo = -1;
                }

                if(targetPin.connectedTo != -1){
                    Pin oldPin = graph.findPinById(targetPin.connectedTo);
                    oldPin.connectedTo = -1;
                }

                if(sourcePin != null && targetPin != null){
                    if (sourcePin.connectedTo != targetPin.connectedTo || (targetPin.connectedTo == -1 || sourcePin.connectedTo == -1)) {
                        sourcePin.connectedTo = targetPin.getID();
                        targetPin.connectedTo = sourcePin.getID();
                    }
                }
            }
        }

        if(isLinkDropped(LINK_A, false)) {

            Pin pin1 = graph.findPinById(LINK_A.get());
            if (pin1.connectedTo != -1) {
                Pin pin2 = graph.findPinById(pin1.connectedTo);
                pin1.connectedTo = -1;
                pin2.connectedTo = -1;
            }
        }
    }

    private void addPin(Pin pin){
        switch (pin.getPinType()){
            case Input:
                switch (pin.getDataType()){
                    case Flow:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 255, 255)));
                        beginInputAttribute(pin.getID(), ImNodesPinShape.Triangle);
                        ImNodes.popColorStyle();
                        break;
                    case Bool:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 50, 50)));
                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        ImNodes.popColorStyle();
                        break;
                    case Int:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(80, 50, 200)));
                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        ImNodes.popColorStyle();
                        break;
                    case Float:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(5, 50, 190)));
                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        ImNodes.popColorStyle();
                        break;
                    case Double:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 250, 190)));
                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        ImNodes.popColorStyle();
                        break;
                    case String:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 50, 100)));
                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        ImNodes.popColorStyle();
                        break;
                    default:
                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                }
                pushItemWidth(250);
                configurePinUI(pin);
                popItemWidth();
                endOutputAttribute();
                sameLine();
                break;
            case Output:
                switch (pin.getDataType()){
                    case Flow:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 255, 255)));
                        beginOutputAttribute(pin.getID(), ImNodesPinShape.Triangle);
                        ImNodes.popColorStyle();
                        break;
                    case Bool:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 50, 50)));
                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        ImNodes.popColorStyle();
                        break;
                    case Int:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(80, 50, 200)));
                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        ImNodes.popColorStyle();
                        break;
                    case Float:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(5, 50, 190)));
                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        ImNodes.popColorStyle();
                        break;
                    case Double:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 250, 190)));
                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        ImNodes.popColorStyle();
                        break;
                    case String:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 50, 100)));
                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        ImNodes.popColorStyle();
                        break;
                    default:
                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                }
//                sameLine(curNodeSize / 2);
                sameLine();
//                newLine();
//                configurePinType(pin);
                text(pin.getName());
                endOutputAttribute();
                sameLine();
                break;
        }
    }

    /**
     * adds input fields to the Pin Type
     */
    private void configurePinUI(Pin pin) {
        switch (pin.getDataType()){
            case Flow:
                break;
            case Bool:
                if(checkbox(pin.getName(), pin.getBoolean())){

                }
                break;
            case Int:
                if(inputInt(pin.getName(), pin.getInt())){

                }
                break;
            case Float:
                if(inputFloat(pin.getName(), pin.getFloat())){

                }
                break;
            case Double:
                if(inputDouble(pin.getName(), pin.getDouble())){

                }
                break;
            case String:
                if(inputText(pin.getName(), pin.getString())){

                }
                break;
        }
    }
}