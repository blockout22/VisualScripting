package visual.scripting;

import imgui.ImGuiStyle;
import imgui.ImVec4;

import java.io.*;

public class DarkStyle {

    private ImGuiStyle style;
    float[][] colors;
    public int ImGuiCol_Text = 0;
    public int ImGuiCol_TextDisabled = 1;
    public int ImGuiCol_WindowBg = 2;
    public int ImGuiCol_ChildBg = 3;
    public int ImGuiCol_PopupBg = 4;
    public int ImGuiCol_Border = 5;
    public int ImGuiCol_BorderShadow = 6;
    public int ImGuiCol_FrameBg = 7;
    public int ImGuiCol_FrameBgHovered = 8;
    public int ImGuiCol_FrameBgActive = 9;
    public int ImGuiCol_TitleBg = 10;
    public int ImGuiCol_TitleBgActive = 11;
    public int ImGuiCol_TitleBgCollapsed = 12;
    public int ImGuiCol_MenuBarBg = 13;
    public int ImGuiCol_ScrollbarBg = 14;
    public int ImGuiCol_ScrollbarGrab = 15;
    public int ImGuiCol_ScrollbarGrabHovered = 16;
    public int ImGuiCol_ScrollbarGrabActive = 17;
    public int ImGuiCol_CheckMark = 18;
    public int ImGuiCol_SliderGrab = 19;
    public int ImGuiCol_SliderGrabActive = 20;
    public int ImGuiCol_Button = 21;
    public int ImGuiCol_ButtonHovered = 22;
    public int ImGuiCol_ButtonActive = 23;
    public int ImGuiCol_Header = 24;
    public int ImGuiCol_HeaderHovered = 25;
    public int ImGuiCol_HeaderActive = 26;
    public int ImGuiCol_Separator = 27;
    public int ImGuiCol_SeparatorHovered = 38;
    public int ImGuiCol_SeparatorActive = 29;
    public int ImGuiCol_ResizeGrip = 30;
    public int ImGuiCol_ResizeGripHovered = 31;
    public int ImGuiCol_ResizeGripActive = 32;
    public int ImGuiCol_Tab = 33;
    public int ImGuiCol_TabHovered = 34;
    public int ImGuiCol_TabActive = 35;
    public int ImGuiCol_TabUnfocused = 36;
    public int ImGuiCol_TabUnfocusedActive = 37;
    public int ImGuiCol_DockingPreview = 38;
    public int ImGuiCol_DockingEmptyBg = 39;
    public int ImGuiCol_PlotLines = 40;
    public int ImGuiCol_PlotLinesHovered = 41;
    public int ImGuiCol_PlotHistogram = 42;
    public int ImGuiCol_PlotHistogramHovered = 43;
    public int ImGuiCol_TableHeaderBg = 44;
    public int ImGuiCol_TableBorderStrong = 45;
    public int ImGuiCol_TableBorderLight = 46;
    public int ImGuiCol_TableRowBg = 47;
    public int ImGuiCol_TableRowBgAlt = 48;
    public int ImGuiCol_TextSelectedBg = 49;
    public int ImGuiCol_DragDropTarget = 50;
    public int ImGuiCol_NavHighlight = 51;
    public int ImGuiCol_NavWindowingHighlight = 52;
    public int ImGuiCol_NavWindowingDimBg = 53;
    public int ImGuiCol_ModalWindowDimBg = 54;

