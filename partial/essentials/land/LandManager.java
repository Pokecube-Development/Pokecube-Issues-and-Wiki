package thut.essentials.land;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thut.essentials.Essentials;
import thut.essentials.ThutEssentials;
import thut.essentials.util.Coordinate;

public class LandManager
{

    /** Stores a list of invited to team names. */
    public static class Invites
    {
        public Set<String> teams = Sets.newHashSet();
    }

    /** Stores a set of members, a set of permission strings, and an optional
     * prefix for the rank. */
    public static class PlayerRank
    {
        /** Who has this rank. */
        public Set<UUID>   members = Sets.newHashSet();
        /** Optional prefix for the rank. */
        public String      prefix;
        /** Perms for this rank. */
        public Set<String> perms   = Sets.newHashSet();
    }

    public static class Relation
    {
        /** Permissions for the relation */
        public Set<String> perms = Sets.newHashSet();
    }

    public static class LandTeam
    {
        // These are perms checked for ranks.
        /** Can edit enter/leave/deny messages. */
        public static final String     EDITMESSAGES   = "editMessages";
        /** Can claim. */
        public static final String     CLAIMPERM      = "claim";
        /** can unclaim */
        public static final String     UNCLAIMPERM    = "unclaim";
        /** Can change prefixes */
        public static final String     SETPREFIX      = "prefix";
        /** Can set team home. */
        public static final String     SETHOME        = "sethome";
        /** Can invite people */
        public static final String     INVITE         = "invite";
        /** Can kick people */
        public static final String     KICK           = "kick";
        /** Can chunkload. */
        public static final String     LOADPERM       = "cload";
        /** Can chunkload. */
        public static final String     UNLOADPERM     = "uncload";

        // These are perms checked for relations
        /** Can interact with things freely */
        public static final String     PUBLIC         = "public";
        /** Can place blocks */
        public static final String     PLACE          = "place";
        /** Can break blocks. */
        public static final String     BREAK          = "break";
        /** Are counted as "ally" by any system that cares about that. */
        public static final String     ALLY           = "ally";

        public TeamLand                land           = new TeamLand();
        public String                  teamName;
        /** Admins of this team. */
        public Set<UUID>               admin          = Sets.newHashSet();
        /** UUIDs of members of this team. */
        public Set<UUID>               member         = Sets.newHashSet();
        /** Mobs in here are specifically set as protected, this is a whitelist,
         * anything not in here is not protected. */
        public Set<UUID>               protected_mobs = Sets.newHashSet();
        /** Mobs in heere are specifically set to be public, this is a
         * whitelist, anything not in here is not public, unless team is set to
         * allPublic */
        public Set<UUID>               public_mobs    = Sets.newHashSet();
        /** Non-Stored map for quick lookup of rank for each member. */
        public Map<UUID, PlayerRank>   _ranksMembers  = Maps.newHashMap();
        /** Maps of rank name to rank, this is what is actually stored. */
        public Map<String, PlayerRank> rankMap        = Maps.newHashMap();
        /** List of public blocks for the team. */
        public Set<Coordinate>         anyUse         = Sets.newHashSet();
        /** List of public blocks for the team. TODO implement this. */
        public Set<Coordinate>         anyBreakSet    = Sets.newHashSet();
        /** List of public blocks for the team. TODO implement this. */
        public Set<Coordinate>         anyPlaceSet    = Sets.newHashSet();
        /** Home coordinate for the team, used for thome command. */
        public Coordinate              home;
        /** Message sent on exiting team land. */
        public String                  exitMessage    = "";
        /** Mssage sent on entering team land. */
        public String                  enterMessage   = "";
        /** Message sent when denying interactions in the team. */
        public String                  denyMessage    = "";
        /** Prefix infront of team members names. */
        public String                  prefix         = "";
        /** If true, this team is not cleaned up when empty, and cannot be
         * freely joined when empty. */
        public boolean                 reserved       = false;
        /** If this is player specific, currently not used. */
        public boolean                 players        = false;
        /** If true, players cannot take damage here. */
        public boolean                 noPlayerDamage = false;
        /** If true, fakeplayers can run. */
        public boolean                 fakePlayers    = false;
        /** If true, mobs cannot spawn here. */
        public boolean                 noMobSpawn     = false;
        /** If true, team members can hurt each other. */
        public boolean                 friendlyFire   = true;
        /** If true, explosions cannot occur in team land. */
        public boolean                 noExplosions   = false;
        /** If true, anything in this team's land is considered public for
         * interactions. */
        public boolean                 allPublic      = false;
        /** If true, any player can place in this teams land. */
        public boolean                 anyPlace       = false;
        /** If true, any player can break in this teams land. */
        public boolean                 anyBreak       = false;
        /** If false, itemframes are not protected from projectiles. */
        public boolean                 protectFrames  = true;
        /** Map of details about team relations. */
        public Map<String, Relation>   relations      = Maps.newHashMap();
        /** Last time a member of this team was seen. */
        public long                    lastSeen       = 0;
        /** Override of maximum land allowed for the team, if this is not -1, it
         * will be used instead. */
        public int                     maxLand        = -1;
        /** Override of maximum land allowed for the team, if this is not -1, it
         * will be used instead. */
        public int                     maxLoaded      = -1;

