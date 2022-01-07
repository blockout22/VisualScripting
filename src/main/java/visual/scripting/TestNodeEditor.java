package visual.scripting;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.NodeEditorStyle;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import imgui.extension.nodeditor.flag.NodeEditorStyleColor;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImLong;

import static imgui.ImGui.*;

public class TestNodeEditor {
    private static final NodeEditorContext CONTEXT;

    private static int nodeID = 1;
    private static int pinID = 1000;

    private static NPin pin;


    static {
        NodeEditorConfig config = new NodeEditorConfig();
        config.setSettingsFile(null);
        CONTEXT = new NodeEditorContext(config);

        pin = new NPin();
        pin.id = pinID++;
        pin.name = "MyPin";
        pin.type = 1;
        pin.x = 100;
        pin.y = 100;
    }

    private static void nodeStyleEditor(){
        setNextWindowSize(800, 600, ImGuiCond.Once);
        if(begin("Node Editor Style")) {
            float paneWidth = getContentRegionAvail().x;

            NodeEditorStyle editorStyle = NodeEditor.getStyle();
            textUnformatted("Values");
            if (button("reset to defaults")) {
                editorStyle = NodeEditor.getStyle();
            }

            spacing();
            float[] padVal = {editorStyle.getNodePadding().x, editorStyle.getNodePadding().y, editorStyle.getNodePadding().z, editorStyle.getNodePadding().w};
            dragFloat4("Node Padding", padVal, 0.1f, 0.0f, 40);
            editorStyle.setNodePadding(padVal[0], padVal[1], padVal[2], padVal[3]);

            float[] roundVal = {editorStyle.getNodeRounding()};
            dragFloat("Node Rounding", roundVal, 0.1f, 0.0f, 40);
            editorStyle.setNodeRounding(roundVal[0]);

            float[] borderWidthVal = {editorStyle.getNodeBorderWidth()};
            dragFloat("Node Border Width", borderWidthVal, 0.1f, 0.0f, 40);
            editorStyle.setNodeBorderWidth(borderWidthVal[0]);

            float[] hoverBorderWidthVal = {editorStyle.getHoveredNodeBorderWidth()};
            dragFloat("Hovered Node Border Width", hoverBorderWidthVal, 0.1f, 0.0f, 40);
            editorStyle.setHoveredNodeBorderWidth(hoverBorderWidthVal[0]);

            float[] selectBorderWidthVal = {editorStyle.getSelectedNodeBorderWidth()};
            dragFloat("Selected Node Border Width", selectBorderWidthVal, 0.1f, 0.0f, 40);
            editorStyle.setSelectedNodeBorderWidth(selectBorderWidthVal[0]);

            float[] pinRoundVal = {editorStyle.getPinRounding()};
            dragFloat("Pin Rounding", pinRoundVal, 0.1f, 0.0f, 40);
            editorStyle.setPinRounding(pinRoundVal[0]);

            float[] pinBorderWidthVal = {editorStyle.getPinBorderWidth()};
            dragFloat("Pin Border Width", pinBorderWidthVal, 0.1f, 0.0f, 40);
            editorStyle.setPinBorderWidth(pinBorderWidthVal[0]);

            float[] linkStrengthVal = {editorStyle.getLinkStrength()};
            dragFloat("Link Strength", linkStrengthVal, 0.1f, 0.0f, 500);
            editorStyle.setLinkStrength(linkStrengthVal[0]);

            //
            float[] scollDuration = {editorStyle.getScrollDuration()};
            dragFloat("Scroll Duration", scollDuration, 0.1f,0.0f, 40);
            editorStyle.setScrollDuration(scollDuration[0]);

            float[] flowMarkerDistance = {editorStyle.getFlowMarkerDistance()};
            dragFloat("Flow Marker Distance", flowMarkerDistance, 0.1f,0.0f, 40);
            editorStyle.setFlowMarkerDistance(flowMarkerDistance[0]);

            float[] flowSpeed = {editorStyle.getFlowSpeed()};
            dragFloat("Flow Speed", flowSpeed, 0.1f,0.0f, 40);
            editorStyle.setFlowSpeed(flowSpeed[0]);

            float[] flowDuration = {editorStyle.getFlowDuration()};
            dragFloat("Flow Duration", flowDuration, 0.1f,0.0f, 40);
            editorStyle.setFlowDuration(flowDuration[0]);

            float[] groupRounding = {editorStyle.getGroupRounding()};
            dragFloat("Group Rounding", groupRounding, 0.1f,0.0f, 40);
            editorStyle.setGroupRounding(groupRounding[0]);

            float[] groupBorderWidth = {editorStyle.getGroupBorderWidth()};
            dragFloat("Group Border Width", groupBorderWidth, 0.1f, 0.0f, 40f);
            editorStyle.setGroupBorderWidth(groupBorderWidth[0]);

            separator();

            spacing();
            float[][] colors = NodeEditor.getStyle().getColors();
            float styleColorCount = colors.length;
            for (int i = 0; i < styleColorCount; i++) {
                String name = NodeEditor.getStyleColorName(i);

                colorEdit4(name, colors[i]);
            }

            NodeEditor.getStyle().setColors(colors);

        }
        end();
    }

