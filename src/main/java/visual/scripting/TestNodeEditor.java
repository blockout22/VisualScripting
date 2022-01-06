package visual.scripting;

import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.NodeEditorStyle;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import imgui.flag.ImGuiCond;
import imgui.type.ImLong;

import static imgui.ImGui.*;

public class TestNodeEditor {
    private static final NodeEditorContext CONTEXT;


    static {
        NodeEditorConfig config = new NodeEditorConfig();
        config.setSettingsFile(null);
        CONTEXT = new NodeEditorContext(config);
    }

    public static void show(){
        setNextWindowSize(800, 600, ImGuiCond.Once);
        if(begin("Advanced Node Editor")){
            NodeEditor.setCurrentEditor(CONTEXT);
            NodeEditor.begin("Editor");{
                NodeEditor.beginNode(1);
                {
//                    NodeEditor.pushStyleColor(3, 255, 0, 0, 255);
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
}
