package visual.scripting;

import imgui.type.*;
import visual.scripting.node.Node;

/**
 * stores and useful static functions here that are used in multiple classes
 */
public class Global {
    public static void setPinValue(Pin pin, String value) {
        switch (pin.getDataType()) {
            case Bool:
                NodeData<ImBoolean> boolData = pin.getData();
                boolData.value.set(Boolean.parseBoolean(value));
                break;
            case Int:
                NodeData<ImInt> intData = pin.getData();
                intData.value.set(Integer.parseInt(value));
                break;
            case Float:
                NodeData<ImFloat> floatData = pin.getData();
                floatData.value.set(Float.parseFloat(value));
                break;
            case Double:
                NodeData<ImDouble> doubleData = pin.getData();
                doubleData.value.set(Double.parseDouble(value));
                break;
            case String:
                NodeData<ImString> stringData = pin.getData();
                stringData.value.set(value);
                break;
            case Object:
//                data.setValue();
                break;
            case Function:
                break;
        }
    }
}
