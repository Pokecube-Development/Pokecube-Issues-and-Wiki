package pokecube.core.entity.professor;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.INPC;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.inventory.healer.HealerContainer;
import pokecube.core.network.packets.PacketChoose;
import thut.api.maths.Vector3;
import thut.core.common.network.EntityUpdate;

public class EntityProfessor extends AgeableEntity implements IEntityAdditionalSpawnData, INPC
{
    public static enum ProfessorType
    {
        PROFESSOR, HEALER;
    }

    public static final EntityType<EntityProfessor> TYPE;

    static
    {
        TYPE = EntityType.Builder.create(EntityProfessor::new, EntityClassification.CREATURE).setCustomClientFactory((s,
                w) -> EntityProfessor.TYPE.create(w)).build("professor");
    }

    public static final ResourceLocation PROFTEX  = new ResourceLocation(PokecubeMod.ID + ":textures/professor.png");
    public static final ResourceLocation NURSETEX = new ResourceLocation(PokecubeMod.ID + ":textures/nurse.png");

    public ProfessorType type       = ProfessorType.PROFESSOR;
    public String        name       = "";
    public String        playerName = "";
    public String        urlSkin    = "";
    public boolean       male       = true;
    public boolean       stationary = false;
    public Vector3       location   = null;

    protected EntityProfessor(final EntityType<? extends AgeableEntity> type, final World world)
    {
        super(type, world);
        this.enablePersistence();
    }

    @Override
    public boolean attackEntityFrom(final DamageSource source, final float i)
    {
        final Entity e = source.getTrueSource();
        if (e instanceof PlayerEntity && ((PlayerEntity) e).abilities.isCreativeMode)
        {
            final PlayerEntity player = (PlayerEntity) e;
            if (!player.getHeldItemMainhand().isEmpty())
            {
                if (!this.getEntityWorld().isRemote)
                {
                    if (this.type == ProfessorType.PROFESSOR) this.type = ProfessorType.HEALER;
                    else if (this.type == ProfessorType.HEALER) this.type = ProfessorType.PROFESSOR;
                    if (this.type == ProfessorType.PROFESSOR) this.male = true;
                    else this.male = false;
                    System.out.println("test");
                    EntityUpdate.sendEntityUpdate(this);
                    return false;
                }
            }
            else this.remove();
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
        return this.type == ProfessorType.PROFESSOR ? EntityProfessor.PROFTEX : EntityProfessor.NURSETEX;
    }

    @Override
    public boolean processInteract(final PlayerEntity player, final Hand hand)
    {
        switch (this.type)
        {
        case HEALER:
            if (player instanceof ServerPlayerEntity) player.openContainer(new SimpleNamedContainerProvider((id,
                    playerInventory, playerIn) -> new HealerContainer(id, playerInventory, IWorldPosCallable.of(
                            this.world, this.getPosition())), player.getDisplayName()));
            return true;
        case PROFESSOR:
            if (player instanceof ServerPlayerEntity)
            {
                final boolean canPick = PacketChoose.canPick(player.getGameProfile());
                final PacketChoose packet = PacketChoose.createOpenPacket(false, canPick, Database.getStarters());
                PokecubeCore.packets.sendTo(packet, (ServerPlayerEntity) player);
            }
            break;
        default:
            break;

        }
        return super.processInteract(player, hand);
    }

    @Override
    public void readAdditional(final CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        this.stationary = nbt.getBoolean("stationary");
        this.male = nbt.getBoolean("gender");
        this.name = nbt.getString("name");
        this.playerName = nbt.getString("playerName");
        this.urlSkin = nbt.getString("urlSkin");
        try
        {
            if (nbt.contains("type")) this.type = ProfessorType.valueOf(nbt.getString("type"));
            else this.type = ProfessorType.PROFESSOR;
        }
        catch (final Exception e)
        {
            this.type = ProfessorType.PROFESSOR;
            e.printStackTrace();
        }
    }

    @Override
    public ITextComponent getDisplayName()
    {
        if (this.name != null && !this.name.isEmpty()) return new StringTextComponent(this.name);
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
        nbt.putBoolean("gender", this.male);
        nbt.putString("name", this.name);
        nbt.putBoolean("stationary", this.stationary);
        nbt.putString("playerName", this.playerName);
        nbt.putString("urlSkin", this.urlSkin);
        nbt.putString("type", this.type.toString());
    }

    @Override
    public void writeSpawnData(final PacketBuffer buffer)
    {
        final CompoundNBT tag = new CompoundNBT();
        this.writeAdditional(tag);
        buffer.writeCompoundTag(tag);
    }

}
