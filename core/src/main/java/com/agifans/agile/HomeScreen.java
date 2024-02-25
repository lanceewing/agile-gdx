package com.agifans.agile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.agifans.agile.config.AppConfig;
import com.agifans.agile.config.AppConfigItem;
import com.agifans.agile.ui.ConfirmResponseHandler;
import com.agifans.agile.ui.DialogHandler;
import com.agifans.agile.ui.ImportTypeResponseHandler;
import com.agifans.agile.ui.ImportType;
import com.agifans.agile.ui.OpenFileResponseHandler;
import com.agifans.agile.ui.PagedScrollPane;
import com.agifans.agile.ui.TextInputResponseHandler;
import com.agifans.agile.ui.ViewportManager;
import com.agifans.agile.util.StringUtils;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.payne.games.piemenu.AnimatedPieMenu;
import com.payne.games.piemenu.PieMenu;

/**
 * (Placeholder for the AGilE title screen and game selection pages)
 */
public class HomeScreen extends InputAdapter implements Screen {

    /**
     * The main Agile class. Allows us to easily change screens.
     */
    private Agile agile;
    
    private Skin skin;
    private Stage portraitStage;
    private Stage landscapeStage;
    private ViewportManager viewportManager;
    private Map<String, AppConfigItem> appConfigMap;
    private Map<String, Texture> buttonTextureMap;
    private Texture backgroundLandscape;
    private Texture backgroundPortrait;
    private MenuWidget portraitMenuWidget;
    private MenuWidget landscapeMenuWidget;
    private Texture whitePixelTexture;
    
    /**
     * Invoked by AGILE whenever it would like to show a dialog, such as when it needs
     * the user to confirm an action, or to choose a file.
     */
    private DialogHandler dialogHandler;
    
    /**
     * The InputProcessor for the Home screen. This is an InputMultiplexor, which includes 
     * both the Stage and the HomeScreen.
     */
    private InputMultiplexer portraitInputProcessor;
    private InputMultiplexer landscapeInputProcessor;
    
    /**
     * The name of the user preference that contains the home screen app list.
     */
    private static final String HOME_SCREEN_APP_LIST_PREF_NAME = "home_screen_app_list";
    
    /**
     * The default JSON to use when creating the home_screen_app_list preference for the
     * first time. This is the basis for starting to add apps to the home screen.
     */
    private static final String DEFAULT_APP_CONFIG_JSON = "{}";
    
    /**
     * Set containing the game IDs for all original Sierra AGI games.
     */
    private static final Set<String> SIERRA_GAMES = new HashSet<>();
    static {
        SIERRA_GAMES.add("bc");
        SIERRA_GAMES.add("dp");
        SIERRA_GAMES.add("gr");
        SIERRA_GAMES.add("kq1");
        SIERRA_GAMES.add("kq2");
        SIERRA_GAMES.add("kq3");
        SIERRA_GAMES.add("kq4");
        SIERRA_GAMES.add("lllll");
        SIERRA_GAMES.add("mh1");
        SIERRA_GAMES.add("mh2");
        SIERRA_GAMES.add("mg");
        SIERRA_GAMES.add("pq");
        SIERRA_GAMES.add("sq");
        SIERRA_GAMES.add("sq2");
    }
    
