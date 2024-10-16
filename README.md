[![Get it on Modrinth](https://img.shields.io/badge/Get%20it%20on-Modrinth-green?style=for-the-badge&logo=modrinth)](https://modrinth.com/plugin/knockbacksync)

Tired of inconsistent knockback ruining your PvP experience? Our plugin recalculates knockback as if it were done clientside, leveling the playing field and ensuring every player enjoys a fair fight, no matter their connection quality.

Minecraft doesn’t factor in network latency when determining a player's actions on the server. This causes the server to receive outdated information that doesn’t reflect the player's clientside position, leading to varying knockback effects based on connection quality.

This plugin intercepts and adjusts knockback calculations to match what would occur clientside, effectively mitigating the disadvantages caused by high latency. By synchronizing knockback handling, we ensure that players experience consistent and fair knockback, providing a balanced and competitive environment for all."
## Frequently Asked Questions (FAQ)

### Does this change put high ping players at a disadvantage?
**It depends on the player.** Some may notice a difference if they're used to relying on high ping to reduce knockback. For others, it could actually be an advantage.

### How does this change benefit high ping players?
**Knockback control.** For example, it will be easier to escape crit chains and punish crit.

Then just type
```
/knockbacksync reload
```
or restart your server.

## What servers are using this plugin?
| IP               | Location                                 | Region | Ping Offset | spike_threshold |
|------------------|------------------------------------------|--------|-------------|-----------------|
| `pvparcade.club` | Ashburn, Virginia, United States         | NA     | 20          | 30              |
| `stray.gg`       | San Francisco, California, United States | NA     | 25          | 20              |
| `eu.stray.gg`    | Limburg an der Lahn, Hesse, Germany      | EU     | 25          | 20              |
| `valed.gg`       | Frankfurt, Hesse, Germany                | EU     | 25          | 20              |
| `eu.catpvp.xyz`  |                                          | EU     | 25          | 20              |
| `as.catpvp.xyz`  |                                          | AS     | 25          | 20              |
| `na.catpvp.xyz`  |                                          | NA     | 25          | 20              |
| `hyperium.pl`    | Wrocław, Dolnośląskie, Poland            | EU     | 25          | 20              |

## Documentation
### Commands
#### **`/knockbacksync ping <player>`**
**Description:** Displays the last ping packet time for a specified player.  
**Usage:** `/knockbacksync ping <player>`  
**Permission:** `knockbacksync.ping`  
**Example:** `/knockbacksync ping Notch`  
Outputs: `Notch's last ping packet took <ping_time>ms.`

#### **`/knockbacksync reload`**
**Description:** Reloads the KnockbackSync configuration file.  
**Usage:** `/knockbacksync reload`  
**Permission:** `knockbacksync.reload`  
**Example:** `/knockbacksync reload`  
Outputs: `Successfully reloaded KnockbackSync config.` (or custom message from `reload_message` config)

#### **`/knockbacksync toggle`**
**Description:** Toggles the KnockbackSync plugin on or off.  Optional argument to toggle off only for a specific player.
**Usage:** `/knockbacksync toggle <playername>`  
**Permission:** `knockbacksync.toggle`  
**Example 1:** `/knockbacksync toggle`  
Outputs:
- Enabled: `Successfully enabled KnockbackSync.` (or custom message from `enable_message` config)
- Disabled: `Successfully disabled KnockbackSync.` (or custom message from `disable_message` config)  

**Example 2:** `/knockbacksync toggle Notch`  
  Outputs:
- Enabled: `Successfully enabled KnockbackSync for Notch` (or custom message from `enable_player_message` config)
- Disabled: `Successfully disabled KnockbackSync for NOtch` (or custom message from `disable_player_message` config)

### Event Listeners
- **`KnockbackSyncConfigReloadEvent`**: Updates messages in `reload` and `toggle` commands based on the latest configuration settings.

## License
GNU General Public License v3.0 or later

See [LICENSE](LICENSE) to see the full text.