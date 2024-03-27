package com.agifans.agile.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.util.HashMap;
import java.util.Map;

import com.agifans.agile.Agile;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication(convertArgsToMap(args));
    }

    private static Map<String, String> convertArgsToMap(String[] args) {
        Map<String, String> argsMap = new HashMap<>();
        if ((args != null) && (args.length > 0)) {
            for (String arg : args) {
                int equalsIndex = arg.indexOf('=');
                if (equalsIndex != -1) {
                    String name = arg.substring(0, equalsIndex);
                    String value = arg.endsWith("=")? "" : arg.substring(equalsIndex + 1);
                    argsMap.put(name, value);
                }
            }
        }
        return argsMap;
    }
    
    private static Lwjgl3Application createApplication(Map<String, String> argsMap) {
        DesktopDialogHandler desktopDialogHandler = new DesktopDialogHandler();
    	DesktopAgileRunner desktopAgileRunner = new DesktopAgileRunner(
    	        new DesktopUserInput(), new DesktopWavePlayer(), 
    	        new DesktopSavedGameStore(), new DesktopPixelData(), 
    	        new DesktopVariableData());
        return new Lwjgl3Application(
                new Agile(desktopAgileRunner, desktopDialogHandler, argsMap), 
        		getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("AGILE");
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
        configuration.setWindowedMode(1600, 1000);
        configuration.setWindowIcon("agile-128x128.png", "agile-64x64.png", "agile-32x32.png", "agile-16x16.png");
        return configuration;
    }
}