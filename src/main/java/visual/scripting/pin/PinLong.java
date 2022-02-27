package visual.scripting.pin;

import imgui.ImDrawList;
import imgui.type.ImLong;
import visual.scripting.NodeData;

public class PinLong extends Pin{

    NodeData<ImLong> data = new NodeData<>();

    public PinLong(){
        setData(data);
        data.setValue(new ImLong());
    }

    @Override
    public void loadValue(String value) {
        data.value.set(Long.valueOf(value));
    }

    @Override
    public void draw(ImDrawList windowDrawList, float posX, float posY, boolean isConnected, boolean pinDragSame) {
        drawDefaultCircle(windowDrawList, posX, posY,isConnected,pinDragSame);
    }

    public void UI(){
        //TODO handle custom data types and convert them to Im Types
//        if(ImGui.inputFloat("##" + getID(), data.getValue())){        }
    }
}
