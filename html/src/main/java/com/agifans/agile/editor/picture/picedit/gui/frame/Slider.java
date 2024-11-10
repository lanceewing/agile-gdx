package com.agifans.agile.editor.picture.picedit.gui.frame;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.FocusWidget;

/**
 * Represents the "range" type of input element.
 */
public class Slider extends FocusWidget {

    public Slider() {
        super(createInputElement(Document.get(), "range"));
    }
    
    public static native InputElement createInputElement(Document doc, String type) /*-{
        var e = doc.createElement("INPUT");
        e.type = type;
        return e;
    }-*/;
    
    public float getValue() {
        return Float.parseFloat(getElement().getPropertyString("value"));
    }
    
    public float getMin() {
        return Float.parseFloat(getElement().getPropertyString("min"));
    }
    
    public float getMax() {
        return Float.parseFloat(getElement().getPropertyString("max"));
    }
    
    public float getStep() {
        return Float.parseFloat(getElement().getPropertyString("step"));
    }
    
    public void setValue(float value) {
        getElement().setPropertyString("value", Float.toString(value));
    }
    
    public void setMin(float min) {
        getElement().setPropertyString("min", Float.toString(min));
    }
    
    public void setMax(float max) {
        getElement().setPropertyString("max", Float.toString(max));
    }
    
    public void setStep(float step) {
        getElement().setPropertyString("step", Float.toString(step));
    }
}
