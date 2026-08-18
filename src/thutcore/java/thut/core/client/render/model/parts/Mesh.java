package thut.core.client.render.model.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.util.FastColor;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.IRetexturableModel;

public class Mesh implements Comparable<Mesh>
{
    public static boolean debug = false;

    public static float windowScale = 1;
    public static double modelCullThreshold = 0;

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
            float len = 0;
            for(var mesh : list)
            {
                verts.addAll(Arrays.stream(mesh.vertices).toList());
                norms.addAll(Arrays.stream(mesh.normals).toList());
                normsList.addAll(Arrays.stream(mesh.normalList).toList());
                texs.addAll(Arrays.stream(mesh.textureCoordinates).toList());
                // TODO see if we need to recompute this better?
                len = Math.max(len, mesh.len);
            }
            var mesh = new Mesh(verts.toArray(new Vector3f[0]),
                    norms.toArray(new Vector3f[0]),normsList.toArray(new Vector3f[0]),
                    texs.toArray(new Vector2f[0]), format, len, first.material);
            mesh.poseInfo = first.poseInfo;
            mesh.texChangeHolder = first.texChangeHolder;
            retList.add(mesh);
        });
        return retList;
    }

    public final Vector3f[] vertices;
    public final Vector3f[] normals;
    public final Vector2f[] textureCoordinates;

    public Material material;
    public String name;
    public boolean overrideColour = false;
    public boolean hidden = false;
    private final double[] uvShift =
    { 0, 0 };
    final int GL_FORMAT;
    final Vector3f[] normalList;
    public IModelCustom.PoseInfo poseInfo = new IModelCustom.PoseInfo();
    public Supplier<IPartTexturer> texChangeHolder = new IRetexturableModel.Holder<>();

    public int[] rgbabro = new int[6];

    private boolean same_mat = false;

    public final Mode vertexMode;

    Vector3f min = new Vector3f();
    Vector3f max = new Vector3f();
    public Vector3f mid = new Vector3f();

    final int iter;

    protected final float len;
    public float cullScale = 1;

    public static Vector4f METRIC = new Vector4f(1, 1, 1, 0);

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

    private Mesh(final Vector3f[] vert, final Vector3f[] norm, final Vector3f[] normList, final Vector2f[] tex,
            final int GL_FORMAT, float len, Material material){
        this.vertices= vert;
        this.normals = norm;
        this.normalList = normList;
        this.textureCoordinates = tex;
        this.GL_FORMAT = GL_FORMAT;
        this.iter = GL_FORMAT == GL11.GL_TRIANGLES ? 3 : 4;
        vertexMode = GL_FORMAT == GL11.GL_TRIANGLES ? Mode.TRIANGLES : Mode.QUADS;
        this.material = material;
        this.len = len;
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
        this.iter = GL_FORMAT == GL11.GL_TRIANGLES ? 3 : 4;

        vertexMode = GL_FORMAT == GL11.GL_TRIANGLES ? Mode.TRIANGLES : Mode.QUADS;

        Vector3f mins = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f maxs = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        final Vector3f a = new Vector3f(),b = new Vector3f(),c = new Vector3f();

        int i_1, i_2, i_3, i_4;
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

            clip(mins, v1, false);
            clip(mins, v2, false);
            clip(mins, v3, false);

            clip(maxs, v1, true);
            clip(maxs, v2, true);
            clip(maxs, v3, true);

            if (iter == 4)
            {
                i_4 = order[i + 3];
                vertex = vertTmp.get(i_4);
                Vector3f v4 = new Vector3f(vertex.x, vertex.y, vertex.z);

                clip(mins, v4, false);
                clip(maxs, v4, true);
            }

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

        min.set(mins);
        max.set(maxs);
        // First set to extents for len calc
        mid.set(max).sub(min);
        len = (float) Math.sqrt(mid.dot(mid));

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

        // Then compute mean point
        mid.set(0);
        for(var v: _verts) mid.add(v);
        mid.div(_verts.size());

        this.vertices = _verts.toArray(new Vector3f[0]);
        this.normalList = _normsL.toArray(new Vector3f[0]);
        this.normals = norm!=null?_norms.toArray(new Vector3f[0]):normalList;
        this.textureCoordinates = _tex.toArray(new Vector2f[0]);

        // Initialize a "default" material for us
        this.material = new Material("auto:" + this.name);
        this.material.vertexMode = this.vertexMode;
    }

    private final Vector3f dn = new Vector3f();
    private final Vector4f dp = new Vector4f();
    private final Vector2f texdR = new Vector2f(), texdS = new Vector2f(), texUV =new Vector2f();

    protected final void doRender(Vector3f[] normals, Matrix3f norms, Matrix4f pos, int argb, int overlayUV, int lightmapUV, VertexConsumer buffer)
    {
        // Hopefully the JIT sees what goes on here and optimises it...
        for(int i = 0; i<vertices.length; i++)
        {
            // Compute transformed normal
            normals[i].mul(norms, dn);
            // Then the vertex
            dp.set(vertices[i], 1);
            dp.mul(pos);
            // Then the texture
            texdR.fma(textureCoordinates[i], texdS, texUV);
            // We use the default mob format, since that is what mobs use.
            // This means we need these in this order!
            buffer.addVertex(
            //@formatter:off
                dp.x, dp.y, dp.z,
                argb,
                texUV.x, texUV.y,
                overlayUV, lightmapUV,
                dn.x, dn.y, dn.z);
            //@formatter:on
        }
    }

    public void setPose(PoseStack mat)
    {
        poseInfo.set(mat.last());
    }

    public void renderShape(VertexConsumer buffer)
    {
        if(hidden) return;
        // Check culling
        if (modelCullThreshold > 0)
        {
            Matrix4f pos = poseInfo.pose();
            float a = windowScale;
            float s = len * cullScale;

            dp.set(s, s, s, 0);
            dp.mul(pos);
            dp.mul(a);
            double dr2_us = dp.dot(dp);

            dp.set(0, 0, 0, 1);
            dp.mul(pos);
            double dr2_2 = dp.dot(dp);

            boolean size_cull = modelCullThreshold * dr2_2 >= dr2_us;

            if (size_cull) return;
        }

        // Apply Texturing.
        var texturer = texChangeHolder.get();
        if (texturer != null)
        {
            texturer.shiftUVs(this.material.name, this.uvShift);
            if (texturer.isHidden(this.material.name)) return;
            if (!same_mat && texturer.isHidden(this.name)) return;
            texturer.modifiyRGBA(this.material.name, material.rgbabro);
            if (!same_mat) texturer.modifiyRGBA(this.name, material.rgbabro);
        }

        // Apply material effects
        if (this.material.emissiveMagnitude > 0)
        {
            final int j = (int) (this.material.emissiveMagnitude * 15);
            material.rgbabro[4] = j << 20 | j << 4;
        }

        float du = (float) this.uvShift[0], dv = (float) this.uvShift[1];
        float su = 1, sv = 1;

        if (this.material.getTexture() != null)
        {
            float[] ouv = this.material.getTexture().getTexOffset();
            float[] suv = this.material.getTexture().getTexScale();
            du += ouv[0];
            dv += ouv[1];

            su *= suv[0];
            sv *= suv[1];
        }
        texdR.set(du, dv);
        texdS.set(su, sv);

        // Find buffer to render to, this is presently most expensive part here...
        buffer = this.material.preRender(buffer, this.vertexMode);

        // Update colouring as needed
        int red = material.rgbabro[0];
        int green = material.rgbabro[1];
        int blue = material.rgbabro[2];
        int alpha = (int) (this.material.alpha * material.rgbabro[3]);
        int lightmapUV = material.rgbabro[4];
        int overlayUV = material.rgbabro[5];

        if (debug || overrideColour)
        {
            red = this.rgbabro[0];
            green = this.rgbabro[1];
            blue = this.rgbabro[2];
            alpha = (int) (this.material.alpha * this.rgbabro[3]);
            lightmapUV = this.rgbabro[4];
            overlayUV = this.rgbabro[5];
        }
        int argb = FastColor.ARGB32.color(alpha, red, green, blue);

        final boolean flat = this.material.flat;
        Vector3f[] normals = flat ? this.normalList : this.normals;
        final Matrix3f norms = poseInfo.normal();
        final Matrix4f pos = poseInfo.pose();
        // Finally render, this should be JIT Compiler friendly
        doRender(normals, norms, pos, argb, overlayUV, lightmapUV, buffer);
    }

    public void setMaterial(final Material material)
    {
        this.material = material;
        this.name = material.name;
        same_mat = true;
    }

    @Override
    public int compareTo(@NotNull Mesh o)
    {
        // Compares by material, ignores edited flag check
        boolean editO = this.material.edited;
        this.material.edited = o.material.edited;
        int comp = this.material.compareTo(o.material);
        this.material.edited = editO;
        return comp;
    }
}
