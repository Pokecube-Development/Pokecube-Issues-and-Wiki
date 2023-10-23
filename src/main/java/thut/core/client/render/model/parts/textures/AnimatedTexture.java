package thut.core.client.render.model.parts.textures;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import thut.api.util.JsonUtil;
import thut.lib.ResourceHelper;

public class AnimatedTexture extends BaseTexture implements Tickable
{
    public static class McMeta
    {
        public AnimInfo animation;
    }

    public static class AnimInfo
    {
        public int frametime = 3;
        public float width = -1;
        public float height = -1;
        public boolean interpolate = false;
        public List<JsonElement> frames = new ArrayList<>();
        public List<AnimFrame> _frames = new ArrayList<>();
        public int _size = 0;
        public float[] _uvOffset =
        { 0, 0 };
        public float[] _uvScale =
        { 1, 1 };

        public void init(NativeImage img)
        {
            if (frames.isEmpty())
            {
                double expectedAspectRatio = height / width;
                double imgAspectRatio = img.getHeight() / ((double) img.getWidth());
                _size = (int) Math.round(imgAspectRatio / expectedAspectRatio);
                for (int i = 0; i < _size; i++)
                {
                    AnimFrame frame = new AnimFrame();
                    frame.index = i;
                    frame.time = this.frametime;
                    this._frames.add(frame);
                }
            }
            else
            {
                frames.forEach(json -> {
                    if (json.isJsonPrimitive())
                    {
                        int index = json.getAsInt();
                        AnimFrame frame = new AnimFrame();
                        frame.index = index;
                        frame.time = this.frametime;
                        _frames.add(frame);
                    }
                    else if (json.isJsonObject())
                    {
                        JsonObject object = json.getAsJsonObject();
                        if (object.has("time"))
                        {
                            int time = object.get("time").getAsInt();
                            AnimFrame frame = new AnimFrame();
                            int index = this._frames.size();
                            frame.index = index;
                            frame.time = time;
                            _frames.add(frame);
                        }
                    }
                });
                this._frames.sort(null);
            }
            this._size = _frames.size();

            if (_size > 0)
            {
                _uvScale[1] /= _size;
            }
        }

        public void tick(AnimFrame frame)
        {
            _uvOffset[1] = frame.index / (float) _size;
        }
    }

    public static class AnimFrame implements Comparable<AnimFrame>
    {
        public int index;
        public int time;

        @Override
        public int compareTo(AnimFrame o)
        {
            return Integer.compare(index, o.index);
        }
    }

    private AnimInfo info = new AnimInfo();
    private int subFrame = 0;
    private int frame = 0;

    public AnimatedTexture(ResourceLocation location, NativeImage img, float expectedH, float expectedW,
            boolean hasMeta)
    {
        super(location);
        try
        {
            var manager = Minecraft.getInstance().getResourceManager();
            if (img == null)
            {
                img = this.getImage();
            }
            if (hasMeta)
            {
                ResourceLocation mcmeta = new ResourceLocation(location.getNamespace(), location.getPath() + ".mcmeta");
                var reader = ResourceHelper.getReader(mcmeta, manager);
                info = JsonUtil.gson.fromJson(reader, McMeta.class).animation;
                if (info != null)
                {
                    if (info.width < 0) info.width = expectedW;
                    if (info.height < 0) info.height = expectedH;
                    info.init(img);
                }
                else this.info = new AnimInfo();
            }
            else
            {
                if (this.info.width < 0) this.info.width = expectedW;
                if (this.info.height < 0) this.info.height = expectedH;
                this.info.init(img);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void tick()
    {
        // Error occured on load, we had no frames!
        if (this.info._size == 0) return;

        // see AtlasTextureSprite for vanilla's very similar implementation of
        // this.
        this.subFrame++;
        int timer = this.subFrame;
        AnimFrame frame = this.info._frames.get(this.frame);
        if (timer >= frame.time)
        {
            this.frame = (this.frame + 1) % this.info._size;
            this.subFrame = 0;
        }
        this.info.tick(frame);
    }

    @Override
    public float[] getTexOffset()
    {
        return this.info._uvOffset;
    }

    @Override
    public float[] getTexScale()
    {
        return this.info._uvScale;
    }

}
