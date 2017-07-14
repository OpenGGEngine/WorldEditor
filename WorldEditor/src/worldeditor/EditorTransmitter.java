/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor;

import com.opengg.core.world.Action;
import com.opengg.core.world.ActionTransmitter;

/**
 *
 * @author Javier
 */
public class EditorTransmitter implements ActionTransmitter{
    WorldEditor editor;
    @Override
    public void doAction(Action action) {
        editor.onAction(action);
    }
    
}
