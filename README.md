Minecraft doesn’t factor in network latency when determining a player's actions on the server.
This causes the server to receive outdated information that doesn’t reflect the player's clientside position.
As a result, players take negative velocity when they're on the ground clientside, but not serverside.

This plugin handles knockback as if it were calculated clientside, ensuring that no player is at a disadvantage,
regardless of their own or their opponent’s connection.

## Frequently Asked Questions (FAQ)

### Does this change put high ping players at a disadvantage?
**It depends on the player.** Some may notice a difference if they're used to relying on high ping to reduce knockback. For others, it could actually be an advantage.

## License
GNU General Public License v3.0 or later

See [COPYING](COPYING) to see the full text.
