package pokecube.adventures.items;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.PokecubeAdv;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.utils.CapHolders;
import thut.api.ThutCaps;
import thut.api.attachments.IOwnable;
import thut.api.attachments.Linkable;
import thut.api.attachments.Linkable.ILinkStorage;
import thut.api.attachments.Linkable.LinkHolder;
import thut.api.attachments.Linkable.LinkStorage;
import thut.api.util.PermNodes;
import thut.api.util.PermNodes.DefaultPermissionLevel;
import thut.lib.TComponent;

public class Linker extends Item
{
    private static final ResourceLocation LINKSTOREKEY = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID,
            "linker");

    private static class LinkStore extends LinkStorage
    {
        @Override
        public boolean setLinkedMob(final UUID mobid, final Entity user)
        {
            // we do not link mobs;
            return false;
        }

        @Override
        public boolean setLinkedPos(final GlobalPos pos, final Entity user)
        {
            if (pos == null || user.isCrouching())
            {
                super.setLinkedPos(null, user);
                if (!user.level().isClientSide)
                {
                    if (user instanceof Player player)
                    {
                        player.displayClientMessage(TComponent.translatable("item.pokecube_adventures.linker.unset"),
                                true);
                        player.swing(player.getUsedItemHand());
                    }
                }
            }
            else
            {
                super.setLinkedPos(pos, user);
                if (!user.level().isClientSide)
                {
                    if (user instanceof Player player)
                    {
                        player.displayClientMessage(TComponent.translatable("item.pokecube_adventures.linker.set"),
                                true);
                        player.swing(player.getUsedItemHand());
                    }
                }
                if (user.level().isClientSide) try
                {
                    final String loc = String.format("%d %d %d", pos.pos().getX(), pos.pos().getY(), pos.pos().getZ());
                    Minecraft.getInstance().keyboardHandler.setClipboard(loc);
                    if (user instanceof Player player)
                    {
                        player.displayClientMessage(TComponent.translatable("item.pokecube_adventures.linker.set"),
                                true);
                        player.swing(player.getUsedItemHand());
                    }
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }

            }
            return true;
        }
    }

    public static boolean interact(final ServerPlayer playerIn, final Entity target, final ItemStack stack)
    {
        final IGuardAICapability ai = CapHolders.getGuardAI(target);
        LinkHolder storage = ThutCaps.getLinkStorage(stack);
        if (storage == null || storage.tag().isEmpty()) return false;
        if (storage.link() == null)
        {
            storage = storage.withContext(playerIn.registryAccess());
            stack.set(Linkable.LINK_STORE, storage);
        }
        ILinkStorage store = storage.link();
        final GlobalPos pos = store.getLinkedPos(playerIn);
        if (ai != null && pos != null && pos.dimension() == target.level().dimension())
        {
            final IOwnable ownable = ThutCaps.getOwnable(target);
            boolean valid = false;
            if (ownable != null && ownable.getOwnerId() != null) valid = playerIn.getUUID().equals(ownable.getOwnerId())
                    && PermNodes.getBooleanPerm(playerIn, Linker.PERMLINKPET);
            else if (TrainerCaps.getHasPokemobs(target) != null)
                valid = PermNodes.getBooleanPerm(playerIn, Linker.PERMLINKTRAINER);
            if (valid)
            {
                final BlockPos bpos = pos.pos().above();
                ai.getPrimaryTask().setPos(pos.pos().above());
                playerIn.displayClientMessage(TComponent.translatable("item.pokecube_adventures.linked.mob",
                        target.getDisplayName(), bpos.getX(), bpos.getY(), bpos.getZ()), true);
                playerIn.swing(playerIn.getUsedItemHand());
                return true;
            }
            else playerIn.displayClientMessage(TComponent.translatable("item.pokecube_adventures.linked.mob.fail"),
                    true);
        }
        return false;
    }

    public static String PERMLINKTRAINER = "linker.link_npc";
    public static String PERMLINKPET = "linker.link_pet";

    public Linker(final Properties properties)
    {
        super(properties);
        PermNodes.registerBooleanNode(PokecubeAdv.MODID, Linker.PERMLINKTRAINER, DefaultPermissionLevel.OP,
                "Is the player allowed to use the linker item to set a trainer's stationary location");
        PermNodes.registerBooleanNode(PokecubeAdv.MODID, Linker.PERMLINKPET, DefaultPermissionLevel.ALL,
                "Is the player allowed to use the linker item to set their pokemob's stationary location");
    }

}
