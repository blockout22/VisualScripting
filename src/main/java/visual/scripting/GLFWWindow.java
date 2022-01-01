package visual.scripting;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GLFWWindow {

    private static long windowID;
    private static int curWidth;
    private static int curHeight;

    public static void createWindow(int width, int height, String title){
        GLFWErrorCallback.createPrint(System.err).set();
        if(!glfwInit())
        {
            throw new RuntimeException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

        windowID = glfwCreateWindow(width, height, title, NULL, NULL);
        if(windowID == NULL){
            throw new RuntimeException("Failed to create GLFW Window");
        }

        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(windowID, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(windowID,(vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwSetWindowSizeCallback(windowID, (window, w, h) -> {
            GLFWWindow.curWidth = w;
            GLFWWindow.curHeight = h;
        });

        glfwMakeContextCurrent(windowID);
        glfwSwapInterval(1);
        glfwShowWindow(windowID);
        GL.createCapabilities();

        GLFWWindow.curWidth = width;
        GLFWWindow.curHeight = height;

    }

    public static boolean shouldClose(){
        return glfwWindowShouldClose(windowID);
    }

    public static void update(){
        if(windowID == NULL){
            throw new IllegalStateException("Window must be created before calling update!");
        }

        glfwSwapBuffers(windowID);
        glfwPollEvents();
    }

    public static long getWindowID()
    {
        return windowID;
    }

    public static int getWidth(){
        return curWidth;
    }

    public static int getHeight(){
        return curHeight;
    }

    public static void close()
    {
        glfwFreeCallbacks(windowID);
        glfwDestroyWindow(windowID);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
