
package com.badlogic.gradletest;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = HelloApp.SCREEN_WIDTH;
        config.height = HelloApp.SCREEN_HEIGHT;
        config.title = "Atomic Fission! - Tomas Kotula (Kotucz) - Ludum Dare 28";
        config.addIcon("icon32.png", Files.FileType.Internal);
        new LwjglApplication(new HelloApp(), config);
	}
}
