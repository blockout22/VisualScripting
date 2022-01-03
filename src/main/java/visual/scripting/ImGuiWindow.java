package visual.scripting;

import imgui.*;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static imgui.ImGui.*;
import static imgui.flag.ImGuiWindowFlags.*;

public class ImGuiWindow {

    private final ImGuiImplGlfw imGuiGLFW = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    //stores the list of open Graph Windows
    private ArrayList<GraphWindow> graphWindows = new ArrayList<>();
    private ArrayList<GraphWindow> queueRemoveGraphWindow = new ArrayList<>();

    private File workingDir = new File(System.getProperty("user.dir"));

    private DarkStyle darkStyle;

    public static PluginManager pluginManager;

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

        darkStyle = new DarkStyle();
//        try {
//            darkStyle.convertToJava(new File("C:\\Users\\kie\\Downloads\\colors.txt"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        getStyle().setColors(darkStyle.getColors());
        try {
            loadPlugins();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPlugins() throws Exception {
        File file = new File("plugins");
        pluginManager = new DefaultPluginManager();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
    }

    private byte[] loadFromResources(String name){
        try{
            return Files.readAllBytes(Paths.get(new File(name).toURI()));
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void update(){
//        Graph.resetLocalVariableID();
        imGuiGLFW.newFrame();
        ImGui.newFrame();
        {
            //do UI stuff here
            createMainMenuBar();
            float menuBarHeight = 20f;
            //fill screen widget here to enable snapping on viewport it's self
            setNextWindowSize(GLFWWindow.getWidth(), GLFWWindow.getHeight(), ImGuiCond.Always);
            setNextWindowPos(getMainViewport().getPosX(), getMainViewport().getPosY(), ImGuiCond.Always);
            setNextWindowViewport(getMainViewport().getID());


            if(begin("New Window", NoBringToFrontOnFocus | NoBackground | NoTitleBar | NoDocking | NoScrollbar)){
                setCursorScreenPos(getMainViewport().getPosX(), getMainViewport().getPosY() + menuBarHeight);
                dockSpace(1, GLFWWindow.getWidth(), GLFWWindow.getHeight() - menuBarHeight, ImGuiDockNodeFlags.NoResize | NoScrollbar);
            }
            end();

            //content starts here
            setNextWindowSize(GLFWWindow.getWidth() / 2, GLFWWindow.getHeight() / 2, ImGuiCond.Once);
            setNextWindowPos(getMainViewport().getPosX(), getMainViewport().getPosY() + 20, ImGuiCond.Once);
            if(begin("FileViewer")){

                for(File file : workingDir.listFiles()) {
                    if(button(file.getName())){
                        System.out.println(file.length());
                    }
                    sameLine();
                    if(file.isDirectory()){
                        text("Directory");
                    }else{
                        text("File");
                    }
                }
            }
            end();
//            showStyleEditor();
            for(GraphWindow graphWindow : graphWindows){
                graphWindow.show(menuBarHeight);
            }

            for (int i = 0; i < queueRemoveGraphWindow.size(); i++) {
                graphWindows.remove(queueRemoveGraphWindow.get(i));
            }
        }


        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if(ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)){
            final long backupWindowPtr = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupWindowPtr);
        }
    }

    public void removeGraphWindow(GraphWindow graphWindow){
        queueRemoveGraphWindow.add(graphWindow);
    }

    private void createMainMenuBar()
    {
        beginMainMenuBar();
        {
            if(beginMenu("File", true)){
                if(menuItem("New Graph")){
                    graphWindows.add(new GraphWindow(this));
                }
                endMenu();
            }
        }
        endMainMenuBar();
    }

    public void close(){
        ImNodes.destroyContext();
        ImGui.destroyContext();
    }
}
