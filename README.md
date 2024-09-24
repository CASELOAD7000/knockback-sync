Minecraft doesn’t factor in network latency when determining a player's actions on the server.
This causes the server to receive outdated information that doesn’t reflect the player's clientside position.
As a result, players take negative velocity when they're on the ground clientside, but not serverside.

This plugin handles knockback as if it were calculated clientside, ensuring that no player is at a disadvantage,
regardless of their own or their opponent’s connection.

## Frequently Asked Questions (FAQ)

### Test
Test

## License
GNU General Public License v3.0 or later

See [COPYING](COPYING) to see the full text.
