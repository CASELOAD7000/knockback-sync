Tired of inconsistent knockback ruining your PvP experience? Our plugin recalculates knockback as if it were done clientside, leveling the playing field and ensuring every player enjoys a fair fight, no matter their connection quality.

Minecraft doesn’t factor in network latency when determining a player's actions on the server. This causes the server to receive outdated information that doesn’t reflect the player's clientside position, leading to varying knockback effects based on connection quality. 

This plugin intercepts and adjusts knockback calculations to match what would occur clientside, effectively mitigating the disadvantages caused by high latency. By synchronizing knockback handling, we ensure that players experience consistent and fair knockback, providing a balanced and competitive environment for all."

## Frequently Asked Questions (FAQ)

### Does this change put high ping players at a disadvantage?
**It depends on the player.** Some may notice a difference if they're used to relying on high ping to reduce knockback. For others, it could actually be an advantage.

### How does this change benefit high ping players?
**Knockback control.** For example, it will be easier to escape crit chains and punish crit.

### How do I change the ping offset?
You can edit the ping offset in the `config.yml` for this plugin.
```yml:
ping_offset: 25 # Change to the offset your want
```
Then just type
```
/knockbacksync reload
```
or restart your server.

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
**Description:** Toggles the KnockbackSync plugin on or off.  
**Usage:** `/knockbacksync toggle`  
**Permission:** `knockbacksync.toggle`  
**Example:** `/knockbacksync toggle`  
Outputs:
- Enabled: `Successfully enabled KnockbackSync.` (or custom message from `enable_message` config)
- Disabled: `Successfully disabled KnockbackSync.` (or custom message from `disable_message` config)

### Event Listeners
- **`KnockbackSyncConfigReloadEvent`**: Updates messages in `reload` and `toggle` commands based on the latest configuration settings.

## License
GNU General Public License v3.0 or later

See [LICENSE](LICENSE) to see the full text.