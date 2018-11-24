package worldeditor.assetloader;

import com.opengg.core.model.ggmodel.GGModel;

public class ModelOptions {

    public boolean bmf;
    public boolean tootle;
    public boolean convexhull;
    public boolean lod;
    public String name="defaultop";
    public GGModel model;
    public ModelOptions(){
        bmf = true;
    }
    public ModelOptions(boolean bmf,boolean tootle,boolean convexhull, boolean lod){
        this.bmf = bmf;
        this.tootle = tootle;
        this.convexhull = convexhull;
        this.lod = lod;
    }
    public ModelOptions(ModelOptions m){
        this.bmf = m.bmf;
        this.tootle = m.tootle;
        this.convexhull = m.convexhull;
        this.lod = m.lod;
    }

}
