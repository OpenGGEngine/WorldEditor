package worldeditor.assetloader;

import com.opengg.core.model.Model;

public class ModelOptions {

    public boolean bmf;
    public boolean tootle;
    public boolean convexhull;
    public boolean lod;
    public boolean addToEditor;
    public String name="defaultop";
    public Model model;
    public ModelOptions(){
        bmf = true;
    }
    public ModelOptions(boolean bmf,boolean tootle,boolean convexhull, boolean lod,boolean addToEditor){
        this.bmf = bmf;
        this.tootle = tootle;
        this.convexhull = convexhull;
        this.lod = lod;
        this.addToEditor = addToEditor;
    }
    public ModelOptions(ModelOptions m){
        this.bmf = m.bmf;
        this.tootle = m.tootle;
        this.convexhull = m.convexhull;
        this.lod = m.lod;
        this.addToEditor = m.addToEditor;
    }

}
