/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.engine.OpenGG;
import com.opengg.core.render.window.GLFWWindow;

/**
 *
 * @author Warren
 */
public class GlWindow {
     GLFWWindow win;
     
     public GlWindow(){
       //  OpenGG.initializeOpenGG();
         try {
            win = new GLFWWindow(1280, 960, "Test", DisplayMode.WINDOWED);
            
        } catch (Exception ex) {
        }
     }
}
