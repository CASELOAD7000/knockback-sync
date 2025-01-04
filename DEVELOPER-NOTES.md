## Project Structure

- `common` submodule contains shared code for all platforms.
  - It contains the interfaces and abstractions for
    - Permissions
    - Scheduling
    - Players
    - Blocks
    - Worlds/Levels
    - The Server
  - In addition to the interfaces, the code also contains the implementations for
    - Commands
      - Commands use incendio's `cloud` command system. All you have to do to add platform support is to create a CommandManager and call register(). 
    - Handling Events
    - Gathering Statistics
- `fabric` contains code for latest fabric version (currently 1.21)
- `bukkit` contains bukkit-specific code

If you want to add support for a new platform, simply make a new submodule and implement the interfaces for.
- Scheduling Tasks
- Checking for Permissions
- Listening for events and calling the right handler
- Players + Blocks + World + Server
- Metrics

You will need to handle events when Players:
- join/leave
- are damaged
- take velocity (knock back)

If you find your platform is lacking events, take a look at how the PlayerVelocityEvent is implemented fabric for some direction of what to do


## List of version and platform-specific caveats
- Runnable and manual ping does not work on fabric right now due b/c packetevents. Ping measurement will be less accurate