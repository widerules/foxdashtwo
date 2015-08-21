# Introduction #
Below should summarize the plan of attack for Fox Dash Two. Including important dates, goals, and any other details.

# Description #
Fox Dash Two is much like Fox Dash which is much like Canabalt. A continuous running game where the player must jump to avoid hitting obstacles and thus starting over.

## Mechanics ##
The screen will scroll from ~left to right~ right to left depending on the orientation of the device. The fox will run continuously and jump when a finger touches the screen.

## Bad Guys ##
  * Black smoke (give it a name?)
  * Bad pickups that slow you down
  * Pits/jumps
## Good Guys ##
  * Level ups
  * stars much like the first game
  * additional pickups that make you go faster

# Details #

Specific details about Fox Dash Two.

## New ##
Fox Dash Two is running on OpenGL 2.0ES rather than canvas as the first Fox Dash did.

Input and control will be more flexible using an innovative interface.

## Goals ##
Personal goals for Fox Dash Two include
  * Updated Graphics
  * New Music and More Sounds
  * In-Depth Story
  * More Efficient Code
  * Tablet support

## Ideas ##
Some neat ideas that Fox Dash Two could have
  * Multiplayer
  * Visual Effects (Think lights/particles/etc)
  * Constructive/destructive environments
  * Tutorial?

## Schedule ##
May 29 2012 (day 1)
  * Prepare Google Code
  * Upload first chunk of code
  * Detail plan

July 23 2012
  * Base of engine complete

Sept 24 2012
  * Reorganize and re-evaluate goals
  * Story completed
  * Created Trello for the rest of development cycle.

Nov 10 2012
  * Hired all team members
    * Artist (Patrick)
    * Levels (Brady)
    * Music (Fredrik)
    * Sound (Lonzo)
    * Story (Kevin)

## Engine ##
The Engine will be broken down into X number of Parts.


**Audio**
  * Holds audio properties (length/speed/volume)
  * Contains state (playing/paused/etc)
  * Split into two containers
    * Music
    * Sound

**Setup/Activity**
  * Handles loading of the game from cold boot
  * Closing the game
  * Minimizing the game, including saving states
  * Reopening the game (warm boot)
  * Setting/preparing/handling pause states (incoming call for example)

**Input**
  * Manages button presses
  * Including volume/home/etc keys

**Level**
  * Manages levels
  * Manages level objects and lights and sounds and music

**Loader**
  * Loads in all assets
  * Stores all assets properly
  * Destroys assets appropriately
  * Handles reading in strings as well
  * Contains file loading and IO through XML

**Math/Helper**
  * Contains useful methods (linear interpolate)
  * Stores static variables (screen orientation/DPI)
  * FPS Manager is here too (since it is 99% math)
  * Physics are part of this

**Message**
  * Contains the Toast Manager
  * Contains the Dialog Manager

**OpenGL**
  * Contains the Game (onUpdate, onDraw)
  * Contains the renderer

**OpenGL Drawable**
  * Things that can be drawn by Open GL
  * Text, Quads (sprites), Particles, etc

**OpenGL Graphics**
  * Contains classes that visually effect Drawables
  * Lights

**Screens**
  * Each screen will handle a set of levels

**Networking (Not Implemented/Optional)**
  * Manages connection to network/servers
  * Handles sending and receiving of data

**AD Manager**
  * JUST KIDDING
  * Fox Dash will NEVER have ads! :)

## Code Practices ##
Variables will be all lower-case words separated by underscores.
```
int my_int;
```
Methods first word lower-case, all other words camel-case
```
void onUpdate();
```
Files /should/ be all camel-case.
```
MyFile.png
```
  * Everything else is fairgame.
  * It is a wacky system, but it is better than nothing.