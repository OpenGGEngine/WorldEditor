/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assetloader2;

import com.opengg.core.render.shader.VertexArrayFormat;
import org.lwjgl.system.MemoryUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 *
 * @author warre
 */
public class NModel {

    public VertexArrayFormat f;
    public MNode animation;
    ArrayList<NMesh> list;
    public double tickspeed, duration;
   

    public NModel(ArrayList<NMesh> list, boolean hastangents) {
        this.list = list;
        f = new VertexArrayFormat();
    }

    public NModel(ArrayList<NMesh> list, MNode animation) {
        this.list = list;
        this.animation = animation;
        f = new VertexArrayFormat();
    }

    public void putData(File file) throws FileNotFoundException, IOException {
        FileChannel s = new FileOutputStream(file).getChannel();
        for (NMesh mesh : list) {
            mesh.putData(s);
        }
    }

    public void putData(File file, File animations) throws FileNotFoundException, IOException {
        try (FileChannel s = new FileOutputStream(file).getChannel()) {
            ByteBuffer bb = MemoryUtil.memAlloc(4).putInt(list.size());
            System.out.println(list.size());
            bb.flip();
            s.write(bb);
            for (NMesh mesh : list) {
                mesh.putData(s);
            }
            s.close();
        }
        if (this.animation != null) {
            try (FileChannel anim = new FileOutputStream(animations).getChannel()) {
                ByteBuffer airports = MemoryUtil.memAlloc(16);
                airports.putDouble(this.duration);
                airports.putDouble(this.tickspeed);
                airports.flip();
                anim.write(airports);
                recurStore(anim, animation);
            }
        }
    }

    public void recurStore(FileChannel channel, MNode node) throws IOException {
        node.putData(channel);
        channel.write(MemoryUtil.memAlloc(4).putInt(node.children.size()).flip());
        for (MNode child : node.children) {
            recurStore(channel, child);
        }

    }
}
