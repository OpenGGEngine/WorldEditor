/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.assetloader.loader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Warren
 */
public class AnimModel extends Model {

    private Map<String, Animation> animations;

    private Animation currentAnimation;

    public AnimModel(Mesh[] meshes, Map<String, Animation> animations) {
        super(meshes);
        this.animations = animations;
        Optional<Map.Entry<String, Animation>> entry = animations.entrySet().stream().findFirst();
        currentAnimation = entry.isPresent() ? entry.get().getValue() : null;
    }

    public Animation getAnimation(String name) {
        return animations.get(name);
    }

    public Animation getCurrentAnimation() {
        return currentAnimation;
    }

    public void setCurrentAnimation(Animation currentAnimation) {
        this.currentAnimation = currentAnimation;
    }
    public void putData(DataOutputStream ds) throws IOException{
        super.putData(ds,true);
        ds.writeInt(animations.size());
        System.out.println("sd: "+ animations.size());
        for(String s: animations.keySet()){
            ds.writeInt(s.length());
            ds.writeChars(s);
            animations.get(s).writeBuffer(ds);
        }
        
    }
}
