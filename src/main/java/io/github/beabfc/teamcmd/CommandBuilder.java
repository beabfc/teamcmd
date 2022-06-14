package io.github.beabfc.teamcmd;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.Collection;


public class CommandBuilder {
    private static final SimpleCommandExceptionType ADD_DUPLICATE_EXCEPTION =
        new SimpleCommandExceptionType(new TranslatableText("commands.team.add.duplicate"));
    private static final SimpleCommandExceptionType OPTION_NAME_UNCHANGED_EXCEPTION =
        new SimpleCommandExceptionType(new TranslatableText("commands.team.option.name.unchanged"));
    private static final SimpleCommandExceptionType OPTION_COLOR_UNCHANGED_EXCEPTION =
        new SimpleCommandExceptionType(new TranslatableText("commands.team.option.color.unchanged"));
    private static final SimpleCommandExceptionType OPTION_FRIENDLY_FIRE_ALREADY_ENABLED_EXCEPTION =
        new SimpleCommandExceptionType(new TranslatableText("commands.team.option.friendlyfire.alreadyEnabled"));
    private static final SimpleCommandExceptionType OPTION_FRIENDLY_FIRE_ALREADY_DISABLED_EXCEPTION =
        new SimpleCommandExceptionType(new TranslatableText("commands.team.option.friendlyfire.alreadyDisabled"));
    private static final SimpleCommandExceptionType OPTION_SEE_FRIENDLY_INVISIBLES_ALREADY_ENABLED_EXCEPTION =
        new SimpleCommandExceptionType(new TranslatableText("commands.team.option.seeFriendlyInvisibles" +
            ".alreadyEnabled"));
    private static final SimpleCommandExceptionType OPTION_SEE_FRIENDLY_INVISIBLES_ALREADY_DISABLED_EXCEPTION =
        new SimpleCommandExceptionType(new TranslatableText("commands.team.option.seeFriendlyInvisibles" +
            ".alreadyDisabled"));
    private static final SimpleCommandExceptionType ALREADY_IN_TEAM =
        new SimpleCommandExceptionType(new TranslatableText("commands.teamcmd.fail.in_team"));
    private static final SimpleCommandExceptionType NOT_IN_TEAM =
        new SimpleCommandExceptionType(new TranslatableText("commands.teamcmd.fail.no_team"));
    private static final SimpleCommandExceptionType INVITED_TEAMMATE =
        new SimpleCommandExceptionType(new TranslatableText("commands.teamcmd.fail.already_teammate"));
    private static final SimpleCommandExceptionType NOT_INVITED =
        new SimpleCommandExceptionType(new TranslatableText("commands.teamcmd.fail.not_invited"));
    private static final DynamicCommandExceptionType TEAM_NOT_FOUND =
        new DynamicCommandExceptionType(option -> new TranslatableText("team.notFound", option));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> teamCmd = CommandManager.literal("t");
        teamCmd.then(CommandManager
                .literal("create")
                .then(CommandManager
                    .argument("name", StringArgumentType.word())
                    .then(CommandManager
                        .argument("color", ColorArgumentType.color())
                        .executes(ctx -> executeCreate(ctx.getSource(), StringArgumentType.getString(ctx, "name"),
                            ColorArgumentType.getColor(ctx, "color"))))))
            .then(CommandManager
                .literal("list")
                .executes(ctx -> executeListTeams(ctx.getSource()))
                .then(CommandManager
                    .argument("team", TeamArgumentType.team())
                    .executes(ctx -> executeListMembers(ctx.getSource(), TeamArgumentType.getTeam(ctx, "team")))))
            .then(CommandManager.literal("leave").executes(ctx -> executeLeave(ctx.getSource())))
            .then(CommandManager
                .literal("invite")
                .then(CommandManager
                    .argument("player", EntityArgumentType.player())
                    .executes(ctx -> executeInvitePlayer(ctx.getSource(),
                        EntityArgumentType.getPlayer(ctx, "player")))))
            .then(CommandManager.literal("accept").executes(ctx -> executeAcceptInvite(ctx.getSource())));

        LiteralArgumentBuilder<ServerCommandSource> setCommand = CommandManager.literal("set")
            .then(CommandManager
                .literal("color")
                .then(CommandManager
                    .argument("color", ColorArgumentType.color())
                    .executes(ctx -> executeSetColor(ctx.getSource(), ColorArgumentType.getColor(ctx, "color")))))
            .then(CommandManager
                .literal("friendlyFire")
                .then(CommandManager
                    .argument("allowed", BoolArgumentType.bool())
                    .executes(ctx -> executeSetFriendlyFire(ctx.getSource(),
                        BoolArgumentType.getBool(ctx, "allowed")))))

