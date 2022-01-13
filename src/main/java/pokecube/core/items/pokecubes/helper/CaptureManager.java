package pokecube.core.items.pokecubes.helper;

import java.util.Random;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.events.pokemob.CaptureEvent;
import pokecube.core.events.pokemob.CaptureEvent.Pre;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.pokecubes.EntityPokecubeBase;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.maths.Vector3;

public class CaptureManager
{
    public static void onCaptureDenied(final EntityPokecubeBase cube)
    {
        cube.spawnAtLocation(cube.getItem(), (float) 0.5);
        cube.discard();
    }

    public static void captureAttempt(final EntityPokecubeBase cube, final Random rand, final Entity e)
    {
        if (!(cube.getLevel() instanceof ServerLevel)) return;
        if (!e.isAlive()) return;
        if (!(e instanceof LivingEntity)) return;
        if (e.isInvulnerable()) return;
        if (e.getPersistentData().contains(TagNames.CAPTURING)) return;
        if (!(cube.getItem().getItem() instanceof IPokecube)) return;
        if (!((IPokecube) cube.getItem().getItem()).canCapture(e, cube.getItem())) return;
        if (cube.isCapturing) return;
        final LivingEntity mob = (LivingEntity) e;
        if (mob.deathTime > 0) return;

        final IPokemob hitten = CapabilityPokemob.getPokemobFor(e);
        final ResourceLocation cubeId = PokecubeItems.getCubeId(cube.getItem());
        final IPokecube cubeItem = (IPokecube) cube.getItem().getItem();
        final double modifier = cubeItem.getCaptureModifier(mob, cubeId);
        final Vector3 v = new Vector3();
        cube.autoRelease = -1;
        if (modifier <= 0)
        {
            cube.setNotCapturing();
            return;
        }
        if (cube.shooter != null && hitten != null && cube.shooter.equals(hitten.getOwnerId())) return;

        boolean removeMob = false;
        final CaptureEvent.Pre capturePre = new Pre(hitten, cube, mob);
        PokecubeCore.POKEMOB_BUS.post(capturePre);

        if (hitten != null)
        {
            final int tiltBak = cube.getTilt();
            if (capturePre.isCanceled() || capturePre.getResult() == Result.DENY)
            {
                if (cube.getTilt() != tiltBak)
                {
                    if (cube.getTilt() == 5) cube.setTime(10);
                    else cube.setTime(20 * cube.getTilt() + 5);
                    hitten.setPokecube(cube.getItem());
                    cube.setItem(PokecubeManager.pokemobToItem(hitten));
                    PokecubeManager.setTilt(cube.getItem(), cube.getTilt());
                    v.set(cube).addTo(0, mob.getBbHeight() / 2, 0).moveEntity(cube);
                    removeMob = true;
                    cube.setCapturing(mob);
                }
            }
            else
            {
                final int n = Tools.computeCatchRate(hitten, cubeId);
                cube.setTilt(n);

                if (n == 5) cube.setTime(10);
                else cube.setTime(20 * n + 5);

                hitten.setPokecube(cube.getItem());
                cube.setItem(PokecubeManager.pokemobToItem(hitten));
                PokecubeManager.setTilt(cube.getItem(), n);
                v.set(cube).addTo(0, mob.getBbHeight() / 2, 0).moveEntity(cube);
                removeMob = true;
                cube.setCapturing(mob);
            }
        }
        else if (!capturePre.isCanceled())
        {
            int n = 0;
            rate:
            {
                final int catchRate = 250;// TODO configs for this?
                final double cubeBonus = modifier;
                final double statusbonus = 1;// TODO statuses for mobs?
                final double a = Tools.getCatchRate(mob.getMaxHealth(), mob.getHealth(), catchRate, cubeBonus,
                        statusbonus);
                if (a > 255)
                {
                    n = 5;
                    break rate;
                }
                final double b = 1048560 / Math.sqrt(Math.sqrt(16711680 / a));

                if (rand.nextInt(65535) <= b) n++;

                if (rand.nextInt(65535) <= b) n++;

                if (rand.nextInt(65535) <= b) n++;

                if (rand.nextInt(65535) <= b) n++;
            }
            cube.setTilt(n);
            if (n == 5) cube.setTime(10);
            else cube.setTime(20 * n + 5);
            final ItemStack mobStack = cube.getItem().copy();
            PokecubeManager.addToCube(mobStack, mob);
            cube.setItem(mobStack);
            PokecubeManager.setTilt(cube.getItem(), n);
            v.set(cube).addTo(0, mob.getBbHeight() / 2, 0).moveEntity(cube);
            removeMob = true;
            cube.setCapturing(mob);
        }

        if (removeMob) mob.discard();
    }

