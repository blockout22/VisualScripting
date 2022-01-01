package visual.scripting;

public class MainEntry {
    
    public MainEntry()
    {
        init();
        update();
        close();
    }

    private void init() {
        GLFWWindow.createWindow(800, 600, "Visual Scripting");
    }

    private void update()
    {
        while(!GLFWWindow.shouldClose()){
            GLFWWindow.update();
        }
    }

    private void close() {
        GLFWWindow.close();
    }


    public static void main(String[] args) {
        new MainEntry();
    }
}
