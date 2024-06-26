package com.agifans.agile.lwjgl3;

import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import com.agifans.agile.Detection;
import com.agifans.agile.agilib.Game;
import com.agifans.agile.config.AppConfigItem;
import com.agifans.agile.ui.ConfirmResponseHandler;
import com.agifans.agile.ui.DialogHandler;
import com.agifans.agile.ui.ImportTypeResponseHandler;
import com.agifans.agile.ui.ImportType;
import com.agifans.agile.ui.OpenFileResponseHandler;
import com.agifans.agile.ui.TextInputResponseHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

/**
 * Desktop implementation of the DialogHandler interface.
 */
public class DesktopDialogHandler implements DialogHandler {
    
    private boolean dialogOpen;
    
    private Icon importIcon;
    private Icon exportIcon;
    private Icon clearIcon;
    private Icon resetIcon;
    
    private BufferedImage toBufferedImage(Pixmap pixmap) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PixmapIO.PNG writer = new PixmapIO.PNG(pixmap.getWidth() * pixmap.getHeight() * 4);
            try {
                writer.setFlipY(false);
                writer.setCompression(Deflater.NO_COMPRESSION);
                writer.write(baos, pixmap);
            } finally {
                writer.dispose();
            }

            return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
        }
    }
    
    private Image loadImage(String imagePath) {
        try {
            Pixmap iconPixmap = new Pixmap(Gdx.files.internal(imagePath));
            BufferedImage image = toBufferedImage(iconPixmap);
            iconPixmap.dispose();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Icon loadIcon(String iconPath) {
        Image image = loadImage(iconPath);
        if (image != null) {
            return new ImageIcon(image);
        } else {
            return null;
        }
    }
    
    private Icon getImportIcon() {
        if (importIcon == null) {
            importIcon = loadIcon("png/import.png");
        }
        return importIcon;
    }
    
    private Icon getExportIcon() {
        if (exportIcon == null) {
            exportIcon = loadIcon("png/export.png");
        }
        return exportIcon;
    }
    
    private Icon getClearIcon() {
        if (clearIcon == null) {
            clearIcon = loadIcon("png/clear.png");
        }
        return clearIcon;
    }
    
    private Icon getResetIcon() {
        if (resetIcon == null) {
            resetIcon = loadIcon("png/reset.png");
        }
        return resetIcon;
    }
    
    @Override
    public void confirm(final String message, final ConfirmResponseHandler responseHandler) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                dialogOpen = true;
                int output = JOptionPane.showConfirmDialog(null, message, "Please confirm", JOptionPane.YES_NO_OPTION);
                dialogOpen = false;
                if (output != 0) {
                    responseHandler.no();
                } else {
                    responseHandler.yes();
                }
            }
        });
    }

    @Override
    public void promptForImportType(AppConfigItem appConfigItem, ImportTypeResponseHandler importTypeResponseHandler) {
        String gameName = (appConfigItem != null? "\"" + appConfigItem.getName() + "\"" : "an AGI game");
        String[] values = ImportType.getDescriptions();
        dialogOpen = true;
        Object selectedOption = JOptionPane.showInputDialog(
                null, 
                (appConfigItem != null? 
                        "For legal reasons, you must import your own copy of \n" + gameName + "\n\n" : "") + 
                "Please select the type of import:", 
                "Import " + gameName, 
                JOptionPane.DEFAULT_OPTION, 
                null, 
                values, 
                values[0]);
        dialogOpen = false;
        if (selectedOption != null) {
            ImportType importType = ImportType.getImportTypeByDescription(selectedOption.toString());
            importTypeResponseHandler.importTypeResult(true, importType);
        } else {
            importTypeResponseHandler.importTypeResult(false, null);
        }
    }

    @Override
    public void openFileDialog(AppConfigItem appConfigItem, String fileType, String title, final String startPath,
            final OpenFileResponseHandler openFileResponseHandler) {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                String gameNameIn = (appConfigItem != null? "'" + appConfigItem.getName() + "'" : "the AGI game");
                JFileChooser jfc = null;
                if (startPath != null) {
                    jfc = new JFileChooser(startPath);
                } else {
                    jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                }
                if ("DIR".equals(fileType)) {
                    jfc.setDialogTitle("Select the folder containg " + gameNameIn);
                    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    jfc.setAcceptAllFileFilterUsed(false);
                } else {
                    // All other types are normal files.
                    jfc.setDialogTitle("Select the " + fileType + " file containing " + gameNameIn);
                    jfc.setAcceptAllFileFilterUsed(false);
                    jfc.setFileFilter(new FileNameExtensionFilter("ZIP files", "zip"));
                }

                dialogOpen = true;
                int returnValue = jfc.showOpenDialog(null);
                dialogOpen = false;
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    String filePath = jfc.getSelectedFile().getPath();
                    FileHandle fileHandle = new FileHandle(filePath);
                    String gameName = fileHandle.nameWithoutExtension();
                    
                    Game game = decodeGame(filePath);
                    
                    if (game != null) {
                        // Game successfully decoded, so we can store this in app config.
                        Detection detection = new Detection(game);
                        
                        // If game is recognised, use detected name but without version part.
                        if (!detection.gameName.equals("Unrecognised game")) {
                            gameName = detection.gameName;
                            if (gameName.contains("(")) {
                                int bracketIndex = gameName.indexOf('(');
                                gameName = gameName.substring(0, bracketIndex).trim();
                                // TODO: Add check of name against appConfigItem, if provided.
                            }
                        }
                        
                        openFileResponseHandler.openFileResult(true, filePath, gameName, detection.gameId);
                    } else {
                        // Doesn't appear to be a valid AGI game folder.
                        openFileResponseHandler.openFileResult(false, null, null, null);
                    }
                } else {
                    openFileResponseHandler.openFileResult(false, null, null, null);
                }
            }
        });
    }
    
    /**
     * Attempts to load and decode the game, as a way to both validate that it is a
     * valid AGI game folder, and also, if valid, obtain the game's name and ID, so 
     * that the AppConfigItem can be created.
     * 
     * @param filePath The path of the directory containing the AGI game files.
     * 
     * @return The decoded Game, if successfully decoded; otherwise null.
     */
    private Game decodeGame(String filePath) {
        File directory = new File(filePath);
        if (!directory.exists()) {
            return null;
        }
        
        DesktopGameLoader gameLoader = new DesktopGameLoader(null);
        Map<String, byte[]> gameFilesMap = new HashMap<>();
        gameLoader.fetchGameFiles(directory.toURI().toString(), map -> gameFilesMap.putAll(map));
        
        // TODO: Check for VOL and DIR files.
        if (gameFilesMap.containsKey("words.tok") && 
            gameFilesMap.containsKey("object")) {
            // Seems to be an AGI game directory. Let's try to decode it.
            try {
                return new Game(gameFilesMap);
            } catch (RuntimeException e) {
                // Decode failed, so can't be run by AGILE.
                showMessageDialog("AGILE is unable to run the selected game. Please try another one.");
                return null;
            }
        } else {
            // Missing core files.
            showMessageDialog("The selected folder or file does not appear to contain an AGI game.");
            return null;
        }
    }

    @Override
    public void promptForTextInput(final String message, final String initialValue,
            final TextInputResponseHandler textInputResponseHandler) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                dialogOpen = true;
                String text = (String) JOptionPane.showInputDialog(null, message, "Please enter value",
                        JOptionPane.INFORMATION_MESSAGE, null, null, initialValue != null ? initialValue : "");
                dialogOpen = false;
                
                if (text != null) {
                    textInputResponseHandler.inputTextResult(true, text);
                } else {
                    textInputResponseHandler.inputTextResult(false, null);
                }
            }
        });
    }

    @Override
    public void showMessageDialog(String message) {
        dialogOpen = true;
        JOptionPane.showMessageDialog(null, message);
        dialogOpen = false;
    }
    
    @Override
    public void showAboutDialog(String aboutMessage, TextInputResponseHandler textInputResponseHandler) {
        dialogOpen = true;
        
        JButton spacerButton = new JButton(
                "                                                                  ");
        spacerButton.setVisible(false);
        JButton exportButton = new JButton(getExportIcon());
        JButton importButton = new JButton(getImportIcon());
        JButton resetButton = new JButton(getResetIcon());
        JButton clearButton = new JButton(getClearIcon());
        JButton okButton = new JButton("OK");
        //Object[] options = { exportButton, importButton, resetButton, clearButton, spacerButton, okButton };
        Object[] options = { okButton };
        
        final JOptionPane pane = new JOptionPane(
                aboutMessage, 
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, 
                loadIcon("png/agile-64x64.png"),
                options, okButton);
        
        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JButton button = (JButton)e.getComponent();
                if (button == exportButton) {
                    pane.setValue("EXPORT");
                }
                else if (button == importButton) {
                    pane.setValue("IMPORT");
                }
                else if (button == resetButton) {
                    pane.setValue("RESET");
                }
                else if (button == clearButton) {
                    pane.setValue("CLEAR");
                }
                else {
                    pane.setValue("OK");
                }
            }
        };
        
        exportButton.addMouseListener(mouseListener);
        importButton.addMouseListener(mouseListener);
        resetButton.addMouseListener(mouseListener);
        clearButton.addMouseListener(mouseListener);
        okButton.addMouseListener(mouseListener);
        
        pane.setComponentOrientation(JOptionPane.getRootFrame().getComponentOrientation());
        JDialog dialog = pane.createDialog("About AGILE");
        dialog.setIconImage(loadImage("png/agile-32x32.png"));
        dialog.show();
        dialog.dispose();
        
        if (pane.getValue() != null) {
            textInputResponseHandler.inputTextResult(true, (String)pane.getValue());
        } else {
            textInputResponseHandler.inputTextResult(false, null);
        }
        
        dialogOpen = false;
    }

    @Override
    public boolean isDialogOpen() {
        return dialogOpen;
    }
}
