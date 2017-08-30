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
import com.opengg.core.math.Vector2f;
import com.opengg.core.math.Vector3f;
import java.util.List;

public class Utils {

    public static int[] listIntToArray(List<Integer> list) {
        //Stream Meme
        int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
        return result;
    }

    public static float[] listFloatToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }
    
    public static float[] listVector3fToArray(List<Vector3f> list) {
        int size = list != null ? list.size() * 3 : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size/3; i++) {
            floatArr[i*3] = list.get(i).x;
            floatArr[i*3+1] = list.get(i).y;
            floatArr[i*3+2] = list.get(i).z;
        }
        return floatArr;
    }

    
    public static float[] listVector2fToArray(List<Vector2f> list) {
        int size = list != null ? list.size() * 2 : 0;
        float[] floatArr = new float[size];
         for (int i = 0; i < size/2; i++) {
            floatArr[i*2] = list.get(i).x;
            floatArr[i*2+1] = list.get(i).y;
        }
        return floatArr;
    }
}

