/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worldeditor.assetloader.loader;

/**
 *
 * @author Warren
 */

public class VertexWeight {

    private int boneId;

    private int vertexId;

    private float weight;

    public VertexWeight(int boneId, int vertexId, float weight) {
        this.boneId = boneId;
        this.vertexId = vertexId;
        this.weight = weight;
    }

    public int getBoneId() {
        return boneId;
    }

    public int getVertexId() {
        return vertexId;
    }

    public float getWeight() {
        return weight;
    }

    public void setVertexId(int vertexId) {
        this.vertexId = vertexId;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
