package thut.core.client.render.model.parts;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MeshRenderable extends Mesh
{
    public final Mode vertexMode;

    private final Vector3f dn = new Vector3f();
    private final Vector4f dp = new Vector4f();
    private final Vector2f texdR = new Vector2f(), texdS = new Vector2f(), texUV =new Vector2f();

    public MeshRenderable(Integer[] order, Vector3f[] vert, Vector3f[] norm, Vector2f[] tex, int GL_FORMAT)
    {
        super(order, vert, norm, tex, GL_FORMAT);
        this.vertexMode = GL_FORMAT == TRIANGLE_FMT ? Mode.TRIANGLES : Mode.QUADS;

        // Initialize a "default" material for us
        this.materialRenderable = new MaterialRenderable("auto:" + this.name);
        this.material = this.materialRenderable;
        this.materialRenderable.vertexMode = this.vertexMode;
    }

    public MeshRenderable(final Vector3f[] vert, final Vector3f[] norm, final Vector3f[] normList, final Vector2f[] tex,
            final int GL_FORMAT, Object material)
    {
        super(vert, norm, normList, tex, GL_FORMAT, material);
        this.material = (MaterialRenderable) material;
        this.materialRenderable = (MaterialRenderable) material;
        this.vertexMode = GL_FORMAT == TRIANGLE_FMT ? Mode.TRIANGLES : Mode.QUADS;
    }

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

    public void renderShape(VertexConsumer buffer)
    {
        render:
        if(!hidden)
        {
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

                if (size_cull) break render;
            }

            float du = (float) this.uvShift[0], dv = (float) this.uvShift[1];
            float su = 1, sv = 1;

            // Apply Texturing.
            var texturer = texChangeHolder.get();
            if (texturer != null)
            {
                texturer.shiftUVs(this.material.name, this.uvShift);
                if (texturer.isHidden(this.material.name)) break render;
                if (!same_mat && texturer.isHidden(this.name)) break render;
                texturer.modifiyRGBA(this.material.name, rgbabro);
                if (!same_mat) texturer.modifiyRGBA(this.name, rgbabro);

                var texture = this.materialRenderable.getTexture();
                if (texture != null && (du != 0 || dv != 0))
                {
                    float[] ouv = texture.getTexOffset();
                    float[] suv = texture.getTexScale();
                    du += ouv[0];
                    dv += ouv[1];

                    su *= suv[0];
                    sv *= suv[1];
                }
            }

            // Apply material effects
            if (this.material.emissiveMagnitude > 0)
            {
                final int j = (int) (this.material.emissiveMagnitude * 15);
                rgbabro[4] = j << 20 | j << 4;
            }
            texdR.set(du, dv);
            texdS.set(su, sv);

            var vertexMode = GL_FORMAT == TRIANGLE_FMT ? Mode.TRIANGLES : Mode.QUADS;
            // Find buffer to render to, this is presently most expensive part here...
            buffer = this.materialRenderable.preRender(buffer, vertexMode);

            // Update colouring as needed
            int red = this.rgbabro[0];
            int green = this.rgbabro[1];
            int blue = this.rgbabro[2];
            int alpha = (int) (this.material.alpha * this.rgbabro[3]);
            int lightmapUV = this.rgbabro[4];
            int overlayUV = this.rgbabro[5];
            int argb;
            if (MaterialRenderable.HAS_IRIS && material.isShadow)
            {
                argb = MaterialRenderable.SHADOW_ARGB;
                lightmapUV = overlayUV = 0;
            }
            else argb = FastColor.ARGB32.color(alpha, red, green, blue);

            final boolean flat = this.material.flat;
            Vector3f[] normals = flat ? this.normalList : this.normals;
            final Matrix3f norms = poseInfo.normal();
            final Matrix4f pos = poseInfo.pose();
            // Finally render, this should be JIT Compiler friendly
            doRender(normals, norms, pos, argb, overlayUV, lightmapUV, buffer);
        }
    }

    @Override
    public void setMaterial(Material material)
    {
        super.setMaterial(material);
        this.materialRenderable = (MaterialRenderable) material;
    }

    @Override
    public int compareTo(Mesh o)
    {
        if(!(o.material instanceof Material remderMat)) return 0;
        // Compares by material, ignores edited flag check
        boolean editO = this.material.edited;
        this.materialRenderable.edited = remderMat.edited;
        int comp = this.materialRenderable.compareTo(remderMat);
        this.material.edited = editO;
        return comp;
    }
}
