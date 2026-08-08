package thut.core.client.render.model.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.util.FastColor;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.texturing.TextureCoordinate;

public class Mesh
{
    public static boolean debug = false;

    public static float windowScale = 1;
    public static int verts = 0;
    public static double modelCullThreshold = 0;

    public static Mesh merge(Mesh... meshs)
    {
        if(meshs.length==0) return null;
        if(!Arrays.stream(meshs).allMatch(mesh -> mesh.GL_FORMAT==meshs[0].GL_FORMAT)) return null;
        List<Vertex> verts = new ArrayList<>();
        List<Vertex> norms = new ArrayList<>();
        List<Vertex> normsList = new ArrayList<>();
        List<TextureCoordinate> texs = new ArrayList<>();
        float len = 0;
        for(var mesh:meshs)
        {
            verts.addAll(Arrays.stream(mesh.vertices).toList());
            norms.addAll(Arrays.stream(mesh.normals).toList());
            normsList.addAll(Arrays.stream(mesh.normalList).toList());
            texs.addAll(Arrays.stream(mesh.textureCoordinates).toList());
            // TODO see if we need to recompute this better?
            len = Math.max(len, mesh.len);
        }
        return new Mesh(verts.toArray(new Vertex[0]),
                norms.toArray(new Vertex[0]),normsList.toArray(new Vertex[0]),
                texs.toArray(new TextureCoordinate[0]), meshs[0].GL_FORMAT,len, meshs[0].material);
    }

    public final Vertex[] vertices;
    public final Vertex[] normals;
    public final TextureCoordinate[] textureCoordinates;

    Material material;
    public String name;
    public boolean overrideColour = false;
    private final double[] uvShift =
    { 0, 0 };
    final int GL_FORMAT;
    final Vertex[] normalList;

    public int[] rgbabro = new int[6];

    private boolean same_mat = false;

    public final Mode vertexMode;

    Vertex min = new Vertex(0, 0);
    Vertex max = new Vertex(0, 0);

    final int iter;

    protected final float len;
    public float cullScale = 1;
    public float renderScale = 1;

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

    private Mesh(final Vertex[] vert, final Vertex[] norm, final Vertex[] normList, final TextureCoordinate[] tex,
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

    public Mesh(final Integer[] order, final Vertex[] vert, final Vertex[] norm, final TextureCoordinate[] tex,
            final int GL_FORMAT)
    {
        List<Vertex> vertTmp = new ArrayList<>(Arrays.stream(vert).toList());
        List<Vertex> normATmp = new ArrayList<>(order.length);
        List<Vertex> normBTmp = new ArrayList<>(order.length);
        for(int i = 0; i< order.length;i++) {normBTmp.add(null);normATmp.add(null);}
        List<TextureCoordinate> texTmp = tex==null? new ArrayList<>(): Arrays.stream(tex).toList();
        // In this case, just fill all with dummy tex.
        TextureCoordinate dummyTex = new TextureCoordinate(0, 0);
        if(tex==null) for(int i=0; i<order.length;i++) texTmp.add(dummyTex);

        this.GL_FORMAT = GL_FORMAT;
        Vertex vertex;
        Vertex normal;
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
            normal = new Vertex(c.x, c.y, c.z);
            for (int j = i; j < i + iter; j++)
            {
                int i_0 = order[j];
                normBTmp.set(j, normal);
                if (norm != null) normATmp.set(j, norm[i_0]);
            }
        }

        min.set(mins);
        max.set(maxs);

        Vector3f dummy_1 = new Vector3f();
        dummy_1.set(max.x - min.x, max.y - min.y, max.z - min.z);
        len = (float) Math.sqrt(dummy_1.dot(dummy_1));

        // Now sort everything to no longer need the "order" array
        List<Vertex> _verts = new ArrayList<>();
        List<Vertex> _norms  = new ArrayList<>();
        List<Vertex> _normsL  = new ArrayList<>();
        List<TextureCoordinate> _tex = new ArrayList<>();

        for (int i0 = 0; i0 < order.length; i0++)
        {
            int i = order[i0];
            _norms.add(normATmp.get(i0));
            _normsL.add(normBTmp.get(i0));
            _verts.add(vertTmp.get(i));
            _tex.add(texTmp.get(i));
        }

        this.vertices = _verts.toArray(new Vertex[0]);
        this.normalList = _normsL.toArray(new Vertex[0]);
        this.normals = norm!=null?_norms.toArray(new Vertex[0]):normalList;
        this.textureCoordinates = _tex.toArray(new TextureCoordinate[0]);

        // Initialize a "default" material for us
        this.material = new Material("auto:" + this.name);
        this.material.vertexMode = this.vertexMode;
    }

