package visual.scripting;

import visual.scripting.node.Node;
import visual.scripting.pin.Pin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph {


    private String language;
    private final Map<Integer, Node> nodes = new HashMap<>();
    private ArrayList<Integer> queuedForRemoval = new ArrayList<>();

    private static int localeVariableID = 0;
    private static int nextNodeID = 1;
    private static int nextPinID = 1000;

    public Graph(String language){
        this.language = language;
    }

    public boolean addNode(Node node){
        node.setID(nextNodeID++);
        node.setName(node.getName());
        nodes.put(node.getID(), node);
        return true;
    }

    public String getLanguage(){
        return language;
    }

    public void setLanguage(String language){
        this.language = language;
    }

    public void removeNode(int node){
        queuedForRemoval.add(node);
    }

    public void update(){
        for(Integer q : queuedForRemoval){
            Node n = nodes.get(q);

            //Clear any pin existing connections
            for(Pin pin : n.outputPins){
                if (pin.connectedTo != -1) {
                    Pin oldPin = findPinById(pin.connectedTo);
                    oldPin.connectedTo = -1;
                }
            }
            for(Pin pin : n.inputPins){
                if (pin.connectedTo != -1) {
                    Pin oldPin = findPinById(pin.connectedTo);
                    oldPin.connectedTo = -1;
                }
            }
            nodes.remove(q);
        }
        queuedForRemoval.clear();
    }

    public Map<Integer, Node> getNodes()
    {
        return nodes;
    }

    public static void resetLocalVariableID(){
        Graph.localeVariableID = 0;
    }

    public static int getNextLocalVariableID(){
        return localeVariableID++;
    }

    public static int getNextAvailablePinID(){
        return nextPinID++;
    }

    public void setHighestPinID(int id){
        if (id > nextPinID){
            nextPinID = id;
        }
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
