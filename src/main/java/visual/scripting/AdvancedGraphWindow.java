package visual.scripting;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesStyle;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import imgui.extension.nodeditor.flag.NodeEditorStyleColor;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.extension.texteditor.TextEditor;
import imgui.flag.*;
import imgui.internal.ImRect;
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

public class AdvancedGraphWindow {

    private ImBoolean closable = new ImBoolean(true);

    private ImGuiWindow window;
    private String id;
    private Graph graph;
    //    private ImNodesContext context;
    private boolean windowFocused;

    private ArrayList<Class<? extends Node>> nodeList = new ArrayList<>();

    private static final ImInt LINK_A = new ImInt();
    private static final ImInt LINK_B = new ImInt();
    private static final ImLong LINKA = new ImLong();
    private static final ImLong LINKB = new ImLong();

    private NodeCompiler nodeCompiler = new NodeCompiler();
    private TextEditor EDITOR = new TextEditor();

    public Map<Integer, ImVec2> nodeQPos = new HashMap<>();

    //change from ImNodes to NodeEditor
    private NodeEditorContext context;
    private int navigateTo = -1;

    public AdvancedGraphWindow(ImGuiWindow window){
        this.window = window;
        //id will be changed to file name
        this.id = "new" + new Random().nextInt(100);
        graph = new Graph();
//        context = ImNodes.editorContextCreate();

        NodeEditorConfig config = new NodeEditorConfig();
        config.setSettingsFile(null);
        context = new NodeEditorContext(config);

        //add a node to allow more than one flow
        addNodeToList(NodeSplitFlow.class);
        //add a starter node to the graph
        graph.addNode(new NodeEntry(graph));

        for(VisualScriptingPlugin plugin : ImGuiWindow.pluginManager.getExtensions(VisualScriptingPlugin.class)){
            //TODO null not acceptable
            plugin.init(null);
        }
    }

