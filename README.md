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
* In your MANIFEST.MF you need to include the following `Plugin-Class: <package>.<PluginClass>` `Plugin-Id: <Anything>` `Plugin-Version: 0.0.1` (full example below)

### Examples 

An example of a node that doesn't require the exec function
```java
import imgui.type.ImString;
import visual.scripting.Graph;
import visual.scripting.NodeData;
import visual.scripting.Pin;
import visual.scripting.node.Node;

public class Node_PrintString extends Node {

    private Pin flowIn, strIn, flowOut;

    public Node_PrintString(Graph graph) {
        super(graph);
        //set the name of the node
        setName("Print String");
    }

    @Override
    public void init() {
        flowIn = addInputPin(Pin.DataType.Flow, this);
        strIn = addInputPin(Pin.DataType.String, this);

        flowOut = addOutputPin(Pin.DataType.Flow, this);
    }

    @Override
    public String printSource(StringBuilder sb) {
        NodeData<ImString> data = strIn.getData();

        String strOutput = "\"" + data.value.get() + "\"";

        if(strIn.connectedTo != -1){
            Pin pin = getGraph().findPinById(strIn.connectedTo);
            strOutput = pin.getNode().printSource(sb);
        }

        sb.append("System.out.println(" + strOutput + ");\n");
        return "";
    }
}
```

An example of a node that makes use of the exec function and use of variables 
```java
import imgui.type.ImInt;
import imgui.type.ImString;
import visual.scripting.Graph;
import visual.scripting.NodeData;
import visual.scripting.Pin;
import visual.scripting.node.Node;

public class Node_IntToString extends Node {

    private Pin in, out;

    public Node_IntToString(Graph graph) {
        super(graph);
        //set the name of the node
        setName("IntToString");
    }

    @Override
    public void init() {
        in = addInputPin(Pin.DataType.Int, this);

        out = addOutputPin(Pin.DataType.String, this);

        // call getGraph().getNextLocalVariableID(); to avoid having same variable names in the same function
        String var = "intToString" + getGraph().getNextLocalVariableID();
        //set variable name to let any nodes connect to this pin know what the variable name will be and can reference it
        out.setVariable(var);
    }

    @Override
    public void execute() {
        NodeData<ImInt> inData = in.getData();
        NodeData<ImString> outData = out.getData();

        outData.getValue().set(String.valueOf(inData.value.get()));
    }

    @Override
    public String printSource(StringBuilder sb) {
        NodeData<ImInt> inData = in.getData();
        NodeData<ImString> outData = out.getData();

        //sets the value to the value of the in pin
        String input = String.valueOf(inData.value.get());

        //checks if pin is connected to another pin then changed input string to a variable
        if(in.connectedTo != -1){
            Pin pin = getGraph().findPinById(in.connectedTo);
            //gets the variable name from the connected pin
            input = pin.getNode().printSource(sb);
        }

        String toPrint = "int " + out.getVariable() + " = " + input;
        //add to the source
        sb.append(toPrint + "\n");
        return out.getVariable();
    }
}
```

```mf
Manifest-Version: 1.0
Plugin-Class: com.PluginTest
Plugin-Id: PluginTest
Plugin-Version: 0.0.1
```

 
## Screenshots

![img](Images/example.png)
