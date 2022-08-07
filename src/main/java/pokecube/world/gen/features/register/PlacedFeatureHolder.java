package pokecube.world.gen.features.register;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import pokecube.core.PokecubeCore;
import pokecube.world.gen.features.FeaturesInit;

public class PlacedFeatureHolder implements Holder<PlacedFeature>
{
    Holder<PlacedFeature> when_loaded;
    ResourceLocation name;
    MinecraftServer server = null;
    Supplier<PlacedFeature> dummy_getter = FeaturesInit.DUMMY_PF;

    public PlacedFeatureHolder(String name)
    {
        if (!name.contains(":")) this.name = new ResourceLocation("pokecube_world", name);
        else this.name = new ResourceLocation(name);
    }

    private boolean init()
    {
        if (when_loaded == null || server != PokecubeCore.proxy.getServer())
        {
            server = PokecubeCore.proxy.getServer();
            if (server != null)
            {
                Registry<PlacedFeature> reg = server.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
                when_loaded = Holder.direct(reg.get(name));
            }
        }
        return when_loaded != null;
    }

    @Override
    public PlacedFeature value()
    {
        if (!init()) return dummy_getter.get();
        return when_loaded.value();
    }

    @Override
    public boolean isBound()
    {
        if (!init()) return false;
        return when_loaded.isBound();
    }

    @Override
    public boolean is(ResourceLocation a)
    {
        if (!init()) return false;
        return when_loaded.is(a);
    }

    @Override
    public boolean is(ResourceKey<PlacedFeature> a)
    {
        if (!init()) return false;
        return when_loaded.is(a);
    }

    @Override
    public boolean is(Predicate<ResourceKey<PlacedFeature>> a)
    {
        if (!init()) return false;
        return when_loaded.is(a);
    }

    @Override
    public boolean is(TagKey<PlacedFeature> a)
    {
        if (!init()) return false;
        return when_loaded.is(a);
    }

    @Override
    public Stream<TagKey<PlacedFeature>> tags()
    {
        if (!init()) return Stream.of();
        return when_loaded.tags();
    }

    @Override
    public Either<ResourceKey<PlacedFeature>, PlacedFeature> unwrap()
    {
        if (!init()) return Either.right(dummy_getter.get());
        return when_loaded.unwrap();
    }

    @Override
    public Optional<ResourceKey<PlacedFeature>> unwrapKey()
    {
        if (!init()) return Optional.empty();
        return when_loaded.unwrapKey();
    }

    @Override
    public Kind kind()
    {
        if (!init()) return Kind.DIRECT;
        return Kind.REFERENCE;
    }

    @Override
    public boolean isValidInRegistry(Registry<PlacedFeature> p_205708_)
    {
        if (!init()) return true;
        return when_loaded.isValidInRegistry(p_205708_);
    }

}
