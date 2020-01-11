[![Codacy Badge](https://api.codacy.com/project/badge/Grade/53cb55ef384a4eeab0306ef59652b7cd)](https://www.codacy.com/manual/iYushu/EBCM?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yusshu/EBCM&amp;utm_campaign=Badge_Grade)
My new command manager slightly inspired by EngineHub/Piston
## Purpose
The purpose of the EBCM is:
  * Fix some problems of my previous command manager
  * Allow things that weren't possible on the previous version
## Current State
While the command manager is working, it's not complete yet, there are a lot of features missing from the old command manager,
like the parametric commands, and still has some bugs that will be solved with the time. But, since it's relatively functional I released it
## TODO
* [ ] ~Fix the bug that's related to subcommands and optional arguments~(not actually a bug)
* [x] Develop the parametric commands
* [x] Allow subcommands in the parametric commands
* [ ] Allow more than 1 depth of subcommands in the parametric commands 
* [x] Modify the parser so it's more maintainable and has better performance
* [ ] Refactor some things(command structure, implementations of interfaces) to make the above task easier to complete
* [x] Allow optional subcommands
* [x] Create a bukkit wrapper
* [x] Create a bungee wrapper
