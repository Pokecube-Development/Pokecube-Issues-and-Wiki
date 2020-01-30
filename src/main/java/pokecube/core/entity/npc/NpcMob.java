package pokecube.core.entity.npc;

import java.util.function.Consumer;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.GuardAI;
import thut.api.maths.Vector3;

public class NpcMob extends AbstractVillagerEntity implements IEntityAdditionalSpawnData
{
    public static final EntityType<NpcMob> TYPE;

    static
    {
        TYPE = EntityType.Builder.create(NpcMob::new, EntityClassification.CREATURE).setCustomClientFactory((s,
                w) -> NpcMob.TYPE.create(w)).build("pokecube:npc");
    }
    private NpcType type       = NpcType.NONE;
    public String   name       = "";
    public String   playerName = "";
    public String   urlSkin    = "";
    public String   customTex  = "";
    private boolean male       = true;
    public boolean  stationary = false;
    public Vector3  location   = null;
    public GuardAI  guardAI;

    private Consumer<MerchantOffers> init_offers = t ->
    {
    };

    private Consumer<MerchantOffer> use_offer = t ->
    {
    };

    protected NpcMob(final EntityType<? extends NpcMob> type, final World world)
    {
        super(type, world);
        this.enablePersistence();
        this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
    }

    @Override
    public boolean attackEntityFrom(final DamageSource source, final float i)
    {
        final Entity e = source.getTrueSource();
        if (e instanceof PlayerEntity && ((PlayerEntity) e).abilities.isCreativeMode)
        {
            final PlayerEntity player = (PlayerEntity) e;
            if (player.getHeldItemMainhand().isEmpty()) this.remove();
        }
        return super.attackEntityFrom(source, i);
    }

    @Override
    public AgeableEntity createChild(final AgeableEntity ageable)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public ResourceLocation getTex()
    {
        if (!this.playerName.isEmpty()) return PokecubeCore.proxy.getPlayerSkin(this.playerName);
        else if (!this.customTex.isEmpty()) return new ResourceLocation(this.customTex);
        return this.isMale() ? this.getNpcType().getMaleTex() : this.getNpcType().getFemaleTex();
    }

    @Override
    public boolean processInteract(final PlayerEntity player, final Hand hand)
    {
        if (this.getNpcType().getInteraction().processInteract(player, hand, this)) return true;
        return super.processInteract(player, hand);
    }

    @Override
    public void readAdditional(final CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        this.stationary = nbt.getBoolean("stationary");
        this.setMale(nbt.getBoolean("gender"));
        this.name = nbt.getString("name");
        this.playerName = nbt.getString("playerName");
        this.urlSkin = nbt.getString("urlSkin");
        this.customTex = nbt.getString("customTex");
        try
        {
            if (nbt.contains("type")) this.setNpcType(NpcType.byType(nbt.getString("type")));
            else this.setNpcType(NpcType.PROFESSOR);
        }
        catch (final Exception e)
        {
            this.setNpcType(NpcType.PROFESSOR);
            e.printStackTrace();
        }
    }

    @Override
    public ITextComponent getDisplayName()
    {
        if (this.name != null && !this.name.isEmpty())
        {
            ITextComponent display;
            if (this.name.startsWith("pokecube."))
            {
                final String[] args = this.name.split(":");
                if (args.length == 2) display = new TranslationTextComponent(args[0], args[1]);
                else display = new StringTextComponent(this.name);
            }
            else display = new StringTextComponent(this.name);
            display.applyTextStyle((style) ->
            {
                style.setHoverEvent(this.getHoverEvent()).setInsertion(this.getCachedUniqueIdString());
            });
            return display;
        }
        return super.getDisplayName();
    }

    @Override
    public void readSpawnData(final PacketBuffer additionalData)
    {
        this.readAdditional(additionalData.readCompoundTag());
    }

    @Override
    public void writeAdditional(final CompoundNBT nbt)
    {
        super.writeAdditional(nbt);
        nbt.putBoolean("gender", this.isMale());
        nbt.putString("name", this.name);
        nbt.putBoolean("stationary", this.stationary);
        nbt.putString("playerName", this.playerName);
        nbt.putString("urlSkin", this.urlSkin);
        nbt.putString("customTex", this.customTex);
        nbt.putString("type", this.getNpcType().getName());
    }

    @Override
    public void writeSpawnData(final PacketBuffer buffer)
    {
        final CompoundNBT tag = new CompoundNBT();
        this.writeAdditional(tag);
        buffer.writeCompoundTag(tag);
    }

    @Override
    protected void func_213713_b(final MerchantOffer offer)
    {
        this.use_offer.accept(offer);
    }

    @Override
    protected void populateTradeData()
    {
        this.init_offers.accept(this.offers);
    }

    public void setInitOffers(final Consumer<MerchantOffers> in)
    {
        this.init_offers = in;
        // Clear offers so that it can be reset.
        this.offers = null;
    }

    public void setUseOffers(final Consumer<MerchantOffer> in)
    {
        this.use_offer = in;
    }

    /**
     * @return the type
     */
    public NpcType getNpcType()
    {
        return this.type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setNpcType(final NpcType type)
    {
        this.type = type;
    }

    /**
     * @return the male
     */
    public boolean isMale()
    {
        return this.male;
    }

    /**
     * @param male
     *            the male to set
     */
    public void setMale(final boolean male)
    {
        this.male = male;
    }
}
