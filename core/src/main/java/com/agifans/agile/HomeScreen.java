package com.agifans.agile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.agifans.agile.config.AppConfig;
import com.agifans.agile.config.AppConfigItem;
import com.agifans.agile.config.AppConfigItem.FileLocation;
import com.agifans.agile.ui.ConfirmResponseHandler;
import com.agifans.agile.ui.DialogHandler;
import com.agifans.agile.ui.OpenFileResponseHandler;
import com.agifans.agile.ui.PagedScrollPane;
import com.agifans.agile.ui.TextInputResponseHandler;
import com.agifans.agile.ui.ViewportManager;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.Viewport;

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
     * The default JSON to use when creating the home_screen_app_list preference for the
     * first time. This is the basis for starting to add apps to the home screen.
     */
    private static final String DEFAULT_APP_CONFIG_JSON = "{}";
    
    /**
     * Constructor for HomeScreen.
     * 
     * @param agile The Agile instance.
     * @param dialogHandler 
     */
    public HomeScreen(Agile agile, DialogHandler dialogHandler) {
        this.agile = agile;
        this.dialogHandler = dialogHandler;

        // Load the app meta data.
        Json json = new Json();
        //String appConfigJson = Gdx.files.internal("data/programs.json").readString();
        String appConfigJson =
            agile.getPreferences().getString("home_screen_app_list", DEFAULT_APP_CONFIG_JSON);
        agile.getPreferences().putString("home_screen_app_list", appConfigJson);
        AppConfig appConfig = json.fromJson(AppConfig.class, appConfigJson);
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

        // The stage handles most of the input, but we need to handle the BACK button
        // separately.
        portraitInputProcessor = new InputMultiplexer();
        portraitInputProcessor.addProcessor(portraitStage);
        portraitInputProcessor.addProcessor(this);
        landscapeInputProcessor = new InputMultiplexer();
        landscapeInputProcessor.addProcessor(landscapeStage);
        landscapeInputProcessor.addProcessor(this);
    }
    
    private Stage createStage(Viewport viewport, AppConfig appConfig, int columns, int rows) {
        Stage stage = new Stage(viewport);
        addAppButtonsToStage(stage, appConfig, columns, rows);
        return stage;
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
        
        PagedScrollPane scroll = new PagedScrollPane();
        scroll.setFlingTime(0.01f);

        int itemsPerPage = columns * rows;
        int pageItemCount = 0;

        Table currentPage = new Table().pad(0, 0, 0, 0);
        currentPage.defaults().pad(0, horizPaddingUnit, 0, horizPaddingUnit);

        for (AppConfigItem appConfigItem : appConfig.getApps()) {
            // Every itemsPerPage apps, add a new page.
            if (pageItemCount == itemsPerPage) {
                scroll.addPage(currentPage);
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

        // Add the last page of apps.
        if (pageItemCount <= itemsPerPage) {
            AppConfigItem appConfigItem = new AppConfigItem();
            for (int i = pageItemCount; i < itemsPerPage; i++) {
                if ((i % columns) == 0) {
                    currentPage.row();
                }
                currentPage.add(buildAppButton(appConfigItem)).expand().fill();
            }
            scroll.addPage(currentPage);
            if (pageItemCount == itemsPerPage) {
                currentPage = new Table().pad(0, 0, 0, 0);
                currentPage.defaults().pad(0, horizPaddingUnit, 0, horizPaddingUnit);
                for (int i = 0; i < itemsPerPage; i++) {
                    if ((i % columns) == 0) {
                        currentPage.row();
                    }
                    currentPage.add(buildAppButton(appConfigItem)).expand().fill();
                }
                scroll.addPage(currentPage);
            }
        }

        container.add(scroll).expand().fill();
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(portraitInputProcessor);
        Gdx.graphics.setTitle("AGILE");
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
        if ((appConfigItem.getIconPath() != null) && (!appConfigItem.getIconPath().equals(""))) {
            Texture iconTexture = buttonTextureMap.get(appConfigItem.getIconPath());
            if (iconTexture == null) {
                try {
                    iconTexture = new Texture(appConfigItem.getIconPath());
                    iconTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
                    buttonTextureMap.put(appConfigItem.getIconPath(), iconTexture);
                    icon = new Image(iconTexture);
                    icon.setAlign(Align.center);
                } catch (Exception e) {
                    icon = new Image(drawEmptyIcon(ICON_IMAGE_WIDTH, ICON_IMAGE_HEIGHT));
                    icon.setAlign(Align.center);
                }
            } else {
                icon = new Image(iconTexture);
                icon.setAlign(Align.center);
            }
        } else {
            // See if there is screenshot in the screenshot store.
            String friendlyAppName = appConfigItem.getName().replaceAll("[ ,\n/\\:;*?\"<>|!]", "_");
            String screenshotData = agile.getScreenshotStore().getString(friendlyAppName, "");
            if (screenshotData != "") {
                //try {
                //    byte[] decodedScreenshotData = Base64Coder.decode(screenshotData);
                //    Pixmap screenshotPixmap = new Pixmap(decodedScreenshotData, 0, decodedScreenshotData.length);
                //    Texture screenshotTexture = new Texture(screenshotPixmap);
                //    screenshotTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
                //    icon = new Image(screenshotTexture);
                //    icon.setAlign(Align.center);
                //} catch (Exception e) {
                    icon = new Image(drawEmptyIcon(ICON_IMAGE_WIDTH, ICON_IMAGE_HEIGHT));// ,                                                                                           // 110));
                //}
            } else {
                icon = new Image(drawEmptyIcon(ICON_IMAGE_WIDTH, ICON_IMAGE_HEIGHT));// ,                                                                                                // 110));
            }
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
            label = new Label("[empty]", skin);
            label.setColor(new Color(1f, 1f, 1f, 0.6f));
        } else {
            label = new Label(appConfigItem.getDisplayName(), skin);
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
                // Tape or Disk file.
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
        Json json = new Json();
        String appConfigJson = json.toJson(appConfig);
        agile.getPreferences().putString("home_screen_app_list", appConfigJson);
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
    }

    public ActorGestureListener appGestureListener = new ActorGestureListener() {
        public boolean longPress(final Actor actor, float x, float y) {
            actor.debug();
            String appName = actor.getName();
            if ((appName != null) && (!appName.equals(""))) {
                final AppConfigItem appConfigItem = appConfigMap.get(appName);
                if (appConfigItem != null) {
                    longPressActor = actor;
                    String displayName = appConfigItem.getDisplayName().replace("\n", "\\n");
                    dialogHandler.promptForTextInput("Program display name", displayName,
                            new TextInputResponseHandler() {
                                @Override
                                public void inputTextResult(boolean success, String text) {
                                    if (success && (text != null) & !text.isEmpty()) {
                                        String displayName = text.replace("\\n", "\n");
                                        String name = text.replace("\\n", " ").replaceAll(" +", " ");
                                        appConfigItem.setName(name);
                                        appConfigItem.setDisplayName(displayName);
                                        updateHomeScreenButtonStages();
                                    }
                                    actor.setDebug(false);
                                }
                            });
                }
            }
            return true;
        }

        public void fling(InputEvent event, float velocityX, float velocityY, int button) {
            appConfigMap.remove(event.getListenerActor().getName());
            updateHomeScreenButtonStages();
        }
    };

    private Actor longPressActor;

    /**
     * Handle clicking an app button. This will start the Machine and run the
     * selected app.
     */
    public ClickListener appClickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            String appName = event.getListenerActor().getName();
            if ((appName != null) && (!appName.equals(""))) {
                final AppConfigItem appConfigItem = appConfigMap.get(appName);
                if (appConfigItem != null) {
                    if (longPressActor != null) {
                        longPressActor = null;
                    }
                    else {
                        GameScreen gameScreen = agile.getGameScreen();
                        gameScreen.initGame(appConfigItem);
                        agile.setScreen(gameScreen);
                    }
                }
            } else {
                String startPath = agile.getPreferences().getString("open_app_start_path", null);
                dialogHandler.openFileDialog("", startPath, new OpenFileResponseHandler() {
                    
                    @Override
                    public void openFileResult(boolean success, String filePath, String gameName) {
                        if (success && (filePath != null) && (!filePath.isEmpty())) {
                            if (!Gdx.app.getType().equals(ApplicationType.WebGL)) {
                                // GWT/HTML5/WEBGL doesn't support FileHandle and doesn't need it anyway.
                                FileHandle fileHandle = new FileHandle(filePath);
                                filePath = "file:" + slashify(filePath, fileHandle.isDirectory());
                                agile.getPreferences().putString("open_app_start_path", fileHandle.parent().path());
                                agile.getPreferences().flush();
                            }
                            final String appConfigFilePath = filePath;
                            dialogHandler.promptForTextInput("Program name", gameName,
                                new TextInputResponseHandler() {
                                    @Override
                                    public void inputTextResult(boolean success, String text) {
                                        if (success) {
                                            AppConfigItem appConfigItem = new AppConfigItem();
                                            appConfigItem.setName(text);
                                            appConfigItem.setFilePath(appConfigFilePath);
                                            if (Gdx.app.getType().equals(ApplicationType.WebGL)) {
                                                appConfigItem.setFileLocation(FileLocation.OPFS);
                                                appConfigItem.setFileType("GAMEFILES.DAT");
                                            } else {
                                                appConfigItem.setFileLocation(FileLocation.ABSOLUTE);
                                                if (appConfigFilePath.toLowerCase().endsWith(".zip")) {
                                                    appConfigItem.setFileType("ZIP");
                                                }
                                                else if (appConfigFilePath.toLowerCase().endsWith(".dsk")) {
                                                    appConfigItem.setFileType("DISK");
                                                }
                                                else {
                                                    appConfigItem.setFileType("DIR");
                                                }
                                            }
                                            appConfigMap.put(appConfigItem.getName(), appConfigItem);
                                            updateHomeScreenButtonStages();
                                        }
                                    }
                                });
                        }
                    }
                });
            }
        }
    };
    
    // Copied for java.io.File. GWT version doesn't have this in the emulated File class.
    private static String slashify(String path, boolean isDirectory) {
        String p = path;
        if (File.separatorChar != '/')
            p = p.replace(File.separatorChar, '/');
        if (!p.startsWith("/"))
            p = "/" + p;
        if (!p.endsWith("/") && isDirectory)
            p = p + "/";
        return p;
    }
}