            .then(CommandManager
                .literal("seeInvisibles")
                .then(CommandManager
                    .argument("allowed", BoolArgumentType.bool())
                    .executes(ctx -> executeSetShowFriendlyInvisibles(ctx.getSource(), BoolArgumentType.getBool(ctx,
                        "allowed")))))
            .then(CommandManager
                .literal("displayName")
                .then(CommandManager
                    .argument("displayName", StringArgumentType.word())
                    .executes(ctx -> executeSetDisplayName(ctx.getSource(), StringArgumentType.getString(ctx,
                        "displayName")))));

        teamCmd.then(setCommand);
        dispatcher.register(teamCmd);

    }

    private static int executeCreate(ServerCommandSource source, String name, Formatting color) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        String teamName = name.toLowerCase();
        Scoreboard scoreboard = source.getServer().getScoreboard();
        if (player.getScoreboardTeam() != null) {
            throw ALREADY_IN_TEAM.create();
        } else if (scoreboard.getTeam(teamName) != null) {
            throw ADD_DUPLICATE_EXCEPTION.create();
        } else {
            Team newTeam = scoreboard.addTeam(teamName);
            newTeam.setDisplayName(new LiteralText(name));
            scoreboard.addPlayerToTeam(player.getEntityName(), newTeam);
            newTeam.setColor(color);
            setPrefix(newTeam);
            source.sendFeedback(new TranslatableText("commands.team.add.success", newTeam.getFormattedName()), false);
            return 1;
        }

    }

    private static int executeSetDisplayName(ServerCommandSource source, String displayName) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        Team team = (Team) player.getScoreboardTeam();
        if (team == null) {
            throw NOT_IN_TEAM.create();
        } else if (team.getDisplayName().getString().equals(displayName)) {
            throw OPTION_NAME_UNCHANGED_EXCEPTION.create();
        }

        team.setDisplayName(new LiteralText(displayName));
        setPrefix(team);

        source.sendFeedback(new TranslatableText("commands.team.option.name.success", team.getFormattedName()), false);
        return 0;
    }

    private static int executeSetColor(ServerCommandSource source, Formatting color) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        Team team = (Team) player.getScoreboardTeam();
        if (team == null) {
            throw NOT_IN_TEAM.create();
        } else if (team.getColor().equals(color)) {
            throw OPTION_COLOR_UNCHANGED_EXCEPTION.create();
        }

        team.setColor(color);
        setPrefix(team);

        source.sendFeedback(new TranslatableText("commands.team.option.color.success", team.getFormattedName(),
            color.getName()), false);
        return 0;
    }

    private static int executeSetFriendlyFire(ServerCommandSource source, boolean allowed) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        Team team = (Team) player.getScoreboardTeam();
        if (team == null) {
            throw NOT_IN_TEAM.create();
        } else if (team.isFriendlyFireAllowed() == allowed) {
            throw allowed ? OPTION_FRIENDLY_FIRE_ALREADY_ENABLED_EXCEPTION.create() :
                OPTION_FRIENDLY_FIRE_ALREADY_DISABLED_EXCEPTION.create();
        }
        team.setFriendlyFireAllowed(allowed);
        source.sendFeedback(new TranslatableText("commands.team.option.friendlyfire." + (allowed ? "enabled" :
            "disabled"), team.getFormattedName()), false);
        return 0;
    }

    private static int executeSetShowFriendlyInvisibles(ServerCommandSource source, boolean allowed) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        Team team = (Team) player.getScoreboardTeam();
        if (team == null) {
            throw NOT_IN_TEAM.create();
        } else if (team.shouldShowFriendlyInvisibles() == allowed) {
            throw allowed ? OPTION_SEE_FRIENDLY_INVISIBLES_ALREADY_ENABLED_EXCEPTION.create() :
                OPTION_SEE_FRIENDLY_INVISIBLES_ALREADY_DISABLED_EXCEPTION.create();
        }
        team.setShowFriendlyInvisibles(allowed);
        source.sendFeedback(new TranslatableText("commands.team.option.seeFriendlyInvisibles." + (allowed ?
            "enabled" : "disabled"), team.getFormattedName()), false);
        return 0;
    }

    private static int executeInvitePlayer(ServerCommandSource source, ServerPlayerEntity newPlayer) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        Team team = (Team) player.getScoreboardTeam();
        if (team == null) {
            throw NOT_IN_TEAM.create();
        } else if (newPlayer.isTeammate(player)) {
            throw INVITED_TEAMMATE.create();
        }

        TeamUtil.addInvite(newPlayer, team.getName());
        TeamUtil.sendToTeammates(player, new TranslatableText("commands.teamcmd.teammates.invite",
            player.getDisplayName(), newPlayer.getDisplayName()));
        source.sendFeedback(new TranslatableText("commands.teamcmd.invite.success", newPlayer.getDisplayName()), false);

        Text inviteText = new TranslatableText("commands.teamcmd.invite", player.getDisplayName(),
            team.getFormattedName()).formatted(Formatting.GRAY).styled((style) -> style
            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/t accept"))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("/t accept")))
            .withInsertion("/t accept"));


        newPlayer.sendSystemMessage(inviteText, Util.NIL_UUID);
        return 0;
    }

    private static int executeAcceptInvite(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        String teamName = TeamUtil.getInvitedTeam(player);

        if (player.getScoreboardTeam() != null) {
            throw ALREADY_IN_TEAM.create();
        } else if (teamName == null) {
            throw NOT_INVITED.create();
        }

        Team team = player.getScoreboard().getTeam(teamName);
        if (team == null) {
            throw TEAM_NOT_FOUND.create(teamName);
        }

        player.getScoreboard().addPlayerToTeam(player.getEntityName(), team);
        source.sendFeedback(new TranslatableText("commands.teamcmd.joined", team.getFormattedName()), false);
        TeamUtil.resetInvite(player);

        TeamUtil.sendToTeammates(player, new TranslatableText("commands.teamcmd.teammates.joined",
            player.getDisplayName()));
        return 1;
    }

    private static int executeLeave(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        Team team = (Team) player.getScoreboardTeam();
        if (team == null) {
            throw NOT_IN_TEAM.create();
        }

        TeamUtil.sendToTeammates(player, new TranslatableText("commands.teamcmd.teammates.left",
            player.getDisplayName()));
        player.getScoreboard().clearPlayerTeam(player.getEntityName());
        if (team.getPlayerList().size() == 0) player.getScoreboard().removeTeam(team);
        source.sendFeedback(new TranslatableText("commands.teamcmd.left", team.getFormattedName()), false);
        return 1;
    }

    private static int executeListMembers(ServerCommandSource source, Team team) {
        Collection<String> collection = team.getPlayerList();
        if (collection.isEmpty()) {
            source.sendFeedback(new TranslatableText("commands.team.list.members.empty", team.getFormattedName()),
                false);
        } else {
            source.sendFeedback(new TranslatableText("commands.team.list.members.success", team.getFormattedName(),
                collection.size(), Texts.joinOrdered(collection)), false);
        }
        return collection.size();
    }

    private static int executeListTeams(ServerCommandSource source) {
        Collection<Team> collection = source.getServer().getScoreboard().getTeams();
        if (collection.isEmpty()) {
            source.sendFeedback(new TranslatableText("commands.team.list.teams.empty"), false);
        } else {
            source.sendFeedback(new TranslatableText("commands.team.list.teams.success", collection.size(),
                Texts.join(collection, Team::getFormattedName)), false);
        }
        return collection.size();
    }

    private static void setPrefix(Team team) {
        Formatting teamColor = team.getColor();
        Formatting color;
        switch (teamColor) {
            case AQUA -> color = Formatting.DARK_AQUA;
            case DARK_AQUA -> color = Formatting.AQUA;
            case BLUE -> color = Formatting.DARK_BLUE;
            case DARK_BLUE -> color = Formatting.BLUE;
            case WHITE -> color = Formatting.GRAY;
            case GRAY -> color = Formatting.WHITE;
            case DARK_GRAY -> color = Formatting.BLACK;
            case BLACK -> color = Formatting.DARK_GRAY;
            case RED -> color = Formatting.DARK_RED;
            case DARK_RED -> color = Formatting.RED;
            case GREEN -> color = Formatting.DARK_GREEN;
            case DARK_GREEN -> color = Formatting.GREEN;
            case LIGHT_PURPLE -> color = Formatting.DARK_PURPLE;
            case DARK_PURPLE -> color = Formatting.LIGHT_PURPLE;
            case YELLOW -> color = Formatting.GOLD;
            case GOLD -> color = Formatting.YELLOW;
            default -> {
                team.setColor(Formatting.WHITE);
                color = Formatting.GRAY;
            }
        }
        team.setPrefix(new LiteralText("[" + team.getDisplayName().getString().charAt(0) + "] ").formatted(color));
    }
}
