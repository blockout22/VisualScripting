package visual.scripting;


import static org.lwjgl.opengl.GL11.*;

public class MainEntry {

    private ImGuiWindow imGuiWindow;
    
    public MainEntry()
    {
        init();
        update();
        close();
    }

    private void init() {
        GLFWWindow.createWindow(1920, 1080, "Visual Scripting");
        imGuiWindow = new ImGuiWindow(GLFWWindow.getWindowID());
    }

    private void update()
    {
        while(!GLFWWindow.shouldClose()){
            imGuiWindow.update();
            GLFWWindow.update();
            glClear(GL_COLOR_BUFFER_BIT);
        }
    }

    private void close() {
        imGuiWindow.close();
        GLFWWindow.close();
    }


    public static void main(String[] args) {
        new MainEntry();
    }
}
