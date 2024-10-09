package com.agifans.agile.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LogicEditPanel extends Composite {

    interface Binder extends UiBinder<HorizontalPanel, LogicEditPanel> { }
    private static final Binder binder = GWT.create(Binder.class);
    
    @UiField
    HorizontalPanel horizontalPanel;
    
    @UiField
    ScrollPanel logicsScrollPanel;
    
    @UiField
    VerticalPanel logicsVerticalPanel;
    
    public LogicEditPanel() {
        initWidget(binder.createAndBindUi(this));
    }
}