    /**
     * Constructor for HomeScreen.
     * 
     * @param agile The Agile instance.
     * @param dialogHandler 
     */
    public HomeScreen(Agile agile, DialogHandler dialogHandler) {
        this.agile = agile;
        this.dialogHandler = dialogHandler;

        AppConfig appConfig = loadAppConfig();
        appConfigMap = new TreeMap<String, AppConfigItem>();
        for (AppConfigItem appConfigItem : appConfig.getApps()) {
            appConfigMap.put(appConfigItem.getName(), appConfigItem);
        }

        buttonTextureMap = new HashMap<String, Texture>();
        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        skin.add("top", skin.newDrawable("default-round", new Color(0, 0, 0, 0)), Drawable.class);
        skin.add("empty", skin.newDrawable("default-round", new Color(1f, 1f, 1f, 0.1f)), Drawable.class);

        backgroundLandscape = new Texture("jpg/landscape_back.jpg");
        backgroundLandscape.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        backgroundPortrait = new Texture("jpg/portrait_back.jpg");
        backgroundPortrait.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        viewportManager = ViewportManager.getInstance();
        portraitStage = createStage(viewportManager.getPortraitViewport(), appConfig, 3, 5);
        landscapeStage = createStage(viewportManager.getLandscapeViewport(), appConfig, 5, 3);
        
        whitePixelTexture = createWhitePixelTexture();
        portraitMenuWidget = createMenuWidget(portraitStage);
        landscapeMenuWidget = createMenuWidget(landscapeStage);
        
        // The stage handles most of the input, but we need to handle the BACK button
        // separately.
        portraitInputProcessor = new InputMultiplexer();
        portraitInputProcessor.addProcessor(portraitStage);
        portraitInputProcessor.addProcessor(this);
        landscapeInputProcessor = new InputMultiplexer();
        landscapeInputProcessor.addProcessor(landscapeStage);
        landscapeInputProcessor.addProcessor(this);
    }
    
    private AppConfig loadAppConfig() {
        Json json = getJson();
        String appConfigJson = null;
        if (agile.getPreferences().contains(HOME_SCREEN_APP_LIST_PREF_NAME)) {
            appConfigJson = json.prettyPrint(agile.getPreferences().getString(HOME_SCREEN_APP_LIST_PREF_NAME));
        } else {
            if (Gdx.app.getType().equals(ApplicationType.WebGL)) {
                // First time use for web version, so load preconfigured file.
                appConfigJson = Gdx.files.internal("data/games.json").readString();
            } else {
                // Desktop currently empty to begin with.
                appConfigJson = DEFAULT_APP_CONFIG_JSON;
            }
        }
        agile.getPreferences().putString(HOME_SCREEN_APP_LIST_PREF_NAME, appConfigJson);
        return json.fromJson(AppConfig.class, appConfigJson);
    }
    
