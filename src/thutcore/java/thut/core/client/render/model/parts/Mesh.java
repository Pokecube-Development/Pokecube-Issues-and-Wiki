package thut.core.client.render.model.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector3f;

import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;

public class Mesh implements Comparable<Mesh>
{
    public static interface MeshFactory
    {
        Mesh create(Integer[] order, Vector3f[] vert, Vector3f[] norm, Vector2f[] tex, int GL_FORMAT);
    }

    public static interface MeshMergeFactory
    {
        Mesh create(Vector3f[] vert, Vector3f[] norm, Vector3f[] normList, Vector2f[] tex, int GL_FORMAT,
                Object material);
    }

    public static MeshFactory MESH_FACTORY = Mesh::new;
    public static MeshMergeFactory MERGE_FACTORY = Mesh::new;

    public static boolean debug = false;

    public static float windowScale = 1;
    public static double modelCullThreshold = 0;
    public static int TRIANGLE_FMT = 4; // GL11.GL_TRIANGLES
    public static int QUAD_FMT = 7; // GL11.GL_QUADS

    public static List<Mesh> merge(List<Mesh> meshs)
    {
        if (meshs.size() < 2) return meshs;
        Map<Integer, List<Mesh>> byFMT = new HashMap<>();
        meshs.forEach(mesh->byFMT.computeIfAbsent(mesh.GL_FORMAT, i->new ArrayList<>()).add(mesh));
        List<Mesh> retList = new ArrayList<>();
        byFMT.forEach((format, list)->{
            var first = list.getFirst();
            if(list.size() == 1)
            {
                retList.add(first);
                return;
            }
            List<Vector3f> verts = new ArrayList<>();
            List<Vector3f> norms = new ArrayList<>();
            List<Vector3f> normsList = new ArrayList<>();
            List<Vector2f> texs = new ArrayList<>();
            for(var mesh : list)
            {
                verts.addAll(Arrays.stream(mesh.vertices).toList());
                norms.addAll(Arrays.stream(mesh.normals).toList());
                normsList.addAll(Arrays.stream(mesh.normalList).toList());
                texs.addAll(Arrays.stream(mesh.textureCoordinates).toList());
            }
            var mesh = MERGE_FACTORY.create(verts.toArray(new Vector3f[0]),
                    norms.toArray(new Vector3f[0]),normsList.toArray(new Vector3f[0]),
                    texs.toArray(new Vector2f[0]), format, first.material);
            mesh.poseInfo = first.poseInfo;
            mesh.texChangeHolder = first.texChangeHolder;
            mesh.name = first.name;
            // Marks the "same mat" as true
            if (first.name.equals(first.material.name)) mesh.setMaterial(first.material);
            retList.add(mesh);
        });
        return retList;
    }

    public final Vector3f[] vertices;
    /**
     * Vertex normals, whatever are loaded in from the model,
     * otherwise computed identically to normalList
     */
    public final Vector3f[] normals;
    public final Vector2f[] textureCoordinates;

    /**
     * This is the Material normally associated with this mesh,
     * it is whatever results from the model loading set of code.
     */
    public Material material;
    public String name;
    public boolean overrideColour = false;
    public boolean hidden = false;
    public boolean is2D = false;
    protected final double[] uvShift =
    { 0, 0 };
    public final int GL_FORMAT;
    /**
     * Face normals,computed from render order itself.
     */
    final Vector3f[] normalList;
    public IModelCustom.PoseInfo poseInfo = new IModelCustom.PoseInfo();
    public Supplier<IPartTexturer> texChangeHolder = new IRetexturableModel.Holder<>();

    public int[] rgbabro = new int[6];

    protected boolean same_mat = false;

    Vector3f min = new Vector3f();
    Vector3f max = new Vector3f();

    protected float len;
    public float cullScale = 1;

    private static void clip(Vector3f bound, Vector3f point, boolean up)
    {
        if (up)
        {
            if (point.x > bound.x) bound.x = point.x;
            if (point.y > bound.y) bound.y = point.y;
            if (point.z > bound.z) bound.z = point.z;
        }
        else
        {
            if (point.x < bound.x) bound.x = point.x;
            if (point.y < bound.y) bound.y = point.y;
            if (point.z < bound.z) bound.z = point.z;
        }
    }

    protected Mesh(final Vector3f[] vert, final Vector3f[] norm, final Vector3f[] normList, final Vector2f[] tex,
            final int GL_FORMAT, Object material){
        this.vertices= vert;
        this.normals = norm;
        this.normalList = normList;
        this.textureCoordinates = tex;
        this.GL_FORMAT = GL_FORMAT;
        initStats();
    }

