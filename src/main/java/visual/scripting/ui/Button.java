package visual.scripting.ui;

import imgui.ImGui;

public class Button extends UiComponent{

    private String text = "##";

    public Button(String text){
        this.text = text;
    }

    public void show(){
        if(ImGui.button(text + "##" + uniqueID)){
        }

        pollEvents();
    }
}
