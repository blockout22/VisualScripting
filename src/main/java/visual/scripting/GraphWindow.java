package visual.scripting;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import imgui.extension.nodeditor.flag.NodeEditorStyleColor;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.extension.texteditor.TextEditor;
import imgui.flag.*;
import imgui.type.*;
import org.lwjgl.opengl.GL11;
import visual.scripting.node.*;
import visual.scripting.pin.Pin;
import visual.scripting.ui.Button;
import visual.scripting.ui.ConfirmSaveDialog;
import visual.scripting.ui.ListView;
import visual.scripting.ui.UiComponent;
import visual.scripting.ui.listeners.LeftClickListener;
import visual.scripting.ui.listeners.HoverListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static imgui.ImGui.*;
import static org.lwjgl.glfw.GLFW.*;

public class GraphWindow {

    private boolean requiresSave = false;
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
    private long lastHoldingPinID = -1;
    private long lastActivePin = -1;
    private ImVec2 cursorPos;

    private ImString nodeSearch = new ImString();

//    private Pin.DataType curSelectedPinDataType = null;
    private Pin curSelectedPinDataType = null;

    private int openSourcePreview = 0;
    private int currentNodeSourceID = -1;
    private float h = 0f;

    private Texture texture;

    private boolean justLoadedFromFile = false;
    private boolean justOpenedContextMenu = false;

    private ConfirmSaveDialog saveDialog = new ConfirmSaveDialog();

    //UiComponents
    private Button convertAndSaveBtn = new Button("Save & Convert");
    private Button clearGraphBtn = new Button("Clear Graph");
    private Button showFlowBtn = new Button("Show Flow");

    private ListView nodeListView = new ListView("Node List");

    protected GraphWindow(){
        graph = new Graph("");
    }

    public GraphWindow(ImGuiWindow window, String filename, Graph graph){
        this.window = window;
        this.id = filename;
        this.graph = graph;
        justLoadedFromFile = true;
        init();
    }

    public GraphWindow(ImGuiWindow window, String fileName, String language){
        this.window = window;
        this.id = fileName;
        graph = new Graph(language);
//        graph.addNode(new NodeEntry(graph));
        init();
    }

    private void init(){
        NodeEditorConfig config = new NodeEditorConfig();
        config.setSettingsFile(null);
        context = new NodeEditorContext(config);

        //add built-in nodes to the list
        addNodeToList(NodeSplitFlow.class);
        addNodeToList(NodeEntry.class);
        addNodeToList(NodeVisualTest.class);
        addNodeToList(NodeTime.class);
        addNodeToList(NodeLongToFloat.class);
        addNodeToList(NodeReroute.class);
        //add a starter node to the graph

        addNodeToList(NodeVariable.class);

        for(VisualScriptingPlugin plugin : ImGuiWindow.pluginManager.getExtensions(VisualScriptingPlugin.class)){
            plugin.init(this);
        }

        //LINKA.get();

        try {
            texture = TextureLoader.loadTexture("white.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        convertAndSaveBtn.addLeftClickListener(new LeftClickListener() {
            @Override
            public void onClicked() {
                save();
            }
        });

        convertAndSaveBtn.addHoverListener(new HoverListener() {
            @Override
            public void onHovered() {
                setNextWindowPos(getMousePosOnOpeningCurrentPopupX(), getMousePosOnOpeningCurrentPopupY() + 10, ImGuiCond.Always);
                beginTooltip();
                textUnformatted("Saves & converts nodes to source code");
                endTooltip();
            }
        });

        clearGraphBtn.addLeftClickListener(new LeftClickListener() {
            @Override
            public void onClicked() {
                graph.getNodes().clear();
            }
        });

        showFlowBtn.addLeftClickListener(() -> {
            int uniqueLinkId = 1;
            for (Node node : graph.getNodes().values()) {
                for (Pin pin : node.outputPins) {
                    if (pin.connectedTo != -1) {
                        NodeEditor.flow(uniqueLinkId++);
                    }
                }
            }
        });

        nodeListView.addLeftClickListener(() -> {
            System.out.println(nodeListView.getSelectedItem());
        });
    }


