package com.agifans.agile.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

public class StagePanel extends Composite {

    interface Binder extends UiBinder<Widget, StagePanel> { }
    private static final Binder binder = GWT.create(Binder.class);

    public StagePanel() {
      initWidget(binder.createAndBindUi(this));
    }
}
