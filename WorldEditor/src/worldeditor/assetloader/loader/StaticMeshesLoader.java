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
import com.opengg.core.math.Tuple;
import com.opengg.core.math.Vector2f;
import com.opengg.core.math.Vector3f;
import com.opengg.core.math.Vector4f;
import com.opengg.core.model.Material;
import com.opengg.core.model.Mesh;
import com.opengg.core.model.Model;
import java.io.File;
import java.nio.FloatBuffer;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_AMBIENT;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_DIFFUSE;
import static org.lwjgl.assimp.Assimp.AI_MATKEY_COLOR_SPECULAR;
import static org.lwjgl.assimp.Assimp.aiGetMaterialColor;
import static org.lwjgl.assimp.Assimp.aiImportFile;
import static org.lwjgl.assimp.Assimp.aiProcess_FixInfacingNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_GenSmoothNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_JoinIdenticalVertices;
import static org.lwjgl.assimp.Assimp.aiProcess_Triangulate;
import static org.lwjgl.assimp.Assimp.aiTextureType_DIFFUSE;
import static org.lwjgl.assimp.Assimp.aiTextureType_NONE;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

public class StaticMeshesLoader {
    
    public static Model load(String resourcePath) throws Exception {
        return load(resourcePath,
                aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
                | aiProcess_FixInfacingNormals);
    }
    
    public static Model load(String resourcePath, int flags) throws Exception {
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
        
        int numMeshes = aiScene.mNumMeshes();
        PointerBuffer aiMeshes = aiScene.mMeshes();
        List<Mesh> meshes = new ArrayList<>();
        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            Mesh mesh = processMesh(aiMesh, materials);
            meshes.add(mesh);
        }      
        
        Model model = new Model("", meshes);
        
        return model;
    }
    
    protected static List<Integer> processIndices(AIMesh aiMesh) {
        List<Integer> indices = new ArrayList<>();
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }
        return indices;
    }
    
    protected static Material processMaterial(AIMaterial aiMaterial) throws Exception {
        AIColor4D colour = AIColor4D.create();
        Material material = new Material(aiMaterial.toString());
        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null,
                null, null, null, null, null);
        
        String textPath = path.dataString();
        if (textPath != null && textPath.length() > 0) {
            material.hascolmap = true;
            material.mapKdFilename = textPath;
        }
        
        Vector4f ambient = Material.DEFAULT_COLOUR;
        int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0,
                colour);
        if (result == 0) {
            ambient = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
        }
        material.ka = new Vector3f(ambient);
        
        Vector4f diffuse = Material.DEFAULT_COLOUR;
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0,
                colour);
        if (result == 0) {
            diffuse = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
        }
        
        material.kd = new Vector3f(diffuse);
        
        Vector4f specular = Material.DEFAULT_COLOUR;
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0,
                colour);
        if (result == 0) {
            specular = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
        }
        
        material.ks = new Vector3f(specular);
        
        return material;
    }
    
    private static Mesh processMesh(AIMesh aiMesh, List<Material> materials) { 
        List<Vector3f>  vertices  = processVertices(aiMesh);
        List<Vector3f>  normals   = processNormals(aiMesh);
        List<Vector2f>  texcoords = processTexCoords(aiMesh);
        List<Integer>   indices   = processIndices(aiMesh);
        
        Tuple<FloatBuffer, IntBuffer> buffers = MeshUtil.getMeshBuffers(
                Utils.listVector3fToArray(vertices),
                Utils.listVector2fToArray(texcoords),
                Utils.listVector3fToArray(normals),
                Utils.listIntToArray(indices));
        
        Material material;
        int materialIdx = aiMesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = Material.defaultmaterial;
        }
        
        Mesh mesh = new Mesh(buffers.x, buffers.y, material, false);
        
        return mesh;
    }
    
    protected static List<Vector3f> processNormals(AIMesh aiMesh) {
        ArrayList<Vector3f> normals = new ArrayList();
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        while (aiNormals.remaining() > 0) {
            normals.add(toVector3f(aiNormals.get()));
        }
        return normals;
    }
    
    protected static List<Vector2f> processTexCoords(AIMesh aiMesh) {
        ArrayList<Vector2f> texcoords = new ArrayList<>();
        AIVector3D.Buffer texCoords = aiMesh.mTextureCoords(0);
        int numTexCoords = texCoords != null ? texCoords.remaining() : 0;
        for (int i = 0; i < numTexCoords; i++) {
            AIVector3D texCoord = texCoords.get();
            texcoords.add(new Vector2f(texCoord.x(), 1-texCoord.y()));
        }
        return texcoords;
    }
    
    protected static List<Vector3f> processVertices(AIMesh aiMesh) {
        ArrayList<Vector3f> vertices = new ArrayList();
        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        while (aiVertices.remaining() > 0) {
            vertices.add(toVector3f(aiVertices.get()));
        }
        return vertices;
    }
    
    public static Vector3f toVector3f(AIVector3D vector){
        return new Vector3f(vector.x(), vector.y(), vector.z());
    }
}
