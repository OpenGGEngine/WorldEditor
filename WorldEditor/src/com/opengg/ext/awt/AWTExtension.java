package com.opengg.ext.awt;

import com.opengg.core.extension.Extension;
import com.opengg.core.render.window.WindowTypeRegister;
import com.opengg.ext.awt.window.GGCanvas;

import javax.swing.*;
import java.awt.*;

public class AWTExtension extends Extension {
    public AWTExtension(Container container){
        extname = "AWT Window Support";
        requirement = LWJGL;
        GGCanvas.container = container;
    }

    @Override
    public void loadExtension() {
        WindowTypeRegister.registerWindowType("AWT", new GGCanvas());
    }

    @Override
    public void update(float delta) {

    }
}
