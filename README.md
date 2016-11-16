# JohnnyScript
This is a Java based compiler used to compile files for the [JohnnySimulator](https://sourceforge.net/projects/johnnysimulator/) by Peter Dauscher which aims to simulate a simplified von Neumann computer but lacks a real assembly language.

## How to install
Simply download the **JohnnyScript.java** file from the src folder and compile it as you would with any other java program.
```
javac JohnnyScript.java
```

## Usage
### Compilation
Just call the compiled program with the path to the source file as an argument
```
java JohnnyScript sourcefile.jns
```


### Syntax
#### Instructions
How to use instructions:

> __instruction__ *value*

Default JohnnySimulator macro instructions are supported:

* TAKE
* ADD
* SUB
* SAVE
* JMP
* TST
* INC
* DEC
* NULL
* HLT

Please refer to the JohnnySimulator documentation to find out what the purpose of those instructions is.

Valid values are addresses, variables and for the JMP instruction jump points (see below).

Instructions are not case-sensitive

#### Comments
JohnnyScript can be commented with two leading slashes "//" like so:
> __//__ This is a comment

#### Variables
How to define a variable:

> __#__*name integer*

How to reference a variable:

> __instruction #__*name*

The variable can be named any way you like but use of special characters is discouraged.

Variable names have to be unique

Variables can contain an integer ranging from 0 to 999 (JohnnySimulator limitation).

Variable names are case-sensitive

#### Jump points
How to define a jump point:

> __:__*name*

How to jump to this point:

> __JMP__ *name*

Similar to variables, the jump points can also be named in any way and have to be unique. The name is also case-sensitive

### Info
The compiled ram code is split into variables and instructions with all variables at the beginning of the code.
The compiler automatically generates a line zero which is used to jump over all variables to the first instruction.