        /** Random UUID for the team, this can be used for things like
         * accounts. */
        public UUID                    uuid           = UUID.randomUUID();

        private GameProfile            _teamprofile;

        public LandTeam()
        {
        }

        public LandTeam(String name)
        {
            teamName = name;
        }

        public GameProfile getProfile()
        {
            if (_teamprofile == null) _teamprofile = new GameProfile(uuid, "team:" + teamName);
            return _teamprofile;
        }

        public boolean isMember(UUID id)
        {
            return member.contains(id);
        }

        public boolean isMember(Entity player)
        {
            return isMember(player.getUniqueID());
        }

        public boolean isAdmin(UUID id)
        {
            return admin.contains(id);
        }

        public boolean isAdmin(Entity player)
        {
            return isAdmin(player.getUniqueID());
        }

        public boolean hasRankPerm(UUID player, String perm)
        {
            if (admin.contains(player)) return true;
            PlayerRank rank = _ranksMembers.get(player);
            if (rank == null) return false;
            return rank.perms.contains(perm);
        }

        public void setRankPerm(String rankName, String perm)
        {
            PlayerRank rank = rankMap.get(rankName);
            if (rank != null) rank.perms.add(perm);
        }

        public void unsetRankPerm(String rankName, String perm)
        {
            PlayerRank rank = rankMap.get(rankName);
            if (rank != null) rank.perms.remove(perm);
        }

        /** This is for checking whether the player is in a team with a relation
         * that allows breaking blocks in our land.
         * 
         * @param player
         * @return */
        public boolean canBreakBlock(UUID player, Coordinate location)
        {
            if (anyBreak || anyBreakSet.contains(location)) return true;
            LandTeam team = LandManager.getTeam(player);
            Relation relation = relations.get(team.teamName);
            if (relation != null) { return relation.perms.contains(BREAK); }
            return member.contains(player);
        }

        /** This is for checking whether the player is in a team with a relation
         * that allows placing blocks in our land.
         * 
         * @param player
         * @return */
        public boolean canPlaceBlock(UUID player, Coordinate location)
        {
            if (anyPlace || anyPlaceSet.contains(location)) return true;
            LandTeam team = LandManager.getTeam(player);
            Relation relation = relations.get(team.teamName);
            if (relation != null) { return relation.perms.contains(PLACE); }
            return member.contains(player);
        }

        /** This is for checking whether the player is in a team with a relation
         * that allows using any random thing in our land.
         * 
         * @param player
         * @return */
        public boolean canUseStuff(UUID player, Coordinate location)
        {
            if (allPublic || anyUse.contains(location)) return true;
            LandTeam team = LandManager.getTeam(player);
            Relation relation = relations.get(team.teamName);
            if (relation != null) { return relation.perms.contains(PUBLIC); }
            return member.contains(player);
        }

