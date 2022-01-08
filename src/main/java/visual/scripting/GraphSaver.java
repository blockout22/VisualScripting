package visual.scripting;

import imgui.ImVec2;
import org.pf4j.PluginWrapper;
import visual.scripting.node.Node;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class GraphSaver {

    //things to save
    //Nodes them self's
    //Node position
    //Nodes and their pins they are connected to
    private static File file = new File("savedGraph.vsgraph");

    private static ArrayList<NodeSave> savedNodes = new ArrayList<>();
    private static StringBuilder sb = new StringBuilder();

    /**
     * Saves nodes to a file [WIP]
     * @param graph
     */
    public static void save(Graph graph){
        sb.setLength(0);
        savedNodes.clear();
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        for(Node node : graph.getNodes().values()){
            String className = node.getClass().getName();
            ImVec2 pos = new ImVec2();
//            getNodeEditorSpacePos(node.getID(), pos);

            NodeSave save = new NodeSave();
            save.className = className;
            save.x = pos.x;
            save.y = pos.y;
            savedNodes.add(save);
        }

        for(NodeSave save : savedNodes) {
            sb.append("class=" + save.className + "," + "posX=" + save.x + "," + "posY=" + save.y + "\n");
        }

        try {
            PrintWriter pw = new PrintWriter(file);
            pw.write(sb.toString());
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads Nodes from a file and adds the to the Graph [WIP]
     * @param graphWindow
     * @param graph
     */
    public static void load(GraphWindow graphWindow, Graph graph) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                NodeSave save = new NodeSave();
                save.className = line.split(",")[0].split("=")[1];
                save.x = Float.valueOf(line.split(",")[1].split("=")[1]);
                save.y = Float.valueOf(line.split(",")[2].split("=")[1]);
                Class classNode = null;

                try {
                    //try to load the node from the current jar classpath
                    classNode = Class.forName(save.className, true, null);
                } catch (Exception e) {
//                    e.printStackTrace();
                }

                List<PluginWrapper> listWrapper = ImGuiWindow.pluginManager.getPlugins();
                for(PluginWrapper f : listWrapper)
                    {
                        try {
                            ClassLoader loader = f.getPluginClassLoader();
                            classNode = Class.forName(save.className, true, loader);
                        } catch (Exception e) {
//                            e.printStackTrace();
                        }
                    }

                    if(classNode == null){
                        System.out.println("Class was null, couldn't load");
                        return;
                    }

                Node node = (Node) classNode.getDeclaredConstructor(Graph.class).newInstance(graph);
                graph.addNode(node);
                node.init();
                ImVec2 newPos = new ImVec2();
                newPos.set(save.x, save.y);
                graphWindow.nodeQPos.put(node.getID(), newPos);
            }

            br.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    public static class NodeSave{
        private String className;
        private float x;
        private float y;

    }
}