    private final Vector3f dummy3 = new Vector3f();
    private final Vector4f dummy4 = new Vector4f();

    protected void doRender(final PoseStack mat, final VertexConsumer buffer)
    {
        final PoseStack.Pose matrixstack$entry = mat.last();
        final Matrix4f pos = matrixstack$entry.pose();
        final Vector4f dp = this.dummy4;

        float x, y, z, u, v;
        if (modelCullThreshold > 0)
        {
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
        Vertex[] normals = flat ? this.normalList : this.normals;
        final Vector3f dn = this.dummy3;
        final Matrix3f norms = matrixstack$entry.normal();

        Vertex vertex;
        Vertex normal;
        TextureCoordinate textureCoordinate;

        float du = (float) this.uvShift[0];
        float dv = (float) this.uvShift[1];
        float su = 1;
        float sv = 1;

        if (this.material.getTexture() != null)
        {
            float[] ouv = this.material.getTexture().getTexOffset();
            float[] suv = this.material.getTexture().getTexScale();
            du += ouv[0];
            dv += ouv[1];

            su *= suv[0];
            sv *= suv[1];
        }

        if (this.renderScale != 1)
        {
            float dx = (max.x - min.x) / 2;
            float mx = min.x + dx;

            float dy = (max.y - min.y) / 2;
            float my = min.y + dy;

            float dz = (max.z - min.z) / 2;
            float mz = min.z + dz;

            // This loop is copied here vs below for performance reasons, we
            // can't guarentee compiler flags are set properly.
            for(int i = 0; i<this.vertices.length; i++)
            {
                // Next we can pull out the coordinates if not culled.
                textureCoordinate = this.textureCoordinates[i];
                vertex = this.vertices[i];

                verts++;

                // Normals first, as they define culling.=
                dn.set(normals[i]);
                dn.mul(norms);

                x = Math.fma(this.renderScale, (vertex.x - mx), mx);
                y = Math.fma(this.renderScale, (vertex.y - my), my);
                z = Math.fma(this.renderScale, (vertex.z - mz), mz);

                dp.set(x, y, z, 1);
                dp.mul(pos);

                // This results in u * su + du
                u = Math.fma(textureCoordinate.u, su, du);
                v = Math.fma(textureCoordinate.v, sv, dv);

                // We use the default mob format, since that is what mobs use.
                // This means we need these in this order!
                buffer.addVertex(
                //@formatter:off
                    dp.x(), dp.y(), dp.z(),
                    argb,
                    u, v,
                    overlayUV, lightmapUV,
                    dn.x(), dn.y(), dn.z());
                //@formatter:on
            }
        }
        else for(int i = 0; i<this.vertices.length; i++)
        {
            // Next we can pull out the coordinates if not culled.
            textureCoordinate = this.textureCoordinates[i];

            verts++;

            // Normals first, as they define culling.
            dn.set(normals[i]);
            dn.mul(norms);

            // Then the vertex
            dp.set(this.vertices[i], 1);
            dp.mul(pos);

            // This results in u * su + du
            u = Math.fma(textureCoordinate.u, su, du);
            v = Math.fma(textureCoordinate.v, sv, dv);

            // We use the default mob format, since that is what mobs use.
            // This means we need these in this order!
            buffer.addVertex(
            //@formatter:off
                dp.x(), dp.y(), dp.z(),
                argb,
                u, v,
                overlayUV, lightmapUV,
                dn.x(), dn.y(), dn.z());
            //@formatter:on
        }
    }

    public void renderShape(final PoseStack mat, VertexConsumer buffer, final IPartTexturer texturer)
    {
        // Apply Texturing.
        if (texturer != null)
        {
            texturer.shiftUVs(this.material.name, this.uvShift);
            if (texturer.isHidden(this.material.name)) return;
            if (!same_mat && texturer.isHidden(this.name)) return;
            texturer.modifiyRGBA(this.material.name, material.rgbabro);
            if (!same_mat) texturer.modifiyRGBA(this.name, material.rgbabro);
        }
        if (this.material.emissiveMagnitude > 0)
        {
            final int j = (int) (this.material.emissiveMagnitude * 15);
            material.rgbabro[4] = j << 20 | j << 4;
        }
        buffer = this.material.preRender(mat, buffer, this.vertexMode);
        this.doRender(mat, buffer);
    }

    public void setMaterial(final Material material)
    {
        this.material = material;
        this.name = material.name;
        same_mat = true;
    }
}
