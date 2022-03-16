package visual.scripting;

import visual.scripting.node.Node;
import visual.scripting.node.NodeEntry;
import visual.scripting.pin.Pin;
import visual.scripting.pin.PinFlow;

import java.util.LinkedHashSet;

public class NodeCompiler {

    private StringBuilder output = new StringBuilder();
    private LinkedHashSet<String> requires = new LinkedHashSet<>();

    /**
     * converts nodes to source each section starting from NodeEntry
     * @param graph
     * @return
     */
    public String compile(Graph graph){
        output.setLength(0);
        for(Node node : graph.getNodes().values()){
//            handleNode(graph, node);

            if(node instanceof NodeEntry){
                System.out.println("Found Entry Node");
                handleNode(graph, node);
            }
        }

        StringBuilder out = new StringBuilder();
        for(String s : requires) {
            out.append(s + "\n");
        }

        out.append(output);

        return out.toString();
    }

    private void handleNode(Graph graph, Node node) {
        requires.add(node.requires());
        node.printSource(output);

        for(Pin pin : node.outputPins){
//            if(pin.getDataType() == Pin.DataType.Flow)
            if(pin.getClass() == PinFlow.class)
            {
                if(pin.connectedTo != -1) {
                    Pin p = graph.findPinById(pin.connectedTo);
                    //goto next node
                    System.out.println(pin.getClass());
                    handleNode(graph, p.getNode());
                }
            }
        }
    }
}
