package visual.scripting.ui;

import imgui.ImGui;
import imgui.type.ImInt;
import visual.scripting.Popup;
import visual.scripting.PopupHandler;

public class Combobox extends UiComponent{

    Popup popup = new Popup() {
        @Override
        public boolean show() {
            for (int i = 0; i < items.length; i++) {
                if(ImGui.menuItem(items[i])){
                    currentSelectedIndex = i;
                    return true;
                }
            }
            currentSelectedIndex = -1;
            return false;
        }
    };

    private boolean requestPopup = false;

    private int currentSelectedIndex = -1;
    private String[] items = {
    };

    public void addOption(String option){
        String[] temp = new String[items.length + 1];

        for (int i = 0; i < items.length; i++) {
            temp[i] = items[i];
        }

        temp[temp.length - 1] = option;

        items = temp;
    }

    public int getSelectedIndex(){
        return currentSelectedIndex;
    }

    public String getSelectedValue(){
        if(currentSelectedIndex != -1){
            return items[currentSelectedIndex];
        }
        return "";
    }

    @Override
    public void show() {
        if(requestPopup){
            PopupHandler.open(popup);
            requestPopup = false;
        }
//
//        if(ImGui.isPopupOpen("popup" + uniqueID)){
//            if(ImGui.beginPopup("popup" + uniqueID)){
//                ImGui.listBox("list", currentSelection, items);
//                System.out.println("Popup");
////                    ImGui.closeCurrentPopup();
//
//            }
//            ImGui.endPopup();
//        }
//
        if(ImGui.button("button" + uniqueID)){
            requestPopup = true;
        }
    }
}
