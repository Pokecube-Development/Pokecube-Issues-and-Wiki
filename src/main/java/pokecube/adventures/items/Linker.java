package pokecube.adventures.items;

import java.util.UUID;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.PermNodes;
import pokecube.core.utils.PermNodes.DefaultPermissionLevel;
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
            if (this.linker.getOrCreateTag().contains("thutcore:pos"))
                return GlobalPos.CODEC.decode(NbtOps.INSTANCE, this.linker.getOrCreateTag().getCompound("thutcore:pos"))
                        .result().get().getFirst();
            else return null;
        }

        @Override
        public boolean setLinkedPos(final GlobalPos pos, final Entity user)
        {
            if (pos == null || user.isCrouching())
            {
                this.linker.getOrCreateTag().remove("thutcore:pos");
                if (!user.getLevel().isClientSide)
                {
                    if (user instanceof Player)
                    {
                        final Player player = (Player) user;
                        player.displayClientMessage(new TranslatableComponent("item.pokecube_adventures.linker.unset"),
                                true);
                        player.swing(player.getUsedItemHand());
                    }
                    else
                    {
                        user.sendMessage(new TranslatableComponent("item.pokecube_adventures.linker.unset"),
                                Util.NIL_UUID);
                    }
                }
            }
            else
            {
                this.linker.getOrCreateTag().put("thutcore:pos",
                        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).get().left().get());
                if (!user.getLevel().isClientSide)
                {
                    if (user instanceof Player)
                    {
                        final Player player = (Player) user;
                        player.displayClientMessage(new TranslatableComponent("item.pokecube_adventures.linker.set"),
                                true);
                        player.swing(player.getUsedItemHand());
                    }
                    else
                    {
                        user.sendMessage(new TranslatableComponent("item.pokecube_adventures.linker.set"),
                                Util.NIL_UUID);
                    }
                }
                if (user.getLevel().isClientSide) try
                {
                    final String loc = String.format("%d %d %d", pos.pos().getX(), pos.pos().getY(), pos.pos().getZ());
                    Minecraft.getInstance().keyboardHandler.setClipboard(loc);
                    if (user instanceof Player)
                    {
                        final Player player = (Player) user;
                        player.displayClientMessage(new TranslatableComponent("item.pokecube_adventures.linker.set"),
                                true);
                        player.swing(player.getUsedItemHand());
                    }
                    else
                    {
                        user.sendMessage(new TranslatableComponent("item.pokecube_adventures.linker.set"),
                                Util.NIL_UUID);
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

    @SubscribeEvent
    public static void attachCaps(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(Linker.LINKSTOREKEY)) return;
        if (event.getObject().getItem() instanceof Linker)
            event.addCapability(Linker.LINKSTOREKEY, new LinkStore(event.getObject()));
    }

    public static boolean interact(final ServerPlayer playerIn, final Entity target, final ItemStack stack)
    {
        final IGuardAICapability ai = target.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        final LazyOptional<ILinkStorage> test_stack = stack.getCapability(ThutCaps.STORE, null);
        if (!test_stack.isPresent()) return false;
        final ILinkStorage storage = test_stack.orElse(null);
        final GlobalPos pos = storage.getLinkedPos(playerIn);
        if (ai != null && pos != null && pos.dimension() == target.getLevel().dimension())
        {
            final IOwnable ownable = OwnableCaps.getOwnable(target);
            boolean valid = false;
            if (ownable != null && ownable.getOwnerId() != null) valid = playerIn.getUUID().equals(ownable.getOwnerId())
                    && PermNodes.getBooleanPerm(playerIn, Linker.PERMLINKPET);
            else if (TrainerCaps.getHasPokemobs(target) != null)
                valid = PermNodes.getBooleanPerm(playerIn, Linker.PERMLINKTRAINER);
            if (valid)
            {
                final BlockPos bpos = pos.pos().above();
                ai.getPrimaryTask().setPos(pos.pos().above());
                playerIn.displayClientMessage(new TranslatableComponent("item.pokecube_adventures.linked.mob",
                        target.getDisplayName(), bpos.getX(), bpos.getY(), bpos.getZ()), true);
                playerIn.swing(playerIn.getUsedItemHand());
                return true;
            }
            else playerIn.displayClientMessage(new TranslatableComponent("item.pokecube_adventures.linked.mob.fail"),
                    true);
        }
        return false;
    }

    public static String PERMLINKTRAINER = "linker.link_npc";
    public static String PERMLINKPET = "linker.link_pet";

    public Linker(final Properties properties)
    {
        super(properties);
        PermNodes.registerNode(Linker.PERMLINKTRAINER, DefaultPermissionLevel.OP,
                "Is the player allowed to use the linker item to set a trainer's stationary location");
        PermNodes.registerNode(Linker.PERMLINKPET, DefaultPermissionLevel.ALL,
                "Is the player allowed to use the linker item to set their pokemob's stationary location");
    }

}
