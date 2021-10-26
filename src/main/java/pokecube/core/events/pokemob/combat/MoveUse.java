package pokecube.core.events.pokemob.combat;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import thut.api.maths.Vector3;

/** These events are fired on the {@link pokecube.core.PokecubeCore#MOVE_BUS} */
public class MoveUse extends Event
{
    public static class ActualMoveUse extends MoveUse
    {
        @Cancelable
        /**
         * This is called when the move entity is made to start using the move.
         * Cancelling this prevents the move from occurring.<br>
         * <br>
         * this is fired on the {@link pokecube.core.PokecubeCore#MOVE_BUS}
         */
        public static class Init extends ActualMoveUse
        {
            public Init(IPokemob user, Move_Base move, Entity target)
            {
                super(user, move, target);
            }
        }

        /**
         * This is called after the post move use.<br>
         * <br>
         * this is fired on the {@link pokecube.core.PokecubeCore#MOVE_BUS}
         */
        public static class Post extends ActualMoveUse
        {
            public Post(IPokemob user, Move_Base move, Entity target)
            {
                super(user, move, target);
            }
        }

        /**
         * This is called during the pre move use method of the move
         * calculations <br>
         * <br>
         * this is fired on the {@link pokecube.core.PokecubeCore#MOVE_BUS}
         */
        public static class Pre extends ActualMoveUse
        {
            public Pre(IPokemob user, Move_Base move, Entity target)
            {
                super(user, move, target);
            }
        }

        final Entity target;

        public ActualMoveUse(IPokemob user, Move_Base move, Entity target)
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
        @Cancelable
        /**
         * Cancelling this event prevents the default implementation from being
         * applied. <br>
         * <br>
         * this is fired on the {@link pokecube.core.PokecubeCore#MOVE_BUS}
         */
        public static class Post extends DuringUse
        {
            public Post(MovePacket packet, boolean fromUser)
            {
                super(packet, fromUser);
            }
        }

        @Cancelable
        /**
         * Cancelling this event prevents the default implementation from being
         * applied. <br>
         * <br>
         * this is fired on the {@link pokecube.core.PokecubeCore#MOVE_BUS}
         */
        public static class Pre extends DuringUse
        {
            public Pre(MovePacket packet, boolean fromUser)
            {
                super(packet, fromUser);
            }
        }

        private final boolean fromUser;

        private final MovePacket packet;

        public DuringUse(MovePacket packet, boolean fromUser)
        {
            super(packet.attacker, packet.getMove());
            this.fromUser = fromUser;
            this.packet = packet;
        }

        public MovePacket getPacket()
        {
            return this.packet;
        }

        public boolean isFromUser()
        {
            return this.fromUser;
        }
    }

    public static class MoveWorldAction extends MoveUse
    {
        @Cancelable
        public static class AffectItem extends MoveWorldAction
        {
            public final List<ItemEntity> items;

            public AffectItem(Move_Base move, IPokemob user, Vector3 location, List<ItemEntity> items)
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
         * this is fired on the {@link pokecube.core.PokecubeCore#MOVE_BUS}
         */
        @Cancelable
        public static class OnAction extends MoveWorldAction
        {
            public OnAction(Move_Base move, IPokemob user, Vector3 location)
            {
                super(move, user, location);
            }
        }

        public static class PostAction extends MoveWorldAction
        {
            public PostAction(Move_Base move, IPokemob user, Vector3 location)
            {
                super(move, user, location);
            }
        }

        @Cancelable
        public static class PreAction extends MoveWorldAction
        {
            public PreAction(Move_Base move, IPokemob user, Vector3 location)
            {
                super(move, user, location);
            }
        }

        private final Vector3 location;

        MoveWorldAction(Move_Base move, IPokemob user, Vector3 location)
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

    final Move_Base move;

    public MoveUse(IPokemob user, Move_Base move)
    {
        this.user = user;
        this.move = move;
    }

    public Move_Base getMove()
    {
        return this.move;
    }

    public IPokemob getUser()
    {
        return this.user;
    }
}
