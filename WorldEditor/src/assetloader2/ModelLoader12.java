/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assetloader2;

import com.opengg.core.math.Matrix4f;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVertexWeight;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.system.MemoryUtil;

/**
 *
 * @author warre
 */
public class ModelLoader12 {

    public static void loadModel(File file) throws IOException {
        AIScene scene = Assimp.aiImportFile(file.toString(),
                Assimp.aiProcess_Triangulate
                | Assimp.aiProcess_GenSmoothNormals
                | Assimp.aiProcess_FlipUVs
                | Assimp.aiProcess_CalcTangentSpace
                        | Assimp.aiProcess_LimitBoneWeights
        );

        if (scene == null | scene.mNumAnimations() == 0) {
            System.err.println("the imported file does not contain any animations.");
            System.exit(0);
        }
        ArrayList<NMesh> meshlist = new ArrayList<>();
        for (int d = 0; d < scene.mNumMeshes(); d++) {
            AIMesh mesh = AIMesh.create(scene.mMeshes().get(d));
            int sizeOfVertex = 19;
            int sizeOfVertexUnrigged = 11;

            float array[] = new float[mesh.mNumVertices() * sizeOfVertex];
            int index = 0;

            for (int v = 0; v < mesh.mNumVertices(); v++) {
                AIVector3D position = mesh.mVertices().get(v);
                AIVector3D normal = mesh.mNormals().get(v);
                AIVector3D tangent;
                if(mesh.mTangents() != null){
                tangent = mesh.mTangents().get(v);
                }else{
                    tangent = normal;
                }
                AIVector3D texCoord = mesh.mTextureCoords(0).get(v);

                array[index++] = position.x();
                array[index++] = position.y();
                array[index++] = position.z();

                array[index++] = tangent.x();
                array[index++] = tangent.y();
                array[index++] = tangent.z();

                array[index++] = normal.x();
                array[index++] = normal.y();
                array[index++] = normal.z();
                
                array[index++] = texCoord.x();
                array[index++] = texCoord.y();

                array[index++] = 0;
                array[index++] = 0;
                array[index++] = 0;
                array[index++] = 0;

                array[index++] = 0;
                array[index++] = 0;
                array[index++] = 0;
                array[index++] = 0;
            }

            index = 0;

            ByteBuffer indices = MemoryUtil.memAlloc(4 *mesh.mNumFaces() * mesh.mFaces().get(0).mNumIndices());
            IntBuffer indicesi = indices.asIntBuffer();

            for (int f = 0; f < mesh.mNumFaces(); f++) {
                AIFace face = mesh.mFaces().get(f);
                for (int ind = 0; ind < face.mNumIndices(); ind++) {
                    indicesi.put(face.mIndices().get(ind));
                }
            }

            Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();
            int numBones = mesh.mNumBones();
            PointerBuffer aiBones = mesh.mBones();
            MBone[] bones = new MBone[numBones];
            for (int i = 0; i < numBones; i++) {
                AIBone aiBone = AIBone.create(aiBones.get(i));
                MBone bone = new MBone();
                bone.id = i;
                bone.name = aiBone.mName().dataString();
                bone.offsetMatrix= toMatrix(aiBone.mOffsetMatrix());
                bones[i] = (bone);
                int numWeights = aiBone.mNumWeights();
                AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
                for (int j = 0; j < numWeights; j++) {
                    AIVertexWeight aiWeight = aiWeights.get(j);
                    VertexWeight vw = new VertexWeight(bone.id, aiWeight.mVertexId(),
                            aiWeight.mWeight());
                    List<VertexWeight> vertexWeightList = weightSet.get(vw.getVertexId());
                    if (vertexWeightList == null) {
                        vertexWeightList = new ArrayList<>();
                        weightSet.put(vw.getVertexId(), vertexWeightList);
                    }
                    vertexWeightList.add(vw);
                }
            }

            int numVertices = mesh.mNumVertices();
            for (int i = 0; i < numVertices; i++) {
                List<VertexWeight> vertexWeightList = weightSet.get(i);
                int size = vertexWeightList != null ? vertexWeightList.size() : 0;
                int indexpointer = (i)* (19) + 11;
                for (int j = 0; j < 4; j++) {
                    if (j < size) {
                        VertexWeight vw = vertexWeightList.get(j);
                        array[indexpointer] = vw.getBoneId();
                        array[indexpointer+4] = vw.getWeight();
                        indexpointer++;
                    } else {
                        array[indexpointer] = 0;
                        array[indexpointer+4] = 0 ;
                        indexpointer++;
                    }
                }
            }

            NMesh meshes = new NMesh( createFBufferFromArr(array), indices);
            meshes.mbones = bones;
            meshlist.add(meshes);
        }

        AIAnimation a = AIAnimation.create(scene.mAnimations().get(0));
        MNode miss = parseNode(scene.mRootNode(), true, a);
        NModel model = new NModel(meshlist, miss);
        model.duration = a.mDuration();
        model.tickspeed = a.mTicksPerSecond();
        try {
            model.putData(new File("C:/res/model.bmf"),new File("C:/res/anim.gga"));
        } catch (FileNotFoundException ex) {
           
        }

    }

    public static MNode parseNode(AINode node, boolean top, AIAnimation d) {
        MNode itsawonderfullife = new MNode(node, top, d);
        for (int i = 0; i < node.mNumChildren(); i++) {
            itsawonderfullife.children.add(parseNode(AINode.create(node.mChildren().get(i)), false, d));
        }
        return itsawonderfullife;
    }

    public static Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4) {
        var m00=(aiMatrix4x4.a1());
        var m10=(aiMatrix4x4.a2());
        var m20=(aiMatrix4x4.a3());
        var m30=(aiMatrix4x4.a4());
        var m01=(aiMatrix4x4.b1());
        var m11=(aiMatrix4x4.b2());
        var m21=(aiMatrix4x4.b3());
        var m31=(aiMatrix4x4.b4());
        var m02=(aiMatrix4x4.c1());
        var m12=(aiMatrix4x4.c2());
        var m22=(aiMatrix4x4.c3());
        var m32=(aiMatrix4x4.c4());
        var m03=(aiMatrix4x4.d1());
        var m13=(aiMatrix4x4.d2());
        var m23=(aiMatrix4x4.d3());
        var m33=(aiMatrix4x4.d4());

        Matrix4f result = new Matrix4f(m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33);
        return result;
    }
    public static ByteBuffer createFBufferFromArr(float[] x){
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(x.length * Float.BYTES); //4 bytes per float
        byteBuf.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = byteBuf.asFloatBuffer();
        buffer.put(x);
        buffer.position(0);
        return byteBuf;
    }

    
}
