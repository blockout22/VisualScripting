package visual.scripting;

import imgui.flag.ImGuiCond;

import java.util.Random;

import static imgui.ImGui.*;
import static imgui.ImGui.getMainViewport;

public class GraphWindow {

    private String id;

    public GraphWindow(){
        this.id = "new" + new Random().nextInt(100);
    }

    public void show(float menuBarHeight){
        setNextWindowSize(GLFWWindow.getWidth(), GLFWWindow.getHeight() - menuBarHeight, ImGuiCond.Once);
        setNextWindowPos(getMainViewport().getPosX(), getMainViewport().getPosY() + menuBarHeight, ImGuiCond.Once);

        if(begin(id)){

        }
        end();
    }
}