    private Texture createWhitePixelTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1,1,1,1);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
    
    private MenuWidget createMenuWidget(Stage stage) {
        PieMenu.PieMenuStyle style = new PieMenu.PieMenuStyle();
        style.sliceColor = new Color(0.6f, 0.6f, 0.6f, 0.9f);
        style.separatorWidth = 5;
        style.circumferenceWidth = 5;
        style.circumferenceColor = new Color(1f, 1f, 1f, 0.9f);
        style.separatorColor = style.circumferenceColor;
        
        MenuWidget widget = new MenuWidget(new TextureRegion(whitePixelTexture), style, skin);
        stage.addActor(widget);
        widget.setVisible(false);        
        
        return widget;
    }
    
    private Stage createStage(Viewport viewport, AppConfig appConfig, int columns, int rows) {
        Stage stage = new Stage(viewport);
        addAppButtonsToStage(stage, appConfig, columns, rows);
        return stage;
    }
    
    private MenuWidget getCurrentMenuWidget() {
        if (viewportManager.isPortrait()) {
            return portraitMenuWidget;
        }
        else {
            return landscapeMenuWidget;
        }
    }

    private void addAppButtonsToStage(Stage stage, AppConfig appConfig, int columns, int rows) {
        Table container = new Table();
        stage.addActor(container);
        container.setFillParent(true);

        int totalHorizPadding = 0;
        int horizPaddingUnit = 0;

        if (columns > rows) {
            // Landscape.
            container.setBackground(new Image(backgroundLandscape).getDrawable());
            totalHorizPadding = 1920 - (ICON_IMAGE_WIDTH * columns);
            horizPaddingUnit = totalHorizPadding / (columns * 2);
        } else {
            // Portrait.
            container.setBackground(new Image(backgroundPortrait).getDrawable());
            totalHorizPadding = 1080 - (ICON_IMAGE_WIDTH * columns);
            horizPaddingUnit = totalHorizPadding / (columns * 2);
        }
        
        PagedScrollPane pagedScrollPane = new PagedScrollPane();
        pagedScrollPane.setFlingTime(0.01f);

        int itemsPerPage = columns * rows;
        int pageItemCount = 0;

        Table currentPage = new Table().pad(0, 0, 0, 0);
        currentPage.defaults().pad(0, horizPaddingUnit, 0, horizPaddingUnit);

        // Add empty slot at the start that will always be present for adding a new game.
        AppConfigItem addGameIcon = new AppConfigItem();
        addGameIcon.setGameId("ADD_GAME");
        addGameIcon.setFileType("ADD");
        currentPage.add(buildAppButton(addGameIcon)).expand().fill();
        pageItemCount++;
        
        // Add the original Sierra AGI games first, then the fan made games.
        for (int loopCount=0; loopCount < 2; loopCount++) {
            for (AppConfigItem appConfigItem : appConfig.getApps()) {
                String gameId = appConfigItem.getGameId().toLowerCase();
                if (((loopCount == 0) && (SIERRA_GAMES.contains(gameId))) ||
                    ((loopCount == 1) && (!SIERRA_GAMES.contains(gameId)))) {
                    // Every itemsPerPage apps, add a new page.
                    if (pageItemCount == itemsPerPage) {
                        pagedScrollPane.addPage(currentPage);
                        pageItemCount = 0;
                        currentPage = new Table().pad(0, 0, 0, 0);
                        currentPage.defaults().pad(0, horizPaddingUnit, 0, horizPaddingUnit);
                    }
        
                    // Every number of columns apps, add a new row to the current page.
                    if ((pageItemCount % columns) == 0) {
                        currentPage.row();
                    }
        
                    currentPage.add(buildAppButton(appConfigItem)).expand().fill();
        
                    pageItemCount++;
                }
            }
        }

        // Add the last page of apps.
        if (pageItemCount <= itemsPerPage) {
            AppConfigItem appConfigItem = new AppConfigItem();
            for (int i = pageItemCount; i < itemsPerPage; i++) {
                if ((i % columns) == 0) {
                    currentPage.row();
                }
                currentPage.add(buildAppButton(appConfigItem)).expand().fill();
            }
            pagedScrollPane.addPage(currentPage);
            if (pageItemCount == itemsPerPage) {
                currentPage = new Table().pad(0, 0, 0, 0);
                currentPage.defaults().pad(0, horizPaddingUnit, 0, horizPaddingUnit);
                for (int i = 0; i < itemsPerPage; i++) {
                    if ((i % columns) == 0) {
                        currentPage.row();
                    }
                    currentPage.add(buildAppButton(appConfigItem)).expand().fill();
                }
                pagedScrollPane.addPage(currentPage);
            }
        }

        container.add(pagedScrollPane).expand().fill();
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(portraitInputProcessor);
        if (!Gdx.app.getType().equals(ApplicationType.WebGL)) {
            Gdx.graphics.setTitle("AGILE");
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (viewportManager.isPortrait()) {
            portraitStage.act(delta);
            portraitStage.draw();
        } else {
            landscapeStage.act(delta);
            landscapeStage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewportManager.update(width, height);
        if (viewportManager.isPortrait()) {
            Gdx.input.setInputProcessor(portraitInputProcessor);
        } else {
            Gdx.input.setInputProcessor(landscapeInputProcessor);
        }
    }

    @Override
    public void pause() {
        // Nothing to do.
    }

    @Override
    public void resume() {
        // Nothing to do.
    }

    @Override
    public void hide() {
        // Nothing to do.
    }

    @Override
    public void dispose() {
        portraitStage.dispose();
        landscapeStage.dispose();
        skin.dispose();

        for (Texture texture : buttonTextureMap.values()) {
            texture.dispose();
        }

        whitePixelTexture.dispose();
        
        saveAppConfigMap();
    }
    
    /**
     * Called when a key was released
     * 
     * @param keycode one of the constants in {@link Input.Keys}
     * 
     * @return whether the input was processed
     */
    public boolean keyUp(int keycode) {
        // Close the radial menu, if it is open.
        getCurrentMenuWidget().closeImmediately();
        
        float pageWidth = 0.0f;
        PagedScrollPane pagedScrollPane = null;
        if (viewportManager.isPortrait()) {
            Table table = (Table)portraitStage.getActors().get(0);
            pagedScrollPane = (PagedScrollPane)table.getChild(0);
            pageWidth = 1130.0f;
        }
        else {
            Table table = (Table)landscapeStage.getActors().get(0);
            pagedScrollPane = (PagedScrollPane)table.getChild(0);
            pageWidth = 1970.0f;
        }
        
        if (keycode == Keys.BACK) {
            if (Gdx.app.getType().equals(ApplicationType.Android)) {
                dialogHandler.confirm("Do you really want to Exit?", new ConfirmResponseHandler() {
                    public void yes() {
                        // Pressing BACK from the home screen will leave AGILE on Android.
                        Gdx.app.exit();
                    }

                    public void no() {
                        // Ignore. We're staying on the Home screen.
                    }
                });
                return true;
            }
        }
        else if (keycode == Keys.LEFT) {
            float newScrollX = MathUtils.clamp(pagedScrollPane.getScrollX() - pageWidth, 0, pagedScrollPane.getMaxX());
            pagedScrollPane.setScrollX(newScrollX);
            pagedScrollPane.setLastScrollX(newScrollX);
        }
        else if (keycode == Keys.RIGHT) {
            float newScrollX = MathUtils.clamp(pagedScrollPane.getScrollX() + pageWidth, 0, pagedScrollPane.getMaxX());
            pagedScrollPane.setScrollX(newScrollX);
            pagedScrollPane.setLastScrollX(newScrollX);
        }
        else if (keycode == Keys.UP) {
            pagedScrollPane.setScrollX(0.0f);
            pagedScrollPane.setLastScrollX(0.0f);
        }
        else if (keycode == Keys.DOWN) {
            pagedScrollPane.setScrollX(pagedScrollPane.getMaxX());
            pagedScrollPane.setLastScrollX(pagedScrollPane.getMaxX());
        }
        else if ((keycode >= Keys.A) && (keycode <= Keys.Z)) {
            // Shortcut keys for accessing games that start with each letter.
            // Keys.A is 29, Keys.Z is 54. ASCII is A=65, Z=90. So we add 36.
            int gameIndex = getIndexOfFirstGameStartingWithChar((char)(keycode + 36));
            if (gameIndex > -1) {
                // Add one to allow for the "Add Game" icon in the first slot.
                showGamePage(gameIndex + 1, false);
            }
        }
        return false;
    }

    /**
     * Draws and returns the icon to be used for game slots when we don't have
     * a proper screenshot icon for the identified game.
     * 
     * @param iconWidth
     * @param iconHeight
     * 
     * @return The Texture for the drawn icon.
     */
    public Texture drawEmptyIcon(int iconWidth, int iconHeight) {
        Pixmap pixmap = new Pixmap(iconWidth, iconHeight, Pixmap.Format.RGBA8888);
        Texture texture = new Texture(pixmap, Pixmap.Format.RGBA8888, false);
        pixmap.setColor(1.0f, 1.0f, 1.0f, 0.10f);
        pixmap.fill();
        texture.draw(pixmap, 0, 0);
        return texture;
    }
    
    private static final int ICON_IMAGE_WIDTH = 320;
    private static final int ICON_IMAGE_HEIGHT = 240;

    /**
     * Creates a button to represent the given AppConfigItem.
     * 
     * @param appConfigItem AppConfigItem containing details about the app to build a Button to represent.
     * 
     * @return The button to use for running the given AppConfigItem.
     */
    public Button buildAppButton(AppConfigItem appConfigItem) {
        Button button = new Button(skin);
        ButtonStyle style = button.getStyle();
        style.up = style.down = null;

        // An app button can contain an optional icon.
        Image icon = null;
        String iconPath = appConfigItem.getGameId() != null?
                StringUtils.format("screenshots/{0}.png", appConfigItem.getGameId().toUpperCase()) : "";
        
        Texture iconTexture = buttonTextureMap.get(iconPath);
        if (iconTexture == null) {
            if (!iconPath.isEmpty()) {
                try {
                    // See if there is screenshot icon in the assets folder.
                    Pixmap iconPixmap = new Pixmap(Gdx.files.internal(iconPath));
                    if ("UNK".equals(appConfigItem.getFileType())) {
                        iconPixmap.setColor(0.0f, 0.0f, 0.0f, 0.5f);
                        iconPixmap.fillRectangle(0, 0, iconPixmap.getWidth(), iconPixmap.getHeight());
                    }
                    
                    // If there is, then it's expected to be 320x200, so we scale it to right aspect ratio.
                    Pixmap iconStretchedPixmap = new Pixmap(ICON_IMAGE_WIDTH, ICON_IMAGE_HEIGHT, iconPixmap.getFormat());
                    iconStretchedPixmap.drawPixmap(iconPixmap,
                            0, 0, iconPixmap.getWidth(), iconPixmap.getHeight(),
                            0, 0, iconStretchedPixmap.getWidth(), iconStretchedPixmap.getHeight()
                    );
                    iconTexture = new Texture(iconStretchedPixmap);
                    iconPixmap.dispose();
                    iconStretchedPixmap.dispose();
                    
                    iconTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
                    buttonTextureMap.put(iconPath, iconTexture);
                    icon = new Image(iconTexture);
                    icon.setAlign(Align.center);
                } catch (Exception e) {
                    icon = new Image(drawEmptyIcon(ICON_IMAGE_WIDTH, ICON_IMAGE_HEIGHT));
                }
            } else {
                icon = new Image(drawEmptyIcon(ICON_IMAGE_WIDTH, ICON_IMAGE_HEIGHT));
            }
        } else {
            icon = new Image(iconTexture);
            icon.setAlign(Align.center);
        }
            
        if (icon != null) {
            Container<Image> iconContainer = new Container<Image>();
            iconContainer.setActor(icon);
            iconContainer.align(Align.center);
            button.stack(new Image(skin.getDrawable("top")), iconContainer).width(ICON_IMAGE_WIDTH)
                    .height(ICON_IMAGE_HEIGHT);
        }
        button.row();

        Label label = null;
        if ((appConfigItem.getDisplayName() == null) || appConfigItem.getDisplayName().trim().isEmpty()) {
            if ("ADD_GAME".equals(appConfigItem.getGameId())) {
                label = new Label("Add Game", skin);
            } else {
                label = new Label("[empty]", skin);
            }
            label.setColor(new Color(1f, 1f, 1f, 0.6f));
        } else {
            label = new Label(appConfigItem.getDisplayName(), skin);
            if ("UNK".equals(appConfigItem.getFileType())) {
                label.setColor(new Color(1f, 1f, 1f, 0.6f));
            }
        }
        label.setFontScale(2f);
        label.setAlignment(Align.top);
        label.setWrap(false);
        button.add(label).width(150).height(90).padTop(20);

        button.setName(appConfigItem.getName());
        button.addListener(appClickListener);
        button.addListener(appGestureListener);
        return button;
    }

    /**
     * Converts the given Map of AppConfigItems to an AppConfig instance.
     * 
     * @param appConfigMap The Map of AppConfigItems to convert.
     * 
     * @return The AppConfig.
     */
    private AppConfig convertAppConfigItemMapToAppConfig(Map<String, AppConfigItem> appConfigMap) {
        AppConfig appConfig = new AppConfig();
        for (String appName : appConfigMap.keySet()) {
            AppConfigItem item = appConfigMap.get(appName);
            if ((item.getFileType() != null) && (!item.getFileType().trim().isEmpty())) {
                appConfig.getApps().add(item);
            }
        }
        return appConfig;
    }

    /**
     * Converts the AppConfigItem Map to JSON and stores in the associated
     * preference.
     */
    private void saveAppConfigMap() {
        AppConfig appConfig = convertAppConfigItemMapToAppConfig(appConfigMap);
        String appConfigJson = getJson().prettyPrint(appConfig);
        agile.getPreferences().putString(HOME_SCREEN_APP_LIST_PREF_NAME, appConfigJson);
    }

    /**
     * Gets an instance of the libgdx Json class to use to serialise and deserialise the
     * list of games.
     * 
     * @return
     */
    private Json getJson() {
        Json json = new Json();
        json.setOutputType(OutputType.json);
        return json;
    }
    
    /**
     * Updates the application buttons on the home screen Stages to reflect the
     * current AppConfigItem Map.
     */
    public void updateHomeScreenButtonStages() {
        AppConfig appConfig = convertAppConfigItemMapToAppConfig(appConfigMap);
        portraitStage.clear();
        landscapeStage.clear();
        addAppButtonsToStage(portraitStage, appConfig, 3, 5);
        addAppButtonsToStage(landscapeStage, appConfig, 5, 3);
        saveAppConfigMap();
        agile.getPreferences().flush();
        portraitStage.addActor(portraitMenuWidget);
        landscapeStage.addActor(landscapeMenuWidget);
    }

    public ActorGestureListener appGestureListener = new ActorGestureListener() {
        public boolean longPress(final Actor actor, float x, float y) {
            String appName = actor.getName();
            if ((appName != null) && (!appName.equals(""))) {
                final AppConfigItem appConfigItem = appConfigMap.get(appName);
                if (appConfigItem != null) {
                    MenuWidget menu = getCurrentMenuWidget();
                    if (!menu.isOpen()) {
                        menu.open(appConfigItem, actor.getX(Align.center), actor.getY(Align.center) + 35);
                    }
                }
            }
            return true;
        }

        public void fling(InputEvent event, float velocityX, float velocityY, int button) {
            appConfigMap.remove(event.getListenerActor().getName());
            updateHomeScreenButtonStages();
        }
    };

    /**
     * Handle clicking an app button. This will start the AGI interpreter and run the
     * selected AGI game.
     */
    public ClickListener appClickListener = new ClickListener(-1) {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            Actor actor = event.getListenerActor();
            String appName = actor.getName();
            MenuWidget menu = getCurrentMenuWidget();
            
            if (event.getButton() == Buttons.RIGHT) {
                if ((appName != null) && (!appName.equals(""))) {
                    final AppConfigItem appConfigItem = appConfigMap.get(appName);
                    if (appConfigItem != null) {
                        if (!menu.isOpen()) {
                            menu.open(appConfigItem, actor.getX(Align.center), actor.getY(Align.center) + 35);
                        }
                    }
                }
            } 
            else {
                if (menu.isOpen()) {
                    if ((appName == null) || !appName.equals(menu.getGameName())) {
                        menu.close();
                    }
                } else {
                    if ((appName != null) && (!appName.equals(""))) {
                        final AppConfigItem appConfigItem = appConfigMap.get(appName);
                        if (appConfigItem != null) {
                            if ("UNK".equals(appConfigItem.getFileType())) {
                                // Known game but hasn't yet been imported.
                                importGame(appConfigItem);
                            } else {
                                GameScreen gameScreen = agile.getGameScreen();
                                gameScreen.initGame(appConfigItem);
                                agile.setScreen(gameScreen);
                            }
                        }
                    } else {
                        // Add miscellaneous game option (i.e. the plus icon).
                        importGame(null);
                    }
                }
            }
        }
    };
    
    private void importGame(AppConfigItem appConfigItem) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // Start by determining the type of import.
                dialogHandler.promptForImportType(appConfigItem, new ImportTypeResponseHandler() {
                    @Override
                    public void importTypeResult(boolean success, ImportType importType) {
                        if (success) {
                            // TODO: Add support for URL fetch?
                            importGameUsingOpenFileDialog(appConfigItem, importType);
                        }
                    }
                });
            }
        });
    }
    
    private void importGameUsingOpenFileDialog(AppConfigItem appConfigItem, ImportType importType) {
        String startPath = agile.getPreferences().getString("open_app_start_path", null);
        dialogHandler.openFileDialog(appConfigItem, importType.name(), "", startPath, new OpenFileResponseHandler() {
            @Override
            public void openFileResult(boolean success, String filePath, String gameName, String gameId) {
                if (success && (filePath != null) && (!filePath.isEmpty())) {
                    if (!Gdx.app.getType().equals(ApplicationType.WebGL)) {
                        // GWT/HTML5/WEBGL doesn't support FileHandle and doesn't need it anyway.
                        FileHandle fileHandle = new FileHandle(filePath);
                        agile.getPreferences().putString("open_app_start_path", fileHandle.parent().path());
                        agile.getPreferences().flush();
                    }
                    final String appConfigFilePath = filePath;
                    dialogHandler.promptForTextInput("Confirm name of AGI game", gameName,
                        new TextInputResponseHandler() {
                            @Override
                            public void inputTextResult(boolean success, String text) {
                                if (success) {
                                    AppConfigItem appConfigItem = new AppConfigItem();
                                    appConfigItem.setGameId(gameId);
                                    appConfigItem.setName(text);
                                    appConfigItem.setFilePath(appConfigFilePath);
                                    if (Gdx.app.getType().equals(ApplicationType.WebGL)) {
                                        appConfigItem.setFileType("GAMEFILES.DAT");
                                    } else {
                                        if (appConfigFilePath.toLowerCase().endsWith(".zip")) {
                                            appConfigItem.setFileType("ZIP");
                                        }
                                        else {
                                            appConfigItem.setFileType("DIR");
                                        }
                                    }
                                    appConfigMap.put(appConfigItem.getName(), appConfigItem);
                                    updateHomeScreenButtonStages();
                                    showGamePage(appConfigItem);
                                }
                            }
                        });
                }
            }
        });
    }
    
    private int getIndexOfFirstGameStartingWithChar(char letter) {
        // Ignore the original Sierra AGI games, as they're all on the first page.
        int gameIndex = 14;
        for (String gameName : appConfigMap.keySet()) {
            gameIndex++;
            if (gameName.toUpperCase().startsWith("" + letter)) {
                return gameIndex;
            }
        }
        return -1;
    }
    
    private int getGameIndex(AppConfigItem appConfigItem) {
        int gameIndex = 0;
        for (String gameName : appConfigMap.keySet()) {
            gameIndex++;
            if (gameName.equals(appConfigItem.getName())) {
                return gameIndex;
            }
        }
        return 0;
    }
    
    private void showGamePage(AppConfigItem appConfigItem) {
        showGamePage(getGameIndex(appConfigItem), false);
    }
    
    private void showGamePage(int gameIndex, boolean skipScroll) {
        // Work out how far to move from far left to get to game's page.
        float pageWidth = viewportManager.isPortrait()? 1130.0f : 1970.0f;
        float newScrollX = pageWidth * (gameIndex / 15);
        
        // Apply scroll X without animating, i.e. move immediately to the page.
        Stage currentStage = viewportManager.isPortrait()? portraitStage : landscapeStage;
        PagedScrollPane pagedScrollPane = (PagedScrollPane)
                ((Table)currentStage.getActors().get(0)).getChild(0);
        currentStage.act(0f);
        pagedScrollPane.setScrollX(newScrollX);
        pagedScrollPane.setLastScrollX(newScrollX);
        if (skipScroll) {
            pagedScrollPane.updateVisualScroll();
        }
    }
    
    /**
     * An AnimatedPieMenu (i.e. radial menu) for interacting with games on the home screen.
     */
    private class MenuWidget extends AnimatedPieMenu {
        
        private AppConfigItem appConfigItem;
        
        /**
         * Constructor for MenuWidget.
         * 
         * @param whitePixel
         * @param style
         * @param labelSkin 
         */
        public MenuWidget(TextureRegion whitePixel, PieMenu.PieMenuStyle style, Skin labelSkin) {
            super(whitePixel, style, 160, 40f/160, 315, 270);
            
            Label playLabel = new Label("Run", labelSkin);
            playLabel.setFontScale(2f);
            playLabel.setAlignment(Align.center);
            addActor(playLabel);
            
            Label deleteLabel = new Label("Delete", labelSkin);
            deleteLabel.setFontScale(2f);
            deleteLabel.setAlignment(Align.center);
            addActor(deleteLabel);
            
            Label editLabel = new Label("Edit", labelSkin);
            editLabel.setFontScale(2f);
            editLabel.setAlignment(Align.center);
            addActor(editLabel);
            
            addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // Note: Sometimes this gets called when the selectedIndex is -1
                    if (getSelectedIndex() >= 0) {
                        switch (getSelectedIndex()) {
                            case 0:
                                runGame();
                                break;
                                
                            case 1:
                                deleteGame();
                                break;
                                
                            case 2:
                                editGame();
                                break;
                                
                            default:
                                break;
                        }
                    }
                }
            });
        }
        
        private void runGame() {
            AppConfigItem gameToRun = appConfigItem;
            closeImmediately();
            
            GameScreen gameScreen = agile.getGameScreen();
            gameScreen.initGame(gameToRun);
            agile.setScreen(gameScreen);
        }
        
        private void deleteGame() {
            AppConfigItem gameToDelete = appConfigItem;
            closeImmediately();
            dialogHandler.confirm(
                    "Do you want to remove '" + gameToDelete.getName() + "'?", 
                    new ConfirmResponseHandler() {
                @Override
                public void yes() {
                    int gameIndexBeforeClose = getGameIndex(gameToDelete);
                    appConfigMap.remove(gameToDelete.getName());
                    // TODO: GWT needs to remove data from OPFS.
                    Gdx.app.postRunnable(new Runnable(){
                        @Override
                        public void run() {
                            updateHomeScreenButtonStages();
                            showGamePage(gameIndexBeforeClose, true);
                        }
                    });
                }
                
                @Override
                public void no() {
                }
            });
        }
        
        private void editGame() {
            AppConfigItem gameToEdit = appConfigItem;
            closeImmediately();
            String displayName = gameToEdit.getDisplayName().replace("\n", "\\n");
            dialogHandler.promptForTextInput("Program display name", displayName,
                    new TextInputResponseHandler() {
                        @Override
                        public void inputTextResult(boolean success, String text) {
                            if (success && (text != null) & !text.isEmpty()) {
                                String oldName = gameToEdit.getName();
                                String displayName = text.replace("\\n", "\n");
                                String name = text.replace("\\n", " ").replaceAll(" +", " ");
                                gameToEdit.setName(name);
                                gameToEdit.setDisplayName(displayName);
                                // If the name has changed, we need to change the key in the Map.
                                appConfigMap.remove(oldName);
                                appConfigMap.put(gameToEdit.getName(), gameToEdit);
                                int gameIndexBeforeClose = getGameIndex(gameToEdit);
                                Gdx.app.postRunnable(new Runnable(){
                                    @Override
                                    public void run() {
                                        updateHomeScreenButtonStages();
                                        showGamePage(gameIndexBeforeClose, true);
                                    }
                                });
                            }
                        }
                    });
        }
        
        public String getGameName() {
            return (appConfigItem != null? appConfigItem.getName() : "");
        }
        
        public boolean isOpen() {
            return (appConfigItem != null);
        }
        
        public void open(AppConfigItem appConfigItem, float x, float y) {
            if (!isOpen()) {
                this.appConfigItem = appConfigItem;
                this.setPosition(x, y, Align.center);
                this.transitionToOpening(0.7f);
            }
        }
        
        public void close() {
            close(0.7f);
        }
        
        public void closeImmediately() {
            close(0.0f);
        }
        
        public void close(float durationSeconds) {
            if (isOpen()) {
                this.appConfigItem = null;
                this.transitionToClosing(durationSeconds);
            }
        }
    }
}
