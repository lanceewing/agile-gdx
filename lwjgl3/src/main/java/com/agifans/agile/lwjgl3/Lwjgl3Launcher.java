package com.agifans.agile.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.agifans.agile.Agile;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        DesktopDialogHandler desktopDialogHandler = new DesktopDialogHandler();
    	DesktopAgileRunner desktopAgileRunner = new DesktopAgileRunner(
    	        new DesktopUserInput(), new DesktopWavePlayer(), 
    	        new DesktopSavedGameStore(), new DesktopPixelData(), 
    	        new DesktopVariableData());
        return new Lwjgl3Application(
                new Agile(desktopAgileRunner, desktopDialogHandler), 
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
        configuration.setWindowedMode(960, 600);
        configuration.setWindowIcon("agile-128x128.png", "agile-64x64.png", "agile-32x32.png", "agile-16x16.png");
        return configuration;
    }
}