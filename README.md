# EntityCompass
Minecraft compass tracking plugin capable of tracking players and items

Modrinth page: 

https://modrinth.com/plugin/entitycompass







# TeamsCompass has been completely rewritten to become EntityCompass. Now accommodating players AND ITEMS.

Using the knowledge for the first iteration of this plugin I have made it much more robust and maintainable.


### Compass controls: 

Right-Click

Shift+ Right-Click


### Terminology

TrackedEntity - instance of an entity (players or items) that is currently on an entity list aka "Tracked"

EntityList - a list of TrackedEntities that has a custom name to organize teams or categories.



### Commands:

/EntityCompass

Alias: /e



/EntityCompass create [name] ----- creates a new "EntityList"

/EntityCompass join [EntityList name] ----- joins team of specifed name

/EntityCompass leave ----- remove yourself from the Entitylist you're on

/EntityCompass additem [EntityList name]  ----- adds the item in your main hand to specified EntityList

/EntityCompass add [Player name] ----- adds player to specified EntityList

/EntityCompass remove [TrackedEntity name] ----- remove TrackedEntity by name from its list

/EntityCompass list ----- shows all the EntityList and the name of each TrackedEntity on them


experimental gamemode where you have to throw an item into the end portal after beating the dragon

/EntityCompass relicrush start

/EntityCompass relicrush end


## Features

• Item tracking (first plugin to do this afaik)

• Compass tracking through dimensions

• Compass works in all dimensions (doesn't just switch to coordinates in the nether)

• Tracked Items are indestructible (until they aren't tracked anymore)



## Future goals

• Add a ton of configuration settings for the plugin

• fix a bunch of edge cases that break tracking

• Automatically remove tracking on unreachable items

• Add optional GUI for large amounts of entity lists and entities

• Add support for mob tracking


# Submit issues or requests on the GitHub "issues" page tagged appropriately.

## Also tell me if it works properly on older versions and I will add them to the support list (On github issues page).
