package visual.scripting.node;

import visual.scripting.Graph;
import visual.scripting.Pin;

import java.util.ArrayList;

public class NodeBuilder {

    private String name;

    public ArrayList<String> inputPins = new ArrayList<>();
    public ArrayList<String> outputPins = new ArrayList<>();

    public void addInputPin(String value) {
        inputPins.add(value);

    }
    public void addOutputPin(String value){
        outputPins.add(value);
    }


    public void setName(String name){
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public Node build(Graph graph)
    {
        Node node = new Node(graph);
        node.setName(name);


        //TODO finish all pin types
        for(String pin : inputPins){
            switch (pin){
                case "Flow":
                    node.addInputPin(Pin.DataType.Flow, node);
                    break;
                case "Float":
                    node.addInputPin(Pin.DataType.Float, node);
                    break;
                case "Double":
                    node.addInputPin(Pin.DataType.Double, node);
                    break;
            }
        }

        for(String pin : outputPins){
            switch (pin){
                case "Flow":
                    node.addOutputPin(Pin.DataType.Flow, node);
                    break;
                case "Float":
                    node.addOutputPin(Pin.DataType.Float, node);
                    break;
                case "Double":
                    node.addOutputPin(Pin.DataType.Double, node);
                    break;
            }
        }

        return node;
    }
}
