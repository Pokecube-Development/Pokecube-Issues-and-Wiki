package thut.core.client.render.tabula.old;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.core.client.render.tabula.json.TblJson;
import thut.core.common.ThutCore;

public class TabulaModelParser
{
    @OnlyIn(Dist.CLIENT)
    public static ModelJson<?> load(final ResourceLocation model)
    {
        try
        {
            final IResource res = Minecraft.getInstance().getResourceManager().getResource(model);
            if (res == null) return new ModelJson<>(new TblJson());
            final ZipInputStream zip = new ZipInputStream(res.getInputStream());
            final Scanner scanner = new Scanner(zip);
            zip.getNextEntry();
            final String json = scanner.nextLine();
            scanner.close();
            return TabulaModelParser.parse(json);
        }
        catch (final IOException e)
        {
            if (!(e instanceof FileNotFoundException)) ThutCore.LOGGER.error("error loading " + model, e);
            return null;
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static ModelJson<?> parse(final String json) throws IOException
    {
        TblJson tabulaModel = null;
        final InputStream in = IOUtils.toInputStream(json, "UTF-8");
        final InputStreamReader reader = new InputStreamReader(in);
        tabulaModel = JsonFactory.getGson().fromJson(reader, TblJson.class);
        reader.close();
        ModelJson<?> model = null;
        if (tabulaModel != null)
        {
            model = new ModelJson<>(tabulaModel);
            model.valid = true;
        }
        else model = new ModelJson<>(new TblJson());
        return model;
    }
}
