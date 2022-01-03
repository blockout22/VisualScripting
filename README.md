# VisualScripting

## Custom Nodes

### Add Nodes using plugins

**Requirements**
* VisualScripting.jar or this repo
* ImGui 
```html
<dependency>
    <groupId>io.github.spair</groupId>
    <artifactId>imgui-java-binding</artifactId>
    <version>${imgui.java.version}</version>
</dependency>
<dependency>
    <groupId>io.github.spair</groupId>
    <artifactId>imgui-java-lwjgl3</artifactId>
    <version>${imgui.java.version}</version>
</dependency>
<dependency>
    <groupId>io.github.spair</groupId>
    <artifactId>imgui-java-natives-windows</artifactId>
    <version>${imgui.java.version}</version>
</dependency>
```
* pf4j
 ```html
 <dependency>
     <groupId>org.pf4j</groupId>
     <artifactId>pf4j</artifactId>
     <version>${pf4j.version}</version>
 </dependency> 
 ```
 
 ### Making the plugin
* Create a new project with the above Requirements
* Create a new Class that `extends Plugin`   (this can be left as is, it's just required by pf4j)
* Create a new Class that `extends VisualScriptingPlugin`
* Inside the `VisualScriptingPlugin` class in the `init` function you can add you custom Nodes by calling `graph.addNodeToList(InsertCustomNodeNameHere.class);`
* To create an actual Custom node, create a new class that extends `Node` and from that node class you can deside what happens with your node (example below)

```java
import imgui.type.ImInt;
import visual.scripting.Graph;
import visual.scripting.NodeData;
import visual.scripting.Pin;
import visual.scripting.node.Node;

public class Node_Print extends Node {

    //pin reference stored outside so they can be accessed in other methods
    private Pin in1Pin, in2Pin, output;

    public Node_Print(Graph graph) {
        super(graph);
        //sets the name of the node which is the title
        setName("Node Name");

        in1Pin = addInputPin(Pin.DataType.Int, this);
        in1Pin.setName("Input 1");

        in2Pin = addInputPin(Pin.DataType.Int, this);
        in2Pin.setName("Input 2");

        output = addOutputPin(Pin.DataType.Int, this);
    }

    //this is what will visually happen in the NodeEditor, for this example is adds the 2 incoming pins and displays the answer on the outgoing pin
    @Override
    public void execute(){
        NodeData<ImInt> pin1 = in1Pin.getData();
        NodeData<ImInt> pin2 = in2Pin.getData();

        NodeData<ImInt> out = output.getData();
        out.getValue().set(pin1.value.get() + pin2.value.get());

        output.setName(output.getData().value + "");
    }
}
```

 
## Screenshots

![img](Images/example.png)
