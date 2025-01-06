[![Get it on Modrinth](https://img.shields.io/badge/Get%20it%20on-Modrinth-green?style=for-the-badge&logo=modrinth)](https://modrinth.com/plugin/knockbacksync)

Tired of inconsistent knockback ruining your PvP experience? Our plugin recalculates knockback as if it were done clientside, leveling the playing field and ensuring every player enjoys a fair fight, no matter their connection quality.

Minecraft doesn’t factor in network latency when determining a player's actions on the server. This causes the server to receive outdated information that doesn’t reflect the player's clientside position, leading to varying knockback effects based on connection quality.

This plugin intercepts and adjusts knockback calculations to match what would occur clientside, effectively mitigating the disadvantages caused by high latency. By synchronizing knockback handling, we ensure that players experience consistent and fair knockback, providing a balanced and competitive environment for all."

Showcase: https://www.youtube.com/watch?v=SVokpr3v-TA

Official Discord: https://discord.gg/nnpqpAtyVW

## Frequently Asked Questions (FAQ)

### Does this change put high ping players at a disadvantage?
**It depends on the player.** Some may notice a difference if they're used to relying on high ping to reduce knockback. For others, it could actually be an advantage.

### How does this change benefit high ping players?
**Knockback control.** For example, it will be easier to escape crit chains and punish crit.

### Why was the configurability of ping offset removed?
**It promotes consistency across all servers.** Extensive testing with top players has shown that an offset of 25 provides a balanced experience for everyone.

### How do I change the ping offset?
**You must run a modified build of KnockbackSync.** The variable can be changed inside of the [PlayerData](common/src/main/java/me/caseload/knockbacksync/player/PlayerData.java) class.

## What servers are using this plugin?
| IP               | Location                          | Region | Ping Offset | spike_threshold |
|------------------|-----------------------------------|--------|-------------|-----------------|
| `pvparcade.club` | Ashburn, Virginia, United States  | NA     | 20          | 15              |
| `stray.gg`       | Ashburn, Virginia, United States  | NA     | 25          | 20              |
| `eu.stray.gg`    | Frankfurt, Hesse, Germany         | EU     | 25          | 20              |
| `valed.gg`       | Frankfurt, Hesse, Germany         | EU     | 25          | 20              |
| `eu.catpvp.xyz`  | Frankfurt, Hesse, Germany         | EU     | 25          | 20              |
| `as.catpvp.xyz`  | Singapore                         | AS     | 25          | 20              |
| `na.catpvp.xyz`  | New York, New York, United States | NA     | 25          | 20              |
| `hyperium.pl`    | Wrocław, Dolnośląskie, Poland     | EU     | 25          | 20              |

## Commands
---
### /knockbacksync ping [target]

**Description:**

This command allows you to check the ping of a player, including jitter. If no target is specified, it will check your own ping.

**Permissions:**

* `knockbacksync.ping` (defaults to true for players)

**Examples:**

* `/knockbacksync ping`: Checks your own ping.
* `/knockbacksync ping Steve`: Checks the ping of a player named Steve.

**Output:**

* If a pong packet has been received:  "Your last ping packet took [ping]ms. Jitter: [jitter]ms." or "[Player]'s last ping packet took [ping]ms. Jitter: [jitter]ms."
* If a pong packet has not been received: "Pong not received. Your estimated ping is [estimated ping]ms." or "Pong not received. [Player]'s estimated ping is [estimated ping]ms."

**Notes:**

* The estimated ping is based on the player's platform reported ping.
* Jitter represents the variation in ping over time.
---
### /knockbacksync status [target]

**Description:**

This command allows you to check the KnockbackSync status of a player or the server. If no target is specified, it will show both the global status and your own status.

**Permissions:**

* `knockbacksync.status.self` (defaults to true for players): Allows checking your own status.
* `knockbacksync.status.other` (defaults to op only): Allows checking the status of other players.

**Examples:**

* `/knockbacksync status`: Shows the global status and your own status.
* `/knockbacksync status Steve`: Shows the status of a player named Steve.

**Output:**

* **Global status:** "Global KnockbackSync status: [Enabled/Disabled]"
* **Player status:** "[Player]'s KnockbackSync status: [Enabled/Disabled]" (or "Disabled (Global toggle is off)" if the global toggle is off)

**Notes:**

* The player status will be "Disabled" if the global toggle is off, even if the player has individually enabled KnockbackSync.
* The messages displayed by this command are configurable in the `config.yml` file.
---
### /knockbacksync toggle [target]

**Description:**

This command allows you to toggle KnockbackSync for yourself, another player, or globally.

**Permissions:**

* `knockbacksync.toggle.self` (defaults to true for players): Allows toggling KnockbackSync for yourself.
* `knockbacksync.toggle.other` (defaults to op only): Allows toggling KnockbackSync for other players.
* `knockbacksync.toggle.global` (defaults to op only): Allows toggling KnockbackSync globally for the server.

**Examples:**

* `/knockbacksync toggle`: Toggles KnockbackSync globally (if you have permission) or for yourself.
* `/knockbacksync toggle Steve`: Toggles KnockbackSync for a player named Steve.

**Output:**

* **Global toggle:** Sends a message indicating whether KnockbackSync has been enabled or disabled globally. The messages are configurable in the `config.yml` file.
* **Player toggle:** Sends a message indicating whether KnockbackSync has been enabled or disabled for the specified player. The messages are configurable in the `config.yml` file.
* **Ineligible player:** If a player is ineligible for KnockbackSync (e.g., due to permissions), a configurable message will be sent.
* **KnockbackSync disabled:** If KnockbackSync is disabled globally and you try to toggle it for a player, a message will be sent indicating that KnockbackSync is disabled.

**Notes:**

* If KnockbackSync is disabled globally, toggling it for a player will have no effect until KnockbackSync is enabled globally.
* Players can only toggle KnockbackSync for themselves if they have the `knockbacksync.toggle.self` permission.
* Operators can toggle KnockbackSync for other players and globally.
---
### /knockbacksync reload

**Description:**

This command reloads the KnockbackSync plugin's configuration file.

**Permissions:**

* `knockbacksync.reload` (defaults to op only)

**Examples:**
* `/knockbacksync reload`

**Output:**

* Sends a message to the command sender indicating that the configuration has been reloaded. The message is configurable in the `config.yml` file.

**Notes:**

* This command is useful for applying changes made to the configuration file without restarting the server.
---
### /knockbacksync toggleoffground

  **Description:**
  
  This command toggles the experimental off-ground knockback synchronization feature.
  
  **Permissions:**
  
  * `knockbacksync.toggleoffground` (defaults to op only)
  
  **Examples:**
  
  * `/knockbacksync toggleoffground`
  
  **Output:**
  
  * Sends a message indicating whether the experimental off-ground knockback synchronization has been enabled or disabled. The messages are configurable in the `config.yml` file.
  
  **Notes:**
    * This feature is experimental and may not work as expected.

## License
GNU General Public License v3.0 or later

See [LICENSE](https://www.gnu.org/licenses/gpl-3.0.en.html) to see the full text.
