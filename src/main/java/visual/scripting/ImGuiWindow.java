package visual.scripting;

import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static imgui.ImGui.*;
import static imgui.flag.ImGuiWindowFlags.*;

public class ImGuiWindow {

    private final ImGuiImplGlfw imGuiGLFW = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public ImGuiWindow(){
        //Create ImGui
        ImNodes.createContext();
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.setConfigViewportsNoTaskBarIcon(false);

        //create Fonts
        io.getFonts().addFontDefault();

        final ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder(); // Glyphs ranges provide
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesDefault());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesCyrillic());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesJapanese());
//        rangesBuilder.addRanges(FontAwesomeIcons._IconRange);

        // Font config for additional fonts
        // This is a natively allocated struct so don't forget to call destroy after atlas is built
        final ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setMergeMode(true);  // Enable merge mode to merge cyrillic, japanese and icons with default font

        final short[] glyphRanges = rangesBuilder.buildRanges();
        io.getFonts().addFontFromMemoryTTF(loadFromResources("OpenSans-Regular.ttf"), 14, fontConfig, glyphRanges); // cyrillic glyphsio.getFonts().build();

        fontConfig.destroy();

        imGuiGLFW.init(GLFWWindow.getWindowID(), true);
        imGuiGl3.init("#version 150");
    }

    private byte[] loadFromResources(String name){
        try{
            return Files.readAllBytes(Paths.get(new File(name).toURI()));
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void update(){
        imGuiGLFW.newFrame();
        ImGui.newFrame();
        {
            //do UI stuff here
            //fill screen widget here to enable snapping on viewport it's self
            setNextWindowSize(GLFWWindow.getWidth(), GLFWWindow.getHeight(), ImGuiCond.Always);
            setNextWindowPos(getMainViewport().getPosX(), getMainViewport().getPosY(), ImGuiCond.Always);
            setNextWindowViewport(getMainViewport().getID());
            if(begin("New Window", NoBringToFrontOnFocus | NoBackground | NoTitleBar )){

            }
            end();

            //content starts here
            if(begin("Some window")){

            }
            end();

            if(begin("another window")){

            }
            end();
        }
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if(ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)){
            final long backupWindowPtr = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();;
            GLFW.glfwMakeContextCurrent(backupWindowPtr);
        }
    }

    public void close(){
        ImGui.destroyContext();
    }
}
