package visual.scripting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import imgui.type.*;
import org.pf4j.PluginWrapper;
import visual.scripting.node.Node;
import visual.scripting.pin.Pin;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;

public class GraphSaver {

    //things to save
    //Nodes them self's
    //Node position
    //Nodes and their pins they are connected to
//    private static File file = new File("savedGraph.vsgraph");

//    private static transient ArrayList<NodeSave> savedNodes = new ArrayList<>();
    private static transient GraphSave graphSave = new GraphSave();
    private static StringBuilder sb = new StringBuilder();

    static String workspace = "Workspace";

    /**
     * Saves nodes to a file [WIP]
     * @param graph
     */
    public static void save(String fileName, Graph graph){
        sb.setLength(0);
        graphSave.nodeSaves.clear();
        File file = new File(workspace + File.separator + fileName + ".vsgraph");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        graphSave.language = graph.getLanguage();
//        System.out.println(graphSave.language + " : " + graph.getNodes().size());

        for(Node node : graph.getNodes().values()){
            String className = node.getClass().getName();
            ImVec2 pos = new ImVec2();
            NodeEditor.getNodePosition(node.getID(), pos);
//            getNodeEditorSpacePos(node.getID(), pos);

            NodeSave save = new NodeSave();
            //TODO save pin values
            save.className = className;
            save.x = pos.x;
            save.y = pos.y;
            for(Pin inputs : node.inputPins){
                PinData pinData = new PinData();
                pinData.ID = inputs.getID();
                pinData.type = inputs.getClass().getName(); //inputs.getDataType().name();
                pinData.connectedTo = inputs.connectedTo;
                if(inputs.getData() != null) {
                    System.out.println("Pin Save: " + inputs.getData().getValue());
                    if(inputs.getData().getValue() != null) {
                        pinData.value = inputs.getData().getValue().toString();
                    }
                }

                save.inputPins.add(pinData);
//                save.connectedToList.add(inputs.connectedTo);
            }

            for(Pin outputs : node.outputPins){
                PinData pinData = new PinData();
                pinData.ID = outputs.getID();
                pinData.type = outputs.getClass().getName(); //outputs.getDataType().name();

                pinData.connectedTo = outputs.connectedTo;

                save.outputPins.add(pinData);
//                save.connectedToList.add(outputs.connectedTo);
            }
            graphSave.nodeSaves.add(save);
        }

//        for(NodeSave save : graphSave.nodeSaves) {
//            sb.append("class=" + save.className + ":" + "posX=" + save.x + ":" + "posY=" + save.y + ":");
//            sb.append("connections[");
//            for (int i = 0; i < save.connectedToList.size(); i++) {
//                sb.append("connectedTo=" + save.connectedToList.get(i));
//                if(i < save.connectedToList.size() - 1){
//                    sb.append(",");
//                }
//            }
//            sb.append("]\n");
//        }

        try {
            Gson json = new GsonBuilder().setPrettyPrinting().create();
            String output = json.toJson(graphSave);

//            System.out.println(output);

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
     */
    public static Graph load(String fileName) {
        try {
            File file = new File(workspace + File.separator + fileName);
            BufferedReader br = new BufferedReader(new FileReader(file));

            Gson json = new GsonBuilder().setPrettyPrinting().create();
//            ArrayList<NodeSave> saveList = new ArrayList<>();
//            TypeToken<ArrayList<NodeSave>> list = new TypeToken<ArrayList<NodeSave>>(){};
//            ArrayList<NodeSave> saveList = json.fromJson(br, new TypeToken<ArrayList<NodeSave>>(){}.getType());
            GraphSave gs = json.fromJson(br, GraphSave.class);

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

            Node[] loadedNode = new Node[gs.nodeSaves.size()];

            Graph graph = new Graph(gs.language);

            //add node to graph and set it's position
            for (int i = 0; i < gs.nodeSaves.size(); i++) {
                NodeSave save = gs.nodeSaves.get(i);
                Class classNode = null;

                try {
                    //try to load the node from the current jar classpath
                    classNode = Class.forName(save.className, true, null);
                } catch (Exception e) {
//                    e.printStackTrace();
                }

                List<PluginWrapper> listWrapper = ImGuiWindow.pluginManager.getPlugins();
                if(listWrapper.size() > 0) {
                    for (PluginWrapper f : listWrapper) {
                        try {
                            ClassLoader loader = f.getPluginClassLoader();
                            classNode = Class.forName(save.className, true, loader);
                        } catch (ClassNotFoundException e) {
                            //e.printStackTrace();
                        }
                    }
                }else{
                    try{
                        ClassLoader loader = GraphSaver.class.getClassLoader();
                        classNode = Class.forName(save.className, true, loader);
                    }catch (ClassNotFoundException e){

                    }
                }

                if (classNode == null) {
                    System.out.println("Class was null, couldn't load");
                    return null;
                }



                Node node = (Node) classNode.getDeclaredConstructor(Graph.class).newInstance(graph);
                graph.addNode(node);
                node.setLoadedPosition(save.x, save.y);
                node.init();
//                NodeEditor.setNodePosition(node.getID(), save.x, save.y);
                loadedNode[i] = node;
            }



            //setup connections between any nodes that have connectedTo != -1

            for (int i = 0; i < loadedNode.length; i++) {
                Node node = loadedNode[i];
                if(node != null){
                    NodeSave save = gs.nodeSaves.get(i);
//                    System.out.println(save.className);

//                    int index = 0;
//                    Pin[] pins = new Pin[node.inputPins.size() + node.outputPins.size()];
//                    for(Pin pin : node.inputPins){
//                        pins[index++] = pin;
//                    }
//
//                    for(Pin pin : node.outputPins){
//                        pins[index++] = pin;
//                    }
//
//                    for (int j = 0; j < save.pins.size(); j++) {
//                        pins[j].setID(save.pins.get(j).ID);
//                    }

                    for (int j = 0; j < save.inputPins.size(); j++) {
                        //TODO check data type before adding
                        if(j >= node.inputPins.size()){
//                            Pin customPin = node.addInputPin(Pin.DataType.valueOf(save.inputPins.get(j).type), node);
//                            //TODO get pin class name and create pin
                            Class classNode = null;

                            try {
                                //try to load the node from the current jar classpath
                                classNode = Class.forName(save.inputPins.get(j).type, true, null);
                            } catch (Exception e) {
//                    e.printStackTrace();
                            }

                            List<PluginWrapper> listWrapper = ImGuiWindow.pluginManager.getPlugins();
                            if(listWrapper.size() > 0) {
                                for (PluginWrapper f : listWrapper) {
                                    try {
                                        System.out.println(save.inputPins.get(j).type);
                                        ClassLoader loader = f.getPluginClassLoader();
                                        classNode = Class.forName(save.inputPins.get(j).type, true, loader);
                                    } catch (ClassNotFoundException e) {
                                        //e.printStackTrace();
                                    }
                                }
                            }else{
                                try{
                                    ClassLoader loader = GraphSaver.class.getClassLoader();
                                    classNode = Class.forName(save.inputPins.get(j).type, true, loader);
                                }catch (ClassNotFoundException e){

                                }
                            }

                            if (classNode == null) {
                                System.out.println("Class was null, couldn't load");
                                return null;
                            }

                            int id = Graph.getNextAvailablePinID();
                            Pin pin = (Pin) classNode.getDeclaredConstructor().newInstance();
                            pin.setNode(node);

                            pin.setCanDelete(true);
                            node.inputPins.add(pin);
                        }

                        node.inputPins.get(j).setID(save.inputPins.get(j).ID);
//                        Global.setPinValue(node.inputPins.get(j), save.inputPins.get(j).value);

                        //TODO do this for output pins too
                        if(node.inputPins.get(j).getData() != null) {
                            node.inputPins.get(j).loadValue(save.inputPins.get(j).value);
//                            node.inputPins.get(j).getData().setValue();
                        }

                        if(save.inputPins.get(j).connectedTo != -1){
                            node.inputPins.get(j).connectedTo = save.inputPins.get(j).connectedTo;
                        }
                    }

                    for (int j = 0; j < save.outputPins.size(); j++) {

                        //TODO check data type before adding
                        if(j >= node.outputPins.size()){
                            Class classNode = null;

                            try {
                                //try to load the node from the current jar classpath
                                classNode = Class.forName(save.outputPins.get(j).type, true, null);
                            } catch (Exception e) {
//                    e.printStackTrace();
                            }

                            List<PluginWrapper> listWrapper = ImGuiWindow.pluginManager.getPlugins();
                            if(listWrapper.size() > 0) {
                                for (PluginWrapper f : listWrapper) {
                                    try {
                                        System.out.println(save.outputPins.get(j).type);
                                        ClassLoader loader = f.getPluginClassLoader();
                                        classNode = Class.forName(save.outputPins.get(j).type, true, loader);
                                    } catch (ClassNotFoundException e) {
                                        //e.printStackTrace();
                                    }
                                }
                            }else{
                                try{
                                    ClassLoader loader = GraphSaver.class.getClassLoader();
                                    classNode = Class.forName(save.outputPins.get(j).type, true, loader);
                                }catch (ClassNotFoundException e){

                                }
                            }

                            if (classNode == null) {
                                System.out.println("Class was null, couldn't load");
                                return null;
                            }

                            int id = Graph.getNextAvailablePinID();
                            Pin pin = (Pin) classNode.getDeclaredConstructor().newInstance();

                            pin.setCanDelete(true);
                            node.outputPins.add(pin);
//                            Pin customPin = node.addOutputPin(Pin.DataType.valueOf(save.outputPins.get(j).type), node);
//                            Pin customPin = node.addOutputPin(Pin.DataType.valueOf("Flow"), node);
                            //any extra pins added will be allowed for deletion
//                            customPin.setCanDelete(true);
                        }

                        node.outputPins.get(j).setID(save.outputPins.get(j).ID);
                        if(save.outputPins.get(j).connectedTo != -1){
                            node.outputPins.get(j).connectedTo = save.outputPins.get(j).connectedTo;
                        }
                    }

//                    for (int j = 0; j < save.connectedToList.size(); j++) {
//                        if(save.connectedToList.get(j) != -1){
//                            pins[j].connectedTo = save.connectedToList.get(j);
////                            System.out.println(save.connectedToList.get(j));
//                        }
//                    }
                }
            }
            return graph;

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

        return null;
    }

    private static class GraphSave{
        private String language;
        private ArrayList<NodeSave> nodeSaves = new ArrayList<>();
    }

    private static class PinData{
        private Integer ID;
        private String type;
        private String value;
        private Integer connectedTo;
    }


    private static class NodeSave{
        private String className;
        private float x;
        private float y;
        //save Pin array
        private ArrayList<PinData> inputPins = new ArrayList<>();
        private ArrayList<PinData> outputPins = new ArrayList<>();
//        private ArrayList<Integer> connectedToList = new ArrayList<>();

    }
}
