# TeamCommand

This mod adds a new command to Minecraft which allows players to create and manage their own teams. The command is
similar to factions which you might know from other mods but is based on the Vanilla `/team` command which only
operators can use.

![Players in different teams](https://i.ibb.co/HdxZjC3/players.png)

## Commands

- `/t create <name> <color>` Create a new team and be its first member.
- `/t list [<team>]` Lists all teams or the players in a specific team.
- `/t invite <player>` Invite a new player to join your team.
- `/t accept` Accept the last invitation and join the team.
- `/t set`
    - `displayName <name>` Change the displayed name of your team.
    - `color <color>` Change the color of your team.
    - `friendlyFire <allowed>` Specify wether players in your team can inflict damage to each other or not.
    - `seeInvisibles <allowed>` Specify wether members of your team can see invisible teammates.
- `/t leave` Leave your current team.

Additionally, players can use every vanilla or modded feature integrated with teams like `/teammsg` to send a chat
message to all teammates.

## Configuration

The mods configuration file is located at `teamcommand.toml` in your servers config folder.

- `commandName`: The name of the new team command _(default: `t`)_.
- `allowDuplicateColors`: Specify wether there can be multiple teams with the same color _(default: `true`)_.
- `allowDuplicateDisplaynames`: Specify wether there can be multiple teams with the same name  _(default: `false`)_.
- `prefixFormat`: A format string which will determine the team prefix based on the displayName _(default: `[%.1s] `)_.
- `prefixUseTeamColor`: Wether the prefix will be in the teams color or a secondary color. _(default: `false`)_
- `suffixFormat`: A format string which will determine the team suffix based on the displayName _(default: empty string)_
- `suffixUseTeamColor`: Wether the suffix will be in the teams color or a secondary color. _(default: `false`)_

### Prefix & Suffix Format Strings

The format string for prefix and suffix is passed the displayed name of the team. You can look up the official syntax for
java format strings [here](https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax).

Examples:

- `[%.1s] ` display the first letter of the displayed name in square brackets (default prefix)
- (empty string) display nothing (default suffix)
- `TEAM %s ` display the letters "TEAM", a whitespace and the team name
- `%S ` the team name in uppercase characters
- `%.3S ` the first three letters of the team name in uppercase characters

It is recommeded to end the prefix and begin the suffix with a whitespace to prevent them from looking like a part of
the players name.

## Limitations

- There is no hierarchy in teams, everybody can invite new members.
- It's not possible to change the actual name of a team, only the displayed name.
- The amount of teams is limited at 16 if `allowDuplicateColors` is disabled as Minecraft only allows 16 colors.