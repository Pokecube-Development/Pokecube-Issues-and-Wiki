package thut.tech.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import thut.core.common.ThutCore;
import thut.core.common.config.Config;
import thut.core.common.network.PacketHandler;
import thut.tech.Reference;
import thut.tech.common.blocks.lift.ControllerBlock;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.items.ItemLinker;
import thut.tech.common.util.RecipeSerializers;

@Mod(value = Reference.MOD_ID)
public class TechCore
{
    public final static PacketHandler packets = new PacketHandler(new ResourceLocation(Reference.MOD_ID, "comms"),
            Reference.NETVERSION);

    public static final DeferredRegister<Item> ITEMS;
    public static final DeferredRegister<Block> BLOCKS;
    public static final DeferredRegister<EntityType<?>> ENTITY;
    public static final DeferredRegister<BlockEntityType<?>> TILEENTITY;

    public static final RegistryObject<Block> LIFTCONTROLLER;

    public static final RegistryObject<Item> LIFT;
    public static final RegistryObject<Item> LINKER;

    public static final RegistryObject<EntityType<EntityLift>> LIFTTYPE;

    public static final RegistryObject<BlockEntityType<ControllerTile>> CONTROLTYPE;

    public static final ConfigHandler config = new ConfigHandler(Reference.MOD_ID);

    static
    {
        TILEENTITY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Reference.MOD_ID);
        ENTITY = DeferredRegister.create(ForgeRegistries.ENTITIES, Reference.MOD_ID);
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);

        LIFTTYPE = TechCore.ENTITY.register("lift", () -> new EntityLift.BlockEntityType<>(EntityLift::new));

        CONTROLTYPE = TechCore.TILEENTITY.register("controller",
                () -> BlockEntityType.Builder.of(ControllerTile::new, TechCore.LIFTCONTROLLER.get()).build(null));
        LIFTCONTROLLER = TechCore.BLOCKS.register("controller", () -> new ControllerBlock(
                Block.Properties.of(Material.METAL).strength(3.5f).dynamicShape().noOcclusion()));

        LIFT = TechCore.ITEMS.register("lift", () -> new Item(new Item.Properties().tab(ThutCore.THUTITEMS)));
        LINKER = TechCore.ITEMS.register("linker", () -> new ItemLinker(new Item.Properties().tab(ThutCore.THUTITEMS)));

        for (final RegistryObject<Block> reg : TechCore.BLOCKS.getEntries())
            TechCore.ITEMS.register(reg.getId().getPath(),
                    () -> new BlockItem(reg.get(), new Item.Properties().tab(ThutCore.THUTITEMS)));
    }

    public TechCore()
    {
        ThutCore.FORGE_BUS.register(this);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register recipe serializers
        RecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        TechCore.ITEMS.register(modEventBus);
        TechCore.BLOCKS.register(modEventBus);
        TechCore.TILEENTITY.register(modEventBus);
        TechCore.ENTITY.register(modEventBus);

        // Register Config stuff
        Config.setupConfigs(TechCore.config, Reference.MOD_ID, Reference.MOD_ID);
    }
}
