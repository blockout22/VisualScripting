package visual.scripting;

import visual.scripting.node.Node;
import visual.scripting.node.NodeEntry;

public class NodeCompiler {

    private StringBuilder output = new StringBuilder();

    /**
     * converts nodes linked to Start node to text
     * @param graph
     * @return
     */
    public String compile(Graph graph){
        output.setLength(0);
        for(Node node : graph.getNodes().values()){
            node.setHandled(false);
        }

        for(Node node : graph.getNodes().values()){
//            handleNode(graph, node);
            if(node instanceof NodeEntry){
                handleNode(graph, node);
            }
        }

        return output.toString();
    }

    private void handleNode(Graph graph, Node node) {
        node.printSource(output);

        for(Pin pin : node.outputPins){
            if(pin.getDataType() == Pin.DataType.Flow){
                if(pin.connectedTo != -1) {
                    Pin p = graph.findPinById(pin.connectedTo);
                    //goto next node
                    handleNode(graph, p.getNode());
                }
            }
        }
    }
}
