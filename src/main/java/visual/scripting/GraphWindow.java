package visual.scripting;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.extension.texteditor.TextEditor;
import imgui.flag.*;
import imgui.type.*;
import visual.scripting.node.Node;
import visual.scripting.node.NodeVisualTest;
import visual.scripting.node.NodeEntry;
import visual.scripting.node.NodeSplitFlow;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static imgui.ImGui.*;

public class GraphWindow {

    private final ImBoolean closable = new ImBoolean(true);

    private ImGuiWindow window;
    private String id;
    protected Graph graph;
    private NodeEditorContext context;
//    private boolean windowFocused;

//    private ArrayList<NodeBuilder> nodesTypes = new ArrayList<>();
    protected final ArrayList<Class<? extends Node>> nodeList = new ArrayList<>();
    private final ArrayList<Node> nodeInstanceCache = new ArrayList<>();

    private final ImLong LINKA = new ImLong();
    private final ImLong LINKB = new ImLong();

    private final NodeCompiler nodeCompiler = new NodeCompiler();
    private final TextEditor EDITOR = new TextEditor();

    public Map<Integer, ImVec2> nodeQPos = new HashMap<>();
    private int nodeNavigateTo = -1;
    private boolean firstFrame = true;
    private long holdingPinID = -1;
    private long lastActivePin = -1;
    private ImVec2 cursorPos;

    private Pin.DataType curSelectedPinDataType = null;

    private int openSourcePreview = 0;
    private int currentNodeSourceID = -1;

    private Texture texture;

    protected GraphWindow(){
        graph = new Graph();
    }