    public Mesh(final Integer[] order, final Vector3f[] vert, final Vector3f[] norm, final Vector2f[] tex,
            final int GL_FORMAT)
    {
        List<Vector3f> vertTmp = new ArrayList<>(Arrays.stream(vert).toList());
        List<Vector3f> normATmp = new ArrayList<>(order.length);
        List<Vector3f> normBTmp = new ArrayList<>(order.length);
        for(int i = 0; i< order.length;i++) {normBTmp.add(null);normATmp.add(null);}
        List<Vector2f> texTmp = tex==null? new ArrayList<>(): Arrays.stream(tex).toList();
        // In this case, just fill all with dummy tex.
        Vector2f dummyTex = new Vector2f(0, 0);
        if(tex==null) for(int i=0; i<order.length;i++) texTmp.add(dummyTex);

        this.GL_FORMAT = GL_FORMAT;
        Vector3f vertex;
        Vector3f normal;
        int iter = GL_FORMAT == TRIANGLE_FMT ? 3 : 4;

        final Vector3f a = new Vector3f(),b = new Vector3f(),c = new Vector3f();

        int i_1, i_2, i_3;
        // Calculate the normals for each triangle.
        for (int i = 0; i < order.length; i += iter)
        {
            i_1 = order[i + 0];
            i_2 = order[i + 1];
            i_3 = order[i + 2];

            Vector3f v1, v2, v3;
            vertex = vertTmp.get(i_1);
            v1 = new Vector3f(vertex.x, vertex.y, vertex.z);
            vertex = vertTmp.get(i_2);
            v2 = new Vector3f(vertex.x, vertex.y, vertex.z);
            vertex = vertTmp.get(i_3);
            v3 = new Vector3f(vertex.x, vertex.y, vertex.z);

            v2.sub(v1,a);
            v3.sub(v1,b);
            a.cross(b, c);
            c.normalize();
            if (Double.isNaN(c.x))
            {
                c.x = 0;
                c.y = 0;
                c.z = 1;
            }
            normal = new Vector3f(c.x, c.y, c.z);
            for (int j = i; j < i + iter; j++)
            {
                int i_0 = order[j];
                normBTmp.set(j, normal);
                if (norm != null) normATmp.set(j, norm[i_0]);
            }
        }

        // Now sort everything to no longer need the "order" array
        List<Vector3f> _verts = new ArrayList<>();
        List<Vector3f> _norms  = new ArrayList<>();
        List<Vector3f> _normsL  = new ArrayList<>();
        List<Vector2f> _tex = new ArrayList<>();

        for (int i0 = 0; i0 < order.length; i0++)
        {
            int i = order[i0];
            _norms.add(normATmp.get(i0));
            _normsL.add(normBTmp.get(i0));
            _verts.add(vertTmp.get(i));
            _tex.add(texTmp.get(i));
        }

        this.vertices = _verts.toArray(new Vector3f[0]);
        this.normalList = _normsL.toArray(new Vector3f[0]);
        this.normals = norm!=null?_norms.toArray(new Vector3f[0]):normalList;
        this.textureCoordinates = _tex.toArray(new Vector2f[0]);

        initStats();
    }

    private void initStats()
    {
        Vector3f dn = new Vector3f();
        dn.set(normalList[0]);
        double epsD = 1e-10;
        is2D = true;
        for (var n1 : normalList)
        {
            if (!is2D) break;
            is2D &= Math.abs(dn.dot(n1)) > 1 - epsD;
        }
        Vector3f mins = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f maxs = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        for (var v : vertices)
        {
            clip(mins, v, false);
            clip(maxs, v, true);
        }
        min.set(mins);
        max.set(maxs);
        // First set to extents for len calc
        Vector3f _len = new Vector3f();
        _len.set(max).sub(min);
        len = (float) Math.sqrt(_len.dot(_len));
    }

    @OnlyIn(Dist.CLIENT)
    public void renderShape(com.mojang.blaze3d.vertex.VertexConsumer buffer)
    {

    }

    public void setMaterial(final Material material)
    {
        this.material = material;
        this.name = material.name;
        same_mat = true;
    }

    @OnlyIn(Dist.CLIENT)
    protected MaterialRenderable materialRenderable;
    @OnlyIn(Dist.CLIENT)
    public MaterialRenderable getRenderMaterial()
    {
        return this.materialRenderable;
    }

    @Override
    public int compareTo(Mesh o)
    {
        return this.name.compareTo(o.name);
    }
}
