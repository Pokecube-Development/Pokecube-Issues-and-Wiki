package pokecube.adventures.items;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import thut.api.LinkableCaps.LinkStorage;
import thut.api.maths.Vector4;

public class Linker extends Item
{
    private static final ResourceLocation LINKSTOREKEY = new ResourceLocation(PokecubeAdv.ID, "linker");

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
            if (pos == null || user.isSneaking())
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

    public Linker(final Properties properties)
    {
        super(properties);
    }

}
