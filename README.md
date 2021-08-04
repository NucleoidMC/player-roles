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
      "permission_level": 4,
      "command_feedback": true,
      "commands": {
        ".*": "allow"
      }
    }
  },
  "spectator": {
    "level": 10,
    "overrides": {
      "commands": {
        "gamemode (spectator|adventure)": "allow"
      }
    }
  },
  "mute": {
    "level": 1,
    "overrides": {
      "mute": true
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
Currently, the supported override types are `commands`, `name_style`, `chat_format`, `mute`, `command_feedback`, `permission_level` and `entity_selectors`.

It is important to consider how overrides are applied when multiple roles target the same things. Conflicts like this are resolved by always choosing the role with the highest level.
So, in the case of the example: although `everyone` declares every command except `help` to be disallowed, because `admin` and `spectator` have higher levels, they will override this behaviour.

##### commands 
The `commands` override is used to manipulate the commands that a player is able to use.
Each override entry specifies a regular expression pattern to match, and then a strategy for how to respond when the mod encounters that pattern.

For example, the pattern `.*` matches every possible command, while `gamemode (spectator|adventure)` would match the gamemode command only with spectator and adventure mode.
The strategies that can then be used alongside these patterns are `allow` and `deny`: 
`allow` will make sure that the player is allowed to use this command, while `deny` will prevent the player from using this command.

For example:
```json
"commands": {
  "gamemode (spectator|adventure)": "allow"
}
```

The commands override can additionally make use of the `hidden` rule result, which will allow the command to be used, 
while hiding it from command suggestions.

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

##### permission level
The `permission_level` override sets the vanilla [permission level](https://minecraft.gamepedia.com/Server.properties#op-permission-level) for assigned players. 
This is useful for interacting with other mods, as well as with vanilla features that aren't supported by this mod.

Permission level is declared like:
```json
"permission_level": 4
```

##### mute
The `mute` override functions very simply by preventing assigned players from typing in chat.

Mute is declared like:
```json
"mute": true
```

##### command feedback
By default, all operators receive global feedback when another player runs a command. 
The `command_feedback` override allows specific roles to receive this same kind of feedback.

Command feedback is declared like:
```json
"command_feedback": true
```

#### entity_selectors
Normally, only command sources with a permission level of two or higher can use entity selectors.
The `entity_selectors` override allows specific roles to use entity selectors.

Entity selectors can be allowed like:
```json
"entity_selectors": true
```

#### Other configuration
Roles can additionally be applied to command blocks or function executors through the configuration file.
For example:
```json
{
  "commands": {
    "apply": {
      "command_block": true,
      "function": true
    },
    "overrides": {
    }
  }
}
```

It may also be useful for a role to inherit the overrides from another role.
This can be done with the `includes` declaration by referencing other roles with a lower level.
For example:
```json
{
  "foo": {
    "includes": ["bar"],
    "overrides": {
      "commands": {
        ".*": "allow"
      }
    }
  },
  "bar": {
    "overrides": {
      "name_style": "red"
    }
  }
}
```

With this configuration, the `foo` role will inherit the red `name_style`.

#### Applying roles in-game
Once you've made modifications to the `roles.json` file, you can reload it by using the `/role reload`.

All role management goes through this `role` command via various subcommands. For example:

- `role assign Gegy admin`: assigns the `admin` role to `Gegy`
- `role remove Gegy admin`: removes the `admin` role from `Gegy`
- `role list Gegy`: lists all the roles that have been applied to `Gegy`
- `role reload`: reloads the `roles.json` configuration file
