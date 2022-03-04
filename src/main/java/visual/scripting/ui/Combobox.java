package visual.scripting.ui;

import imgui.ImGui;
import imgui.type.ImInt;
import visual.scripting.Popup;
import visual.scripting.PopupHandler;

public class Combobox extends UiComponent{

    Popup popup = new Popup() {
        @Override
        public boolean show() {
            if(ImGui.menuItem("Hello Menu")){
                return true;
            }

            return false;
        }
    };

    private boolean requestPopup = false;

    private ImInt currentSelection = new ImInt();
    private String[] items = {
            "Item1",
            "Item2",
    };

    @Override
    public void show() {

        if(requestPopup){
            PopupHandler.open(popup);
            requestPopup = false;
        }

        if(ImGui.isPopupOpen("popup" + uniqueID)){
            if(ImGui.beginPopup("popup" + uniqueID)){
                ImGui.listBox("list", currentSelection, items);
//                    ImGui.closeCurrentPopup();

            }
            ImGui.endPopup();
        }

        if(ImGui.button("button" + uniqueID)){
            requestPopup = true;
        }
    }
}
