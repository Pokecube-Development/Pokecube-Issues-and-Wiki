package pokecube.adventures.items;

import java.util.UUID;

import com.mojang.datafixers.Dynamic;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.utils.CapHolders;
import thut.api.IOwnable;
import thut.api.LinkableCaps.ILinkStorage;
import thut.api.LinkableCaps.LinkStorage;
import thut.api.OwnableCaps;
import thut.api.ThutCaps;

public class Linker extends Item
{
    private static final ResourceLocation LINKSTOREKEY = new ResourceLocation(PokecubeAdv.MODID, "linker");

    private static class LinkStore extends LinkStorage
    {
        private final ItemStack linker;

        public LinkStore(final ItemStack linker)
        {
            this.linker = linker;
        }

        @Override
        public boolean setLinkedMob(final UUID mobid, final Entity user)
        {
            // we do not link mobs;
            return false;
        }

        @Override
        public GlobalPos getLinkedPos(final Entity user)
        {
            if (this.linker.getOrCreateTag().contains("thutcore:pos")) return GlobalPos.deserialize(new Dynamic<>(
                    NBTDynamicOps.INSTANCE, this.linker.getOrCreateTag().getCompound("thutcore:pos")));
            else return null;
        }

        @Override
        public boolean setLinkedPos(final GlobalPos pos, final Entity user)
        {
            if (pos == null || user.isSneaking())
            {
                this.linker.getOrCreateTag().remove("thutcore:pos");
                if (!user.getEntityWorld().isRemote) user.sendMessage(new TranslationTextComponent(
                        "item.pokecube_adventures.linker.unset"));
            }
            else
            {
                this.linker.getOrCreateTag().put("thutcore:pos", pos.serialize(NBTDynamicOps.INSTANCE));
                if (!user.getEntityWorld().isRemote) user.sendMessage(new TranslationTextComponent(
                        "item.pokecube_adventures.linker.set"));
                if (user.getEntityWorld().isRemote) try
                {
                    final String loc = String.format("%d %d %d", pos.getPos().getX(), pos.getPos().getY(), pos.getPos()
                            .getZ());
                    Minecraft.getInstance().keyboardListener.setClipboardString(loc);
                    user.sendMessage(new TranslationTextComponent("item.pokecube_adventures.linker.copied"));
                }
                catch (final Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            return true;
        }
    }

    @SubscribeEvent
    public static void attachCaps(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(Linker.LINKSTOREKEY)) return;
        if (event.getObject().getItem() instanceof Linker) event.addCapability(Linker.LINKSTOREKEY, new LinkStore(event
                .getObject()));
    }

    public static boolean interact(final ServerPlayerEntity playerIn, final Entity target, final ItemStack stack)
    {
        final IGuardAICapability ai = target.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        final LazyOptional<ILinkStorage> test_stack = stack.getCapability(ThutCaps.STORE, null);
        if (!test_stack.isPresent()) return false;
        final ILinkStorage storage = test_stack.orElse(null);
        final GlobalPos pos = storage.getLinkedPos(playerIn);
        if (ai != null && pos != null && pos.getDimension() == target.dimension)
        {
            final IOwnable ownable = OwnableCaps.getOwnable(target);
            boolean valid = false;
            if (ownable != null && ownable.getOwnerId() != null) valid = playerIn.getUniqueID().equals(ownable
                    .getOwnerId()) && PermissionAPI.hasPermission(playerIn, Linker.PERMLINKPET);
            else if (TrainerCaps.getHasPokemobs(target) != null) valid = PermissionAPI.hasPermission(playerIn,
                    Linker.PERMLINKTRAINER);
            if (valid)
            {
                final BlockPos bpos = pos.getPos().up();
                ai.getPrimaryTask().setPos(pos.getPos().up());
                playerIn.sendMessage(new TranslationTextComponent("item.pokecube_adventures.linked.mob", target
                        .getDisplayName(), bpos.getX(), bpos.getY(), bpos.getZ()));
                return true;
            }
            else playerIn.sendMessage(new TranslationTextComponent("item.pokecube_adventures.linked.mob.fail"));
        }
        return false;
    }

    public static String PERMLINKTRAINER = "pokecube_adventures.linker.link_npc";
    public static String PERMLINKPET     = "pokecube_adventures.linker.link_pet";

    public Linker(final Properties properties)
    {
        super(properties);
        PermissionAPI.registerNode(Linker.PERMLINKTRAINER, DefaultPermissionLevel.OP,
                "Is the player allowed to use the linker item to set a trainer's stationary location");
        PermissionAPI.registerNode(Linker.PERMLINKPET, DefaultPermissionLevel.ALL,
                "Is the player allowed to use the linker item to set their pokemob's stationary location");
    }

}
