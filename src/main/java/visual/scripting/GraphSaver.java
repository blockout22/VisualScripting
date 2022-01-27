package visual.scripting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
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

    private static transient ArrayList<NodeSave> savedNodes = new ArrayList<>();
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
            NodeEditor.getNodePosition(node.getID(), pos);
//            getNodeEditorSpacePos(node.getID(), pos);

            NodeSave save = new NodeSave();
            save.className = className;
            save.x = pos.x;
            save.y = pos.y;
            for(Pin inputs : node.inputPins){
                save.pinIDs.add(inputs.getID());
                save.connectedToList.add(inputs.connectedTo);
            }

            for(Pin outputs : node.outputPins){
                save.pinIDs.add(outputs.getID());
                save.connectedToList.add(outputs.connectedTo);
            }
            savedNodes.add(save);
        }

        for(NodeSave save : savedNodes) {
            sb.append("class=" + save.className + ":" + "posX=" + save.x + ":" + "posY=" + save.y + ":");
            sb.append("connections[");
            for (int i = 0; i < save.connectedToList.size(); i++) {
                sb.append("connectedTo=" + save.connectedToList.get(i));
                if(i < save.connectedToList.size() - 1){
                    sb.append(",");
                }
            }
            sb.append("]\n");
        }

        try {
            Gson json = new GsonBuilder().setPrettyPrinting().create();
            String output = json.toJson(savedNodes);

            System.out.println(output);

            PrintWriter pw = new PrintWriter(file);
//            pw.write(sb.toString());
            pw.write(output);
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

            Gson json = new GsonBuilder().setPrettyPrinting().create();
//            ArrayList<NodeSave> saveList = new ArrayList<>();
//            TypeToken<ArrayList<NodeSave>> list = new TypeToken<ArrayList<NodeSave>>(){};
            ArrayList<NodeSave> saveList = json.fromJson(br, new TypeToken<ArrayList<NodeSave>>(){}.getType());

//            String line;
//            while ((line = br.readLine()) != null) {
//                NodeSave save = new NodeSave();
//                save.className = line.split(":")[0].split("=")[1];
//                save.x = Float.valueOf(line.split(":")[1].split("=")[1]);
//                save.y = Float.valueOf(line.split(":")[2].split("=")[1]);
//                //connections
//                String connectionList = line.split(":")[3].split("\\[")[1];
//                connectionList = connectionList.substring(0, connectionList.length() - 1);
//
//                for (String val : connectionList.split(",")) {
//                    save.connectedToList.add(Integer.valueOf(val.split("=")[1]));
//                }
//
//                saveList.add(save);
//            }
            br.close();

            Node[] loadedNode = new Node[saveList.size()];

            //add node to graph and set it's position
            for (int i = 0; i < saveList.size(); i++) {
                NodeSave save = saveList.get(i);
                Class classNode = null;

                try {
                    //try to load the node from the current jar classpath
                    classNode = Class.forName(save.className, true, null);
                } catch (Exception e) {
//                    e.printStackTrace();
                }

                List<PluginWrapper> listWrapper = ImGuiWindow.pluginManager.getPlugins();
                for (PluginWrapper f : listWrapper) {
                    try {
                        ClassLoader loader = f.getPluginClassLoader();
                        classNode = Class.forName(save.className, true, loader);
                    } catch (Exception e) {
//                            e.printStackTrace();
                    }
                }

                if (classNode == null) {
                    System.out.println("Class was null, couldn't load");
                    return;
                }

                Node node = (Node) classNode.getDeclaredConstructor(Graph.class).newInstance(graph);
                graph.addNode(node);
                node.init();
                NodeEditor.setNodePosition(node.getID(), save.x, save.y);
                loadedNode[i] = node;
            }



            //setup connections between and nodes that have connectedTo != -1

            for (int i = 0; i < loadedNode.length; i++) {
                Node node = loadedNode[i];
                if(node != null){
                    NodeSave save = saveList.get(i);
                    System.out.println(save.className);

                    int index = 0;
                    Pin[] pins = new Pin[node.inputPins.size() + node.outputPins.size()];
                    for(Pin pin : node.inputPins){
                        pins[index++] = pin;
                    }

                    for(Pin pin : node.outputPins){
                        pins[index++] = pin;
                    }

                    for (int j = 0; j < save.pinIDs.size(); j++) {
                        pins[j].setID(save.pinIDs.get(j));
                    }

                    for (int j = 0; j < save.connectedToList.size(); j++) {
                        if(save.connectedToList.get(j) != -1){
                            pins[j].connectedTo = save.connectedToList.get(j);
                            System.out.println(save.connectedToList.get(j));
                        }
                    }
                }
            }


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
        private ArrayList<Integer> pinIDs = new ArrayList<>();
        private ArrayList<Integer> connectedToList = new ArrayList<>();

    }
}
