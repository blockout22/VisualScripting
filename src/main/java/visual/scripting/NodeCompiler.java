package visual.scripting;

import visual.scripting.node.Node;
import visual.scripting.node.NodeEntry;
import visual.scripting.pin.Pin;
import visual.scripting.pin.PinFlow;

public class NodeCompiler {

    private StringBuilder output = new StringBuilder();

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
                System.out.println("Foiund Entry Node");
                handleNode(graph, node);
            }
        }

        return output.toString();
    }

    private void handleNode(Graph graph, Node node) {
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
