package pokecube.api.events.pokemobs.combat;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import thut.api.maths.Vector3;

/** These events are fired on the {@link pokecube.core.PokecubeAPI#MOVE_BUS} */
public class MoveUse extends Event
{
    public static class ActualMoveUse extends MoveUse
    {
        @Cancelable
        /**
         * This is called when the move entity is made to start using the move.
         * Cancelling this prevents the move from occurring.<br>
         * <br>
         * If you cancel this event, make sure to send the appropriate
         * notification messages! <br>
         * this is fired on the {@link pokecube.core.PokecubeAPI#MOVE_BUS}
         */
        public static class Init extends ActualMoveUse
        {
            public Init(IPokemob user, MoveEntry move, Entity target)
            {
                super(user, move, target);
            }
        }

        /**
         * This is called after the post move use.<br>
         * <br>
         * this is fired on the {@link pokecube.core.PokecubeAPI#MOVE_BUS}
         */
        public static class Post extends ActualMoveUse
        {
            public Post(IPokemob user, MoveEntry move, Entity target)
            {
                super(user, move, target);
            }
        }

        /**
         * This is called during the pre move use method of the move
         * calculations <br>
         * <br>
         * this is fired on the {@link pokecube.core.PokecubeAPI#MOVE_BUS}
         */
        public static class PreMoveStatus extends ActualMoveUse
        {
            public PreMoveStatus(IPokemob user, MoveEntry move, Entity target)
            {
                super(user, move, target);
            }
        }

        final Entity target;

        public ActualMoveUse(IPokemob user, MoveEntry move, Entity target)
        {
            super(user, move);
            this.target = target;
        }

        public Entity getTarget()
        {
            return this.target;
        }
    }

    public static class DuringUse extends MoveUse
    {
        /**
         * This is not @Cancelable, It is fired after processing the move use
         * and effects. <br>
         * <br>
         * this is fired on the {@link pokecube.core.PokecubeAPI#MOVE_BUS}
         */
        public static class Post extends DuringUse
        {
            public Post(MoveApplication packet)
            {
                super(packet);
            }
        }

        @Cancelable
        /**
         * Cancelling this event prevents the default implementation from being
         * applied. You can edit the contents of the MoveApplication here for
         * any needed effects, or cancel this event to prevent any application.
         * <br>
         * <br>
         * this is fired on the {@link pokecube.core.PokecubeAPI#MOVE_BUS}
         */
        public static class Pre extends DuringUse
        {
            public Pre(MoveApplication packet)
            {
                super(packet);
            }
        }

        private final MoveApplication packet;

        public DuringUse(MoveApplication packet)
        {
            super(packet.getUser(), packet.getMove());
            this.packet = packet;
        }

        public MoveApplication getPacket()
        {
            return this.packet;
        }
    }

    public static class MoveWorldAction extends MoveUse
    {
        @Cancelable
        public static class AffectItem extends MoveWorldAction
        {
            public final List<ItemEntity> items;

            public AffectItem(MoveEntry move, IPokemob user, Vector3 location, List<ItemEntity> items)
            {
                super(move, user, location);
                this.items = items;
            }
        }

        /**
         * This event is called to actually do the world action, it is handled
         * by an event handler if PreAction is not cancelled. The default
         * actions for this will be set to lowest priority, and not receive
         * cancelled, so if you want to interfere, make sure to cancel this
         * event.<br>
         * <br>
         * this is fired on the {@link pokecube.core.PokecubeAPI#MOVE_BUS}
         */
        @Cancelable
        public static class OnAction extends MoveWorldAction
        {
            public OnAction(MoveEntry move, IPokemob user, Vector3 location)
            {
                super(move, user, location);
            }
        }

        public static class PostAction extends MoveWorldAction
        {
            public PostAction(MoveEntry move, IPokemob user, Vector3 location)
            {
                super(move, user, location);
            }
        }

        @Cancelable
        public static class PreAction extends MoveWorldAction
        {
            public PreAction(MoveEntry move, IPokemob user, Vector3 location)
            {
                super(move, user, location);
            }
        }

        private final Vector3 location;

        MoveWorldAction(MoveEntry move, IPokemob user, Vector3 location)
        {
            super(user, move);
            this.location = location;
        }

        public Vector3 getLocation()
        {
            return this.location;
        }
    }

    final IPokemob user;

    final MoveEntry move;

    public MoveUse(IPokemob user, MoveEntry move)
    {
        this.user = user;
        this.move = move;
    }

    public MoveEntry getMove()
    {
        return this.move;
    }

    public IPokemob getUser()
    {
        return this.user;
    }
}
