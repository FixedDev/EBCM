# EBCM [![Codacy Badge](https://api.codacy.com/project/badge/Grade/78392a0229da4390a2069ab0efc24534)](https://www.codacy.com/manual/FixedDev/EBCM?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FixedDev/EBCM&amp;utm_campaign=Badge_Grade)
My new command manager slightly inspired by EngineHub/Piston
## Purpose
The purpose of the EBCM is:
  * Fix some problems of my previous command manager
  * Allow things that weren't possible on the previous version
## Current State
While the command manager is working, it's not complete yet, there are a lot of features missing from the old command manager,
 and still has some bugs that will be solved with the time, but, since it's relatively functional I released it
## TODO
* [ ] ~Fix the bug that's related to subcommands and optional arguments~(not actually a bug)
* [x] Develop the parametric commands
* [x] Allow subcommands in the parametric commands
* [x] Allow more than 1 depth of subcommands in the parametric commands 
* [x] Allow parent commands parameters to be used on the subcommands 
* [x] Modify the parser so it's more maintainable and has better performance
* [ ] Refactor some things(command structure, implementations of interfaces) to make the above task easier to complete
* [x] Allow optional subcommands
* [x] Create a bukkit wrapper
* [x] Create a bungee wrapper

## Usage
The command manager actually has 2 types of commands.
The basic commands and the parametric commands, the basic commands are commands created using a CommandBuilder in which you build every argument and later you specify an action for that command. The Parametric Commands are created by annotations on methods which specify certain needed things.
### Common
There's a common thing that you need to do for the 2 types of commands.
Initialize a CommandManager instance!
Here's how you do it
```java
CommandManager manager = new SimpleCommandManager(); // There are more constructors for this but, they're for other things
```
This automatically creates all the needed objects and gives you a ready to work instance, but, for more complex scenarios like using the wrapper for bukkit, or registering ParameterProvider's you need to create the objects manually, like this.
```java
Authorizer authorizer = new BukkitAuthorizer();
ParameterProviderRegistry providerRegistry = ParameterProviderRegistry.createRegistry();
Messager messager = new BukkitMessager();

CommandManager commandManager = new SimpleCommandManager(authorizer, messager, providerRegistry);

```
### Basic Commands
To create a basic command you need to use the ImmutableCommand.Builder class or the MutableCommand.Builder class, here an example
```java
Command command = ImmutableCommand.builder(CommandData.builder("yourcommand"))
                                       .addPart(ArgumentPart.builder("argument1", String.class).build())
                                       .setAction(context -> {
                                           List<CommandPart> parts = context.getParts("argument1");
                                           Optional<String> value = context.getValue(parts.get(0));
                                           
                                           System.out.println(value.orElse("Missing value!"));
                                       }).build();
```
And you register it using the `registerCommand(Command)` method of the CommandManager
```java
commandManager.registerCommand(command);
```
### ParametricCommands
Creating a ParametricCommand is easy but, you need to create a new object to create them
```java
ParametricCommandBuilder builder = new ReflectionParametricCommandBuilder();
```
It only has 2 methods: `fromClass` and `fromMethod`. You will need the method `fromClass` almost everytime that you use this command manager.
The first method accepts an instance of an instance of an interface called `CommandClass` which doesn't has any method, it's just a marking interface, there you can add your commands and they will be converted to normal commands when the method is run.
Here an example of how to create a command with the `CommandClass`
```java
class TestCommand implements CommandClass {
  @ACommand(names = "yourcommand")
  public boolean test(@Default("Missing Value!") @Named("argument1") String argument1) {
    System.out.println(argument1);
  }
}
```
When you have your instance of CommandClass created, you can use the `ParametricCommandBuilder` to convert it
```java
List<Command> commands = builder.fromClass(new TestCommand());
```
Wait, you may ask. Why does the method return a list of commands?
Well, that's because on the same CommandClass you can have multiple commands and even subcommands 
```java
class TestCommands implements CommandClass {
  @ACommand(names = "command1")
  public boolean test(@Default("Missing Value!") @Named("argument1") String argument1) {
    System.out.println("Command1");
    System.out.println(argument1);
  }

  @ACommand(names = "command2")
  public boolean test(@Default("Missing Value!") @Named("argument1") String argument1) {
    System.out.println("Command2");
    System.out.println(argument1);
  }
}
```
Now, how you register that List of commands returned? Well, like any other command, they're not special instances so you can register them easily
```java
commandManager.registerCommands(commands);
```
Also, you see that ugly `@Named` annotation? Well, using java8 `-parameters` compiler flag you can get rid of it!
### Finally
There are lots of things that you can do, since the CommandManager was created to be really flexible and it will be updated to be better. At the moment it doesn't has a wiki but, soon it will be created, to make the experience of using it easier
