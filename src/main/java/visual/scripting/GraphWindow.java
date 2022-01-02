package visual.scripting;

import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesPinShape;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiMouseButton;
import imgui.type.ImBoolean;
import visual.scripting.node.Node;
import visual.scripting.node.NodeBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import static imgui.ImGui.*;
import static imgui.ImGui.getMainViewport;
import static imgui.extension.imnodes.ImNodes.*;

public class GraphWindow {

    private ImBoolean closable = new ImBoolean(true);

    private ImGuiWindow window;
    private String id;
    private Graph graph;
    private NodeEditorContext context;

    private ArrayList<NodeBuilder> nodesTypes = new ArrayList<>();

    public GraphWindow(ImGuiWindow window){
        this.window = window;
        this.id = "new" + new Random().nextInt(100);
        graph = new Graph();
        NodeEditorConfig config = new NodeEditorConfig();
        context = new NodeEditorContext(config);

        try {
            loadNodeTypes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void show(float menuBarHeight){
        setNextWindowSize(GLFWWindow.getWidth(), GLFWWindow.getHeight() - menuBarHeight, ImGuiCond.Once);
        setNextWindowPos(getMainViewport().getPosX(), getMainViewport().getPosY() + menuBarHeight, ImGuiCond.Once);

        if(begin(id, closable)){
            //checks is value has been changed from clicking the close button
            if(closable.get() == false){
                //should init save
                window.removeGraphWindow(this);
            }

            if(beginChild(id + "1")) {
                beginNodeEditor();
                {
                    for (Node node : graph.getNodes().values()) {
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
                    }
                }
                endNodeEditor();
            }
            endChild();
        }
        end();

        if(isMouseClicked(ImGuiMouseButton.Right)){
            final int hoveredNode = getHoveredNode();
            if(hoveredNode != -1){
                openPopup("node_menu");
                getStateStorage().setInt(getID("delete_node_id"), hoveredNode);
            }else{
                openPopup("context_menu");
            }
        }

        if(isPopupOpen("context_menu")){
            if(beginPopup("context_menu")){
                for(NodeBuilder nb : nodesTypes){
                    if(menuItem(nb.getName()))
                    {
                        Node node = nb.build(graph);
                        graph.addNode(node.getName(), node);
                        closeCurrentPopup();
                    }
                }
                if(menuItem("Add Node")){
                    Node node = new Node(graph);
                    graph.addNode("TestNode", node);
                    node.setName("name");

                    closeCurrentPopup();
                }

                endPopup();
            }
        }
    }

    private void addPin(Pin pin){
        switch (pin.getPinType()){
            case Input:
                switch (pin.getDataType()){
                    case Flow:
                        beginInputAttribute(pin.getID(), ImNodesPinShape.Triangle);
                        break;
                    default:
                        beginInputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                }
//                configurePinType(pin);
                endOutputAttribute();
                sameLine();
                break;
            case Output:
                switch (pin.getDataType()){
                    case Flow:
                        beginOutputAttribute(pin.getID(), ImNodesPinShape.Triangle);
                        break;
                    default:
                        beginOutputAttribute(pin.getID(), ImNodesPinShape.CircleFilled);
                }
//                sameLine(curNodeSize / 2);
//                configurePinType(pin);
                text(pin.getName());
                endOutputAttribute();
                sameLine();
                break;
        }
    }

    private void loadNodeTypes() throws IOException {
        File file = new File("NodeTypes" + File.separator);


        for(File f : file.listFiles()){
            BufferedReader br = new BufferedReader(new FileReader(f));

            NodeBuilder nb = new NodeBuilder();
            String line;
            while((line = br.readLine()) != null){
                if(line.startsWith("name")){
                    String value = line.split("=")[1];
                    nb.setName(value);
                }

                if(line.startsWith("in")){
                    String value = line.split("=")[1];
                    nb.addInputPin(value);
                }

                if(line.startsWith("out")){
                    String value = line.split("=")[1];
                    nb.addOutputPin(value);
                }
            }

            nodesTypes.add(nb);
//            graph.addNode(node.getName(), node);
        }
    }
}
