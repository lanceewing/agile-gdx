package com.agifans.agile.ui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public class PagedScrollPane extends ScrollPane {

    private boolean wasPanDragFling = false;

    private float lastScrollX = 0;

    private WindowedMean scrollXDeltaMean = new WindowedMean(5);

    private Table content;

    public PagedScrollPane() {
        super(null);
        setup();
    }

    public PagedScrollPane(Skin skin) {
        super(null, skin);
        setup();
    }

    public PagedScrollPane(Skin skin, String styleName) {
        super(null, skin, styleName);
        setup();
    }

    public PagedScrollPane(Actor widget, ScrollPaneStyle style) {
        super(null, style);
        setup();
    }

    private void setup() {
        content = new Table();
        content.defaults().space(50);
        super.setWidget(content);
    }

    public void addPages(Actor... pages) {
        for (Actor page : pages) {
            content.add(page).expandY().fillY();
        }
    }

    public void addPage(Actor page) {
        content.add(page).expandY().fillY();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (wasPanDragFling && !isPanning() && !isDragging() && !isFlinging()) {
            wasPanDragFling = false;
            scrollToPage();
            scrollXDeltaMean.clear();

        } else {
            if (isPanning() || isDragging() || isFlinging()) {
                wasPanDragFling = true;
                scrollXDeltaMean.addValue(getScrollX() - lastScrollX);
                lastScrollX = getScrollX();
            }
        }
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        if (content != null) {
            for (Cell cell : content.getCells()) {
                cell.width(width);
            }
            content.invalidate();
        }
    }

    public void setPageSpacing(float pageSpacing) {
        if (content != null) {
            content.defaults().space(pageSpacing);
            for (Cell cell : content.getCells()) {
                cell.space(pageSpacing);
            }
            content.invalidate();
        }
    }

    public void setLastScrollX(float lastScrollX) {
        this.lastScrollX = lastScrollX;
    }

    public void reset() {
        this.scrollXDeltaMean.clear();
        this.lastScrollX = 0;
        this.wasPanDragFling = false;
        setScrollX(0);
    }
    
    public int getGamesPerPage() {
        int gamesPerPage = 0;
        if (content.getChildren().notEmpty()) {
            Table secondPage = (Table)content.getChild(1);
            gamesPerPage = secondPage.getColumns() * secondPage.getRows();
        }
        return gamesPerPage;
    }
    
    public int getNumOfPages() {
        return content.getChildren().size;
    }
    
    public int getCurrentPageNumber() {
        int pageNumber = 0;
        if (content.getChildren().notEmpty()) {
            int pageWidth = (int)(content.getChild(0).getWidth() + 50);
            pageNumber = Math.round(getScrollX() / pageWidth);
        }
        return pageNumber;
    }

    private void scrollToPage() {
        final float width = getWidth();
        final float scrollX = getScrollX();
        final float maxX = getMaxX();

        if (scrollX >= maxX || scrollX <= 0)
            return;

        Array<Actor> pages = content.getChildren();
        float pageX = 0;
        float pageWidth = 0;

        float scrollXDir = scrollXDeltaMean.getMean();
        if (scrollXDir == 0) {
            scrollXDir = scrollXDeltaMean.getLatest();
        }

        for (Actor a : pages) {
            pageX = a.getX();
            pageWidth = a.getWidth();
            if (scrollXDir > 0) {
                if (scrollX < (pageX + pageWidth * 0.1)) {
                    break;
                }
            } else if (scrollXDir < 0) {
                if (scrollX < (pageX + pageWidth * 0.9)) {
                    break;
                }
            } else {
                if (scrollX < (pageX + pageWidth * 0.5)) {
                    break;
                }
            }
        }

        float newScrollX = MathUtils.clamp(pageX - (width - pageWidth) / 2, 0, maxX);
        setScrollX(newScrollX);
        this.lastScrollX = newScrollX;
    }
}
