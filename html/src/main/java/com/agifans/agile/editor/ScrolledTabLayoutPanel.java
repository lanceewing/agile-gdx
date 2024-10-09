package com.agifans.agile.editor;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link TabLayoutPanel} that shows scroll buttons if necessary.
 * https://groups.google.com/forum/?fromgroups=#!topic/google-web-toolkit/wN8lLU23wPA
 */
public class ScrolledTabLayoutPanel extends TabLayoutPanel {
    
    private static final String SCROLL_BUTTON_STYLE = "gwt-TabLayoutPanelScrollButton";
    private static final String SCROLL_PANEL_STYLE = "gwt-TabLayoutPanelScrollPanel";

    private final double barHeight;
    private final Unit barUnit;
    private final Resources resources;

    //tabLayoutPanel root widget
    private LayoutPanel panel;
    private FlowPanel tabBar;
    private HorizontalPanel scrollPanel;

    private static Resources DEFAULT_RESOURCES;

    public ScrolledTabLayoutPanel() {
        this(30, Unit.PX);
    }

    public ScrolledTabLayoutPanel(double barHeight, Unit barUnit) {
        this(barHeight, barUnit, getDefaultResources());
    }

    public ScrolledTabLayoutPanel(double barHeight, Unit barUnit, Resources resources) {
        super(barHeight, barUnit);

        this.barUnit = barUnit;
        this.barHeight = barHeight;
        this.resources = resources;

        // The main widget wrapped by this composite, which is a LayoutPanel with the tab bar & the tab content
        panel = (LayoutPanel) getWidget();

        // Find the tab bar, which is the first flow panel in the LayoutPanel
        for(int i = 0; i < panel.getWidgetCount(); i++) {
            Widget widget = panel.getWidget(i);
            if(widget instanceof FlowPanel) {
                tabBar = (FlowPanel) widget;
                break;
            }
        }

        initScrollButtons();
    }

    @Override
    public void onResize() {
        super.onResize();
        showScrollButtonsIfNecessary();
    }

    @Override
    public void insert(Widget child, Widget tab, int beforeIndex) {
        super.insert(child, tab, beforeIndex);
        showScrollButtonsIfNecessary();
    }

    @Override
    public boolean remove(int index) {
        boolean b = super.remove(index);
        showScrollButtonsIfNecessary();
        return b;
    }

    @Override
    public void selectTab(int index, boolean fireEvents) {
        super.selectTab(index, fireEvents);

        //all the code below is for automatic scrolling if selected tab is out of visible area
        Widget selectedTab = tabBar.getWidget(getSelectedIndex());

        int visibleAreaLeftBorder = Math.abs(getCurrentShift());
        int visibleAreaRightBorder = visibleAreaLeftBorder + getTabBarWidth();
        int halfVisibleAreaWidth = getTabBarWidth() / 2;
        int halfTabWidth = (getRightPosition(selectedTab) - getLeftPosition(selectedTab)) / 2;

        if(getLeftPosition(selectedTab) < visibleAreaLeftBorder) {
            //GWT.log("Need scroll to the right");
            int scrollValue =
                    visibleAreaLeftBorder - getLeftPosition(selectedTab) + halfVisibleAreaWidth - halfTabWidth;
            adjustScroll(scrollValue);
        } else if(getRightPosition(selectedTab) > visibleAreaRightBorder) {
            //GWT.log("Need scroll to the left");
            int scrollValue =
                    getRightPosition(selectedTab) - visibleAreaRightBorder + halfVisibleAreaWidth - halfTabWidth;
            adjustScroll(-scrollValue);
        }
    }