    public static void captureFailed(final EntityPokecubeBase cube)
    {
        final LivingEntity mob = SendOutManager.sendOut(cube, true);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        cube.setNotCapturing();

        if (mob != null) mob.moveTo(cube.capturePos.x, cube.capturePos.y, cube.capturePos.z, cube.yRot, 0.0F);
        if (pokemob != null)
        {
            EntityPokecubeBase.setNoCaptureBasedOnConfigs(pokemob);
            
            // FIXME See if AI need init?
            
            pokemob.setLogicState(LogicStates.SITTING, false);
            pokemob.setGeneralState(GeneralStates.TAMED, false);
            pokemob.setOwner((UUID) null);
            if (cube.shootingEntity instanceof Player && !(cube.shootingEntity instanceof FakePlayer))
            {
                final Component mess = new TranslatableComponent("pokecube.missed", pokemob.getDisplayName());
                ((Player) cube.shootingEntity).displayClientMessage(mess, true);
            }
        }
        if (mob instanceof Mob && cube.shootingEntity != null) BrainUtils.initiateCombat((Mob) mob,
                cube.shootingEntity);
    }

    public static boolean captureSucceed(final EntityPokecubeBase cube)
    {
        cube.setNoCollisionRelease();
        PokecubeManager.setTilt(cube.getItem(), -2);
        final Entity mob = PokecubeManager.itemToMob(cube.getItem(), cube.getLevel());
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        final IOwnable ownable = OwnableCaps.getOwnable(mob);
        if (mob == null || cube.shooter == null)
        {
            if (mob == null) PokecubeCore.LOGGER.error("Error with mob capture: {}", mob);
            else cube.playSound(EntityPokecubeBase.POKECUBESOUND, (float) PokecubeCore.getConfig().captureVolume, 1);
            return false;
        }
        if (ownable != null)
        {
            ownable.setOwner(cube.shooter);
            if (mob instanceof Animal && cube.shootingEntity instanceof Player) ForgeEventFactory
                    .onAnimalTame((Animal) mob, (Player) cube.shootingEntity);
        }
        if (pokemob == null)
        {
            final Component mess = new TranslatableComponent("pokecube.caught", mob.getDisplayName());
            if (cube.shootingEntity instanceof Player) ((Player) cube.shootingEntity).displayClientMessage(
                    mess, true);
            cube.playSound(EntityPokecubeBase.POKECUBESOUND, (float) PokecubeCore.getConfig().captureVolume, 1);
            return true;
        }
        HappinessType.applyHappiness(pokemob, HappinessType.TRADE);
        if (cube.shooter != null && !pokemob.getGeneralState(GeneralStates.TAMED)) pokemob.setOwner(cube.shooter);
        if (pokemob.getCombatState(CombatStates.MEGAFORME) || pokemob.getPokedexEntry().isMega())
        {
            pokemob.setCombatState(CombatStates.MEGAFORME, false);
            final IPokemob revert = pokemob.megaRevert();
            if (revert != null) pokemob = revert;
            if (pokemob.getEntity().getPersistentData().contains(TagNames.ABILITY)) pokemob.setAbilityRaw(AbilityManager
                    .getAbility(pokemob.getEntity().getPersistentData().getString(TagNames.ABILITY)));
        }
        final ItemStack pokemobStack = PokecubeManager.pokemobToItem(pokemob);
        cube.setItem(pokemobStack);
        if (cube.shootingEntity instanceof Player && !(cube.shootingEntity instanceof FakePlayer))
        {
            final Component mess = new TranslatableComponent("pokecube.caught", pokemob.getDisplayName());
            ((Player) cube.shootingEntity).displayClientMessage(mess, true);
            cube.setPos(cube.shootingEntity.getX(), cube.shootingEntity.getY(), cube.shootingEntity.getZ());
            cube.playSound(EntityPokecubeBase.POKECUBESOUND, (float) PokecubeCore.getConfig().captureVolume, 1);
        }
        return true;
    }

}
