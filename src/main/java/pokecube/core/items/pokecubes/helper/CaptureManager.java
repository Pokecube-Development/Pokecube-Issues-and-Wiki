package pokecube.core.items.pokecubes.helper;

import java.util.UUID;

import net.minecraft.network.chat.Component;
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
import pokecube.api.PokecubeAPI;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.HappinessType;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.api.events.pokemobs.CaptureEvent;
import pokecube.api.events.pokemobs.CaptureEvent.Pre;
import pokecube.api.items.IPokecube;
import pokecube.api.moves.Battle;
import pokecube.api.utils.TagNames;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.init.Sounds;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.api.maths.Vector3;
import thut.lib.TComponent;

public class CaptureManager
{
    public static int CAPTURE_SHRINK_TIMER = 25;
    public static int CAPTURE_SHAKE_TIME = 20;

    public static void onCaptureDenied(final EntityPokecubeBase cube)
    {
        cube.spawnAtLocation(cube.getItem(), (float) 0.5);
        cube.discard();
    }

    public static void captureAttempt(final EntityPokecubeBase cube, final Entity e)
    {
        if (!(cube.getLevel() instanceof ServerLevel)) return;
        if (!(e instanceof LivingEntity mob)) return;
        if (e.isInvulnerable()) return;
        if (e.getPersistentData().contains(TagNames.CAPTURING)) return;
        if (!(cube.getItem().getItem() instanceof IPokecube cubeItem)) return;
        if (!cubeItem.canCapture(e, cube.getItem())) return;
        if (cube.isCapturing()) return;
        final IOwnable ownable = OwnableCaps.getOwnable(mob);
        if ((ownable != null && ownable.getOwnerId() != null && !PokecubeManager.isFilled(cube.getItem()))) return;
        final ResourceLocation cubeId = PokecubeItems.getCubeId(cube.getItem());
        final double modifier = cubeItem.getCaptureModifier(mob, cubeId);
        final Vector3 v = new Vector3();
        cube.autoRelease = -1;
        if (modifier <= 0)
        {
            cube.setNotCapturing();
            return;
        }

        final IPokemob hitten = PokemobCaps.getPokemobFor(e);
        if (cube.shooter != null && hitten != null && cube.shooter.equals(hitten.getOwnerId())) return;

        boolean removeMob = false;
        final CaptureEvent.Pre capturePre = new Pre(hitten, cube, mob);
        PokecubeAPI.POKEMOB_BUS.post(capturePre);
        if (capturePre.getResult() == Result.DENY) return;

        // If allow, we set tilt to 5 can allow capture.
        if (capturePre.getResult() == Result.ALLOW)
        {
            cube.setTilt(5);
            cube.setTime(CAPTURE_SHRINK_TIMER);
            final ItemStack mobStack = cube.getItem().copy();
            PokecubeManager.addToCube(mobStack, mob);
            cube.setItem(mobStack);
            PokecubeManager.setTilt(cube.getItem(), 5);
            v.set(cube).addTo(0, mob.getBbHeight() / 2, 0).moveEntity(cube);
            removeMob = true;
            cube.setCapturing(mob);
        }
        else if (hitten != null)
        {
            if (capturePre.isCanceled())
            {
                int n = cube.getTilt();
                if (n == 5) cube.setTime(CAPTURE_SHRINK_TIMER);
                else cube.setTime(CAPTURE_SHAKE_TIME * n + CAPTURE_SHRINK_TIMER);
                hitten.setPokecube(cube.getItem());
                cube.setItem(PokecubeManager.pokemobToItem(hitten));
                PokecubeManager.setTilt(cube.getItem(), cube.getTilt());
                v.set(cube).addTo(0, mob.getBbHeight() / 2, 0).moveEntity(cube);
                removeMob = true;
                cube.setCapturing(mob);
            }
            else
            {
                int n = Tools.computeCatchRate(hitten, cubeId);
                cube.setTilt(n);
                if (n == 5) cube.setTime(CAPTURE_SHRINK_TIMER);
                else cube.setTime(CAPTURE_SHAKE_TIME * n + CAPTURE_SHRINK_TIMER);
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
                // If they want these to differ, they can add a pokedex entry
                // for the specfic mob.
                final int catchRate = 250;
                final double cubeBonus = modifier;
                final double statusbonus = 1;
                final double a = Tools.getCatchRate(mob.getMaxHealth(), mob.getHealth(), catchRate, cubeBonus,
                        statusbonus);
                if (a > 255)
                {
                    n = 5;
                    break rate;
                }
                final double b = 1048560 / Math.sqrt(Math.sqrt(16711680 / a));

                if (cube.getRandom().nextInt(65535) <= b) n++;
                if (cube.getRandom().nextInt(65535) <= b) n++;
                if (cube.getRandom().nextInt(65535) <= b) n++;
                if (cube.getRandom().nextInt(65535) <= b) n++;
            }
            cube.setTilt(n);
            if (n == 5) cube.setTime(CAPTURE_SHRINK_TIMER);
            else cube.setTime(CAPTURE_SHAKE_TIME * n + CAPTURE_SHRINK_TIMER);
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
        final LivingEntity living = SendOutManager.sendOut(cube, true);
        if (living == null)
        {
            // Just drop the cube? something went wrong
            cube.spawnAtLocation(cube.getItem());
            PokecubeAPI.LOGGER.error("Error with capture failure for {}", cube.getItem().getTag());
            return;
        }
        final IPokemob pokemob = PokemobCaps.getPokemobFor(living);
        cube.setNotCapturing();
        cube.setReleased(living);

        if (living != null) living.moveTo(cube.capturePos.x, cube.capturePos.y, cube.capturePos.z, cube.yRot, 0.0F);
        if (pokemob != null)
        {
            EntityPokecubeBase.setNoCaptureBasedOnConfigs(pokemob);
            pokemob.setLogicState(LogicStates.SITTING, false);
            pokemob.setGeneralState(GeneralStates.TAMED, false);
            pokemob.setOwner((UUID) null);
            if (cube.shootingEntity instanceof Player player && !(cube.shootingEntity instanceof FakePlayer))
            {
                final Component mess = TComponent.translatable("pokecube.missed", pokemob.getDisplayName());
                player.displayClientMessage(mess, true);
            }
        }
        if (living instanceof Mob mob && cube.shootingEntity != null)
            Battle.createOrAddToBattle(mob, cube.shootingEntity);
    }

    public static boolean captureSucceed(final EntityPokecubeBase cube)
    {
        cube.setNoCollisionRelease();
        PokecubeManager.setTilt(cube.getItem(), -2);
        final Entity mob = PokecubeManager.itemToMob(cube.getItem(), cube.getLevel());
        IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        final IOwnable ownable = OwnableCaps.getOwnable(mob);
        if (mob == null || cube.shooter == null)
        {
            if (mob == null) PokecubeAPI.LOGGER.error("Error with mob capture: {}", mob);
            else cube.playSound(Sounds.CAPTURE_SOUND.get(), (float) PokecubeCore.getConfig().captureVolume, 1);
            return false;
        }
        if (ownable != null)
        {
            ownable.setOwner(cube.shooter);
            if (mob instanceof Animal animal && cube.shootingEntity instanceof Player player)
                ForgeEventFactory.onAnimalTame(animal, player);
        }
        if (pokemob != null)
        {
            final ItemStack pokemobStack = PokecubeManager.pokemobToItem(pokemob);
            cube.setItem(pokemobStack);
            HappinessType.applyHappiness(pokemob, HappinessType.TRADE);
            if (cube.shooter != null && !pokemob.getGeneralState(GeneralStates.TAMED)) pokemob.setOwner(cube.shooter);
            /*
             * Set not to wander around by default, they can choose to enable
             * this later.
             */
            pokemob.setRoutineState(AIRoutine.WANDER, false);
            // Ensure it is not sitting anymore
            pokemob.setLogicState(LogicStates.SITTING, false);

            final IPokemob revert = pokemob.resetForm(false);
            if (revert != null) pokemob = revert;
            if (pokemob.getEntity().getPersistentData().contains(TagNames.ABILITY)) pokemob.setAbilityRaw(
                    AbilityManager.getAbility(pokemob.getEntity().getPersistentData().getString(TagNames.ABILITY)));
            if (cube.shootingEntity instanceof Player player && !(cube.shootingEntity instanceof FakePlayer))
            {
                final Component mess = TComponent.translatable("pokecube.caught", pokemob.getDisplayName());
                player.displayClientMessage(mess, true);
                cube.setPos(cube.shootingEntity.getX(), cube.shootingEntity.getY(), cube.shootingEntity.getZ());
                cube.playSound(Sounds.CAPTURE_SOUND.get(), (float) PokecubeCore.getConfig().captureVolume, 1);
            }
        }
        else if (mob instanceof LivingEntity living)
        {
            PokecubeManager.addToCube(cube.getItem(), living);
            final Component mess = TComponent.translatable("pokecube.caught", mob.getDisplayName());
            if (cube.shootingEntity instanceof Player player) player.displayClientMessage(mess, true);
            cube.playSound(Sounds.CAPTURE_SOUND.get(), (float) PokecubeCore.getConfig().captureVolume, 1);
        }
        return true;
    }

}
