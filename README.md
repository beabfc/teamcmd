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

Configuration options to tune the mod to your needs are planned and will be available soon!

## Limitations

- There is no hierachy in teams, everybody can invite new members.
- It's not possible to change the actual name of a team, only the displayed name.