        public boolean isAlly(UUID player)
        {
            LandTeam team = LandManager.getTeam(player);
            if (team != null) return isAlly(team);
            return member.contains(player);
        }

        public boolean isAlly(LandTeam team)
        {
            if (team == this) return true;
            Relation relation = relations.get(team.teamName);
            if (relation != null) { return relation.perms.contains(ALLY); }
            return false;
        }

        public void init(MinecraftServer server)
        {
            Set<UUID> members = Sets.newHashSet(member);
            if (!teamName.equals(Essentials.config.defaultTeamName))
            {
                for (UUID id : members)
                    LandManager.getInstance()._playerTeams.put(id, this);
            }
            for (UUID id : public_mobs)
                LandManager.getInstance()._public_mobs.put(id, this);
            for (UUID id : protected_mobs)
                LandManager.getInstance()._protected_mobs.put(id, this);
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof LandTeam) { return ((LandTeam) o).teamName.equals(teamName); }
            return false;
        }

        @Override
        public int hashCode()
        {
            return teamName.hashCode();
        }
    }

    public static class TeamLand
    {
        public HashSet<Coordinate> land   = Sets.newHashSet();
        public int                 loaded = 0;

        public boolean addLand(Coordinate land)
        {
            return this.land.add(land);
        }

        public int countLand()
        {
            return land.size();
        }

        public boolean removeLand(Coordinate land)
        {
            return this.land.remove(land);
        }
    }

    static LandManager      instance;

    public static final int VERSION = 1;

    public static void clearInstance()
    {
        if (instance != null)
        {
            LandSaveHandler.saveGlobalData();
            for (String s : instance._teamMap.keySet())
                LandSaveHandler.saveTeam(s);
        }
        instance = null;
    }

    public static LandManager getInstance()
    {
        if (instance == null)
        {
            LandSaveHandler.loadGlobalData();
        }
        return instance;
    }

    public static LandTeam getTeam(UUID id)
    {
        LandTeam playerTeam = getInstance()._playerTeams.get(id);
        if (playerTeam == null) return getDefaultTeam();
        return playerTeam;
    }

    public static LandTeam getTeam(Entity player)
    {
        return getTeam(player.getUniqueID());
    }

    public static LandTeam getDefaultTeam()
    {
        return getInstance().getTeam(Essentials.config.defaultTeamName, true);
    }

    public static LandTeam getWildTeam()
    {
        if (!Essentials.config.wildernessTeam) return null;
        LandTeam wilds = getInstance().getTeam(Essentials.config.wildernessTeamName, false);
        if (wilds == null)
        {
            wilds = getInstance().getTeam(Essentials.config.wildernessTeamName, true);
            wilds.reserved = true;
            wilds.allPublic = true;
            wilds.enterMessage = " ";
            wilds.exitMessage = " ";
            wilds.denyMessage = " ";
        }
        return wilds;
    }

    public static boolean owns(Entity player, Coordinate chunk)
    {
        return getTeam(player).equals(getInstance().getLandOwner(chunk));
    }

    public HashMap<String, LandTeam>        _teamMap        = Maps.newHashMap();
    protected HashMap<Coordinate, LandTeam> _landMap        = Maps.newHashMap();
    protected HashMap<UUID, LandTeam>       _playerTeams    = Maps.newHashMap();
    protected HashMap<UUID, Invites>        invites         = Maps.newHashMap();
    protected Map<UUID, LandTeam>           _protected_mobs = Maps.newHashMap();
    protected Map<UUID, LandTeam>           _public_mobs    = Maps.newHashMap();
    public int                              version         = VERSION;

    LandManager()
    {
    }

    public void toggleMobProtect(UUID mob, LandTeam team)
    {
        if (_protected_mobs.containsKey(mob))
        {
            _protected_mobs.remove(mob);
            team.protected_mobs.remove(mob);
        }
        else
        {
            _protected_mobs.put(mob, team);
            team.protected_mobs.add(mob);
        }
        LandSaveHandler.saveTeam(team.teamName);
    }

    public void toggleMobPublic(UUID mob, LandTeam team)
    {
        if (_public_mobs.containsKey(mob))
        {
            _public_mobs.remove(mob);
            team.public_mobs.remove(mob);
        }
        else
        {
            _public_mobs.put(mob, team);
            team.public_mobs.add(mob);
        }
        LandSaveHandler.saveTeam(team.teamName);
    }

    public void renameTeam(String oldName, String newName) throws CommandException
    {
        if (_teamMap.containsKey(newName)) throw new CommandException("Error, new team name already in use");
        LandTeam team = _teamMap.remove(oldName);
        if (team == null) throw new CommandException("Error, specified team not found");
        _teamMap.put(newName, team);
        for (Invites i : invites.values())
        {
            if (i.teams.remove(oldName))
            {
                i.teams.add(newName);
            }
        }
        team.teamName = newName;
        LandSaveHandler.saveTeam(newName);
        LandSaveHandler.deleteTeam(oldName);
    }

    public void removeTeam(String teamName)
    {
        LandTeam team = _teamMap.remove(teamName);
        LandTeam _default = getDefaultTeam();
        if (team == _default) return;
        for (Coordinate c : team.land.land)
        {
            _landMap.remove(c);
        }
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        for (UUID id : team.member)
        {
            _default.member.add(id);
            _playerTeams.put(id, _default);
            try
            {
                PlayerEntity player = server.getPlayerList().getPlayerByUUID(id);
                if (player != null)
                {
                    player.refreshDisplayName();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        LandSaveHandler.saveTeam(_default.teamName);
        for (Invites i : invites.values())
        {
            i.teams.remove(teamName);
        }
        LandSaveHandler.deleteTeam(teamName);
    }

    public void addTeamLand(String team, Coordinate land, boolean sync)
    {
        LandTeam t = _teamMap.get(team);
        if (t == null)
        {
            Thread.dumpStack();
            return;
        }
        ThutEssentials.logger.log(Level.FINER, "claim: " + team + " Coord: " + land);
        LandTeam prev = _landMap.remove(land);
        t.land.addLand(land);
        if (prev != null) prev.land.removeLand(land);
        _landMap.put(land, t);
        if (sync)
        {
            if (prev != null) LandSaveHandler.saveTeam(prev.teamName);
            LandSaveHandler.saveTeam(team);
        }
    }

    public void addAdmin(UUID admin, String team)
    {
        LandTeam t = getTeam(team, true);
        t.admin.add(admin);
        LandSaveHandler.saveTeam(team);
    }

    public void addToTeam(UUID member, String team)
    {
        LandTeam t = getTeam(team, true);
        if (t.admin.isEmpty() && !t.teamName.equals(Essentials.config.defaultTeamName))
        {
            t.admin.add(member);
        }
        if (_playerTeams.containsKey(member))
        {
            LandTeam old = _playerTeams.remove(member);
            old.member.remove(member);
            old.admin.remove(member);
            LandSaveHandler.saveTeam(old.teamName);
        }
        t.member.add(member);
        _playerTeams.put(member, t);
        Invites invite = invites.get(member);
        if (invite != null)
        {
            invite.teams.remove(team);
        }
        LandSaveHandler.saveTeam(team);
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        try
        {
            PlayerEntity player = server.getPlayerList().getPlayerByUUID(member);
            if (player != null)
            {
                player.refreshDisplayName();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public int countLand(String team)
    {
        LandTeam t = _teamMap.get(team);
        if (t != null) { return t.land.countLand(); }
        return 0;
    }

    public void createTeam(UUID member, String team) throws CommandException
    {
        if (_teamMap.containsKey(team)) throw new CommandException(team + " already exists!");
        getTeam(team, true);
        addToTeam(member, team);
        addAdmin(member, team);
    }

    public List<String> getInvites(UUID member)
    {
        List<String> ret = new ArrayList<String>();
        Invites invite = invites.get(member);
        if (invite == null) return ret;
        return Lists.newArrayList(invite.teams);
    }

    public LandTeam getLandOwner(Coordinate land)
    {
        LandTeam owner = _landMap.get(land);
        if (owner == null) return getWildTeam();
        return owner;
    }

    public LandTeam getTeam(String name, boolean create)
    {
        LandTeam team = _teamMap.get(name);
        if (team == null && create)
        {
            team = new LandTeam(name);
            _teamMap.put(name, team);
        }
        return team;
    }

    public List<Coordinate> getTeamLand(String team)
    {
        ArrayList<Coordinate> ret = new ArrayList<Coordinate>();
        LandTeam t = _teamMap.get(team);
        if (t != null) ret.addAll(t.land.land);
        return ret;
    }

    public boolean hasInvite(UUID member, String team)
    {
        Invites invite = invites.get(member);
        if (invite != null) return invite.teams.contains(team);
        return false;
    }

    public boolean invite(UUID inviter, UUID invitee)
    {
        if (!isAdmin(inviter)) return false;
        String team = _playerTeams.get(inviter).teamName;
        if (hasInvite(invitee, team)) return false;
        Invites invite = invites.get(invitee);
        if (invite == null)
        {
            invite = new Invites();
            invites.put(invitee, invite);
        }
        invite.teams.add(team);
        return true;
    }

    public boolean isAdmin(UUID member)
    {
        LandTeam team = _playerTeams.get(member);
        if (team == null) return false;
        return team.isAdmin(member);
    }

    public boolean isOwned(Coordinate land)
    {
        return _landMap.containsKey(land);
    }

    public boolean isPublic(Coordinate c, LandTeam team)
    {
        return team.allPublic || team.anyUse.contains(c);
    }

    public boolean isTeamLand(Coordinate chunk, String team)
    {
        LandTeam t = _teamMap.get(team);
        if (t != null) return t.land.land.contains(chunk);
        return false;
    }

    public void removeAdmin(UUID member)
    {
        LandTeam t = _playerTeams.get(member);
        if (t != null)
        {
            t.admin.remove(member);
        }
    }

    public void removeFromInvites(UUID member, String team)
    {
        Invites invite = invites.get(member);
        if (invite != null && invite.teams.contains(team))
        {
            invite.teams.remove(team);
            LandSaveHandler.saveGlobalData();
        }
    }

    public void removeFromTeam(UUID member)
    {
        addToTeam(member, getDefaultTeam().teamName);
    }

    public void removeTeamLand(String team, Coordinate land)
    {
        LandTeam t = _teamMap.get(team);
        if (t != null && t.land.removeLand(land))
        {
            _landMap.remove(land);
            ThutEssentials.logger.log(Level.FINER, "unclaim: " + team + " Coord: " + land);
            // Ensure the land is unloaded if it was loaded.
            unLoadLand(land, t);
            LandSaveHandler.saveTeam(team);
        }
    }

    public void setPublic(Coordinate c, LandTeam owner)
    {
        owner.anyUse.add(c);
        LandSaveHandler.saveTeam(owner.teamName);
    }

    public void unsetPublic(Coordinate c, LandTeam owner)
    {
        if (!owner.anyUse.remove(c)) return;
        LandSaveHandler.saveTeam(owner.teamName);
    }

    public void loadLand(World world, Coordinate chunk, LandTeam team)
    {
        if (LandEventsHandler.ChunkLoadHandler.addChunks(world, chunk, team.uuid))
        {
            team.land.loaded++;
            LandSaveHandler.saveTeam(team.teamName);
        }
    }

    public void unLoadLand(Coordinate chunk, LandTeam team)
    {
        if (LandEventsHandler.ChunkLoadHandler.removeChunks(chunk))
        {
            team.land.loaded--;
            LandSaveHandler.saveTeam(team.teamName);
        }
    }
}
