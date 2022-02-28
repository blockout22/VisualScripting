package visual.scripting.pin;

import imgui.ImDrawList;

public class PinFlow extends Pin{

    public PinFlow() {
    }

    @Override
    public void draw(ImDrawList windowDrawList, float posX, float posY, boolean isConnected, boolean pinDragSame) {
        float size = 10f;
        int flowGrey = pinDragSame ? rgbToInt(1, 1, 1, 1) : rgbToInt(50, 50, 50, 255);
        if(isConnected) {
            windowDrawList.addTriangleFilled(posX + 2.5f, posY, posX + 2.5f, posY + size, posX + (size / 2) + 2.5f, posY + (size / 2), flowGrey);
        }else{
            windowDrawList.addTriangle(posX + 2.5f, posY, posX + 2.5f, posY + size, posX + (size / 2) + 2.5f, posY + (size / 2), flowGrey);
        }
    }

    public void UI(){
    }

    public boolean connect(){
        return false;
    }
}
