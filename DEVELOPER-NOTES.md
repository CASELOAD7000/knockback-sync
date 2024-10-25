Project Structure

- `common` submodule contains shared code for all platforms.
  - It contains the interfaces and abstractions for
    - Permissions
    - Scheduling
    - Players
    - Blocks
    - World
    - The Server
  - In addition to the interfaces, the code also contains the implementations for
    - Commands
      - Commands use Brigadier with Reflection to handle version differences. Simply call our command code in common by registering KnockBackSyncCommand.build() and implementing CommandOperations
    - Handling Events
    - Gathering Statistics
- `fabric` contains code for latest fabric version (currently 1.21)
- `bukkit` contains bukkit-specific code

If you want to add support for a new platform, simply make a new submodule and implement the interfaces for.
- Scheduling Tasks
- Checking for Permissions
- Listening for events and calling the right handler
- Players + Blocks + World + Server

You will need to handle events when Players:
- join/leave
- are damaged
- take velocity (knock back)

If you find your platform is lacking events, take a look at how the PlayerVelocityEvent is implemented fabric for some direction of what to do


## List of version and platform-specific caveats
- Runnable and manual ping does not work on fabric right now due b/c packetevents. Ping measurement will be less accurate

- Command System
  - context.getSource().sendSuccess() takes a Supplier<Component> in > 1.19.4
  - context.getSource().sendSuccess() takes a Component in <= 1.19.4 
  - Component.literal() does not exist in <= 1.18.2