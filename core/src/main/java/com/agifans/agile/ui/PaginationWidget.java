package com.agifans.agile.ui;

import com.agifans.agile.HomeScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * A widget from drawing the pagination at the bottom of the PagedScrollPane.
 */
public class PaginationWidget extends Widget {
    
    private static final int PAGINATION_BAR_HEIGHT = 60;
    
    private static final int ICON_SIZE = 50;
    
    private static final int CIRCLE_DIAMETER = 16;
    
    private static final int CIRCLE_RADIUS = CIRCLE_DIAMETER / 2;

    private HomeScreen homeScreen;
    
    private Pixmap pixmap;
    
    private Texture texture;
    
    private Pixmap nextIconPixmap;
    
    private Pixmap prevIconPixmap;
    
    private int width;
    
    /**
     * Constructor for PaginationWidget.
     * 
     * @param homeScreen 
     * @param width 
     */
    public PaginationWidget(HomeScreen homeScreen, float width) {
        this.homeScreen = homeScreen;
        this.width = (int)width;
        
        prevIconPixmap = new Pixmap(Gdx.files.internal("png/prev.png"));
        nextIconPixmap = new Pixmap(Gdx.files.internal("png/next.png"));
        pixmap = new Pixmap((int)width, PAGINATION_BAR_HEIGHT, Pixmap.Format.RGBA8888);
        texture = new Texture(pixmap, Pixmap.Format.RGBA8888, false);
        
        setSize(getPrefWidth(), getPrefHeight());

        addListener(new ClickListener(-1) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //System.out.println("clicked: " + x + ", y: " + y);
                if (y < PAGINATION_BAR_HEIGHT) {
                    if (x < ICON_SIZE) {
                        homeScreen.keyUp(Keys.LEFT);
                    }
                    if (x > (width - ICON_SIZE)) {
                        homeScreen.keyUp(Keys.RIGHT);
                    }
                }
            }
        });
    }
    
    public void draw(Batch batch, float parentAlpha) {
        validate();

        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
        
        pixmap.setColor(1.0f, 1.0f, 1.0f, 0.10f);
        pixmap.fill();
        
        PagedScrollPane pagedScrollPane = homeScreen.getPagedScrollPane();
        if (pagedScrollPane != null) {
            int numOfPages = pagedScrollPane.getNumOfPages();
            if (numOfPages > 0) {
                int gapBetweenCircles = Math.min((width - (PAGINATION_BAR_HEIGHT * 10)) / numOfPages, 20);
                int totalCirclesWidth = (numOfPages * (CIRCLE_DIAMETER + gapBetweenCircles)) - gapBetweenCircles;
                
                int currentPage = pagedScrollPane.getCurrentPageNumber();
                if (currentPage > 0) {
                    pixmap.drawPixmap(prevIconPixmap, 0, 5);
                }
                
                for (int pageNum=0; pageNum < numOfPages; pageNum++) {
                    if (pageNum == currentPage) {
                        pixmap.setColor(1.0f, 1.0f, 1.0f, 0.4f);
                    } else {
                        pixmap.setColor(1.0f, 1.0f, 1.0f, 0.2f);
                    }
                    pixmap.fillCircle(
                            ((width / 2) - (totalCirclesWidth / 2)) + CIRCLE_RADIUS + 
                            (pageNum * (CIRCLE_DIAMETER + gapBetweenCircles)), 
                            PAGINATION_BAR_HEIGHT / 2, 
                            CIRCLE_RADIUS);
                }
                
                if (currentPage < (numOfPages - 1)) {
                    pixmap.drawPixmap(nextIconPixmap, width - ICON_SIZE, 5);
                }
            }
        }
        
        texture.draw(pixmap, 0, 0);
        
        batch.draw(texture, 0, 0);
    }
    
    public float getPrefWidth () {
        return width;
    }

    public float getPrefHeight () {
        return PAGINATION_BAR_HEIGHT;
    }
    
    public float getMaxHeight() {
        return PAGINATION_BAR_HEIGHT;
    }
    
    public void dispose() {
        texture.dispose();
        pixmap.dispose();
        nextIconPixmap.dispose();
        prevIconPixmap.dispose();
    }
}
