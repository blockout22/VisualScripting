package visual.scripting.ui;

import imgui.ImGui;
import visual.scripting.ui.listeners.ClickListener;
import visual.scripting.ui.listeners.HoverListener;

import java.util.ArrayList;
import java.util.UUID;

public abstract class UiComponent {

    private boolean isHovered;

    protected String uniqueID = UUID.randomUUID().toString();
    protected ArrayList<HoverListener> hoverListeners = new ArrayList<>();
    protected ArrayList<ClickListener> clickListeners = new ArrayList<>();

    public void addHoverListener(HoverListener hoverListener){
        hoverListeners.add(hoverListener);
    }

    public void addClickListener(ClickListener clickListener){
        clickListeners.add(clickListener);
    }

    protected void pollEvents(){
        if(ImGui.isItemClicked()){
            for (int i = 0; i < clickListeners.size(); i++) {
                if(clickListeners.get(i) != null){
                    clickListeners.get(i).onClicked();
                }
            }
        }
        isHovered = ImGui.isItemHovered();
        if(isHovered){

            if(hoverListeners.size() > 0){
                for (int i = 0; i < hoverListeners.size(); i++) {
                    if(hoverListeners.get(i) != null){
                        hoverListeners.get(i).onHovered();
                    }
                }
            }
        }
    }

    public boolean isHovered()
    {
        return isHovered;
    }
}
