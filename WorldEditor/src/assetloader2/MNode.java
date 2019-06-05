/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assetloader2;

import com.opengg.core.math.Matrix4f;
import com.opengg.core.math.Quaternionf;
import com.opengg.core.math.Tuple;
import com.opengg.core.math.Vector3f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryUtil;

/**
 * @author warre
 */
public class MNode {

    public static Map<String, MNode> nodeset = new HashMap<>();
    public Matrix4f transform;

    public Tuple<Double, Vector3f>[] positionkeys;
    public Tuple<Double, Quaternionf>[] rotationkeys;
    public Tuple<Double, Vector3f>[] scalingkeys;
    public String name;

    public MNode parent;
    public ArrayList<MNode> children = new ArrayList<>();

    public MNode(AINode m, boolean root, AIAnimation animation) {
        name = m.mName().dataString();
        if (!root) {
            parent = nodeset.get(m.mParent().mName().dataString());
        }
        AINodeAnim sd = FindNodeAnim(animation, name);

        if (sd != null) {
            System.out.println(name);
            positionkeys = new Tuple[sd.mNumPositionKeys()];
            for (int i = 0; i < sd.mNumPositionKeys(); i++) {
                AIVector3D temp = sd.mPositionKeys().get(i).mValue();
                double time = sd.mPositionKeys().get(i).mTime();
                positionkeys[i] = new Tuple<Double, Vector3f>(time, new Vector3f(temp.x(), temp.y(), temp.z()));
            }
            rotationkeys = new Tuple[sd.mNumRotationKeys()];
            for (int i = 0; i < sd.mNumRotationKeys(); i++) {
                AIQuaternion temp = sd.mRotationKeys().get(i).mValue();
                System.out.println(AIMatrix4x4.create());
                double time = sd.mRotationKeys().get(i).mTime();
                rotationkeys[i] = new Tuple<Double, Quaternionf>(time, new Quaternionf(temp.w(), temp.x(), temp.y(), temp.z()));
            }
            scalingkeys = new Tuple[sd.mNumScalingKeys()];
            for (int i = 0; i < sd.mNumScalingKeys(); i++) {
                AIVector3D temp = sd.mScalingKeys().get(i).mValue();
                double time = sd.mScalingKeys().get(i).mTime();
                scalingkeys[i] = new Tuple<Double, Vector3f>(time, new Vector3f(temp.x(), temp.y(), temp.z()));
            }
        }
        this.transform = ModelLoader12.toMatrix(m.mTransformation());
        System.out.println("mirror");
        System.out.println(this.transform.toString());
        nodeset.put(name, this);
    }

    public void putData(FileChannel f) throws IOException {
        writeStr(f, this.name);
        if (positionkeys != null) {
            f.write(MemoryUtil.memAlloc(4).putInt(positionkeys.length).flip());
            ByteBuffer sd = MemoryUtil.memAlloc(positionkeys.length * (3 * Float.BYTES + Double.BYTES));
            for (int i = 0; i < positionkeys.length; i++) {
                Vector3f index = positionkeys[i].y;
                sd.putDouble(positionkeys[i].x);
                sd.putFloat(index.x).putFloat(index.y).putFloat(index.z);
            }
            sd.flip();
            while (sd.hasRemaining()) {
                f.write(sd);
            }
        } else {
            f.write(MemoryUtil.memAlloc(4).putInt(0).flip());
        }
        if (rotationkeys != null) {
            ByteBuffer sd1 = MemoryUtil.memAlloc(rotationkeys.length * (4 * Float.BYTES + Double.BYTES));
            f.write(MemoryUtil.memAlloc(4).putInt(rotationkeys.length).flip());
            for (int i = 0; i < rotationkeys.length; i++) {
                Quaternionf index = rotationkeys[i].y;
                sd1.putDouble(rotationkeys[i].x);
                //Ethan approves.
                sd1.putFloat(index.w).putFloat(index.x).putFloat(index.y).putFloat(index.z);
            }
            sd1.flip();

            while (sd1.hasRemaining()) {
                f.write(sd1);
            }
        } else {
            f.write(MemoryUtil.memAlloc(4).putInt(0).flip());
        }
        if (scalingkeys != null) {
            ByteBuffer sd2 = MemoryUtil.memAlloc(scalingkeys.length * (3 * Float.BYTES + Double.BYTES));
            f.write(MemoryUtil.memAlloc(4).putInt(scalingkeys.length).flip());
            for (int i = 0; i < scalingkeys.length; i++) {
                Vector3f index = scalingkeys[i].y;
                sd2.putDouble(scalingkeys[i].x);
                sd2.putFloat(index.x).putFloat(index.y).putFloat(index.z);

            }
            sd2.flip();
            while (sd2.hasRemaining()) {
                f.write(sd2);
            }
        } else {
            f.write(MemoryUtil.memAlloc(4).putInt(0).flip());
        }
        if (this.transform == null) {
            this.transform = new Matrix4f();
        }
        ByteBuffer sir = MemoryUtil.memAlloc(16 * 4);
        sir.putFloat(this.transform.m00);
        sir.putFloat(this.transform.m01);
        sir.putFloat(this.transform.m02);
        sir.putFloat(this.transform.m03);
        sir.putFloat(this.transform.m10);
        sir.putFloat(this.transform.m11);
        sir.putFloat(this.transform.m12);
        sir.putFloat(this.transform.m13);
        sir.putFloat(this.transform.m20);
        sir.putFloat(this.transform.m21);
        sir.putFloat(this.transform.m22);
        sir.putFloat(this.transform.m23);
        sir.putFloat(this.transform.m30);
        sir.putFloat(this.transform.m31);
        sir.putFloat(this.transform.m32);
        sir.putFloat(this.transform.m33);
        sir.flip();
        while (sir.hasRemaining()) {
            f.write(sir);
        }
    }

    AINodeAnim FindNodeAnim(AIAnimation pAnimation, String NodeName) {
        for (int i = 0; i < pAnimation.mNumChannels(); i++) {
            AINodeAnim pNodeAnim = AINodeAnim.create(pAnimation.mChannels().get(i));

            if (pNodeAnim.mNodeName().dataString().equals(NodeName)) {
                return pNodeAnim;
            }
        }

        return null;
    }

    public void writeStr(FileChannel f, String s) throws IOException {
        System.out.println(Arrays.toString(s.getBytes(StandardCharsets.US_ASCII)));
        ByteBuffer sd = ByteBuffer.wrap(s.getBytes(StandardCharsets.US_ASCII));

        ByteBuffer holder = MemoryUtil.memAlloc(4);
        holder.putInt(sd.limit());
        holder.flip();

        f.write(holder);

        while (sd.hasRemaining()) {
            f.write(sd);
        }
    }

}
