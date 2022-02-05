package visual.scripting.ui;

import imgui.ImGui;
import visual.scripting.ui.listeners.ClickListener;
import visual.scripting.ui.listeners.HoverListener;

import java.util.ArrayList;
import java.util.UUID;

public class Button {

    private String text = "##";
    private String uniqueID = UUID.randomUUID().toString();

    private ArrayList<HoverListener> hoverListeners = new ArrayList<>();
    private ArrayList<ClickListener> clickListeners = new ArrayList<>();

    public Button(String text){
        this.text = text;
    }

    public void addHoverListener(HoverListener hoverListener){
        hoverListeners.add(hoverListener);
    }

    public void addClickListener(ClickListener clickListener){
        clickListeners.add(clickListener);
    }

    public void show(){
//        ImGui.pushID("##");
        if(ImGui.button(text + "##" + uniqueID)){
            if(clickListeners.size() > 0){
                for (int i = 0; i < clickListeners.size(); i++) {
                    if(clickListeners.get(i) != null){
                        clickListeners.get(i).onClicked();
                    }
                }
            }
        }

        if(ImGui.isItemHovered()){
            if(hoverListeners.size() > 0){
                for (int i = 0; i < hoverListeners.size(); i++) {
                    if(hoverListeners.get(i) != null){
                        hoverListeners.get(i).onHovered();
                    }
                }
            }
        }

//        ImGui.popID();
    }
}
