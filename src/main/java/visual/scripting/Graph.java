package visual.scripting;

import visual.scripting.node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph {


    private final Map<Integer, Node> nodes = new HashMap<>();
    private ArrayList<Integer> queuedForRemoval = new ArrayList<>();

    private static int localeVariableID = 0;
    private static int nextNodeID = 1;
    private static int nextPinID = 1;

    public boolean addNode(String name, Node node){
        node.setID(nextNodeID++);
        node.setName(name);
        nodes.put(node.getID(), node);
        return true;
    }

    public void removeNode(int node){
        queuedForRemoval.add(node);
    }

    public Map<Integer, Node> getNodes()
    {
        return nodes;
    }

    public static int getNextLocalVariableID(){
        return localeVariableID++;
    }

    public static int getNextAvailablePinID(){
        return nextPinID++;
    }

    public Pin findPinById(final int ID) {
        for(Node node : nodes.values()){
            for(Pin pin : node.inputPins){
                if(pin.getID() == ID){
                    return pin;
                }
            }

            for(Pin pin : node.outputPins){
                if(pin.getID() == ID){
                    return pin;
                }
            }
        }

        return null;
    }
}
