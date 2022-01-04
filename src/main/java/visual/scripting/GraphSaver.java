package visual.scripting;

import imgui.ImVec2;
import visual.scripting.node.Node;

import java.io.*;
import java.util.ArrayList;

import static imgui.extension.imnodes.ImNodes.*;

public class GraphSaver {

    //things to save
    //Nodes them self's
    //Node position
    //Nodes and their pins they are connected to
    private static File file = new File("savedGraph.vsgraph");

    private static ArrayList<NodeSave> savedNodes = new ArrayList<>();
    private static StringBuilder sb = new StringBuilder();

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
            getNodeEditorSpacePos(node.getID(), pos);

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

    public static void load(GraphWindow graphWindow, Graph graph) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while((line = br.readLine()) != null){
                NodeSave save = new NodeSave();
                save.className = line.split(",")[0].split("=")[1];
                save.x = Float.valueOf(line.split(",")[1].split("=")[1]);
                save.y = Float.valueOf(line.split(",")[2].split("=")[1]);

                Class classNode = Class.forName(save.className);

                Node node = (Node) classNode.getDeclaredConstructor(Graph.class).newInstance(graph);

                System.out.println(node.getName());
                graph.addNode(node);
                ImVec2 newPos = new ImVec2();
                newPos.set(save.x, save.y);
                graphWindow.nodeQPos.put(node.getID(), newPos);
            }

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class NodeSave{
        private String className;
        private float x;
        private float y;

    }
}