    public int ToNodeColor(NodeColor nodeColor){
        int Red = (nodeColor.r << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        int Green = (nodeColor.g << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        int Blue = nodeColor.b & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    public int rgbToInt(int r, int g, int b){
        int Red = (r << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        int Green = (g << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        int Blue = b & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    private void drawPinIcon(boolean filled, int color){
        float s_PinIconSize = 24f;
        if(isRectVisible(s_PinIconSize, s_PinIconSize)) {
            ImVec2 cursorPos = getCursorPos();
            ImVec2 size = new ImVec2(cursorPos.x + s_PinIconSize, cursorPos.y + s_PinIconSize);

            ImRect rect = new ImRect(cursorPos, size);
            float rect_x = rect.min.x;
            float rext_y = rect.min.y;
            float rect_w = rect.max.x - rect.max.x;
            float rect_h = rect.max.y - rect.max.y;
            float rect_center_x = (rect.min.x + rect.max.x) * 0.5f;
            float rect_center_y = (rect.min.y + rect.max.y) * 0.5f;
            ImVec2 rect_center = new ImVec2(rect_center_x, rect_center_y);

            float outline_scale = rect_w / 24.0f;
            int extra_sgments = (int) (2 * outline_scale);

            float origin_scale = rect_w / 24.0f;

            float offset_x = 1.0f * origin_scale;
            float offset_y = 0.0f * origin_scale;
            float margin = (filled ? 2.0f : 2.0f) * origin_scale;
            float rounding = 0.1f * origin_scale;
            float tip_round = 0.7f;

            ImRect canvas = new ImRect(rect.min.x + margin + offset_x,
                    rect.min.y + margin + offset_y,
                    rect.max.x - margin + offset_x,
                    rect.max.y - margin + offset_y);

            float canvas_x = canvas.min.x;
            float canvas_y = canvas.min.y;
            float canvas_w = canvas.max.y - canvas.min.x;
            float canvas_h = canvas.max.y = canvas.min.y;

            float left   = canvas_x + canvas_w            * 0.5f * 0.3f;
            float right  = canvas_x + canvas_w - canvas_w * 0.5f * 0.3f;
            float top    = canvas_y + canvas_h            * 0.5f * 0.2f;
            float bottom = canvas_y + canvas_h - canvas_h * 0.5f * 0.2f;
            float center_y = (top + bottom) * 0.5f;

            ImVec2 tip_top    = new ImVec2(canvas_x + canvas_w * 0.5f, top);
            ImVec2 tip_right  = new ImVec2(right, center_y);
            ImVec2 tip_bottom = new ImVec2(canvas_x + canvas_w * 0.5f, bottom);

            ImDrawList drawList = getWindowDrawList();

            drawList.pathLineTo(left, top + rounding);
            drawList.pathBezierCubicCurveTo(left, top, left, top, left + rounding, top + 0);
            drawList.pathLineTo(tip_top.x, tip_top.y);
            drawList.pathLineTo(tip_top.x + (tip_right.x - tip_top.x) * tip_round, tip_top.y + (tip_right.y - tip_top.y) * tip_round);
            drawList.pathBezierCubicCurveTo(tip_right.x, tip_right.y, tip_right.x, tip_right.y, tip_bottom.x + (tip_right.x - tip_top.x) * rounding, tip_bottom.y + (tip_right.y - tip_top.y) * rounding);
            drawList.pathLineTo(tip_bottom.x, tip_bottom.y);
            drawList.pathLineTo(left + rounding, bottom + 0);
            drawList.pathBezierCubicCurveTo(left, bottom, left, bottom, left + 0, bottom + rounding);

            if(!filled){
                drawList.pathStroke(color, 1, 2.0f * outline_scale);
            }else{
                drawList.pathFillConvex(color);
            }
        }
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
                //TODO null not acceptable
                window.removeGraphWindow(null);
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
                                navigateTo = node.getID();
                            }
                        }
                    }
                    endChild();

                    sameLine();

//                    editorContextSet(context);
                    NodeEditor.setCurrentEditor(context);
//                    beginNodeEditor();
                    NodeEditor.pushStyleColor(NodeEditorStyleColor.NodeBg, .89453125f, .89453125f, .89453125f, 0.78125f);
                    NodeEditor.pushStyleColor(NodeEditorStyleColor.NodeBorder, .48828125f, .48828125f, .48828125f, 0.78125f);
                    NodeEditor.pushStyleColor(NodeEditorStyleColor.PinRect, .89453125f, .89453125f, .89453125f, 0.234375f);
                    NodeEditor.pushStyleColor(NodeEditorStyleColor.PinRectBorder, .48828125f, .48828125f, .48828125f, 0.234375f);

//                    NodeEditor.getStyle().setNodePadding(0, 0, 0, 0);
//                    NodeEditor.getStyle().setNodeRounding(5.0f);
//                    NodeEditor.getStyle().setSourceDirection(0.0f, 1.0f);
//                    NodeEditor.getStyle().setTargetDirection(0.0f, -1.0f);
//                    NodeEditor.getStyle().setLinkStrength(0.0f);
//                    NodeEditor.getStyle().setPinBorderWidth(1.0f);
//                    NodeEditor.getStyle().setPinRadius(6.0f);
                    NodeEditor.begin("Graph");
                    {

                        for (Node node : graph.getNodes().values()) {
//                            ImNodes.pushColorStyle(ImNodesColorStyle.TitleBar, ToNodeColor(node.getStyle().TitleBar));
//                            ImNodes.pushColorStyle(ImNodesColorStyle.TitleBarSelected, ToNodeColor(node.getStyle().TitleBarSelected));
//                            ImNodes.pushColorStyle(ImNodesColorStyle.TitleBarHovered, ToNodeColor(node.getStyle().TitleBarHovered));
//                            ImNodes.pushColorStyle(ImNodesColorStyle.NodeBackground, ToNodeColor(node.getStyle().NodeBackground));
//                            ImNodes.pushColorStyle(ImNodesColorStyle.NodeBackgroundSelected, ToNodeColor(node.getStyle().NodeBackgroundSelected));
//                            ImNodes.pushColorStyle(ImNodesColorStyle.NodeBackgroundHovered, ToNodeColor(node.getStyle().NodeBackgroundHovered));
                            ImNodes.pushStyleVar(NodeEditorStyleVar.NodePadding, 8);
                            NodeEditor.beginNode(node.getID());
//                            beginNode(node.getID());
                            {
//                                beginNodeTitleBar();
                                text("Header: " + node.getName());
//                                endNodeTitleBar();

                                dummy(0, 28);
                                //add node pins
                                int max = Math.max(node.outputPins.size(), node.inputPins.size());
                                for (int i = 0; i < max; i++) {

                                    if (node.inputPins.size() > i) {
                                        Pin inPin = node.inputPins.get(i);
                                        NodeEditor.beginPin(node.getID(), NodeEditorPinKind.Input);
////                                        addPin(inPin);
                                        drawPinIcon(false, rgbToInt(255, 0, 0));
                                        NodeEditor.endPin();
                                    }

//                                    dummy(150, 0);

                                    if (node.outputPins.size() > i) {
                                        Pin outPin = node.outputPins.get(i);
//                                        NodeEditor.beginPin(node.getID(), NodeEditorPinKind.Output);
//                                        addPin(outPin);
                                    }
                                    newLine();
                                }
                            }
//                            endNode();
                            NodeEditor.endNode();
//                            NodeEditor.popStyleVar(7);
//                            NodeEditor.popStyleColor(4);

//                            ImNodes.popColorStyle();
//                            ImNodes.popColorStyle();
//                            ImNodes.popColorStyle();
//                            ImNodes.popColorStyle();
//                            ImNodes.popColorStyle();
//                            ImNodes.popColorStyle();

                            //calculate connect pins values
                            for (int i = 0; i < node.outputPins.size(); i++) {
                                Pin pin = node.outputPins.get(i);

                                if (pin.connectedTo != -1) {
                                    System.out.println(pin.connectedTo);
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
//                                    link(uniqueLinkId++, pin.getID(), pin.connectedTo);
                                    NodeEditor.link(uniqueLinkId, pin.getID(), pin.connectedTo);
//                                    System.out.println("Link");
                                }
                            }
                        }
                    }
                    windowFocused = isWindowHovered();

                    if(NodeEditor.beginCreate()){
                        checkPinConnections();
                    }
                    NodeEditor.endCreate();
                    NodeEditor.suspend();

                    if(navigateTo != -1) {
                        NodeEditor.selectNode(navigateTo, false);
                        NodeEditor.navigateToSelection(false, 1.5f);
                        navigateTo = -1;
                    }

                    final long nodeWithContextMenu = NodeEditor.getNodeWithContextMenu();
                    if(nodeWithContextMenu != -1){
                        openPopup("node_menu" + id);
                        getStateStorage().setInt(getID("delete_node_id"), (int)nodeWithContextMenu);
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

                    if(NodeEditor.showBackgroundContextMenu()){
                        openPopup("context_menu" + id);
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

//                    endNodeEditor();
                    NodeEditor.resume();
                    NodeEditor.end();
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
        end();


        //set nodes position to the current graphs pos
//        for(Integer id : nodeQPos.keySet()){
//            ImVec2 pos = nodeQPos.get(id);
//            getNodeEditorSpacePos(id, pos);
//            setNodeGridSpacePos(id, -pos.x + 300, -pos.y + 300);
//
//        }
//        nodeQPos.clear();


//        checkPinConnections();

//        if(isMouseClicked(ImGuiMouseButton.Right)){
//            final int hoveredNode = getHoveredNode();
//            if(hoveredNode != -1){
//                openPopup("node_menu" + id);
//                getStateStorage().setInt(getID("delete_node_id"), hoveredNode);
//            }else{
//                if(windowFocused) {
//                    openPopup("context_menu" + id);
//                }
//            }
//        }
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
//        if(isLinkCreated(LINK_A, LINK_B)){
        if(NodeEditor.queryNewLink(LINKA, LINKB)){
            final Pin sourcePin = graph.findPinById((int) LINKA.get());
            final Pin targetPin = graph.findPinById((int) LINKB.get());

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

                if(sourcePin != null && targetPin != null && NodeEditor.acceptNewItem(.11f ,1, .5f, 1, 2)){
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
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Input);
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.Triangle);
                        ImNodes.popColorStyle();
                        break;
                    case Bool:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 50, 50)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Input);
                        ImNodes.popColorStyle();
                        break;
                    case Int:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(80, 50, 200)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Input);
                        ImNodes.popColorStyle();
                        break;
                    case Float:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(5, 50, 190)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Input);
                        ImNodes.popColorStyle();
                        break;
                    case Double:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 250, 190)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Input);
                        ImNodes.popColorStyle();
                        break;
                    case String:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 50, 100)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Input);
                        ImNodes.popColorStyle();
                        break;
                    default:
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Input);
                }
