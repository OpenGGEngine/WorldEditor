/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assetloader2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author warre
 */
public class NMesh {

    public FloatBuffer vbo;
    public IntBuffer ib;
    private ByteBuffer backedib, backedfb;
    public MBone[] mbones;

    public NMesh(ByteBuffer vbo, ByteBuffer ib) {
        backedib = ib;
        backedfb = vbo;
        this.vbo = vbo.asFloatBuffer();
        this.ib = ib.asIntBuffer();
    }

    public void putData(FileChannel channel) throws IOException {
        ByteBuffer b = MemoryUtil.memAlloc(4).putInt(backedfb.capacity());
        b.flip();
        channel.write(b);
        while (backedfb.hasRemaining()) {
            channel.write(backedfb);
        }
        ByteBuffer b1 = MemoryUtil.memAlloc(4).putInt(backedib.capacity());
        b1.flip();
        channel.write(b1);
        while (backedib.hasRemaining()) {
            channel.write(backedib);
        }
      
        if (mbones != null) {
            ByteBuffer b3 = MemoryUtil.memAlloc(4).putInt(mbones.length);
            b3.flip();
            channel.write(b3);
            for (MBone bone : mbones) {
                ByteBuffer bb = MemoryUtil.memAlloc((bone.name.length()*2)  + (Float.BYTES * 16)  + 12);
                bb.putInt((bone.name.length()*2)  + (Float.BYTES * 16)  + 8);
                bb.putInt((bone.name.length()*2) );
                for(char c: bone.name.toCharArray()){
                    bb.putChar(c);
                }
                bb.putInt(bone.id);
                bb.putFloat(bone.offsetMatrix.m00);
                bb.putFloat(bone.offsetMatrix.m01);
                bb.putFloat(bone.offsetMatrix.m02);
                bb.putFloat(bone.offsetMatrix.m03);
                
                bb.putFloat(bone.offsetMatrix.m10);
                bb.putFloat(bone.offsetMatrix.m11);
                bb.putFloat(bone.offsetMatrix.m12);
                bb.putFloat(bone.offsetMatrix.m13);
                
                bb.putFloat(bone.offsetMatrix.m20);
                bb.putFloat(bone.offsetMatrix.m21);
                bb.putFloat(bone.offsetMatrix.m22);
                bb.putFloat(bone.offsetMatrix.m23);
                
                bb.putFloat(bone.offsetMatrix.m30);
                bb.putFloat(bone.offsetMatrix.m31);
                bb.putFloat(bone.offsetMatrix.m32);
                bb.putFloat(bone.offsetMatrix.m33);
                
                bb.flip();
                while(bb.hasRemaining()){
                    channel.write(bb);
                }
            }
        } else {
            ByteBuffer b3 = MemoryUtil.memAlloc(4).putInt(0);
            b3.flip();
            channel.write(b3);
        }

    }

}