    public GraphWindow(ImGuiWindow window){
        this.window = window;
        //id will be changed to file name
        this.id = "new" + new Random().nextInt(100);
        graph = new Graph();
        NodeEditorConfig config = new NodeEditorConfig();
        config.setSettingsFile(null);
        context = new NodeEditorContext(config);

        //add a node to allow more than one flow
        addNodeToList(NodeSplitFlow.class);
        addNodeToList(NodeVisualTest.class);
        //add a starter node to the graph
        graph.addNode(new NodeEntry(graph));

        for(VisualScriptingPlugin plugin : ImGuiWindow.pluginManager.getExtensions(VisualScriptingPlugin.class)){
            plugin.init(this);
        }

        //LINKA.get();

        try {
            texture = TextureLoader.loadTexture("white.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public int ToNodeColor(NodeColor nodeColor){
//        int Red = (nodeColor.r << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
//        int Green = (nodeColor.g << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
//        int Blue = nodeColor.b & 0x000000FF; //Mask out anything not blue.
//
//        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
//    }

//    private ImRect rect = new ImRect();
//    private float outputInputSpacing = 0.0f;

    /**
     *  Shows the Graphs window
     */
    //int colID = 0;
    public void show(float menuBarHeight){
        cursorPos = getMousePos();
        graph.update();
        setNextWindowSize(GLFWWindow.getWidth(), GLFWWindow.getHeight() - menuBarHeight, ImGuiCond.Once);
        setNextWindowPos(getMainViewport().getPosX(), getMainViewport().getPosY() + menuBarHeight, ImGuiCond.Once);

        if(begin(id, closable, ImGuiWindowFlags.NoCollapse)){
            //checks is value has been changed from clicking the close button
            if(!closable.get()){
                //should init save
                window.removeGraphWindow(this);
            }

            //used to call the method used to convert the nodes to the nodes output text
            if(button("Compile")){
                EDITOR.setText(nodeCompiler.compile(graph));
                GraphSaver.save(graph);
            }

            //loads the nodes into the graph from a save file
            if(button("load")){
                GraphSaver.load(this, graph);
            }

            //clears all nodes from the graph and resets the graph
            //this may cause an ConcurrentModificationException, needs testing
            if(button("Clear Graph")){
                graph.getNodes().clear();
                graph.addNode(new NodeEntry(graph));
            }

            if(beginTabBar("TabBar")) {
                if(beginTabItem("NodeEditor")) {
//                    if(beginChild("SideBar", 100, 350))
                    beginGroup();
                    dummy(200, 0);
                    {
                        text("Node List");
                        for (Node node : graph.getNodes().values()) {
                            pushID(node.getID());
                            if(button(node.getName())){
                                nodeNavigateTo = node.getID();
                            }
                            popID();
                        }
                    }
                    endGroup();
//                    endChild();

                    sameLine();

                    NodeEditor.setCurrentEditor(context);
                    NodeEditor.getStyle().setNodeRounding(2.0f);
//                    NodeEditor.pushStyleColor(NodeEditorStyleColor.LinkSelRect, 255, 0, 0, 255);
//                    TestNodeEditor.nodeStyleEditor();

                    ImVec2 headerMin;
                    ImVec2 headerMax;

                    float headerMaxY;
                    NodeEditor.begin("Editor");
                    {
                        for (Node node : graph.getNodes().values()) {
//                            headerMin = new ImVec2();
//                            headerMax = new ImVec2();
                            NodeEditor.pushStyleVar(NodeEditorStyleVar.NodePadding, 8, 4, 8, 8);
                            NodeEditor.beginNode(node.getID());
                            {
//                                if(button("NextId")){
////                                    colID++;
//                                }
                                text(node.getName());
                                headerMin = getItemRectMin();
//                                dummy(getItemRectMax().x, 2);
                                headerMaxY = getItemRectMax().y;
                                newLine();
                                //add node pins
                                int max = Math.max(node.outputPins.size(), node.inputPins.size());
                                for (int i = 0; i < max; i++) {

                                    if (node.inputPins.size() > i) {
                                        Pin inPin = node.inputPins.get(i);
//                                        addPin(inPin);
                                        NodeEditor.beginPin(inPin.getID(), NodeEditorPinKind.Input);//
                                        drawPinShapeAndColor(inPin);
                                        dummy(10, 10);

                                        NodeEditor.pinPivotAlignment(0f, .5f);
                                        NodeEditor.endPin();

                                        if(isItemClicked() && holdingPinID == -1){
                                            lastActivePin = inPin.getID();
                                        }

                                        if(isItemHovered()){
                                            setNextWindowPos(NodeEditor.toScreenX(getMousePosX()), NodeEditor.toScreenY(getMousePosY() + 10), ImGuiCond.Always);
                                            beginTooltip();
                                            textUnformatted(inPin.getPinType().name());
                                            textUnformatted("Type: " + inPin.getDataType());
                                            textUnformatted("Value: " + inPin.getData().value);
                                            endTooltip();
                                        }
                                        sameLine();
                                        configurePinUI(inPin);
                                    }

                                    if(node.width != -1) {
                                        sameLine(node.width - 10);
                                    }

                                    if (node.outputPins.size() > i) {
                                        Pin outPin = node.outputPins.get(i);

                                        if(outPin.getDataType() == null){
                                            NodeEditor.beginPin(outPin.getID(), NodeEditorPinKind.Output);
                                            dummy(1f, 1f);
                                            NodeEditor.endPin();
                                        }else {

                                            NodeEditor.beginPin(outPin.getID(), NodeEditorPinKind.Output);

                                            drawPinShapeAndColor(outPin);
                                            dummy(10, 10);
                                            NodeEditor.pinPivotAlignment(1f, .5f);
                                            sameLine();
                                            ImVec2 pos = getCursorPos();
                                            NodeEditor.endPin();

                                            if (isItemHovered()) {
                                                setNextWindowPos(NodeEditor.toScreenX(getMousePosX()), NodeEditor.toScreenY(getMousePosY() + 10), ImGuiCond.Always);
                                                beginTooltip();
                                                textUnformatted(outPin.getPinType().name());
                                                textUnformatted("Type: " + outPin.getDataType());
                                                textUnformatted("Value: " + outPin.getData().value);
                                                endTooltip();
                                            }
                                        }

                                        if (isItemClicked() && holdingPinID == -1) {
                                            lastActivePin = outPin.getID();
                                        }
//                                        addPin(outPin);
                                    }else{
                                        dummy(10, 10);
                                    }
                                    newLine();
                                }

//                                NodeEditor.group(50, 50);
                                headerMax = new ImVec2(getItemRectMax().x, headerMaxY);
                            }
                            NodeEditor.endNode();

                            if(isItemVisible()){
                                int alpha = (int) (getStyle().getAlpha() * 255);

                                ImDrawList drawList = NodeEditor.getNodeBackgroundDrawList(node.getID());
                                float halfBorderWidth = NodeEditor.getStyle().getNodeBorderWidth() * 0.5f;

                                //headerColor = 0;

                                float uvX = (headerMax.x - headerMin.x) / (4.0f * texture.width);
                                float uvY = (headerMax.y - headerMin.y) / (4.0f * texture.height);

                                if ((headerMax.x > headerMin.x) && (headerMax.y > headerMin.y))
                                {
                                    drawList.addImageRounded(texture.ID, headerMin.x - (8 - halfBorderWidth), headerMin.y - (4 - halfBorderWidth), headerMax.x + (8 - halfBorderWidth), headerMax.y + (0), 0, 0, uvX, uvY, rgbToInt(node.getRed(), node.getGreen(), node.getBlue(), node.getAlpha()), NodeEditor.getStyle().getNodeRounding() , ImDrawFlags.RoundCornersTop);
                                }

                                ImVec2 headerSeparatorMin = new ImVec2(headerMin.x, headerMin.y);
                                ImVec2 headerSeparatorMax = new ImVec2(headerMax.x, headerMax.y);

                                if ((headerSeparatorMax.x > headerSeparatorMin.x) && (headerSeparatorMax.y > headerSeparatorMin.y))
                                {
                                    drawList.addLine(headerMin.x - 8 - halfBorderWidth, headerMax.y, headerMax.x + (8 - halfBorderWidth), headerMax.y, rgbToInt(node.getRed(), node.getGreen(), node.getBlue(), 96 * alpha / (3 * 255)), 1);
                                }

                            }
//                            ImRect rect = new ImRect(getItemRectMin(), getItemRectMax());
//                            getWindowDrawList().addRectFilled(rect.min.x, rect.min.y, rect.max.x, rect.max.y, TestNodeEditor.rgbToInt(0, 0, 255));
                            if(node.width == -1) {
                                node.width = NodeEditor.getNodeSizeX(node.getID());
                            }

                            //calculate connected pins values
                            for (int i = 0; i < node.outputPins.size(); i++) {
                                Pin pin = node.outputPins.get(i);

                                if (pin.connectedTo != -1) {
                                    //find the input pin that is connected to this output pin
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
                                    float[] pincolor = getPinColor(pin);
                                    NodeEditor.link(uniqueLinkId++, pin.getID(), pin.connectedTo, pincolor[0], pincolor[1], pincolor[2], pincolor[3], 1);
                                }
                            }
                        }
                    }
                    ImVec2 previewRect = getItemRectMax();
//                    windowFocused = isWindowHovered();

                    if(NodeEditor.beginCreate()) {
                        holdingPinID = lastActivePin;
                        curSelectedPinDataType = graph.findPinById((int) lastActivePin).getDataType();
                        checkPinConnections();
                    }else{
                        //Open context menu if pin link dropped without connecting to another pin
                        if(holdingPinID != -1 && !(LINKA.get() != 0 || LINKB.get() != 0)){
                            holdingPinID = -1;
                            curSelectedPinDataType = null;
//                            System.out.println(LINKA.get() + " : " + LINKB.get());
                            setNextWindowPos(cursorPos.x, cursorPos.y, ImGuiCond.Always);
                            openPopup("context_menu" + id);
                        }
                        LINKA.set(0);
                        LINKB.set(0);
                    }
                    NodeEditor.endCreate();

                    if(NodeEditor.beginDelete()){
                        ImLong link1 = new ImLong();
                        ImLong link2 = new ImLong();
                        ImLong link3 = new ImLong();
                        if(NodeEditor.queryDeletedLink(link1, link2, link3)){
                            Pin pin1 = graph.findPinById((int) link2.get());
                            Pin pin2 = graph.findPinById((int) link3.get());

                            pin1.connectedTo = -1;
                            pin2.connectedTo = -1;
                        }
                    }
                    NodeEditor.endDelete();

                    NodeEditor.suspend();

                    ImVec2 nodeSpawnPos = getMousePos();

                    if(nodeNavigateTo != -1){
                        NodeEditor.selectNode(nodeNavigateTo, false);
                        NodeEditor.navigateToSelection(false, 1.5f);
                        nodeNavigateTo = -1;
                    }

                    final long nodeWithContextMenu = NodeEditor.getNodeWithContextMenu();
                    if(nodeWithContextMenu != -1){
                        openPopup("node_menu" + id);
                        getStateStorage().setInt(getID("delete_node_id"), (int)nodeWithContextMenu);
                    }

                    if(isPopupOpen("node_menu" + id)){
                        final int targetNode = getStateStorage().getInt(getID("delete_node_id"));
                        if(beginPopup("node_menu" + id)){
                            if(menuItem("Duplicate " + graph.getNodes().get(targetNode).getName())){
                                Node newInstance = null;
                                try {
                                    newInstance = graph.getNodes().get(targetNode).getClass().getDeclaredConstructor(Graph.class).newInstance(graph);
                                    graph.addNode(newInstance);
                                    newInstance.init();
                                    nodeQPos.put(newInstance.getID(), new ImVec2());
                                    NodeEditor.setNodePosition(newInstance.getID(), NodeEditor.toCanvasX(getCursorScreenPosX()), NodeEditor.toCanvasY(getCursorScreenPosY()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                closeCurrentPopup();
                            }

                            separator();
                            if(menuItem("Delete " + graph.getNodes().get(targetNode).getName()))
                            {
                                graph.removeNode(targetNode);
                                closeCurrentPopup();
                            }

                            if(menuItem("Preview Source")){
                                currentNodeSourceID = targetNode;
                                openSourcePreview = 1;
                            }
                        }
                        endPopup();
                    }

                    if(openSourcePreview == 1){
                        openSourcePreview = 0;
                        openPopup("PreviewSource");
                    }

                    if(isPopupOpen("PreviewSource")) {
                        if (beginPopupModal("PreviewSource", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoResize)) {
                            StringBuilder sb = new StringBuilder();
                            graph.getNodes().get(currentNodeSourceID).printSource(sb);
                            text(sb.toString());

                            if (button("Close")) {
                                closeCurrentPopup();
                            }
                            endPopup();
//                        System.out.println(sb);
                        }
                    }

                    final long linkWithContextMenu = NodeEditor.getLinkWithContextMenu();
                    if(linkWithContextMenu != -1){
                        openPopup("link_menu" + id);
                        getStateStorage().setInt(getID("delete_link_id"), (int)linkWithContextMenu);
                    }

                    if(isPopupOpen("link_menu" + id)){
                        final int targetLink = getStateStorage().getInt(getID("delete_link_id"));
                        if(beginPopup("link_menu" + id)){
                            if(menuItem("Delete Link")){
                                System.out.println(targetLink);
                                if(NodeEditor.deleteLink(targetLink)){
                                    System.out.println("Deleted link");
                                }
                                closeCurrentPopup();
                            }
                        }
                        endPopup();
                    }

                    if(NodeEditor.showBackgroundContextMenu()){
                        openPopup("context_menu" + id);
                    }

                    if(isPopupOpen("context_menu" + id)){
                        if(beginPopup("context_menu" + id)) {
                            ImVec2 newNodePosition = nodeSpawnPos;
                            //get all loaded nodes and show them in the right click menu
                            if (nodeInstanceCache.isEmpty()){
                                //Create new instances from nodeList and store for later use
                                //this stops the constant spawning of new instances
                                for (Class<? extends Node> node : nodeList) {
                                    Constructor<? extends Node> nodeClass = null;
                                    Node instance = null;
                                    try {
                                        nodeClass = node.getDeclaredConstructor(Graph.class);
                                        instance = nodeClass.newInstance(graph);
                                        nodeInstanceCache.add(instance);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    createContextMenuItem(instance, 0);
                                }
                            }else{
                                for(Node instance : nodeInstanceCache){
                                    createContextMenuItem(instance, 0);
                                }
                            }
                            endPopup();
                        }
                    }

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

        //Navigate to default node when opening the Graph Window
        if(firstFrame){
                if(graph.getNodes().values().size() > 0){
                    nodeNavigateTo = 0;
                }
            firstFrame = false;
        }


        //set nodes position to the current graphs pos
//        for(Integer id : nodeQPos.keySet()){
//            ImVec2 pos = nodeQPos.get(id);
//            getNodeEditorSpacePos(id, pos);
//            setNodeGridSpacePos(id, -pos.x + 300, -pos.y + 300);
//
//        }
//        nodeQPos.clear();


//        if(isMouseDown(ImGuiMouseButton.Left)){
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

    private float[] getPinColor(Pin pin){
        float[] color = new float[4];
        switch (pin.getDataType()) {
            case Flow:
                color[0] = 1;
                color[1] = 1;
                color[2] = 1;
                color[3] = 1;
                break;
            case Bool:
                color[0] = 1;
                color[1] = 1;
                color[2] = 0.196078431f;
                color[3] = 1;
                break;
            case Int:
                color[0] = 0.705882353f;
                color[1] = 0.298039216f;
                color[2] = 0.262745098f;
                color[3] = 1;
                break;
            case Float:
                color[0] = 0.65098039215f;
                color[1] = 0.36862745098f;
                color[2] = 0.18039215686f;
                color[3] = 1;
                break;
            case Double:
                color[0] = 0.19215686274f;
                color[1] = 0.4f;
                color[2] = 0.31372549019f;
                color[3] = 1;
                break;
            case String:
                color[0] = 0.96078431372f;
                color[1] = 0.25098039215f;
                color[2] = 0.1294117647f;
                color[3] = 1;
                break;
            default:
                color[0] = 0.196078431f;
                color[1] = 1;
                color[2] = 0.196078431f;
                color[3] = 1;
                break;
        }

        return color;
    }

    private void createContextMenuItem(Node instance, int depth) {
        if(instance.getCategory() != null) {
            String[] cats = instance.getCategory().split("\\.");
//            for (int i = 0; i < cats.length; i++) {
//                System.out.println(cats[i]);
                if (beginMenu(cats[depth])) {
                    if(depth + 1 < cats.length) {
                        createContextMenuItem(instance, depth + 1);
                    }else{
                        if (menuItem(instance.getName())) {
                            try {
                                Node newInstance = instance.getClass().getDeclaredConstructor(Graph.class).newInstance(graph);
                                graph.addNode(newInstance);
                                newInstance.init();
                                nodeQPos.put(newInstance.getID(), new ImVec2());
                                NodeEditor.setNodePosition(newInstance.getID(), NodeEditor.toCanvasX(getCursorScreenPosX()), NodeEditor.toCanvasY(getCursorScreenPosY()));
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                            closeCurrentPopup();
                        }
                    }
                    endMenu();
                }
//            }
        }else {
            if (menuItem(instance.getName())) {
                try {
                    Node newInstance = instance.getClass().getDeclaredConstructor(Graph.class).newInstance(graph);
                    graph.addNode(newInstance);
                    newInstance.init();
                    nodeQPos.put(newInstance.getID(), new ImVec2());
                    NodeEditor.setNodePosition(newInstance.getID(), NodeEditor.toCanvasX(getCursorScreenPosX()), NodeEditor.toCanvasY(getCursorScreenPosY()));
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                closeCurrentPopup();
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
     * sets the shape of the pin type and the color
     */
    private void drawPinShapeAndColor(Pin pin){
        float size = 10f;
        float posX = getCursorPosX();
        float posY = getCursorPosY();
        switch (pin.getDataType()){
            case Flow:
                int flowGrey = (curSelectedPinDataType != Pin.DataType.Flow && curSelectedPinDataType != null) ? rgbToInt(50, 50, 50, 255) : rgbToInt(255, 255, 255, 255);
                if(pin.connectedTo != -1) {
                    getWindowDrawList().addTriangleFilled(posX + 2.5f, posY, posX + 2.5f, posY + size, posX + (size / 2) + 2.5f, posY + (size / 2), flowGrey);
                }else {
                    getWindowDrawList().addTriangle(posX + 2.5f, posY, posX + 2.5f, posY + size, posX + (size / 2) + 2.5f, posY + (size / 2), flowGrey);
                }
                break;
            case Bool:
                int boolGrey = (curSelectedPinDataType != Pin.DataType.Bool && curSelectedPinDataType != null) ? rgbToInt(50, 50, 50, 255) : rgbToInt(255, 255, 50, 255);
                if(pin.connectedTo != -1) {
                    getWindowDrawList().addCircleFilled(posX + (size / 2), posY + (size / 2), size / 2, boolGrey);
                }else{
                    getWindowDrawList().addCircle(posX + (size / 2), posY + (size / 2), size / 2, boolGrey);
                }
                break;
            case Int:
                int intGrey = (curSelectedPinDataType != Pin.DataType.Int && curSelectedPinDataType != null) ? rgbToInt(50, 50, 50, 255) : rgbToInt(180, 76, 67, 255);
                if(pin.connectedTo != -1) {
                    getWindowDrawList().addCircleFilled(posX + (size / 2), posY + (size / 2), size / 2, intGrey);
                }else{
                    getWindowDrawList().addCircle(posX + (size / 2), posY + (size / 2), size / 2, intGrey);
                }
                break;
            case Float:
                int floatGrey = (curSelectedPinDataType != Pin.DataType.Float && curSelectedPinDataType != null) ? rgbToInt(50, 50, 50, 255) : rgbToInt(166, 94, 46, 255);;
                if(pin.connectedTo != -1) {
                    getWindowDrawList().addCircleFilled(posX + (size / 2), posY + (size / 2), size / 2, floatGrey);
                }else{
                    getWindowDrawList().addCircle(posX + (size / 2), posY + (size / 2), size / 2, floatGrey);
                }
                break;
            case Double:
                int doubleGrey = (curSelectedPinDataType != Pin.DataType.Double && curSelectedPinDataType != null) ? rgbToInt(50, 50, 50, 255) : rgbToInt(49, 102, 80, 255);
                if(pin.connectedTo != -1) {
                    getWindowDrawList().addCircleFilled(posX + (size / 2), posY + (size / 2), size / 2, doubleGrey);
                }else{
                    getWindowDrawList().addCircle(posX + (size / 2), posY + (size / 2), size / 2, doubleGrey);
                }
                break;
            case String:
                int stringGrey = (curSelectedPinDataType != Pin.DataType.String && curSelectedPinDataType != null) ? rgbToInt(50, 50, 50, 255) : rgbToInt(245, 64, 33, 255);
                if(pin.connectedTo != -1) {
                    getWindowDrawList().addCircleFilled(posX + (size / 2), posY + (size / 2), size / 2, stringGrey);
                }else{
                    getWindowDrawList().addCircle(posX + (size / 2), posY + (size / 2), size / 2, stringGrey);
                }
                break;
            default:
                int defaultGrey = (curSelectedPinDataType != Pin.DataType.Flow && curSelectedPinDataType != null) ? rgbToInt(50, 50, 50, 255) : rgbToInt(50, 255, 50, 255);
                if(pin.connectedTo != -1) {
                    getWindowDrawList().addCircleFilled(posX + (size / 2), posY + (size / 2), size / 2, defaultGrey);
                }else{
                    getWindowDrawList().addCircle(posX + (size / 2), posY + (size / 2), size / 2, defaultGrey);
                }
                break;
        }
    }

    /**
     * checks if 2 pins should be connected and checks if the pin should be disconnected
     */
    private void checkPinConnections(){
        if(NodeEditor.queryNewLink(LINKA, LINKB)) {
            final Pin sourcePin = graph.findPinById((int) LINKA.get());
            final Pin targetPin = graph.findPinById((int) LINKB.get());
            //ignore connection attempts to self
            if(sourcePin.getNode() == targetPin.getNode()){
                return;
            }

            if (!(sourcePin.getDataType() == targetPin.getDataType()) || !(sourcePin.getPinType() != targetPin.getPinType())) {
                NodeEditor.rejectNewItem(1, 0, 0, 1, 1);
                holdingPinID = -1;
                curSelectedPinDataType = null;
                return;
            }

//            if (!(sourcePin.getDataType() == targetPin.getDataType())) {
//                System.out.println("Types are not the same");
////                NodeEditor.pushStyleColor(NodeEditorStyleColor.Flow, 1, 0, 0, 1);
//            } else {
////                NodeEditor.pushStyleColor(NodeEditorStyleColor.Flow, 1, 1, 0, 1);
//            }
            if (NodeEditor.acceptNewItem(0, 1, 0, 1, 1)) {
//                if (!(sourcePin.getDataType() == targetPin.getDataType())) {
//                    System.out.println("Types are not the same");
//                } else {
                    if (sourcePin.connectedTo != -1) {
                        Pin oldPin = graph.findPinById(sourcePin.connectedTo);
                        oldPin.connectedTo = -1;
                    }

                    if (targetPin.connectedTo != -1) {
                        Pin oldPin = graph.findPinById(targetPin.connectedTo);
                        oldPin.connectedTo = -1;
                    }

                    if (sourcePin != null && targetPin != null) {
                        if (sourcePin.connectedTo != targetPin.connectedTo || (targetPin.connectedTo == -1 || sourcePin.connectedTo == -1)) {
                            sourcePin.connectedTo = targetPin.getID();
                            targetPin.connectedTo = sourcePin.getID();
                            holdingPinID = -1;
                            curSelectedPinDataType = null;
                        }
                    }
//                }
            }
        }

//        if(isLinkDropped(LINK_A, false)) {
//
//            Pin pin1 = graph.findPinById(LINK_A.get());
//            if (pin1.connectedTo != -1) {
//                Pin pin2 = graph.findPinById(pin1.connectedTo);
//                pin1.connectedTo = -1;
//                pin2.connectedTo = -1;
//            }
//        }
    }

    private void addPin(Pin pin){
        switch (pin.getPinType()){
            case Input:
                switch (pin.getDataType()){
                    case Flow:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 255, 255)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.Triangle);
//                        ImNodes.popColorStyle();
                        break;
                    case Bool:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 50, 50)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
//                        ImNodes.popColorStyle();
                        break;
                    case Int:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(80, 50, 200)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
//                        ImNodes.popColorStyle();
                        break;
                    case Float:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(5, 50, 190)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
//                        ImNodes.popColorStyle();
                        break;
                    case Double:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 250, 190)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
//                        ImNodes.popColorStyle();
                        break;
                    case String:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 50, 100)));
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
//                        ImNodes.popColorStyle();
                        break;
                    default:
//                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                }
                pushItemWidth(250);
                configurePinUI(pin);
                popItemWidth();
//                endOutputAttribute();
                sameLine();
                break;
            case Output:
                switch (pin.getDataType()){
                    case Flow:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 255, 255)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.Triangle);
//                        ImNodes.popColorStyle();
                        break;
                    case Bool:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(255, 50, 50)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
//                        ImNodes.popColorStyle();
                        break;
                    case Int:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(80, 50, 200)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
//                        ImNodes.popColorStyle();
                        break;
                    case Float:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(5, 50, 190)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
//                        ImNodes.popColorStyle();
                        break;
                    case Double:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 250, 190)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
//                        ImNodes.popColorStyle();
                        break;
                    case String:
//                        ImNodes.pushColorStyle(ImNodesColorStyle.Pin, ToNodeColor(new NodeColor(205, 50, 100)));
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
//                        ImNodes.popColorStyle();
                        break;
                    default:
//                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                }
//                sameLine(curNodeSize / 2);
                sameLine();
//                newLine();
//                configurePinType(pin);
                text(pin.getName());
//                endOutputAttribute();
                sameLine();
                break;
        }
    }

    /**
     * adds input fields to the Pin Type
     */
    private void configurePinUI(Pin pin) {
        pushItemWidth(150);
        switch (pin.getDataType()){
            case Flow:
                break;
            case Bool:
                if(checkbox("##" + pin.getID(), pin.getBoolean())){

                }
                break;
            case Int:
                if(inputInt("##" + pin.getID(), pin.getInt())){

                }
                break;
            case Float:
                if(inputFloat("##" + pin.getID(), pin.getFloat())){

                }
                break;
            case Double:
                if(inputDouble("##" + pin.getID(), pin.getDouble())){

                }
                break;
            case String:
                if(inputText("##" + pin.getID(), pin.getString())){
                }
                break;
        }
        popItemWidth();
    }

    private int rgbToInt(int r, int g, int b, int a){
//        int Red = (r << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
//        int Green = (g << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
//        int Blue = b & 0x000000FF; //Mask out anything not blue.

        return ImColor.intToColor(r, g, b, a); // 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }
}
