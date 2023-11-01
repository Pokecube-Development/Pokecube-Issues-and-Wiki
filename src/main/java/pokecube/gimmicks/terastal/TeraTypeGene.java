package pokecube.gimmicks.terastal;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.genetics.GeneticsManager;
import thut.api.entity.genetics.Gene;
import thut.core.common.ThutCore;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.IWearableInventory;

/**
 * Gene based implementation for storing Terastal types. This also keeps track
 * of the silly hats that they wear.
 */
public class TeraTypeGene implements Gene<TeraTypeGene.TeraType>
{
    /**
     * Silly hats worn by terastallized pokemobs
     */
    public static final Map<PokeType, ItemStack> SILLY_HATS = new HashMap<>();

    /**
     * Ensure that the SILLY_HATS map is populated, by default we fill it with
     * bling hats with custom models specified, and alpha of 196.
     */
    protected static void checkHats()
    {
        if (SILLY_HATS.isEmpty())
        {
            for (PokeType type : PokeType.values())
            {
                ItemStack HAT = PokecubeItems.getStack(new ResourceLocation("thut_bling:bling_hat"));
                String name = type.name.equals("???") ? "unknown" : type.name;
                ItemStack ZCRYSTAL = PokecubeItems.getStack("pokecube_legends:z_" + name);
                CompoundTag tag = new CompoundTag();
                tag.put("gemTag", ZCRYSTAL.serializeNBT());
                tag.putInt("alpha", 196);
                tag.putString("model", "pokecube:models/worn/tera_hats/tera-" + name);
                tag.putString("tex", "pokecube:textures/worn/tera_hats/tera-hat-" + name + ".png");
                HAT.setTag(tag);
                if (HAT.getItem() instanceof DyeableLeatherItem dye) dye.setColor(HAT, type.colour);
                SILLY_HATS.put(type, HAT);
            }
        }
    }

    /**
     * Holder for the mob's terastallize information, holds type and present
     * state.
     *
     */
    public static class TeraType implements INBTSerializable<CompoundTag>
    {

        /**
         * Whether we are presently terastallized
         */
        public boolean isTera = false;
        /**
         * The type we terastallize to
         */
        public PokeType teraType = PokeType.unknown;

        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("s", isTera);
            tag.putString("t", teraType.name);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt)
        {
            this.isTera = nbt.getBoolean("s");
            this.teraType = PokeType.getType(nbt.getString("t"));
        }
    }

    Random rand = ThutCore.newRandom();
    private TeraType value = new TeraType();
    private IPokemob holder = null;
    private IWearableInventory worn = null;
    private boolean hadHat = false;

    @Override
    public ResourceLocation getKey()
    {
        return GeneticsManager.TERAGENE;
    }

    @Override
    public TeraType getValue()
    {
        return value;
    }

    @Override
    protected Gene<TeraType> clone()
    {
        Gene<TeraType> gene = new TeraTypeGene();
        gene.getValue().teraType = this.getValue().teraType;
        return gene;
    }

    /**
     * We interpolate by just selecting a random one, us or other, and making a
     * copy.
     */
    @Override
    public Gene<TeraType> interpolate(Gene<TeraType> other)
    {
        Gene<TeraType> type2 = other;
        if (other instanceof TeraTypeGene gene) type2 = gene.clone();
        return rand.nextBoolean() ? this.clone() : type2;
    }

    @Override
    public void load(CompoundTag tag)
    {
        this.getValue().deserializeNBT(tag);
    }

    /**
     * On update, we check the following each second (20ticks):<br>
     * <br>
     * - If terastallized, we ensure the user is monotype our type<br>
     * - if terastallized and client side, we ensure user is wearing a silly
     * hat.<br>
     */
    @Override
    public void onUpdateTick(Entity entity)
    {
        if (entity.tickCount % 20 != 0) return;

        if (this.getValue().isTera)
        {
            if (holder == null)
            {
                holder = PokemobCaps.getPokemobFor(entity);
            }
            if (holder != null)
            {
                holder.setType1(this.getValue().teraType);
                holder.setType2(PokeType.unknown);
            }
        }
        if (entity.getLevel().isClientSide() && entity instanceof LivingEntity living)
        {
            if (worn == null)
            {
                worn = ThutWearables.getWearables(living);
            }
            if (worn != null)
            {
                checkHats();
                ItemStack HAT = SILLY_HATS.get(this.getValue().teraType);
                if (!HAT.isEmpty())
                {
                    hadHat = living.getPersistentData().getBoolean("pokecube:silly_hat");
                    if (this.getValue().isTera)
                    {
                        worn.setWearable(EnumWearable.HAT, HAT, 0);
                        if (!hadHat) living.getPersistentData().putBoolean("pokecube:silly_hat", true);
                    }
                    else if (hadHat)
                    {
                        worn.setWearable(EnumWearable.HAT, ItemStack.EMPTY, 0);
                        living.getPersistentData().putBoolean("pokecube:silly_hat", false);
                    }
                }
            }
        }
    }

    @Override
    public Gene<TeraType> mutate()
    {
        TeraType mut = new TeraType();
        if (PokeType.values().length > 1)
            mut.teraType = PokeType.values()[1 + rand.nextInt(PokeType.values().length - 1)];
        TeraTypeGene type = new TeraTypeGene();
        type.setValue(mut);
        return type;
    }

    @Override
    public CompoundTag save()
    {
        return this.getValue().serializeNBT();
    }

    @Override
    public void setValue(TeraType value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "" + value.teraType;
    }
}