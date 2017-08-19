/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.assetloader.loader;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Warren
 */
public class Model {

    public final int version = 1;

    private Mesh[] meshes;

    public Model() {

    }

    public Model(Mesh mesh) {
        this();
        this.meshes = new Mesh[]{mesh};
    }

    public Model(Mesh[] meshes) {
        this();
        this.meshes = meshes;
    }

    public Mesh getMesh() {
        return meshes[0];
    }

    public Mesh[] getMeshes() {
        return meshes;
    }

    public void setMeshes(Mesh[] meshes) {
        this.meshes = meshes;
    }

    public void setMesh(Mesh mesh) {
        this.meshes = new Mesh[]{mesh};
    }

    public void putData(DataOutputStream ds, boolean isanimated) throws IOException {
        ds.writeInt(1);

        ds.writeBoolean(isanimated);

        ds.writeInt(meshes.length);
        for (Mesh m : meshes) {
            m.putData(ds);
        }
    }
}
