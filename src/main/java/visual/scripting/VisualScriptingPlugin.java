package visual.scripting;


import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

@Extension
public abstract class VisualScriptingPlugin implements ExtensionPoint {
    public abstract void init(GraphWindow graph);
}