    public DarkStyle()
    {
        style = new ImGuiStyle();
        colors = style.getColors();

        set(ImGuiCol_Text, new ImVec4(1.00f,1.00f,1.00f,1.00f));
        set(ImGuiCol_TextDisabled, new ImVec4(0.50f,0.50f,0.50f,1.00f));
        set(ImGuiCol_WindowBg, new ImVec4(0.16f,0.14f,0.14f,0.94f));
        set(ImGuiCol_ChildBg, new ImVec4(0.00f,0.00f,0.00f,0.00f));
        set(ImGuiCol_PopupBg, new ImVec4(0.08f,0.08f,0.08f,0.94f));
        set(ImGuiCol_Border, new ImVec4(0.43f,0.43f,0.50f,0.50f));
        set(ImGuiCol_BorderShadow, new ImVec4(0.00f,0.00f,0.00f,0.00f));
        set(ImGuiCol_FrameBg, new ImVec4(0.24f,0.25f,0.28f,0.54f));
        set(ImGuiCol_FrameBgHovered, new ImVec4(0.13f,0.13f,0.13f,0.40f));
        set(ImGuiCol_FrameBgActive, new ImVec4(0.26f,0.27f,0.28f,0.67f));
        set(ImGuiCol_TitleBg, new ImVec4(0.20f,0.18f,0.18f,1.00f));
        set(ImGuiCol_TitleBgActive, new ImVec4(0.43f,0.43f,0.43f,1.00f));
        set(ImGuiCol_TitleBgCollapsed, new ImVec4(0.00f,0.00f,0.00f,0.51f));
        set(ImGuiCol_MenuBarBg, new ImVec4(0.14f,0.14f,0.14f,1.00f));
        set(ImGuiCol_ScrollbarBg, new ImVec4(0.02f,0.02f,0.02f,0.53f));
        set(ImGuiCol_ScrollbarGrab, new ImVec4(0.31f,0.31f,0.31f,1.00f));
        set(ImGuiCol_ScrollbarGrabHovered, new ImVec4(0.41f,0.41f,0.41f,1.00f));
        set(ImGuiCol_ScrollbarGrabActive, new ImVec4(0.51f,0.51f,0.51f,1.00f));
        set(ImGuiCol_CheckMark, new ImVec4(0.16f,0.17f,0.18f,1.00f));
        set(ImGuiCol_SliderGrab, new ImVec4(0.24f,0.52f,0.88f,1.00f));
        set(ImGuiCol_SliderGrabActive, new ImVec4(0.26f,0.59f,0.98f,1.00f));
        set(ImGuiCol_Button, new ImVec4(0.32f,0.32f,0.32f,0.40f));
        set(ImGuiCol_ButtonHovered, new ImVec4(0.56f,0.57f,0.57f,1.00f));
        set(ImGuiCol_ButtonActive, new ImVec4(0.63f,0.66f,0.70f,1.00f));
        set(ImGuiCol_Header, new ImVec4(0.50f,0.51f,0.52f,0.31f));
        set(ImGuiCol_HeaderHovered, new ImVec4(0.29f,0.30f,0.30f,0.80f));
        set(ImGuiCol_HeaderActive, new ImVec4(0.29f,0.30f,0.31f,1.00f));
        set(ImGuiCol_Separator, new ImVec4(0.43f,0.43f,0.50f,0.50f));
        set(ImGuiCol_SeparatorHovered, new ImVec4(0.10f,0.40f,0.75f,0.78f));
        set(ImGuiCol_SeparatorActive, new ImVec4(0.10f,0.40f,0.75f,1.00f));
        set(ImGuiCol_ResizeGrip, new ImVec4(0.26f,0.59f,0.98f,0.20f));
        set(ImGuiCol_ResizeGripHovered, new ImVec4(0.26f,0.59f,0.98f,0.67f));
        set(ImGuiCol_ResizeGripActive, new ImVec4(0.26f,0.59f,0.98f,0.95f));
        set(ImGuiCol_Tab, new ImVec4(0.30f,0.31f,0.33f,0.86f));
        set(ImGuiCol_TabHovered, new ImVec4(0.18f,0.22f,0.28f,0.80f));
        set(ImGuiCol_TabActive, new ImVec4(0.48f,0.54f,0.61f,1.00f));
        set(ImGuiCol_TabUnfocused, new ImVec4(0.07f,0.10f,0.15f,0.97f));
        set(ImGuiCol_TabUnfocusedActive, new ImVec4(0.14f,0.26f,0.42f,1.00f));
        set(ImGuiCol_DockingPreview, new ImVec4(0.26f,0.59f,0.98f,0.70f));
        set(ImGuiCol_DockingEmptyBg, new ImVec4(0.20f,0.20f,0.20f,1.00f));
        set(ImGuiCol_PlotLines, new ImVec4(0.61f,0.61f,0.61f,1.00f));
        set(ImGuiCol_PlotLinesHovered, new ImVec4(1.00f,0.43f,0.35f,1.00f));
        set(ImGuiCol_PlotHistogram, new ImVec4(0.90f,0.70f,0.00f,1.00f));
        set(ImGuiCol_PlotHistogramHovered, new ImVec4(1.00f,0.60f,0.00f,1.00f));
        set(ImGuiCol_TableHeaderBg, new ImVec4(0.19f,0.19f,0.20f,1.00f));
        set(ImGuiCol_TableBorderStrong, new ImVec4(0.31f,0.31f,0.35f,1.00f));
        set(ImGuiCol_TableBorderLight, new ImVec4(0.23f,0.23f,0.25f,1.00f));
        set(ImGuiCol_TableRowBg, new ImVec4(0.00f,0.00f,0.00f,0.00f));
        set(ImGuiCol_TableRowBgAlt, new ImVec4(1.00f,1.00f,1.00f,0.06f));
        set(ImGuiCol_TextSelectedBg, new ImVec4(0.26f,0.59f,0.98f,0.35f));
        set(ImGuiCol_DragDropTarget, new ImVec4(1.00f,1.00f,0.00f,0.90f));
        set(ImGuiCol_NavHighlight, new ImVec4(0.26f,0.59f,0.98f,1.00f));
        set(ImGuiCol_NavWindowingHighlight, new ImVec4(1.00f,1.00f,1.00f,0.70f));
        set(ImGuiCol_NavWindowingDimBg, new ImVec4(0.80f,0.80f,0.80f,0.20f));
        set(ImGuiCol_ModalWindowDimBg, new ImVec4(0.80f,0.80f,0.80f,0.35f));
        style.setColors(colors);
    }

    /**
     * Colors export to support java float[][]
     */
    public void convertToJava(File file) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file));

        StringBuilder sb = new StringBuilder();

        String line;
        while((line = br.readLine()) != null){
            line = line.replaceAll("\\s+", "");
            if(line.contains("=")) {
                String arg = line.split("=")[0].split("\\[")[1].replace("]", "");

                String val = line.trim().split("=")[1].replace(";", ");");

                sb.append("set(" + arg + ", new " + val + "\n");
            }
        }

        br.close();

        File out = new File("output.java");
        PrintWriter pw = new PrintWriter(out);
        pw.write(sb.toString());
        pw.flush();
        pw.close();
    }

    public void set(int index, ImVec4 val){
        colors[index][0] = val.x;
        colors[index][1] = val.y;
        colors[index][2] = val.z;
        colors[index][3] = val.w;
    }

    public ImGuiStyle getStyle()
    {
        return style;
    }

    public float[][] getColors(){
        return colors;
    }
}
