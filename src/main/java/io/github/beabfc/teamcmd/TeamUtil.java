package io.github.beabfc.teamcmd;

import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class TeamUtil {
    private static final int TPS = 20;
    private static final int TIMEOUT = 120;
    private static final HashMap<UUID, TeamInvite> inviteMap = new HashMap<>();

    public static void sendToTeammates(ServerPlayerEntity player, Text message) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        for (ServerPlayerEntity otherPlayer : server.getPlayerManager().getPlayerList()) {
            if (otherPlayer.isTeammate(player) && !otherPlayer.equals(player)) {
                otherPlayer.sendMessage(message);
            }
        }

    }

    public static void addInvite(ServerPlayerEntity player, String teamName) {
        inviteMap.put(player.getUuid(), new TeamInvite(teamName));
    }

    public static String getInvitedTeam(ServerPlayerEntity player) {
        TeamInvite invite = inviteMap.get(player.getUuid());
        if (invite != null) return invite.getTeamName();
        return null;
    }

    public static void resetInvite(ServerPlayerEntity player) {
        inviteMap.remove(player.getUuid());
    }


    public static void tick(MinecraftServer server) {
        for (Map.Entry<UUID, TeamInvite> entry : inviteMap.entrySet()) {
            TeamInvite invite = entry.getValue();

            if (invite.isExpired()) {
                UUID playerUuid = entry.getKey();
                inviteMap.remove(playerUuid);
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);
                if (player != null) {
                    Team team = player.getScoreboard().getTeam(invite.getTeamName());
                    if (team != null) {
                        player.sendMessage(Text.translatable("commands.teamcmd.invite_expired",
                            team.getFormattedName()));
                    }
                }
            }

            invite.hasTicked();
        }
    }

    private static class TeamInvite {
        private final String teamName;
        private int remainingTicks;

        public TeamInvite(String teamName) {
            this.remainingTicks = TIMEOUT * TPS;
            this.teamName = teamName;
        }

        public String getTeamName() {
            return this.teamName;
        }

        public boolean isExpired() {
            return this.remainingTicks <= 0;
        }

        public void hasTicked() {
            this.remainingTicks--;
        }
    }
}
