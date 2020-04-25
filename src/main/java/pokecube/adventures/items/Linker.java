package pokecube.adventures.items;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
import thut.api.LinkableCaps;
import thut.api.LinkableCaps.ILinkStorage;
import thut.api.LinkableCaps.LinkStorage;
import thut.api.OwnableCaps;
import thut.api.maths.Vector4;

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
        public Vector4 getLinkedPos(final Entity user)
        {
            if (this.linker.getOrCreateTag().contains("link_pos")) return new Vector4(this.linker.getTag().getCompound(
                    "link_pos"));
            else return null;
        }

        @Override
        public boolean setLinkedPos(final Vector4 pos, final Entity user)
        {
            if (pos == null || user.isCrouching())
            {
                this.linker.getOrCreateTag().remove("link_pos");
                user.sendMessage(new TranslationTextComponent("item.pokecube_adventures.linker.unset"));
            }
            else
            {
                final CompoundNBT posTag = new CompoundNBT();
                pos.writeToNBT(posTag);
                this.linker.getOrCreateTag().put("link_pos", posTag);
                user.sendMessage(new TranslationTextComponent("item.pokecube_adventures.linker.set"));
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
        final LazyOptional<ILinkStorage> test_stack = stack.getCapability(LinkableCaps.STORE, null);
        if (!test_stack.isPresent()) return false;
        final ILinkStorage storage = test_stack.orElse(null);
        final Vector4 pos = storage.getLinkedPos(playerIn);
        if (ai != null && pos != null && pos.w == target.dimension.getId())
        {
            final IOwnable ownable = OwnableCaps.getOwnable(target);
            boolean valid = false;
            if (ownable != null && ownable.getOwnerId() != null) valid = playerIn.getUniqueID().equals(ownable
                    .getOwnerId()) && PermissionAPI.hasPermission(playerIn, Linker.PERMLINKPET);
            else if (TrainerCaps.getHasPokemobs(target) != null) valid = PermissionAPI.hasPermission(playerIn,
                    Linker.PERMLINKTRAINER);
            if (valid)
            {
                ai.getPrimaryTask().setPos(new BlockPos(pos.x, pos.y + 1, pos.z));
                playerIn.sendMessage(new TranslationTextComponent("item.pokecube_adventures.linked.mob", target
                        .getDisplayName(), pos.x, pos.y + 1, pos.z));
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
