package worldeditor.assetloader.loader;

import com.opengg.core.model.Material;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author Warren
 */
public class Mesh {

    public static final int MAX_WEIGHTS = 4;

    protected final int vaoId = 1;

    protected final List<Integer> vboIdList = new ArrayList<>();

    public FloatBuffer fb;

    public IntBuffer ib;

    private final int vertexCount;
    
    private final int vbocount;

    private Material material;

    private float boundingRadius;

    public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices) {
        this(positions, textCoords, normals, indices, null, null);
        //this(positions, textCoords, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0), createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0));
    }

    public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices, int[] jointIndices, float[] weights) {
        calculateBoundingRadius(positions);
        vertexCount = indices.length;
        if(jointIndices ==null){
             fb = MemoryUtil.memAllocFloat((positions.length / 3) * 12);
              vbocount = positions.length/3 * 12;
        }else{
            fb = MemoryUtil.memAllocFloat((positions.length / 3) * 20);
             vbocount = positions.length/3 * 20;
        }
        ib = MemoryUtil.memAllocInt(vertexCount);
        
       
        for (int i = 0; i < positions.length / 3; i++) {
            fb.put(positions[i * 3]);
            fb.put(positions[(i*3) + 1]);
            fb.put(positions[(i*3) + 2]);
            
            fb.put(1);
            fb.put(1);
            fb.put(1);
            fb.put(1);
            
            fb.put(normals[i*3]);
            fb.put(normals[(i*3) + 1]);
            fb.put(normals[(i*3) +2]);
            
            fb.put(textCoords[(i*2)]);
            fb.put(textCoords[(i*2) +1]);
            
            if(jointIndices != null){
                fb.put(jointIndices[i*4]);
                fb.put(jointIndices[(i*4) + 1]);
                fb.put(jointIndices[(i*4) + 2]);
                fb.put(jointIndices[(i*4) + 3]);
                
                fb.put(weights[i*4]);
                fb.put(weights[(i*4) + 1]);
                fb.put(weights[(i*4) + 2]);
                fb.put(weights[(i*4) + 3]);
            }
        }
        fb.flip();
        
        ib.put(indices);
        ib.flip();

        System.out.println("---Positions---");
        System.out.println(Arrays.toString(positions));
        System.out.println("---Normals---");
        System.out.println(Arrays.toString(normals));
        System.out.println("---TextCoords---");
        System.out.println(Arrays.toString(textCoords));

    }
    
    public void putData(DataOutputStream s) throws IOException{
        s.writeInt(vbocount);
        while(fb.hasRemaining()){
            s.writeFloat(fb.get());
        }
        s.writeInt(vertexCount);
        while(ib.hasRemaining()){
            s.writeInt(ib.get());
        }
        material.toFileFormat(s);
    }


    private void calculateBoundingRadius(float positions[]) {
        int length = positions.length;
        boundingRadius = 0;
        for (int i = 0; i < length; i++) {
            float pos = positions[i];
            boundingRadius = Math.max(Math.abs(pos), boundingRadius);
        }
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
    
    public int getVertexCount() {
        return vertexCount;
    }

    public float getBoundingRadius() {
        return boundingRadius;
    }

    public void setBoundingRadius(float boundingRadius) {
        this.boundingRadius = boundingRadius;
    }


    protected static float[] createEmptyFloatArray(int length, float defaultValue) {
        float[] result = new float[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

    protected static int[] createEmptyIntArray(int length, int defaultValue) {
        int[] result = new int[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

}
