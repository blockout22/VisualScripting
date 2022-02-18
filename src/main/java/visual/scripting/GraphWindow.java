package visual.scripting;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImVec2;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.NodeEditorStyle;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.extension.texteditor.TextEditor;
import imgui.flag.*;
import imgui.type.*;
import org.lwjgl.opengl.GL11;
import visual.scripting.node.*;
import visual.scripting.ui.Button;
import visual.scripting.ui.ConfirmSaveDialog;
import visual.scripting.ui.ListView;
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

    private Pin.DataType curSelectedPinDataType = null;

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
        addNodeToList(NodeEntry.class);
        addNodeToList(NodeSplitFlow.class);
        addNodeToList(NodeVisualTest.class);
        addNodeToList(NodeTime.class);
        addNodeToList(NodeLongToFloat.class);
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
                    ImVec2 headerMax;

                    float headerMaxY = 0;
                    beginGroup();
                    {
                        NodeEditor.begin("Editor");
                        {
                            //check if loaded from save file
                            if (justLoadedFromFile) {
                                System.out.println("Setting node positions from loaded file");
                                for (Node node : graph.getNodes().values()) {
                                    NodeEditor.setNodePosition(node.getID(), node.getLoadedPosition().x, node.getLoadedPosition().y);
                                }
                                justLoadedFromFile = false;
                            }

                            for (Node node : graph.getNodes().values()) {
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
                                        dummy(10, 10);
                                    }

                                    for (Button nodeButton : node.buttons) {
                                        nodeButton.show();
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

                                            NodeEditor.pinPivotAlignment(0f, .5f);
                                            NodeEditor.endPin();

                                            if (isItemClicked() && holdingPinID == -1) {
                                                lastActivePin = inPin.getID();
                                            }

                                            if (isItemHovered()) {
                                                setNextWindowPos(NodeEditor.toScreenX(getMousePosX()), NodeEditor.toScreenY(getMousePosY() + 10), ImGuiCond.Always);
                                                beginTooltip();
                                                textUnformatted(inPin.getName());
                                                textUnformatted("Type: " + inPin.getDataType());
                                                textUnformatted("Value: " + inPin.getData().value);
                                                endTooltip();
                                            }
                                            sameLine();
                                            beginGroup();
                                            configurePinUI(inPin);
                                            text(inPin.getName());
                                            endGroup();
                                        }

                                        if (node.width != -1) {
                                            sameLine(node.width - 10);
                                        }

                                        if (node.outputPins.size() > i) {
                                            Pin outPin = node.outputPins.get(i);

                                            if (outPin.getDataType() == Pin.DataType.SPACER) {
                                                NodeEditor.beginPin(outPin.getID(), NodeEditorPinKind.Output);
                                                dummy(1f, 1f);
                                                NodeEditor.endPin();
                                            } else {

                                                if(!node.hasTitleBar()) {
//                                                beginGroup();
                                                    text(outPin.getName());
//                                                endGroup();
                                                    sameLine();
                                                }

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
                                                    textUnformatted(outPin.getName());
                                                    textUnformatted("Type: " + outPin.getDataType());
                                                    textUnformatted("Value: " + outPin.getData().value);
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
                                    }

//                                NodeEditor.group(50, 50);
                                    headerMax = new ImVec2(getItemRectMax().x, headerMaxY);
                                }
                                NodeEditor.endNode();

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
                                            case Long:
                                                NodeData<ImLong> longOutData = otherPin.getData();
                                                NodeData<ImLong> longInData = pin.getData();
                                                longOutData.getValue().set(longInData.value.get());
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
                                        NodeEditor.pushStyleVar(NodeEditorStyleVar.FlowMarkerDistance, 50);
                                        NodeEditor.pushStyleVar(NodeEditorStyleVar.FlowDuration, 1000);
                                        NodeEditor.pushStyleVar(NodeEditorStyleVar.FlowSpeed, 25);
                                        NodeEditor.link(uniqueLinkId++, pin.getID(), pin.connectedTo, pincolor[0], pincolor[1], pincolor[2], pincolor[3], 1);

//                                        if(showFlow) {
//
//                                        }
                                        NodeEditor.popStyleVar(3);
                                    }
                                }
                            }
                        }
//                    ImVec2 previewRect = getItemRectMax();
//                    windowFocused = isWindowHovered();

                        if (NodeEditor.beginCreate()) {
                            holdingPinID = lastActivePin;
                            curSelectedPinDataType = graph.findPinById((int) lastActivePin).getDataType();
                            checkPinConnections();
                        } else {
                            //Open context menu if pin link dropped without connecting to another pin
                            if (holdingPinID != -1 && !(LINKA.get() != 0 || LINKB.get() != 0)) {
                                lastHoldingPinID = holdingPinID;
                                holdingPinID = -1;
                                curSelectedPinDataType = null;
//                            System.out.println(LINKA.get() + " : " + LINKB.get());
                                setNextWindowPos(cursorPos.x, cursorPos.y, ImGuiCond.Always);
                                openPopup("context_menu" + id);
                                justOpenedContextMenu = true;
                            }
                            LINKA.set(0);
                            LINKB.set(0);
                        }
                        NodeEditor.endCreate();

                        if (NodeEditor.beginDelete()) {
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
            case Long:
                color[0] = 0.952941176f;
                color[1] = 0.647058824f;
                color[2] = 0.0196078431f;
                color[3] = 1;
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
                        if(instancePin.getDataType() == pin.getDataType()){
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
                        if(instancePin.getDataType() == pin.getDataType()){
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
            case Long:
                int longGrey = (curSelectedPinDataType != Pin.DataType.Double && curSelectedPinDataType != null) ? rgbToInt(50, 50, 50, 255) : rgbToInt(243, 165, 5, 255);
                if(pin.connectedTo != -1) {
                    getWindowDrawList().addCircleFilled(posX + (size / 2), posY + (size / 2), size / 2, longGrey);
                }else{
                    getWindowDrawList().addCircle(posX + (size / 2), posY + (size / 2), size / 2, longGrey);
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
            case Long:
                ImFloat toFloat = new ImFloat(pin.getLong().get());
                if(inputFloat("##" + pin.getID(), toFloat))
                {

                }
                break;
            case String:
                if(inputText("##" + pin.getID(), pin.getString())){
                }
                break;
        }
        popItemWidth();
    }

    public String getId() {
        return id;
    }

    private int rgbToInt(int r, int g, int b, int a){
        return ImColor.intToColor(r, g, b, a);
    }
}
