# Player Roles for Fabric
This is a simple implementation allowing for custom permissions to be assigned to players via Discord-like "roles".
Roles and their permissions are defined within a JSON file, which can be easily modified and reloaded at runtime for rapid iteration.

The roles.json file is located in the config directory (`<root>/config/roles.json`). An example configuration may look like:
```json
{
  "admin": {
    "level": 100,
    "overrides": {
      "name_style": ["red", "bold"],
      "chat_format": "<%s*> %s",
      "commands": {
        ".*": "allow"
      }
    }
  },
  "spectator": {
    "level": 10,
    "overrides": {
      "commands": {
        "gamemode (spectator|survival)": "allow"
      }
    }
  },
  "everyone": {
    "overrides": {
      "commands": {
        "help": "allow",
        ".*": "deny"
      }
    }
  }
}
```

But what's going on here? This JSON file is declaring three roles: `admin`, `spectator` and `everyone`.

`everyone` is the default role: every player will have this role, and it cannot be removed. 
The other roles that are specified function as overrides on top of the `everyone` role.

#### Overrides
Within each role declaration, we list a set of overrides. Overrides are the generic system that this mod uses to change game behavior based on roles.
Currently, the supported override types are `commands`, `name_style` and `chat_format`.

It is important to consider how overrides are applied when multiple roles target the same things. Conflicts like this are resolved by always choosing the role with the highest level.
So, in the case of the example: although `everyone` declares every command except `help` to be disallowed, because `admin` and `spectator` have higher levels, they will override this behaviour.

##### commands 
The `commands` override is used to manipulate the commands that a player is able to use.
Each override entry specifies a regular expression pattern to match, and then a strategy for how to respond when the mod encounters that pattern.

For example, the pattern `.*` matches every possible command, while `gamemode (spectator|survival)` would match the gamemode command only with spectator and survival mode.
The strategies that can then be used alongside these patterns are `allow` and `deny`: 
`allow` will make sure that the player is allowed to use this command, while `deny` will prevent the player from using this command.

For example:
```json
"commands": {
  "gamemode (spectator|survival)": "allow"
}
```

##### chat format
The `chat_format` override is used to change how player messages appear in the chat.
It is declared simply as a string formatter pattern. Every instance of `%s` is replaced by a formatter argument.
The first `%s` is replaced by the player name, and the second is replaced by the message.

For example:
```json
"chat_format": "%s says: %s"
```
...which would format as `Gegy says: hi!`

It is worth nothing that color codes can be used here, but they *will not apply to the player name*.
To apply color to a player name, you should use the `name_style` override.

##### name style
The `name_style` override modifies the name color for players with that role. This has lower priority than scoreboard team colors.

Name style is declared like:
```json
"name_style": ["red", "bold", "underline"]
```

#### Applying roles in-game
Once you've made modifications to the `roles.json` file, you can reload it by using the `/role reload`.

All role management goes through this `role` command via various subcommands. For example:

- `role assign Gegy admin`: assigns the `admin` role to `Gegy`
- `role remove Gegy admin`: removes the `admin` role from `Gegy`
- `role list Gegy`: lists all the roles that have been applied to `Gegy`
- `role reload`: reloads the `roles.json` configuration file
