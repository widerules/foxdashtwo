## This Document ##

Will attempt to breakdown the level editor for FDT into an easy to use and understand tool so that anyone can make levels/maps with quickness and ease.

## Layout ##

First lets walk around the level editor and get our barrings.

### On Screen ###

[Reference Image Here](http://i.imgur.com/brhKQ.png)

  * The Green Outlined area (top left) is the design view. This is where the map is drawn out, manipulated, and designed.
  * Yellow outline (top right) is the individual entity settings. Here objects, lights, and other entities can have their properties changed.
  * Blue outline (middle right) is the XML input/output window. XML can be copied from here and put into the FDT game.
  * Pint outline (bottom) is helpful settings pertaining to the level editor.

### Keyboard and Mouse ###

  * Arrow keys can move an object (or the camera) around
  * Delete key to remove objects
  * Click and draw can move an object (or the camera) around
  * Tab key to switch tabs in entity settings
  * Shift + tab to switch tabs backwords in entity settings

## Entity Settings in detail ##

Each tab will affect what your mouse/keyboard controls will do. Which entities you can manipulate, and generally how a level will be built.

### Level Tab Active ###

  * Control camera
  * Specify the in-game camera bounds with level limits
  * Change the background

### Player Tab Active ###

  * Control the player

### Objects Tab Active ###

Objects are defined as solid entities. Usually ground, walls, or other collision items.

  * Control objects
  * Name allows interaction with events
  * Define an object type (grass, wood, etc)
  * Define object position and z-layer

### Lights Tab Active ###

Lights are semi-transparent entities without collision that illuminate a scene. It is recommended to have at least one Ambient light in a level.

  * Control lights
  * Name allows interaction with events
  * Define a light type (ambient, spot, point, and soon pre-defined)
  * Define its position
  * Define a lights rotation/angle by degrees
  * Reach or Throw distance
  * Activity (on or off)
  * Bloom (visible to the naked eye or not)
  * Other

#### Ambient ####

A light source that affects and illuminates all objects equally.

#### Point ####

A light source that is bright in the middle and fades out along its radius in a circular pattern.

#### Spot ####

A light source that is like a directed point light, brightest in the middle, fading out along its radius in a cone pattern.

### Events Tab Detail ###

Events are invisible to the player, but once a player is in side an event, said event is triggered.

Some example events
  * Make entities active or not (based on name)
  * Teleport player to another location
  * Load the next level
  * Save scores or points

And the details
  * Control Events
  * Name allows interaction with events
  * Event type is what will happen when the event is triggered
  * Event Position, width, and length
  * Event affects are the other entities (comma separated) that will be affected by the event.

## Map XML ##

Levels for Fox Dash Two can be typed out by hand, or use another level editor. All that matters is the XML is correctly formatted.

Example map

```
<level>
  <player>
    <this_object>player</this_object>
    <id>000</id>
    <z_plane>5.0</z_plane>
    <x_pos>627</x_pos>
    <y_pos>499</y_pos>
    <active>true</active>
  </player>
  <object_list>
    <levelObject>
      <id>000x0</id>
      <this_object>A2</this_object>
      <z_plane>5</z_plane>
      <x_pos>567</x_pos>
      <y_pos>292</y_pos>
      <active>true</active>
    </levelObject>
  </object_list>
  <light_list>
    <levelLight class="com.kobaj.level.LevelAmbientLight">
      <active>true</active>
      <id>000x0</id>
      <color>-10197916</color>
    </levelLight>
  </light_list>
  <event_list>
    <levelEvent>
      <this_event>send_to_start</this_event>
      <id>000x0</id>
      <x_pos>781</x_pos>
      <y_pos>291</y_pos>
      <width>200</width>
      <height>200</height>
      <affected_object_strings>
        <String>0</String>
      </affected_object_strings>
    </levelEvent>
  </event_list>
  <right_limit>1600</right_limit>
  <bottom_limit>0</bottom_limit>
  <top_limit>750</top_limit>
  <left_limit>0</left_limit>
</level>
```