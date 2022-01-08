package visual.scripting.node;

import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import imgui.internal.ImRect;

import static imgui.ImGui.*;

public class NodeRenderer {

    private float outputInputSpacing = 0.0f;
    private boolean calculatedSpacing = false;
    private ImRect inputsRect;

    public NodeRenderer(){

    }

    public void show(long nodeID, String title){
        NodeEditor.beginNode(nodeID);
        {
//            text(calcItemWidth() + "");
            text(title);


            inputsRect = new ImRect(getItemRectMin(), getItemRectMax());

            NodeEditor.beginPin(101, NodeEditorPinKind.Input);
            text(">");
            NodeEditor.endPin();
            sameLine();
            text("In");

            sameLine();
//                        getWindowDrawList().addRectFilled(inputsRect.min.x, inputsRect.min.y, inputsRect.max.x - inputsRect.min.x, inputsRect.max.y, rgbToInt(0, 255, 0));


//                        float sizeLeft = inputsRect.max.x - rectMaxLeft.x;
            if(outputInputSpacing > 0.0) {
                dummy(outputInputSpacing - 1, 0);
            }

            sameLine();
            text("Output node is longer than the title");

            if(!calculatedSpacing) {
                System.out.println("Calculate");
                ImVec2 rectMaxLeft = getItemRectMin();
//                getWindowDrawList().addRectFilled(rectMaxLeft.x, rectMaxLeft.y, inputsRect.max.x, inputsRect.max.y, rgbToInt(0, 0, 255));
                outputInputSpacing = inputsRect.max.x - rectMaxLeft.x;
                calculatedSpacing = true;
            }
            sameLine();
            NodeEditor.beginPin(102, NodeEditorPinKind.Output);
            text(">");
            NodeEditor.endPin();
        }
        NodeEditor.endNode();
    }
}
