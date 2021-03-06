package visual.scripting.pin;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.type.ImFloat;
import visual.scripting.NodeData;

public class PinFloat extends Pin{

    NodeData<ImFloat> data = new NodeData<>();

    public PinFloat(){
        setData(data);
        data.setValue(new ImFloat());
        setColor(0.192156863f, 0.4f, 0.31372549f, 1f);
    }

    @Override
    public void loadValue(String value) {
        data.value.set(Float.valueOf(value));
    }

    @Override
    public void draw(ImDrawList windowDrawList, float posX, float posY, boolean isConnected, boolean pinDragSame) {
        drawDefaultCircle(windowDrawList, posX, posY, isConnected, pinDragSame);
//        float size = 10f;
//        int doubleGrey = pinDragSame ? rgbToInt(0.192156863f, 0.4f, 0.31372549f, 1f) : rgbToInt(50, 50, 50, 255);
//        if(isConnected) {
//            windowDrawList.addCircleFilled(posX + (size / 2), posY + (size / 2), size / 2, doubleGrey);
//        }else{
//            windowDrawList.addCircle(posX + (size / 2), posY + (size / 2), size / 2, doubleGrey);
//        }
    }

    @Override
    public void UI() {
        if(ImGui.inputFloat("##" + getID(), data.getValue())){
            System.out.println(data.getValue());
        }
    }
}
