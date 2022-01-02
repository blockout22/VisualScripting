package visual.scripting.node;

import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
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
                    Pin flowPin = node.addInputPin(Pin.DataType.Flow, node);
                    break;
                case "Bool":
                    Pin boolPin = node.addInputPin(Pin.DataType.Bool, node);
                    break;
                case "Int":
                    Pin intPin = node.addInputPin(Pin.DataType.Int, node);
                    break;
                case "Float":
                    Pin floatPin = node.addInputPin(Pin.DataType.Float, node);
                    break;
                case "Double":
                    Pin doublePin = node.addInputPin(Pin.DataType.Double, node);
                    break;
                case "String":
                    Pin stringPin = node.addInputPin(Pin.DataType.String, node);
                    break;
            }
        }

        for(String pin : outputPins){
            switch (pin){
                case "Flow":
                    Pin flowPin = node.addOutputPin(Pin.DataType.Flow, node);
                    break;
                case "Bool":
                    Pin boolPin = node.addOutputPin(Pin.DataType.Bool, node);
                    break;
                case "Int":
                    Pin intPin = node.addOutputPin(Pin.DataType.Int, node);
                    break;
                case "Float":
                    Pin floatPin = node.addOutputPin(Pin.DataType.Float, node);
                    break;
                case "Double":
                    Pin doublePin = node.addOutputPin(Pin.DataType.Double, node);
                    break;
                case "String":
                    Pin stringPin = node.addOutputPin(Pin.DataType.String, node);
                    break;
            }
        }

        return node;
    }
}