    private static float getValueFloat(NodeEditor editorStyle, String name, float initalValue){
        float[] val = {};
        dragFloat(name, val, 0.1f,0.0f, 40);
//        editorStyle.setFlowDuration(scollDuration[0]);

        return val[0];
    }

    static int col = 0;
    public static void show(){
        setNextWindowSize(800, 600, ImGuiCond.Once);
        if(begin("Advanced Node Editor")){
            NodeEditor.setCurrentEditor(CONTEXT);

            if(button("NextInt")){
                col++;
                System.out.println(col);
            }
//            nodeStyleEditor();
            sameLine();
            NodeEditor.begin("Editor");
            {
//                NodeEditor.pushStyleColor(NodeEditorStyleColor.GroupBg, 255, 0, 0, 255);

                //Alpha
                pushStyleVar(0, 0.8f);
                {
                    NodeEditor.beginNode(1);
                    {
                        text("Hello Node");

                        NodeEditor.beginPin(101, NodeEditorPinKind.Input);
                        text("Input");
                        NodeEditor.endPin();

                        sameLine();

                        NodeEditor.beginPin(102, NodeEditorPinKind.Output);
                        text("output");
                        NodeEditor.endPin();
                    }
                    NodeEditor.endNode();

                    NodeEditor.beginNode(2);
                    {
                        text("Second Node");
                        NodeEditor.beginPin(103, NodeEditorPinKind.Input);
                        text("Input");
                        NodeEditor.endPin();

                        sameLine();

                        NodeEditor.beginPin(104, NodeEditorPinKind.Output);
                        text("output");
                        NodeEditor.endPin();


                    }
                    NodeEditor.endNode();
                }
                popStyleVar();
                NodeEditor.setNodePosition(2, 500, 100);
//                NodeEditor.popStyleColor(1);
                NodeEditor.flow(102);

                NodeEditor.link(1, 102, 103);
            }

            if(NodeEditor.beginCreate()) {
                ImLong a = new ImLong();
                ImLong b = new ImLong();
                if (NodeEditor.queryNewLink(a, b)) {

                }
            }
            NodeEditor.endCreate();

            NodeEditor.suspend();

            NodeEditor.resume();
            NodeEditor.end();
        }
        end();
    }

    private static void addNode(String nodeName, NPin... pins)
    {
        NodeEditor.beginNode(nodeID);
        {
            text(nodeName);
            for(NPin pin : pins) {
                NodeEditor.beginPin(pin.id, pin.type == 0 ? NodeEditorPinKind.Input : NodeEditorPinKind.Output);
                text(pin.name);
                NodeEditor.endPin();
            }
        }
        NodeEditor.endNode();
    }

    private static class NPin{
        String name;
        int id;
        int type = 0;

        float x;
        float y;
        private float lastX;
        private float lastY;

        public void updateNodePos(){
            if(lastX != x || lastY != y){
                lastX = x;
                lastY = y;
                NodeEditor.setNodePosition(id, x, y);
                System.out.println("NMesadf");
            }
        }
    }
}