    private void save(){
        //used to convert the nodes to source text
        //Converts to Source
        String text = nodeCompiler.compile(graph);
        File file = new File(ImGuiWindow.workingDir.getAbsolutePath().toString() + File.separator + id + "." + graph.getLanguage());
        System.out.println(file);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(file));
            br.write(text);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EDITOR.setText(text);
        //Saves Node Graph information (nodes, nodes positions, node links etc...)
        GraphSaver.save(id + "." + graph.getLanguage(), graph);
        requiresSave = false;
    }

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
                if(requiresSave){
                    int state = saveDialog.show();
                    if(state == 0) {
                        System.out.println("Don't save");
                        requiresSave = false;

                        GL11.glDeleteTextures(texture.ID);
                        window.removeGraphWindow(this);
                    }else if (state == 1){
                        System.out.println("Save");
                        save();
                        GL11.glDeleteTextures(texture.ID);
                        window.removeGraphWindow(this);
                    }
                }else {
                    GL11.glDeleteTextures(texture.ID);
                    window.removeGraphWindow(this);
                }
            }

            convertAndSaveBtn.show();
            //clears all nodes from the graph and resets the graph
            clearGraphBtn.show();
            showFlowBtn.show();

            if(beginTabBar("TabBar")) {
                if(beginTabItem("NodeEditor")) {
//                    if(beginChild("SideBar", 100, 350))
                    if(beginTabBar("ContentTabBar")) {
                        if (beginTabItem("ContentArea")) {
                            beginGroup();
                            {
//                                nodeListView.show();
                                beginGroup();
                                dummy(200, h);
                                {
                                    text("Node List");
                                    for (Node node : graph.getNodes().values()) {
                                        pushID(node.getID());
                                        if (button(node.getName())) {
                                            nodeNavigateTo = node.getID();
                                        }
                                        popID();
                                    }
                                }
                                endGroup();
                                invisibleButton("invisButonToMoveLikeSplitter", 200, 50);
                                if (isItemActive()) {
                                    h += getIO().getMouseDeltaY();
                                }

                                beginGroup();
                                text("Variable List");
                                for (int i = 0; i < 10; i++) {
                                    button("placeholder var: " + i);
                                }
                                endGroup();
                            }
                            endGroup();
                        }
                        endTabItem();
                    }
                    endTabBar();

                    sameLine();

                    NodeEditor.setCurrentEditor(context);
                    NodeEditor.getStyle().setNodeRounding(2.0f);
//                    NodeEditor.pushStyleColor(NodeEditorStyleColor.LinkSelRect, 255, 0, 0, 255);
//                    TestNodeEditor.nodeStyleEditor();

                    ImVec2 headerMin = null;
                    ImVec2 headerMax = null;

                    float headerMaxY = 0;
                    beginGroup();
                    {
                        NodeEditor.begin("Editor");
                        {
                            NodeEditor.pushStyleColor(NodeEditorStyleColor.Flow, .5f, .5f, .5f, 1);
                            NodeEditor.pushStyleColor(NodeEditorStyleColor.FlowMarker, 1, 1, 1, 1);
                            //check if loaded from save file
                            if (justLoadedFromFile) {
                                System.out.println("Setting node positions from loaded file");
                                for (Node node : graph.getNodes().values()) {
                                    NodeEditor.setNodePosition(node.getID(), node.getLoadedPosition().x, node.getLoadedPosition().y);
                                }
                                justLoadedFromFile = false;
                            }

                            for (Node node : graph.getNodes().values()) {
                                float maxWidth = 0;
//                            headerMin = new ImVec2();
//                            headerMax = new ImVec2();
                                NodeEditor.pushStyleVar(NodeEditorStyleVar.NodePadding, 8, 4, 8, 8);
                                NodeEditor.beginNode(node.getID());
                                {
//                                if(button("NextId")){
////                                    colID++;
//                                }

                                    if(node.hasTitleBar()) {
                                        text(node.getName());
                                        headerMin = getItemRectMin();
//                                dummy(getItemRectMax().x, 2);
                                        headerMaxY = getItemRectMax().y;
                                        newLine();
                                    }else{
//                                        dummy(10, 10);
                                    }

                                    for (UiComponent uiComponent : node.uiComponents) {
                                        uiComponent.show();
                                    }

                                    //add node pins
                                    int max = Math.max(node.outputPins.size(), node.inputPins.size());
                                    for (int i = 0; i < max; i++) {

                                        if (node.inputPins.size() > i) {
                                            Pin inPin = node.inputPins.get(i);
//                                        addPin(inPin);
                                            NodeEditor.beginPin(inPin.getID(), NodeEditorPinKind.Input);//
                                            drawPinShapeAndColor(inPin);
                                            dummy(10, 10);

                                            if(inPin.getName().length() > 0) {
                                                sameLine();
                                                text(inPin.getName());
                                            }

                                            NodeEditor.pinPivotAlignment(0f, .5f);
                                            NodeEditor.endPin();

                                            if (isItemClicked() && holdingPinID == -1) {
                                                lastActivePin = inPin.getID();
                                            }

                                            if (isItemHovered()) {
                                                setNextWindowPos(NodeEditor.toScreenX(getMousePosX()), NodeEditor.toScreenY(getMousePosY() + 10), ImGuiCond.Always);
                                                beginTooltip();
                                                textUnformatted(inPin.getName());
                                                if(inPin.getData() != null) {
//                                                    textUnformatted("Type: " + inPin.getDataType());
                                                    textUnformatted("Value: " + inPin.getData().value);
                                                }
                                                endTooltip();
                                            }
                                            sameLine();
                                            beginGroup();
                                            configurePinUI(inPin);
                                            endGroup();
                                        }

                                        if (node.width != -1) {
                                            if(!node.hasTitleBar()){
                                                sameLine(1);
                                            }else {
                                                sameLine(node.width - 10);
                                            }
                                        }

                                        if (node.outputPins.size() > i) {
                                            Pin outPin = node.outputPins.get(i);
//                                            if (outPin.getDataType() == Pin.DataType.SPACER) {
//                                                NodeEditor.beginPin(outPin.getID(), NodeEditorPinKind.Output);
//                                                dummy(1f, 1f);
//                                                NodeEditor.endPin();
//                                            } else
                                            {
//                                                beginGroup();
////                                                configurePinUI(outPin);
//                                                text(outPin.getName());
//                                                endGroup();
//                                                sameLine();

                                                if(!node.hasTitleBar()) {
//                                                beginGroup();
                                                    text(outPin.getName());
//                                                endGroup();
                                                    sameLine();
                                                }

                                                NodeEditor.beginPin(outPin.getID(), NodeEditorPinKind.Output);

                                                if(outPin.getName().length() > 0) {
                                                    textUnformatted(outPin.getName());
                                                    sameLine();
                                                }

                                                drawPinShapeAndColor(outPin);
                                                dummy(10, 10);
                                                NodeEditor.pinPivotAlignment(1f, .5f);
//                                                sameLine();
                                                ImVec2 pos = getCursorPos();
                                                NodeEditor.endPin();

                                                if (isItemHovered()) {
                                                    setNextWindowPos(NodeEditor.toScreenX(getMousePosX()), NodeEditor.toScreenY(getMousePosY() + 10), ImGuiCond.Always);
                                                    beginTooltip();
                                                    textUnformatted(outPin.getName());
                                                    if(outPin.getData() != null) {
//                                                        textUnformatted("Type: " + outPin.getDataType());
                                                        textUnformatted("Value: " + outPin.getData().value);
                                                    }
                                                    endTooltip();
                                                }
                                            }

                                            if (isItemClicked() && holdingPinID == -1) {
                                                lastActivePin = outPin.getID();
                                            }

//                                        addPin(outPin);
                                        } else {
                                            dummy(10, 10);
                                        }
                                        if(node.hasTitleBar()) {
                                            newLine();
                                        }

                                        if(maxWidth < getItemRectMax().x){
                                            maxWidth = getItemRectMax().x;
                                        }
                                    }

//                                NodeEditor.group(50, 50);


                                    headerMax = new ImVec2(NodeEditor.getNodePositionX(node.getID()) < 0 ? getItemRectMax().x : maxWidth, headerMaxY);


                                    if(node.getError().length() > 0) {
                                        pushStyleColor(ImGuiCol.Text, ImColor.intToColor(255, 0, 0, 255));
                                        text(node.getError());
                                        popStyleColor();
                                    }
                                }
                                NodeEditor.endNode();

                                //handle node movement with arrow keys
                                //TODO figure out how to consume input
                                if(isKeyPressed(GLFW_KEY_UP)){
                                    int size = 10;
                                    long[] selectedNode = new long[size];
                                    NodeEditor.getSelectedNodes(selectedNode, size);
                                    for(long id : selectedNode){
                                        if(id != 0){
                                            NodeEditor.setNodePosition(id, NodeEditor.getNodePositionX(id), NodeEditor.getNodePositionY(id) - 1);
                                        }
                                    }
                                }

                                if(isKeyPressed(GLFW_KEY_DOWN)){
                                    int size = 10;
                                    long[] selectedNode = new long[size];
                                    NodeEditor.getSelectedNodes(selectedNode, size);
                                    for(long id : selectedNode){
                                        if(id != 0){
                                            NodeEditor.setNodePosition(id, NodeEditor.getNodePositionX(id), NodeEditor.getNodePositionY(id) + 1);
                                        }
                                    }
                                }

                                if(isKeyPressed(GLFW_KEY_LEFT)){
                                    int size = 10;
                                    long[] selectedNode = new long[size];
                                    NodeEditor.getSelectedNodes(selectedNode, size);
                                    for(long id : selectedNode){
                                        if(id != 0){
                                            NodeEditor.setNodePosition(id, NodeEditor.getNodePositionX(id) - 1, NodeEditor.getNodePositionY(id));
                                        }
                                    }
                                }

                                if(isKeyPressed(GLFW_KEY_RIGHT)){
                                    int size = 10;
                                    long[] selectedNode = new long[size];
                                    NodeEditor.getSelectedNodes(selectedNode, size);
                                    for(long id : selectedNode){
                                        if(id != 0){
                                            NodeEditor.setNodePosition(id, NodeEditor.getNodePositionX(id) + 1, NodeEditor.getNodePositionY(id));
                                        }
                                    }
                                }

                                if (isItemVisible() && node.hasTitleBar()) {
                                    int alpha = (int) (getStyle().getAlpha() * 255);

                                    ImDrawList drawList = NodeEditor.getNodeBackgroundDrawList(node.getID());
                                    float halfBorderWidth = NodeEditor.getStyle().getNodeBorderWidth() * 0.5f;

                                    //headerColor = 0;

                                    float uvX = (headerMax.x - headerMin.x) / (4.0f * texture.width);
                                    float uvY = (headerMax.y - headerMin.y) / (4.0f * texture.height);

                                    if ((headerMax.x > headerMin.x) && (headerMax.y > headerMin.y)) {
                                        drawList.addImageRounded(texture.ID, headerMin.x - (8 - halfBorderWidth), headerMin.y - (4 - halfBorderWidth), headerMax.x + (8 - halfBorderWidth), headerMax.y + (0), 0, 0, uvX, uvY, rgbToInt(node.getRed(), node.getGreen(), node.getBlue(), node.getAlpha()), NodeEditor.getStyle().getNodeRounding(), ImDrawFlags.RoundCornersTop);
                                    }

                                    ImVec2 headerSeparatorMin = new ImVec2(headerMin.x, headerMin.y);
                                    ImVec2 headerSeparatorMax = new ImVec2(headerMax.x, headerMax.y);

                                    if ((headerSeparatorMax.x > headerSeparatorMin.x) && (headerSeparatorMax.y > headerSeparatorMin.y)) {
                                        drawList.addLine(headerMin.x - 8 - halfBorderWidth, headerMax.y, headerMax.x + (8 - halfBorderWidth), headerMax.y, rgbToInt(node.getRed(), node.getGreen(), node.getBlue(), 96 * alpha / (3 * 255)), 1);
                                    }

                                }
//                            ImRect rect = new ImRect(getItemRectMin(), getItemRectMax());
//                            getWindowDrawList().addRectFilled(rect.min.x, rect.min.y, rect.max.x, rect.max.y, TestNodeEditor.rgbToInt(0, 0, 255));
                                if (node.width == -1) {
                                    node.width = NodeEditor.getNodeSizeX(node.getID());
                                }

                                //calculate connected pins values
                                for (int i = 0; i < node.outputPins.size(); i++) {
                                    Pin pin = node.outputPins.get(i);

                                    if (pin.connectedTo != -1) {
                                        //find the input pin that is connected to this output pin
                                        Pin otherPin = graph.findPinById(pin.connectedTo);

                                        //TODO decide if we automatically set connected pin values or let the plugin handle it
                                        //
//                                        if(otherPin.getData() != null && pin.getData() != null) {
//                                            pin.getData().value = otherPin.getData().getValue();
//                                        }
                                    }
                                }

                                node.execute();
                            }

                            //link node pins together
                            int uniqueLinkId = 1;
                            for (Node node : graph.getNodes().values()) {
                                for (Pin pin : node.outputPins) {
                                    if (pin.connectedTo != -1) {
//                                        float[] pincolor = getPinColor(pin);
                                        NodeEditor.pushStyleVar(NodeEditorStyleVar.FlowMarkerDistance, 50);
                                        NodeEditor.pushStyleVar(NodeEditorStyleVar.FlowDuration, 1000);
                                        NodeEditor.pushStyleVar(NodeEditorStyleVar.FlowSpeed, 25);

                                        NodeEditor.link(uniqueLinkId++, pin.getID(), pin.connectedTo, pin.getColor().x, pin.getColor().y, pin.getColor().z, pin.getColor().w, 1);

                                        NodeEditor.popStyleVar(3);
                                    }
                                }
                            }
                        }
//                    ImVec2 previewRect = getItemRectMax();
//                    windowFocused = isWindowHovered();

                        if (NodeEditor.beginCreate()) {
                            holdingPinID = lastActivePin;
//                            curSelectedPinDataType = graph.findPinById((int) lastActivePin).getDataType();
                            curSelectedPinDataType = graph.findPinById((int) lastActivePin);
                            checkPinConnections();
                        } else {
                            //Open context menu if pin link dropped without connecting to another pin
                            if (holdingPinID != -1 && !(LINKA.get() != 0 || LINKB.get() != 0)) {
                                lastHoldingPinID = holdingPinID;
                                holdingPinID = -1;
                                curSelectedPinDataType = null;
//                            System.out.println(LINKA.get() + " : " + LINKB.get());
//                                setNextWindowPos(cursorPos.x, cursorPos.y, ImGuiCond.Always);
                                openPopup("context_menu" + id);
                                justOpenedContextMenu = true;
                            }
                            LINKA.set(0);
                            LINKB.set(0);
                        }
                        NodeEditor.endCreate();

                        if (NodeEditor.beginDelete()) {
                            int size = 10;
                            long[] list = new long[size];
                            NodeEditor.getSelectedNodes(list, size);
                            ImLong link1 = new ImLong();
                            ImLong link2 = new ImLong();
                            ImLong link3 = new ImLong();
                            if (NodeEditor.queryDeletedLink(link1, link2, link3)) {
                                Pin pin1 = graph.findPinById((int) link2.get());
                                Pin pin2 = graph.findPinById((int) link3.get());

                                pin1.connectedTo = -1;
                                pin2.connectedTo = -1;
                            }

                            ImLong nodeID = new ImLong();
                            if(NodeEditor.queryDeletedNode(nodeID)){
                                graph.removeNode((int)nodeID.get());
                            }
                        }
                        NodeEditor.endDelete();

                        NodeEditor.suspend();

                        ImVec2 nodeSpawnPos = getMousePos();

                        //Double-clicked on link
                        long doubleClickLinkID = NodeEditor.getDoubleClickedLink();
                        if(doubleClickLinkID != 0){
                            System.out.println(doubleClickLinkID);
                        }

                        if (nodeNavigateTo != -1) {
                            NodeEditor.selectNode(nodeNavigateTo, false);
                            NodeEditor.navigateToSelection(false, 1.5f);
                            nodeNavigateTo = -1;
                        }

                        final long pinWithContextMenu = NodeEditor.getPinWithContextMenu();
                        if (pinWithContextMenu != -1) {
                            openPopup("pin_menu" + id);
                            getStateStorage().setInt(getID("node_pin_id"), (int) pinWithContextMenu);
                        }

                        if (isPopupOpen("pin_menu" + id)) {
                            final int targetPin = getStateStorage().getInt(getID("node_pin_id"));
                            Pin pin = graph.findPinById(targetPin);
                            if (pin.isCanDelete()) {
                                if (beginPopup("pin_menu" + id)) {
                                    if (menuItem("Delete Pin")) {
                                        pin.getNode().removePinById(targetPin);
                                        closeCurrentPopup();
                                    }
                                }
                                endPopup();
                            }
                        }

                        final long nodeWithContextMenu = NodeEditor.getNodeWithContextMenu();
                        if (nodeWithContextMenu != -1) {
                            openPopup("node_menu" + id);
                            getStateStorage().setInt(getID("delete_node_id"), (int) nodeWithContextMenu);
                        }

                        if (isPopupOpen("node_menu" + id)) {
                            final int targetNode = getStateStorage().getInt(getID("delete_node_id"));
                            if (beginPopup("node_menu" + id)) {
                                if (menuItem("Duplicate " + graph.getNodes().get(targetNode).getName())) {
                                    Node newInstance = null;
                                    try {
                                        Node target = graph.getNodes().get(targetNode);
                                        newInstance = target.getClass().getDeclaredConstructor(Graph.class).newInstance(graph);
                                        graph.addNode(newInstance);
                                        newInstance.init();
                                        nodeQPos.put(newInstance.getID(), new ImVec2());
                                        NodeEditor.setNodePosition(newInstance.getID(), NodeEditor.toCanvasX(getCursorScreenPosX()), NodeEditor.toCanvasY(getCursorScreenPosY()));

                                        for (int i = 0; i < newInstance.inputPins.size(); i++) {
                                            Global.setPinValue(newInstance.inputPins.get(i), String.valueOf(target.inputPins.get(i).getData().value));
                                        }

                                        //output pins are usually set based on the input pins no need to duplicate
//                                    for (int i = 0; i < newInstance.outputPins.size(); i++) {
//                                      Global.setPinValue(newInstance.outputPins.get(i), String.valueOf(target.outputPins.get(i).getData().value));
//                                    }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    closeCurrentPopup();
                                }

                                separator();
                                if (menuItem("Delete " + graph.getNodes().get(targetNode).getName())) {
                                    graph.removeNode(targetNode);
                                    closeCurrentPopup();
                                }

                                if (menuItem("Preview Source")) {
                                    currentNodeSourceID = targetNode;
                                    openSourcePreview = 1;
                                }
                            }
                            endPopup();
                        }

                        if (openSourcePreview == 1) {
                            openSourcePreview = 0;
                            openPopup("PreviewSource");
                        }

                        if (isPopupOpen("PreviewSource")) {
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
                        if (linkWithContextMenu != -1) {
                            openPopup("link_menu" + id);
                            getStateStorage().setInt(getID("delete_link_id"), (int) linkWithContextMenu);
                        }

                        if (isPopupOpen("link_menu" + id)) {
                            final int targetLink = getStateStorage().getInt(getID("delete_link_id"));
                            if (beginPopup("link_menu" + id)) {
                                if (menuItem("Delete Link")) {
                                    System.out.println(targetLink);
                                    if (NodeEditor.deleteLink(targetLink)) {
                                        System.out.println("Deleted link");
                                    }
                                    closeCurrentPopup();
                                }
                            }
                            endPopup();
                        }

                        if (NodeEditor.showBackgroundContextMenu()) {
                            openPopup("context_menu" + id);
                            justOpenedContextMenu = true;
                        }

                        if (isPopupOpen("context_menu" + id)) {
                            if (beginPopup("context_menu" + id)) {
                                ImVec2 newNodePosition = nodeSpawnPos;
                                //get all loaded nodes and show them in the right click menu
                                if (nodeInstanceCache.isEmpty()) {
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
                                } else {
                                    if(justOpenedContextMenu) {
                                        setKeyboardFocusHere(0);
                                        justOpenedContextMenu = false;
                                    }

                                    inputTextWithHint("##", "Search node", nodeSearch);
                                    for (Node instance : nodeInstanceCache) {
                                        createContextMenuItem(instance, 0);
                                    }
                                }
                                endPopup();
                            }
                        }

                        //Popup handling for inside Graph WIP
//                        for(Popup popup : PopupHandler.openPopups){
                        Popup popup = PopupHandler.currentPopup;
                        if(popup != null){
                            setNextWindowPos(cursorPos.x, cursorPos.y, ImGuiCond.Appearing);
                            if(isPopupOpen(popup.id.toString())){

                                if(beginPopup(popup.id.toString())){
                                    //context here
                                    if(popup.show()){
                                        PopupHandler.remove(popup);
                                        closeCurrentPopup();
                                    };
                                }
                                endPopup();
                            }
                        }

                        NodeEditor.resume();
                        NodeEditor.end();
                    }
                    endGroup();

                    //TODO try to add panel on the right side of the graph
//                    sameLine();
//                    beginGroup();
//                    dummy(200, 200);
//                    {
//                        text("Other List");
//                        for (Node node : graph.getNodes().values()) {
//                            pushID(node.getID() + "1");
//                            if(button(node.getName())){
//                                nodeNavigateTo = node.getID();
//                            }
//                            popID();
//                        }
//                    }
//                    endGroup();

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
    }

//    private float[] getPinColor(Pin pin){
//        float[] color = new float[4];
//        //TODO set link colors here
//        color[0] = 1;
//        color[1] = 1;
//        color[2] = 1;
//        color[3] = 1;
//        return color;
//    }

    private void createContextMenuItem(Node instance, int depth) {
        if(nodeSearch.get().toLowerCase().length() > 0){
            if(!instance.getName().toLowerCase().contains(nodeSearch.get())){
                return;
            }
        }

        if(instance.getCategory() != null) {
            String[] languages = instance.getLanguages();
            boolean shouldAdd = false;
            for(String lang : languages){
                if(lang.equals(graph.getLanguage())){
                    shouldAdd = true;
                    break;
                }
            }

            if(languages.length <= 0){
                shouldAdd = true;
            }

            if(!shouldAdd){
                return;
            }

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
                                requiresSave = true;
                                autoConnectLink(newInstance);
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
                    requiresSave = true;
                    autoConnectLink(newInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                closeCurrentPopup();
            }
        }
    }

    private void autoConnectLink(Node newInstance){
        //check if context menu opened by dragging a pin
        if(lastHoldingPinID != -1){
            Pin pin = graph.findPinById((int)lastActivePin);
            System.out.println(pin.getPinType());
            switch (pin.getPinType()){
                case Input:
                    for (int i = 0; i < newInstance.outputPins.size(); i++) {
                        Pin instancePin = newInstance.outputPins.get(i);
//                        if(instancePin.getDataType() == pin.getDataType())
                        if(instancePin.getClass() == pin.getClass())
                        {
                            System.out.println("Found input");
                            //if a successful connection is made then return/break
                            if(pin.connect(instancePin)){
                                break;
                            }
                        }
                    }
                    break;
                case Output:
                    for (int i = 0; i < newInstance.inputPins.size(); i++) {
                        Pin instancePin = newInstance.inputPins.get(i);
//                        if(instancePin.getDataType() == pin.getDataType())
                        if(instancePin.getClass() == pin.getClass())
                        {
                            System.out.println("Found Output");
                            //if a successful connection is made then return/break
                            if(pin.connect(instancePin)){
                                break;
                            }
                        }
                    }
                    break;
            }
            lastHoldingPinID = -1;
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
        boolean pinDragSame = true;
        if(curSelectedPinDataType != null){
            pinDragSame = pin.getClass() == curSelectedPinDataType.getClass();
        }
        pin.draw(getWindowDrawList(), posX, posY, pin.connectedTo != -1, pinDragSame);
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


//            if (!(sourcePin.getDataType() == targetPin.getDataType()) || !(sourcePin.getPinType() != targetPin.getPinType())) {
            if(!(sourcePin.getClass() == targetPin.getClass()) || !(sourcePin.getPinType() != targetPin.getPinType())){
                NodeEditor.rejectNewItem(1, 0, 0, 1, 1);
                holdingPinID = -1;
//                curSelectedPinDataType = null;
                return;
            }

//            if (!(sourcePin.getDataType() == targetPin.getDataType())) {
//                System.out.println("Types are not the same");
////                NodeEditor.pushStyleColor(NodeEditorStyleColor.Flow, 1, 0, 0, 1);
//            } else {
////                NodeEditor.pushStyleColor(NodeEditorStyleColor.Flow, 1, 1, 0, 1);
//            }
            if (NodeEditor.acceptNewItem(0, 1, 0, 1, 1)) {
                requiresSave = true;
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

                    //Create a new Link connections
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
    }

    /**
     * adds input fields to the Pin Type
     */
    private void configurePinUI(Pin pin) {
        pushItemWidth(150);
        pin.UI();
        popItemWidth();
    }

    public String getId() {
        return id;
    }

    private int rgbToInt(int r, int g, int b, int a){
        return ImColor.intToColor(r, g, b, a);
    }
}