//                pushItemWidth(250);
//                configurePinUI(pin);
//                popItemWidth();
//                endOutputAttribute();
//                sameLine();
                text("in");
                NodeEditor.endPin();
//                sameLine();
                break;
            case Output:
                switch (pin.getDataType()){
                    case Flow:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 255, 255)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.Triangle);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Output);
                        ImNodes.popColorStyle();
                        break;
                    case Bool:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 50, 50)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Output);
                        ImNodes.popColorStyle();
                        break;
                    case Int:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(80, 50, 200)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Output);
                        ImNodes.popColorStyle();
                        break;
                    case Float:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(5, 50, 190)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Output);
                        ImNodes.popColorStyle();
                        break;
                    case Double:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 250, 190)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Output);
                        ImNodes.popColorStyle();
                        break;
                    case String:
                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 50, 100)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Output);
                        ImNodes.popColorStyle();
                        break;
                    default:
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                        NodeEditor.beginPin(pin.getID(), NodeEditorPinKind.Output);
                }
//                sameLine(curNodeSize / 2);
                sameLine();
//                newLine();
//                configurePinType(pin);
                NodeEditor.pinPivotAlignment(1f, 0.5f);
                text(pin.getName());
                text("Out");
//                endOutputAttribute();
                NodeEditor.endPin();
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
