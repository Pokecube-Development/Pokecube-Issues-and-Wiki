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
import net.minecraftforge.fml.common.Mod;
import pokecube.adventures.PokecubeAdv;
import thut.api.LinkableCaps.LinkStorage;
import thut.api.maths.Vector4;

@Mod.EventBusSubscriber
public class Linker extends Item
{
    private static final ResourceLocation LINKSTOREKEY = new ResourceLocation(PokecubeAdv.ID, "linker");

    private static class LinkStore extends LinkStorage
    {
        private final ItemStack linker;

        public LinkStore(ItemStack linker)
        {
            this.linker = linker;
        }

        @Override
        public boolean setLinkedMob(UUID mobid, Entity user)
        {
            // we do not link mobs;
            return false;
        }

        @Override
        public Vector4 getLinkedPos(Entity user)
        {
            if (linker.getOrCreateTag().contains("link_pos"))
            {
                return new Vector4(linker.getTag().getCompound("link_pos"));
            }
            else return null;
        }

        @Override
        public boolean setLinkedPos(Vector4 pos, Entity user)
        {
            if (pos == null || user.isSneaking())
            {
                linker.getOrCreateTag().remove("link_pos");
                user.sendMessage(new TranslationTextComponent("item.pokecube_adventures.linker.unset"));
            }
            else
            {
                CompoundNBT posTag = new CompoundNBT();
                pos.writeToNBT(posTag);
                linker.getOrCreateTag().put("link_pos", posTag);
                user.sendMessage(new TranslationTextComponent("item.pokecube_adventures.linker.set"));
            }
            return true;
        }
    }

    @SubscribeEvent
    public static void attachCaps(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(LINKSTOREKEY)) return;
        if (event.getObject().getItem() instanceof Linker)
        {
            event.addCapability(LINKSTOREKEY, new LinkStore(event.getObject()));
        }
    }

    public Linker(final Properties properties)
    {
        super(properties);
    }

}
