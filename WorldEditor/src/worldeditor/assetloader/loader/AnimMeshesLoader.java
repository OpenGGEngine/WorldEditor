///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
package worldeditor.assetloader.loader;

import com.opengg.core.math.Matrix4f;
import com.opengg.core.math.Quaternionf;
import com.opengg.core.math.Tuple;
import com.opengg.core.math.Vector2f;
import com.opengg.core.math.Vector3f;
import com.opengg.core.model.Material;
import com.opengg.core.model.AnimatedFrame;
import com.opengg.core.model.Animation;
import com.opengg.core.model.Mesh;
import com.opengg.core.model.Model;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_FixInfacingNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_GenSmoothNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_LimitBoneWeights;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIBone;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIQuaternion;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.AIVectorKey;
import org.lwjgl.assimp.AIVertexWeight;
import static worldeditor.assetloader.loader.StaticMeshesLoader.processMaterial;

public class AnimMeshesLoader extends StaticMeshesLoader {

    private static void buildTransFormationMatrices(AINodeAnim aiNodeAnim, Node node) {
        int numFrames = aiNodeAnim.mNumPositionKeys();
        AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
        AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
        AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

        for (int i = 0; i < numFrames; i++) {
            AIVectorKey aiVecKey = positionKeys.get(i);
            AIVector3D vec = aiVecKey.mValue();

            Matrix4f transfMat = Matrix4f.translate(vec.x(), vec.y(), vec.z());
            AIQuatKey quatKey = rotationKeys.get(i);
            AIQuaternion aiQuat = quatKey.mValue();

            Quaternionf quat = new Quaternionf(aiQuat.w(), aiQuat.x(), aiQuat.y(), aiQuat.z());
            transfMat = transfMat.rotate(quat);
            if (i < aiNodeAnim.mNumScalingKeys()) {
                aiVecKey = scalingKeys.get(i);
                vec = aiVecKey.mValue();
                transfMat = transfMat.scale(vec.x(), vec.y(), vec.z());
            }

            node.addTransformation(transfMat);
        }
    }

    public static Model loadAnimModel(String resourcePath)
            throws Exception {
        return loadAnimModel(resourcePath,
                aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
                | aiProcess_FixInfacingNormals | aiProcess_LimitBoneWeights);
    }

    public static Model loadAnimModel(String resourcePath, int flags)
            throws Exception {
        AIScene aiScene = aiImportFile(resourcePath, flags);
        if (aiScene == null) {
            throw new Exception("Error loading model");
        }

        int numMaterials = aiScene.mNumMaterials();
        PointerBuffer aiMaterials = aiScene.mMaterials();
        List<Material> materials = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            Material material = processMaterial(aiMaterial);
            material.texpath = resourcePath.substring(0, resourcePath.lastIndexOf(File.separator) + 1) + "tex" + File.separator;
            materials.add(material);
        }

        List<Bone> boneList = new ArrayList<>();
        int numMeshes = aiScene.mNumMeshes();
        PointerBuffer aiMeshes = aiScene.mMeshes();
        