    private void showScrollButtonsIfNecessary() {
        // Defer size calculations until sizes are available.
        // When calculating immediately after add(), all size methods return zero.
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                boolean scrollingNecessary = isScrollingNecessary();
                if(scrollPanel.isVisible()) {
                    if(!scrollingNecessary) {
                        // The scroll buttons are being hidden, reset the scroll position to zero to avoid
                        // having tabs starting in the middle of the window!
                        scrollTo(0);
                    } else {
                        // Resizing or adding / removing tabs, recompute the scroll
                        adjustScroll(0);
                    }
                }

                scrollPanel.setVisible(scrollingNecessary);
                //setting margin for tab bar to free space for scroll panel
                int marginRight = scrollingNecessary ? getScrollPanelWidth() : 0;
                tabBar.getElement().getParentElement().getStyle().setMarginRight(marginRight, Unit.PX);
            }
        });
    }

    /**
     * Create and attach the scroll button images with a click handler
     */
    private void initScrollButtons() {
        Image scrollLeftButtonImage = new Image(resources.back());
        Image scrollRightButtonImage = new Image(resources.next());
        Image menuButtonImage = new Image(resources.menu());
        int leftArrowWidth = scrollLeftButtonImage.getWidth();
        int rightArrowWidth = scrollRightButtonImage.getWidth();
        int menuWidth = menuButtonImage.getWidth();
        //panel for scroll buttons
        scrollPanel = new HorizontalPanel();
        panel.insert(scrollPanel, 0);
        panel.setWidgetTopHeight(scrollPanel, 0, Unit.PX, barHeight, barUnit);
        //placing scroll panel in the top right corner
        panel.setWidgetRightWidth(scrollPanel, 0, Unit.PX, leftArrowWidth + rightArrowWidth + menuWidth, Unit.PX);
        scrollPanel.setHeight("100%");
        scrollPanel.setWidth("100%");
        scrollPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        scrollPanel.setStyleName(SCROLL_PANEL_STYLE);

        SimplePanel scrollLeftButton = new SimplePanel(scrollLeftButtonImage);
        scrollLeftButton.setStyleName(SCROLL_BUTTON_STYLE);
        scrollLeftButtonImage.addClickHandler(createScrollLeftClickHandler());

        SimplePanel scrollRightButton = new SimplePanel(scrollRightButtonImage);
        scrollRightButton.setStyleName(SCROLL_BUTTON_STYLE);
        scrollRightButtonImage.addClickHandler(createScrollRightClickHandler());

        SimplePanel menuButton = new SimplePanel(menuButtonImage);
        menuButton.setStyleName(SCROLL_BUTTON_STYLE);
        menuButtonImage.addClickHandler(createShowMenuClickHandler(menuButtonImage));

        scrollPanel.add(scrollLeftButton);
        scrollPanel.add(scrollRightButton);
        scrollPanel.add(menuButton);
        scrollPanel.setVisible(false);
    }

    private ClickHandler createShowMenuClickHandler(final Image buttonImage) {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final MyPopup popup = new MyPopup();

                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                            public void setPosition(int offsetWidth, int offsetHeight) {
                                final int SCROLL_BUFFER = 20;
                                //coordinates of menu button for showing popup next to it
                                int left = buttonImage.getElement().getAbsoluteLeft();
                                int top = buttonImage.getElement().getAbsoluteBottom();

                                //to not get popup out of the window
                                if(offsetHeight > Window.getClientHeight()) {
                                    //difference between all popup widget height and its child widget height.
                                    //we need it because when we call popup.setHeight() it actually set height
                                    //of popup child widget rather than popup itself
                                    int diff = offsetHeight - popup.getWidget().getOffsetHeight();
                                    popup.setHeight(Window.getClientHeight() - diff + "px");
                                    top = 0;
                                    //we're about getting the scroll in popup,
                                    //so increase popup width to not get text wrapped
                                    popup.setWidth((popup.getWidget().getOffsetWidth() + SCROLL_BUFFER) + "px");
                                    offsetWidth = offsetWidth + SCROLL_BUFFER;
                                    //if not enough place under the button, lift popup up
                                } else if(Window.getClientHeight() < (top + offsetHeight)) {
                                    top = Window.getClientHeight() - offsetHeight;
                                }

                                //the same stuff but for the width
                                if(offsetWidth > Window.getClientWidth()) { //hard to imagine but just in case
                                    int diff = offsetWidth - popup.getWidget().getOffsetWidth();
                                    popup.setWidth(Window.getClientWidth() - diff + "px");
                                    left = 0;
                                    //if not enough place on the right of the button, shift popup to the left
                                } else if(Window.getClientWidth() < (left + offsetWidth)) {
                                    left = Window.getClientWidth() - offsetWidth;
                                }

                                popup.setPopupPosition(left, top);
                            }
                        });
                    }
                });
            }
        };
    }

    private ClickHandler createScrollRightClickHandler() {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int visibleAreaLeftBorder = Math.abs(getCurrentShift());
                int visibleAreaRightBorder = visibleAreaLeftBorder + getTabBarWidth();

                for(int i = 0; i < tabBar.getWidgetCount(); i++) {
                    int tabRightBorder = getRightPosition(tabBar.getWidget(i));

                    if(tabRightBorder > visibleAreaRightBorder) {
                        int diff = tabRightBorder - visibleAreaRightBorder;
                        adjustScroll(-diff);
                        return;
                    }
                }
            }
        };
    }

    private ClickHandler createScrollLeftClickHandler() {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int visibleAreaLeftBorder = Math.abs(getCurrentShift());

                for(int i = tabBar.getWidgetCount() - 1; i >= 0; i--) {
                    int tabLeftBorder = getLeftPosition(tabBar.getWidget(i));

                    if(tabLeftBorder < visibleAreaLeftBorder) {
                        if(i == 0) { //to show margin
                            scrollTo(0);
                            return;
                        }

                        int diff = visibleAreaLeftBorder - tabLeftBorder;
                        adjustScroll(diff);
                        return;
                    }
                }
            }
        };
    }

    private void adjustScroll(int diff) {
        Widget lastTab = getLastTab();
        if(lastTab == null)
            return;

        int newLeft = getCurrentShift() + diff;
        int rightOfLastTab = getRightPosition(lastTab);

        // Don't scroll for a positive newLeft
        if(newLeft <= 0) {
            // If we are about to scroll too far away from the right border, adjust back
            int gap = rightOfLastTab - getTabBarWidth();
            if(gap < -newLeft) {
                newLeft += -newLeft - gap;
            }
            scrollTo(newLeft);
        } else {
            scrollTo(0);
        }
    }

    private void scrollTo(int pos) {
        final int currentPos = getCurrentShift();
        final int diff = pos - currentPos;

        new Animation() {
            @Override
            protected void onUpdate(double progress) {
                tabBar.getElement().getStyle().setLeft(currentPos + diff * progress, Unit.PX);
            }
        }.run(600);
    }

    private boolean isScrollingNecessary() {
        Widget lastTab = getLastTab();
        return lastTab != null && getRightPosition(lastTab) > getTabBarWidth();
    }

    private int getRightPosition(Widget widget) {
        return widget.getElement().getOffsetLeft() + widget.getElement().getOffsetWidth();
    }

    private int getLeftPosition(Widget widget) {
        return widget.getElement().getOffsetLeft();
    }

    private int getCurrentShift() {
        return parsePosition(tabBar.getElement().getStyle().getLeft());
    }

    private int getTabBarWidth() {
        return tabBar.getElement().getParentElement().getClientWidth();
    }

    private int getScrollPanelWidth() {
        return scrollPanel.getElement().getParentElement().getClientWidth();
    }

    private Widget getLastTab() {
        if(tabBar.getWidgetCount() == 0)
            return null;

        return tabBar.getWidget(tabBar.getWidgetCount() - 1);
    }

    /**
     * get the int value from string, particularly css attribute value.
     * For example, for "-25px" returns -25
     *
     * @param positionString string to be parsed
     * @return parsed int
     */
    private static int parsePosition(String positionString) {
        if(positionString == null || positionString.isEmpty()) return 0;

        int position = 0;
        int sign = 1;
        int i = 0;
        if(positionString.charAt(0) == '-') {
            sign = -1;
            i++;
        }
        for(; i < positionString.length(); i++) {
            char c = positionString.charAt(i);
            if(c < '0' || c > '9')
                break;
            position = 10 * position + c - '0';
        }

        return sign * position;
    }

    private static Resources getDefaultResources() {
        if(DEFAULT_RESOURCES == null) {
            DEFAULT_RESOURCES = GWT.create(Resources.class);
        }
        return DEFAULT_RESOURCES;
    }

    private class MyPopup extends PopupPanel {

        public MyPopup() {
            // PopupPanel's constructor takes 'auto-hide' as its boolean parameter.
            // If this is set, the panel closes itself automatically when the user clicks outside of it.
            super(true);

            List<Widget> tabs = new ArrayList<Widget>();
            for(int i = 0; i < tabBar.getWidgetCount(); i++) {
                tabs.add(tabBar.getWidget(i));
            }

            CellTable<Widget> table = new CellTable<Widget>();
            table.setRowData(tabs);
            table.getElement().getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);

            final SingleSelectionModel<Widget> selectionModel = new SingleSelectionModel<Widget>();
            //todo find a way to set selected but not fire event
            //selectionModel.setSelected(tabBar.getWidget(ScrollableTabLayoutPanel.this.getSelectedIndex()), true);
            table.setSelectionModel(selectionModel);

            selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    Widget selected = selectionModel.getSelectedObject();
                    ScrolledTabLayoutPanel.this.selectTab(tabBar.getWidgetIndex(selected));
                    hide();
                }
            });

            Column<Widget, SafeHtml> column = new Column<Widget, SafeHtml>(new SafeHtmlCell()) {
                @Override
                public SafeHtml getValue(Widget object) {
                    return SafeHtmlUtils.fromSafeConstant(object.getElement().getInnerHTML());
                }
            };

            table.addColumn(column);

            //put the table into the scroll panel for the case if it height exceeds the window height
            setWidget(new ScrollPanel(table));
        }
    }

    public static interface Resources extends ClientBundle {
        @Source("backward_30.png")
        ImageResource back();

        @Source("forward_30.png")
        ImageResource next();

        @Source("menu_30.png")
        ImageResource menu();
    }
}