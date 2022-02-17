package visual.scripting.ui;

import imgui.ImGui;
import imgui.type.ImInt;

public class ListView extends UiComponent{

    private String title = "";
    private ImInt currentSelection = new ImInt();
    private String[] list = new String[0];

    public ListView(String title){
        this.title = title;

        add("Value 1");
        add("Value 4");
        add("Value 3");
        add("Value 10");
    }

    @Override
    public void show() {
        ImGui.beginListBox("##" + uniqueID, 100, 200);
        {
            ImGui.listBox("##", currentSelection, list);
            pollEvents();
        }
        ImGui.endListBox();
    }

    public void add(String value){
        String[] temp = new String[list.length + 1];
        for (int i = 0; i < list.length; i++) {
            temp[i] = list[i];
        }

        temp[temp.length - 1] = value;

        list = temp;
    }

    public void remove(String value){

    }

    public String getSelectedItem(){
        return list[currentSelection.get()];
    }
}