        List<Mesh> meshes = new ArrayList<>();
        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            Mesh mesh = processMesh(aiMesh, materials, boneList);
            meshes.add(mesh);
        }

        AINode aiRootNode = aiScene.mRootNode();
        Matrix4f rootTransfromation = AnimMeshesLoader.toMatrix(aiRootNode.mTransformation());
        Node rootNode = processNodesHierarchy(aiRootNode, null);
        Map<String, Animation> animations = processAnimations(aiScene, boneList, rootNode, rootTransfromation);
        
        Model model = new Model("mname", meshes, animations);

        return model;
    }

    private static List<AnimatedFrame> buildAnimationFrames(List<Bone> boneList, Node rootNode,
            Matrix4f rootTransformation) {

        int numFrames = rootNode.getAnimationFrames();
        List<AnimatedFrame> frameList = new ArrayList<>();
        for (int i = 0; i < numFrames; i++) {
            int numBones = boneList.size();
            
            AnimatedFrame frame = new AnimatedFrame(numBones);
            frameList.add(frame);

            for (int j = 0; j < numBones; j++) {
                Bone bone = boneList.get(j);
                Node node = rootNode.findByName(bone.getBoneName());
                Matrix4f boneMatrix = Node.getParentTransforms(node, i);
                boneMatrix = boneMatrix.multiply(bone.getOffsetMatrix());

                boneMatrix = new Matrix4f(rootTransformation).multiply(boneMatrix);
                frame.setMatrix(j, boneMatrix);
            }
        }

        return frameList;
    }

    private static Map<String, Animation> processAnimations(AIScene aiScene, List<Bone> boneList,
            Node rootNode, Matrix4f rootTransformation) {
        Map<String, Animation> animations = new HashMap<>();

        // Process all animations
        int numAnimations = aiScene.mNumAnimations();
        PointerBuffer aiAnimations = aiScene.mAnimations();
        for (int i = 0; i < numAnimations; i++) {
            AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));

            // Calculate transformation matrices for each node
            int numChanels = aiAnimation.mNumChannels();
            PointerBuffer aiChannels = aiAnimation.mChannels();
            for (int j = 0; j < numChanels; j++) {
                AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(j));
                String nodeName = aiNodeAnim.mNodeName().dataString();
                Node node = rootNode.findByName(nodeName);
                buildTransFormationMatrices(aiNodeAnim, node);
            }

            List<AnimatedFrame> frames = buildAnimationFrames(boneList, rootNode, rootTransformation);
            Animation animation = new Animation(aiAnimation.mName().dataString(), frames, (float) aiAnimation.mDuration());
            animations.put(animation.getName(), animation);
        }
        return animations;
    }

    private static void processBones(AIMesh aiMesh, List<Bone> boneList, List<Integer> boneIds,
            List<Float> weights) {
        Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();
        int numBones = aiMesh.mNumBones();
        PointerBuffer aiBones = aiMesh.mBones();
        for (int i = 0; i < numBones; i++) {
            AIBone aiBone = AIBone.create(aiBones.get(i));
            int id = boneList.size();
            Bone bone = new Bone(id, aiBone.mName().dataString(), toMatrix(aiBone.mOffsetMatrix()));
            boneList.add(bone);
            int numWeights = aiBone.mNumWeights();
            AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
            for (int j = 0; j < numWeights; j++) {
                AIVertexWeight aiWeight = aiWeights.get(j);
                VertexWeight vw = new VertexWeight(bone.getBoneId(), aiWeight.mVertexId(),
                        aiWeight.mWeight());
                List<VertexWeight> vertexWeightList = weightSet.get(vw.getVertexId());
                if (vertexWeightList == null) {
                    vertexWeightList = new ArrayList<>();
                    weightSet.put(vw.getVertexId(), vertexWeightList);
                }
                vertexWeightList.add(vw);
            }
        }

        int numVertices = aiMesh.mNumVertices();
        for (int i = 0; i < numVertices; i++) {
            List<VertexWeight> vertexWeightList = weightSet.get(i);
            int size = vertexWeightList != null ? vertexWeightList.size() : 0;
            for (int j = 0; j < 4; j++) {
                if (j < size) {
                    VertexWeight vw = vertexWeightList.get(j);
                    weights.add(vw.getWeight());
                    boneIds.add(vw.getBoneId());
                } else {
                    weights.add(0.0f);
                    boneIds.add(0);
                }
            }
        }
    }

    private static Mesh processMesh(AIMesh aiMesh, List<Material> materials, List<Bone> boneList) {
        List<Integer> boneIds = new ArrayList<>();
        List<Float> weights = new ArrayList<>();

        List<Vector3f> vertices = processVertices(aiMesh);
        List<Vector3f> normals = processNormals(aiMesh);
        List<Vector2f> texcoords = processTexCoords(aiMesh);
        List<Integer> indices = processIndices(aiMesh);
        processBones(aiMesh, boneList, boneIds, weights);

        Material material;
        int materialIdx = aiMesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = new Material("default");
        }
        
        Tuple<FloatBuffer, IntBuffer> buffers = MeshUtil.getMeshBuffers(Utils.listVector3fToArray(vertices), Utils.listVector2fToArray(texcoords),
                Utils.listVector3fToArray(normals), Utils.listIntToArray(indices),
                Utils.listIntToArray(boneIds), Utils.listFloatToArray(weights));
        
        Mesh mesh = new Mesh(buffers.x, buffers.y, material, true);

        return mesh;
    }

    private static Node processNodesHierarchy(AINode aiNode, Node parentNode) {
        String nodeName = aiNode.mName().dataString();
        Node node = new Node(nodeName, parentNode);

        int numChildren = aiNode.mNumChildren();
        PointerBuffer aiChildren = aiNode.mChildren();
        for (int i = 0; i < numChildren; i++) {
            AINode aiChildNode = AINode.create(aiChildren.get(i));
            Node childNode = processNodesHierarchy(aiChildNode, node);
            node.addChild(childNode);
        }

        return node;
    }

    private static Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4) {
        Matrix4f result = new Matrix4f();
        result.m00 = aiMatrix4x4.a1();
        result.m10 = (aiMatrix4x4.a2());
        result.m20 = (aiMatrix4x4.a3());
        result.m30 = (aiMatrix4x4.a4());
        result.m01 = (aiMatrix4x4.b1());
        result.m11 = (aiMatrix4x4.b2());
        result.m21 = (aiMatrix4x4.b3());
        result.m31 = (aiMatrix4x4.b4());
        result.m02 = (aiMatrix4x4.c1());
        result.m12 = (aiMatrix4x4.c2());
        result.m22 = (aiMatrix4x4.c3());
        result.m32 = (aiMatrix4x4.c4());
        result.m03 = (aiMatrix4x4.d1());
        result.m13 = (aiMatrix4x4.d2());
        result.m23 = (aiMatrix4x4.d3());
        result.m33 = (aiMatrix4x4.d4());
        return result;
    }
}
