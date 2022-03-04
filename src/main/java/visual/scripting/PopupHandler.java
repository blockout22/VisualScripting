package visual.scripting;

import imgui.ImGui;

import java.util.ArrayList;

public class PopupHandler {

    protected static ArrayList<Popup> openPopups = new ArrayList<>();
    protected static ArrayList<Popup> openPopupsToRemove = new ArrayList<>();

    public static void open(Popup popup){
        ImGui.openPopup(popup.id.toString());
        openPopups.add(popup);
    }

    protected static void remove(Popup popup){
        openPopupsToRemove.add(popup);
    }

    protected static void update(){
        for(Popup s : openPopupsToRemove){
            openPopups.remove(s);
        }
        openPopupsToRemove.clear();
    }
}
