package worldeditor.assetloader.loader;

import com.opengg.core.math.Tuple;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryUtil;

/**
 * 
 * @author Warren
 */
public class MeshUtil {

    public static Tuple<FloatBuffer, IntBuffer> getMeshBuffers(float[] positions, float[] texCoords, float[] normals, int[] indices){
        return getMeshBuffers(positions, texCoords, normals, indices, null, null);    
    }
    
    public static Tuple<FloatBuffer, IntBuffer> getMeshBuffers(float[] positions, float[] texCoords, float[] normals, int[] indices, int[] jointIndices, float[] weights) {
        FloatBuffer fb;
        IntBuffer ib;
        int vertexCount = indices.length;
        boolean hasTexCoords = texCoords.length != 0;
        boolean animated = jointIndices != null && jointIndices.length > 0;
        
        if(jointIndices ==null){
             fb = MemoryUtil.memAllocFloat((positions.length / 3) * 12);

        }else{
            fb = MemoryUtil.memAllocFloat((positions.length / 3) * 20);
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
            
            if(hasTexCoords){
                fb.put(texCoords[(i*2)]);
                fb.put(texCoords[(i*2) +1]);
            }else{
                fb.put(0).put(0);
            }
            
            if(animated){
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
        
        return new Tuple<>(fb,ib);
    }

    private void calculateBoundingRadius(float positions[]) {
        float boundingRadius;
        int length = positions.length;
        boundingRadius = 0;
        for (int i = 0; i < length; i++) {
            float pos = positions[i];
            boundingRadius = Math.max(Math.abs(pos), boundingRadius);
        }
    }

}
