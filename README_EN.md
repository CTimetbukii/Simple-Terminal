<div align="center">

# Simple-Terminal

☕ A lightweight, simple, and easy-to-use Java terminal library

🥰 Driven by annotations, equip your application with a full command-line interaction layer in minimal code

</div>

English | [简体中文](README.md)

## Features

- Annotation-driven: **annotate a class/method to register a command**. Simple-Terminal automatically parses user input, dispatches commands, and resolves method parameters. **Say goodbye to hand-written argument parsing**
- Automatic parameter conversion: **user input strings are automatically converted to the parameter types your methods expect** — no more manual try-catch
- Regex support: **validate user input parameters with regular expressions**, letting Simple-Terminal handle 90% of your if-checks
- Subcommands: designate a string as a subcommand; it is automatically stripped from the arguments passed to the handler method
- Context commands: supports commands that need multiple rounds of dialog, plus **context jumping to implement different processing branches**
- Built-in `help` and `stop`: **works out of the box**, and can be replaced with your own implementations
- Internationalization (i18n): automatically loads the matching properties description file based on the system language. **Combined with the built-in help command, command help is effortless** — you just provide the i18n resources, Simple-Terminal does the rest
- Custom input/output streams: **plug in network I/O streams to build a remote terminal**
- Injected parameters: mark fields with `@InjectOutPrintStream` and `@InjectErrPrintStream` to obtain the standard output `PrintStream` and error `PrintStream`, avoiding hardcoded `System.out` / `System.err` and freely connecting network output streams
- Command aliases: assign multiple **aliases** to a command and invoke it however you like
- Graceful resource shutdown: any command processor that holds resources needing release on terminal shutdown just implements the `ClosableFlag` interface and its `closeResource` method. The built-in `stop` command invokes it on exit, **closing resources gracefully**
- 4 parameter conversion strategies: powered by [ElegantConvertChecker](https://github.com/CTimetbukii/ElegantConvertChecker), freely converting user input into method parameters via 4 different strategies. You can also **customize boolean markers** — Y/y/T/t/1/yes, any string you want can be converted to `true` as a method argument
- Minimal usage: chain calls on `TerminalBuilder` to configure a `Terminal` object step by step, then call `run`. **It couldn't be simpler**
- Asynchronous execution: run the terminal listener on a dedicated thread or a custom thread pool
- Resident interactive: Simple-Terminal is a resident interactive console — you don't need to write your own loop, it's resident by nature

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Core Concepts](#core-concepts)
- [Annotations](#annotations)
- [Command Processors](#command-processors)
- [Subcommands](#subcommands)
- [Context Commands](#context-commands)
- [Parameter Validation & Conversion](#parameter-validation--conversion)
- [Field Injection](#field-injection)
- [Built-in Commands](#built-in-commands)
- [Internationalization](#internationalization)
- [Async & Custom Streams](#async--custom-streams)
- [API Reference (Per-Class)](#api-reference-per-class)
- [License](#license)

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.ctimetbukii</groupId>
    <artifactId>simple-terminal</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.ctimetbukii:simple-terminal:1.0.0'
```

> The core module of Simple-Terminal targets Java 1.8.

## Quick Start

The minimal usage — only two lines to launch a terminal with built-in `help` and `stop` commands:

```java
import io.github.ctimetbukii.simpleterminal.Terminal;
import io.github.ctimetbukii.simpleterminal.TerminalBuilder;

public class SimplestDemo {
    public static void main(String[] args) {
        Terminal terminal = TerminalBuilder.builder().build();
        terminal.run();
    }
}
```

Once running, type `help` to list available commands and `stop` to exit the process.

Registering your own commands is just as easy — define a processor class and tell Simple-Terminal which package to scan via an anchor class. Command processor classes must reside in the same package:

```java
// 1. Define a command processor
@CommandProcessor(value = "info", descriptionKey = "cmd.info")
public class InfoCommandProcessor {

    @CommandExecutor(descriptionKey = "cmd.info.default")
    public void info() {
        System.out.println("Simple-Terminal Demo");
    }

    @CommandExecutor(descriptionKey = "cmd.info.greet")
    public void greet(String name) {
        System.out.println("Hello, " + name + "!");
    }
}

// 2. Launch the terminal, specifying the scan package via an anchor class
Terminal terminal = TerminalBuilder.builder()
        .setAnchorPackageClassHelper(InfoCommandProcessor.class) // other processor classes must be in the same package as InfoCommandProcessor
        .build();
terminal.run();
```

Now `info` prints `Simple-Terminal Demo`, and `info CTimet` prints `Hello, CTimet!`.

## Core Concepts

At runtime, Simple-Terminal reflectively scans the package containing the anchor class you specify, finds every class annotated with `@CommandProcessor`, and registers it. Each line of user input is split by spaces; the first segment is treated as the command name (or an alias), and the remaining segments are passed as arguments to the matched executor method.

A command processor class may contain the following kinds of executor methods:

| Executor annotation | Purpose |
| --- | --- |
| `@CommandExecutor` | Regular command executor, handles input like `cmd arg1 arg2` |
| `@SubcommandExecutor` | Subcommand executor, handles input like `cmd -s arg1`. The `-s` is not included in the arguments passed to the method.<br/>Any string without spaces can be set as a subcommand |
| `@ContextCommandExecutor` | Context executor for multi-round commands, distinguished by `stage` |
| `@ArgsIllegalExecutor` | Fallback method triggered when no executor matches the given arguments |

## Annotations

### `@CommandProcessor` (class level)

Marks a class as the handler for a command.

| Attribute | Type | Default | Description |
| --- | --- | --- | --- |
| `value` | `String` | — | Command name (required) |
| `alias` | `String[]` | `{}` | Command aliases |
| `matchAllCases` | `boolean` | `true` | Whether to also match mixed-case command names |
| `hasContext` | `boolean` | `false` | Whether the command has a context (multi-round dialog) |
| `descriptionKey` | `String` | `""` | Properties key for the description shown in help.<br/>Simple-Terminal uses this descriptionKey to look up the command's description from the i18n resource file; the help command prints it |

### `@CommandExecutor` (method level)

Marks a regular command executor method.

| Attribute | Type | Default | Description |
| --- | --- | --- | --- |
| `patterns` | `String[]` | `{}` | Per-position regex constraints for arguments |
| `paraJoin` | `boolean` | `false` | Whether to join all arguments with spaces into a single string |
| `paraTransformMethod` | `ArgsTypeTransformMethod` | `COERCE_WITH_INTERPRET` | Parameter type conversion strategy |
| `descriptionKey` | `String` | `""` | Description key for this executor |

When the user enters a command, Simple-Terminal compares the different `@CommandExecutor`-annotated methods in turn, checking their parameter types, regex constraints, and so on. For example:

```java
@CommandProcessor("hello")
public class HelloCommandProcessor {

    @InjectOutPrintStream
    PrintStream out;

    @CommandExecutor
    public void hello() {
        out.println("hello");
    }

    @CommandExecutor
    public void helloUser(String user) {
        out.println("hello " + user);
    }

    @CommandExecutor
    public void helloId(int id) {
        out.println(id);
    }
}
```

In this example, if you run the command `hello`, the console prints `hello` — the `hello` method is triggered. If you enter `hello a`, the console prints `hello a` — the `helloUser` method is triggered. If you enter `hello 1`, the console prints `1` — the `helloId` method is triggered. Simple-Terminal automatically finds and calls the appropriate method based on the types of the arguments the user passes.

You can also include multiple parameters in the method signature. For example:

```java
@CommandExecutor
public void helloId(int id1, int id2) {
    out.println(id1);
    out.println(id2);
}
```

Now running `hello 1 2` prints `1` then `2`. Argument 1 is parsed as `id1`, argument 2 as `id2`. The order and types of the method parameters match the order and types of the command arguments.

You can also constrain user-supplied arguments with regular expressions:

```java
@CommandExecutor(patterns={"[0-9]", "[0-9]"})
public void helloId(int id1, int id2) {
    out.println(id1);
    out.println(id2);
}
```

Now, when using the `hello` command, the arguments can only be single digits 0–9. If you run `hello 100 100` (an argument that fails the regex constraint) or `hello a b` (an argument that fails the type constraint), Simple-Terminal cannot find a suitable method to call during parsing and considers the command arguments illegal. At this point Simple-Terminal looks for a method annotated with `@ArgsIllegalExecutor`. If one exists, it is invoked. For example:

```java
@CommandExecutor(patterns={"[0-9]", "[0-9]"})
public void helloId(int id1, int id2) {
    out.println(id1);
    out.println(id2);
}

@ArgsIllegalExecutor
public void fail() {
    out.println("Illegal arguments!");
}
```

Now running `hello 100 100` prints `Illegal arguments!`.

A method annotated with `@ArgsIllegalExecutor` may take no parameters or a single parameter. When it has a parameter, it must be exactly one `String[]`, into which Simple-Terminal passes the command arguments. For example:

```java
@CommandExecutor(patterns={"[0-9]", "[0-9]"})
public void helloId(int id1, int id2) {
    out.println(id1);
    out.println(id2);
}

@ArgsIllegalExecutor
public void fail(String[] args) {
    out.println(Arrays.toString(args));
}
```

Now running `hello 100 100` prints `[100, 100]`.

### `@SubcommandExecutor` (method level)

Marks a subcommand executor method. Similar to `@CommandExecutor`, but additionally has `value` (subcommand name), `alias` (aliases), and `matchAllCases` (whether to also match case, default `true`).

For a method marked as a subcommand, the subcommand token is no longer passed as an argument. For example:

```java
@CommandProcessor("info")
public class InfoCommandProcessor {
    @InjectOutPrintStream
    private PrintStream out;

    @CommandExecutor
    public void info1() {
        out.println("info");
    }

    @CommandExecutor
    public void info2(String a) {
        out.println("info " + a);
    }

    @CommandExecutor
    public void info3(String a, String b) {
        out.println("info " + a + " " + b);
    }

    @SubcommandExecutor("-r")
    public void info4(String a) {
        out.println(a);
    }
}
```

Now running `info -r 1` calls `info4` with `a="1"`. The subcommand `-r` is not passed as an argument. The console outputs `1`.

When subcommands are present, Simple-Terminal tries the subcommand channel first, so `info2` is not triggered here. When you run `info -s 1`, since `-s` is not a registered subcommand, Simple-Terminal falls back to looking for a matching `@CommandExecutor`, and `info2` is triggered with `a="-s"`, `b="1"`.

Any string without spaces can serve as a subcommand — it doesn't have to start with `-`. For example:

```java
@SubcommandExecutor("at")
public void info4(String a) {
    out.println(a);
}
```

Now `at` is registered as a subcommand. Running `info at 1` triggers `info4` with `a="1"`.

You can also configure aliases for a subcommand with `alias`, e.g.:

```java
@SubcommandExecutor(value="at", alias={"@","a"})
public void info4(String a) {
    out.println(a);
}
```

Now `info @ 1` and `info a 1` also trigger `info4`.

By default, `matchAllCases` is `true`, meaning both upper and lower case are accepted. In the `info at` example above, `info AT` also triggers `info4`. However, aliases are not case-insensitive: if the alias is `at`, then `AT` will not trigger.

You can set `matchAllCases` to `false` manually, in which case the subcommand must be entered exactly. For example:

```java
@SubcommandExecutor(value="at", alias={"@","a"}, matchAllCases=false)
public void info4(String a) {
    out.println(a);
}
```

Now `info AT` no longer triggers `info4`.


### `@ContextCommandExecutor` (method level)

Marks a handler for a specific round of a context command.

| Attribute | Type | Default | Description |
| --- | --- | --- | --- |
| `stage` | `int` | — | Which round of input. `0` is the initial input; `1` and above are subsequent context inputs |
| `patterns` | `String[]` | `{}` | Argument regex constraints |
| `paraJoin` | `boolean` | `false` | Whether to join arguments |
| `paraTransformMethod` | `ArgsTypeTransformMethod` | `COERCE_WITH_INTERPRET` | Parameter conversion strategy |

> Note: do not put both `@CommandExecutor` and `@ContextCommandExecutor(stage = 0)` on the same method. The method may be invoked twice, and the arguments passed each time are different.

`ContextCommandExecutor` marks commands that require multiple rounds of user input to complete. Each round of input carries context information, hence the name "context command".

Compared to the previous two annotations, `ContextCommandExecutor` has one unique parameter: `stage`, which indicates which round of context input it handles. Do not use `@ContextCommandExecutor(stage=0)` for the initial input — use `@CommandExecutor` instead. The two should not be shared. Here is an example of a context command:

```java
package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.*;
import io.github.ctimetbukii.simpleterminal.process.transform.ArgsTypeTransformMethod;

import java.io.PrintStream;
import java.util.Arrays;


@CommandProcessor(value = "send", matchAllCases = false, hasContext = true)
public class SendContextCommandProcessor {
    @InjectOutPrintStream
    private PrintStream out;

    @CommandExecutor
    public void send(String content) {
        out.println("Send '" + content + "' to whom?");
    }

    @ContextCommandExecutor(stage = 1, paraJoin = true) //paraJoin = true handles usernames containing spaces
    public void acceptUser(String user) {
        out.println("Send to '" + user + "' done.");
        out.println("Your feeling rate: ");
    }

    @ContextCommandExecutor(stage = 2)
    public void acceptUserFeelingRate(int rate) {
        out.println("Rate = " + rate);
        out.println("Your feeling float rate:");
    }

    @ContextCommandExecutor(stage = 3, paraTransformMethod = ArgsTypeTransformMethod.CONVERT) //in this mode, entering a value like 1.111111111111111 that exceeds float precision is reported as illegal
    public void acceptUserFeelingFloatRate(float rate) {
        out.println("F Rate = " + rate);
        out.println("Your feeling float rate 2:");
    }

    @ContextCommandExecutor(stage = 4) //in this round, entering 1.1111111111111111 is not illegal; instead float=1.1112 is passed in — the result of coercion
    public void acceptUserFeelingFloatRate2(float rate) {
        out.println("F Rate = " + rate);
    }

    @ArgsIllegalExecutor
    public void fail(String[] args) {
        out.println("Illegal arguments");
        out.println(Arrays.toString(args));
    }
}

```

This sample is included in SimpleTerminal.Demo. It demonstrates how to use the `ContextCommandExecutor` annotation to handle multi-round user input. Here is the console output:

```bash
> send A                         //triggers the method marked with CommandExecutor
Send 'A' to whom?
> CTimet bukii                   //triggers the method marked with stage=1
Send to 'CTimet bukii' done.
Your feeling rate: 
> 10                             //stage=2, the second round of user input
Rate = 10
Your feeling float rate:
> 10.0                           //stage=3
F Rate = 10.0
Your feeling float rate 2:
> 21                             //stage=4
F Rate = 21.0
>                                //the send command ends; subsequent input is no longer parsed as send's context
```

Sometimes you may want jumps between inputs. For example, when a password exists, ask the user for it; when it doesn't, skip that step. `@ContextCommandExecutor` supports this too — we call it context jumping.

Implementing a context jump is very simple: just have the method annotated with `@ContextCommandExecutor` return an `int`. Here is an example:

```java
package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.ContextCommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.InjectOutPrintStream;
import io.github.ctimetbukii.simpleterminal.exceptions.NeedRepeatException;

import java.io.PrintStream;

@CommandProcessor(value = "repeat", hasContext = true)
public class RepeatCommandProcessor { //this processor does not implement DescriptionFlag; the default help command won't show its description
    @InjectOutPrintStream
    private PrintStream out;

    @CommandExecutor
    public void repeat(String arg) {
        out.println("arg = " + arg);
        if ("true".equals(arg)) {
            out.println("Repeat?");
        }
    }

    @ContextCommandExecutor(stage = 1)
    public void repeat1(boolean repeat) {
        if (repeat) {
            out.println("Repeated. Again?"); 
            throw new NeedRepeatException();  //when NeedRepeatException is thrown inside a method, the next round of input re-enters the current stage. Only NeedRepeatException does this; other exceptions are reported as normal errors
        }
        out.println("Stop. Jump to which stage OR try fail repeat by entering true/false");
    }

    @ContextCommandExecutor(stage = 2)
    public void failedRepeat2(boolean repeat) {
        if (repeat) {
            out.println("Failed repeat example, throw not NeedRepeatException");
            throw new RuntimeException(); // the console reports an error, equivalent to command failure. Re-entering will no longer hit this method
        }
        out.println("Meow@");
    }

    @ContextCommandExecutor(stage = 2, patterns = "^[1-4]\\d*$") //patterns restricts jumpTo to positive integers 1-4
    public int jumpTo(int jumpTo) {
        out.println("Jump to " + jumpTo + ".");
        out.println("Type stage " +jumpTo + "'s args:");
        return jumpTo;
    }

    @ContextCommandExecutor(stage = 3)
    public void stage3(String c) {
        out.println("Stage 3");
    }

    @ContextCommandExecutor(stage = 4)
    public void stage4(String c) {
        out.println("Stage 4");
    }
}

```

This sample is also in SimpleTerminal.Demo. The `jumpTo` method demonstrates how to perform a context jump — simply return an `int`; the returned value is used as the stage, so the next round of user input is captured by that stage's method. Here is the console output:

```bash
> repeat 1                                                              //captured by repeat, handled in CommandExecutor
arg = 1
> false                                                                 //captured by the method marked stage=1, i.e. repeat1
Stop. Jump to which stage OR try fail repeat by entering true/false
> 4                                                                     //captured by the method marked stage=2, entering jumpTo
Jump to 4.
Type stage 4's args:                                                    //jumpTo returns 4
> c                                                                     //so this input is captured by the method marked stage=4
Stage 4                                                                 //stage4 executed successfully
> 
```

The example above also demonstrates how to make a stage repeat — throw `NeedRepeatException`. For instance, when the user enters a wrong password, we may want them to re-enter it, which requires repeating the current stage's input. Just throw `NeedRepeatException` and Simple-Terminal points the next round of input back to the same stage. Here is the console output:

```bash
> repeat true                                                           //captured by repeat, handled in CommandExecutor
arg = true
Repeat?
> true                                                                  //captured by stage=1's method; in stage=1, if the user enters true, NeedRepeatException is thrown, so the next input still enters stage=1
Repeated. Again?
> true
Repeated. Again?
> true
Repeated. Again?
> 
```

Sometimes you may want to exit a stage method midway. For example, if the user enters the wrong password, you want to exit the command immediately and skip the remaining rounds. In that case, throw `ContextInterruptException` and the command exits naturally.


### `@ArgsIllegalExecutor` (method level)

Triggered when the given arguments match no registered method. May take no parameters, or a single `String[]` parameter. When it accepts a `String[]`, the user's input arguments are passed in.

## Command Processors

A complete command processor example:

```java
@CommandProcessor(value = "greet", alias = {"hi", "hello"}, descriptionKey = "cmd.greet")
public class GreetCommandProcessor {

    @CommandExecutor(descriptionKey = "cmd.greet.default")
    public void greet(String name) {
        System.out.println("Hello, " + name + "!");
    }

    @ArgsIllegalExecutor
    public void onIllegal(String[] args) {
        System.err.println("Illegal arguments: " + Arrays.toString(args));
    }
}
```

- `alias` lets you configure multiple aliases for a command; typing an alias is equivalent to typing the formal command name.
- When `matchAllCases = false`, only a command name with exactly matching case will trigger.
- Aliases must not conflict with any registered formal command name, otherwise an `AliasConflictWithCommandException` is thrown at startup.

## Subcommands

Subcommands are useful when a single command has multiple sub-options, such as the built-in `stop` command:

```java
@CommandProcessor(value = "stop", descriptionKey = "simpleterminal.defaults.stop")
public class DefaultStopCommandProcessor {

    @CommandExecutor(descriptionKey = "simpleterminal.defaults.stop.normal")
    public void stop() {
        // stop immediately
    }

    @SubcommandExecutor(value = "-s", descriptionKey = "simpleterminal.defaults.stop.secs")
    public void stopAfterSecs(@ParaDsKey("simpleterminal.defaults.stop.para.secsDelay") long secsDelay) {
        // stop after the given number of seconds
    }
}
```

Typing `stop` stops immediately; typing `stop -s 2` stops after 2 seconds. When the first argument hits a registered subcommand, the subcommand channel is used; otherwise it falls back to the regular `@CommandExecutor`.

## Context Commands

Context commands are for scenarios that need multiple rounds of dialog. For example, a `send` command that first takes a message and then asks who to send it to:

```java
@CommandProcessor(value = "send", hasContext = true, descriptionKey = "cmd.send")
public class SendCommandProcessor {

    // stage = 0: initial input; args contains the top command, i.e. {"send", "hello"}
    @ContextCommandExecutor(stage = 0)
    public void start(String[] args) {
        System.out.println("To whom?");
    }

    // stage = 1: first context input; args is the user's follow-up input
    @ContextCommandExecutor(stage = 1, paraJoin = true)
    public void finish(String target) {
        System.out.println("Send '" + args[1] + "' to " + target + ", done.");
    }
}
```

Interaction:

```
> send hello
To whom?
> CTimet bukii
Send 'hello' to CTimet bukii, done.
```

Advanced controls:

- **Repeat the current round**: throw a `NeedRepeatException` inside the executor; the current `contextTimes` is rolled back so the user can re-enter.
- **Jump to a specific stage**: have the executor return an `int`; Simple-Terminal jumps the context to that stage (must be greater than 0), enabling branching multi-round dialogs.
- **Early termination**: have the executor throw `ContextInterruptException`, and the context command exits naturally.

## Parameter Validation & Conversion

### Argument regex constraints `patterns`

`patterns` is a positional regex array that validates each argument in turn. For example, the date constraints on the built-in `stop -r`:

```java
@SubcommandExecutor(value = "-r",
        patterns = {"-?\\d+", "^(0[1-9]|1[0-2])$", "^(0[1-9]|[12][0-9]|3[01])$"})
public void stopAtRealTime(int targetYear, int targetMon, int targetDay) { ... }
```

### Argument joining `paraJoin`

When user input needs to contain spaces (e.g. a full sentence), enable `paraJoin` to join all arguments with spaces into a single string. When enabled, the method parameter type must be a single `String`.

### Parameter type conversion `paraTransformMethod`

Simple-Terminal relies on [elegant-convert-checker](https://github.com/CTimet/elegant-convert-checker) under the hood to convert strings into target types, offering four strategies:

| Strategy | Description |
| --- | --- |
| `CONVERT` | Strict conversion; every argument must strictly conform to the target type's format |
| `COERCE` | Coerced conversion; tries to fit the string into the target type |
| `INTERPRET` | Interpretive conversion; the string is semantically interpreted before conversion |
| `COERCE_WITH_INTERPRET` | Default strategy; tries interpretation first, then falls back to coercion |

By leveraging method overloading with different `patterns`, you can register multiple signatures for the same command, and Simple-Terminal will automatically match the most appropriate one based on the input.

## Field Injection

Simple-Terminal can automatically inject runtime objects into fields of a command processor class, removing the need for manual wiring:

| Annotation | Injected value |
| --- | --- |
| `@InjectOutPrintStream` | Terminal standard output stream (defaults to `System.out`) |
| `@InjectErrPrintStream` | Terminal error output stream (defaults to `System.err`) |
| `@InjectTerminalOption` | The current `TerminalOptions` instance |
| `@InjectDefaultDescriptionProperties` | `Properties` of built-in command descriptions |
| `@InjectCommandDescriptionProperties` | `Properties` of user-defined command descriptions (auto-loaded by language) |

Example:

```java
@CommandProcessor(value = "echo")
public class EchoCommandProcessor {
    @InjectOutPrintStream
    private PrintStream out;

    @CommandExecutor
    public void echo(String msg) {
        out.println(msg);
    }
}
```

Additionally, you can annotate a method parameter with `@ParaDsKey("xxx")` to assign it a description key; `help [command]` will then show the corresponding (Chinese/English) parameter name. The internationalization section below explains this in detail.

## Built-in Commands

Simple-Terminal registers two commands by default. If you have already defined a custom command named `help` or `stop`, the default implementation will not override it.

### `help`

- `help`: lists all registered commands and their descriptions
- `help [command]`: shows the usage, parameter descriptions, and subcommands of the given command

### `stop`

Closes the process. Before exiting, it calls `closeResources()` on every processor that implements `ClosableFlag`, then runs the `stopRunnable` configured in `TerminalOptions`, and finally calls `System.exit`.

Supported subcommands:

| Subcommand | Example | Description |
| --- | --- | --- |
| `-ms` | `stop -ms 200` | Stop after 200 milliseconds |
| `-s` | `stop -s 2` | Stop after 2 seconds |
| `-min` | `stop -min 2` | Stop after 2 minutes |
| `-h` | `stop -h 2` | Stop after 2 hours |
| `-d` | `stop -d 2` | Stop after 2 days |
| `-mo` | `stop -mo 2` | Stop after 2 months (estimated at 30 days/month) |
| `-y` | `stop -y 2` | Stop after 2 years (estimated at 365 days/year) |
| `-r` | `stop -r 2030 2 17 13 14 15 888` | Stop at 2030-02-17 13:14:15.888; arguments from year down to millis may be omitted |

If your command processor holds resources that need to be released, implement `ClosableFlag`:

```java
@CommandProcessor(value = "db")
public class DbCommandProcessor implements ClosableFlag {
    private Connection conn;

    @Override
    public void closeResources() throws Exception {
        if (conn != null) conn.close();
    }
}
```

## Internationalization

Simple-Terminal automatically picks the description file based on `Locale.getDefault().getLanguage()`:

- Built-in command descriptions ship with `DefaultDescriptions.properties` (English) and `DefaultDescriptions_zh.properties` (Chinese).
- User-defined command descriptions are configured via `TerminalBuilder.setDescriptionsPath(resourceDir, filePrefix)`. Lookup order:
  1. `filePrefix_languageCode.properties` (e.g. `descriptions_zh.properties`)
  2. `filePrefix.properties` (e.g. `descriptions.properties`)

```java
Terminal terminal = TerminalBuilder.builder()
        .setAnchorPackageClassHelper(InfoCommandProcessor.class)
        .setDescriptionsPath("descriptions", "descriptions")
        .build();
terminal.run();
```

Use `setPropertiesLoadCharset` to set the charset used when loading the properties; the default is UTF-8.

After setting up the i18n resource files, you need to configure `descriptionKey`. The `descriptionKey` parameter is available on `@CommandProcessor`, `@CommandExecutor`, and `@SubcommandExecutor`. Its purpose:

When the user runs the `help` command, Simple-Terminal fetches the corresponding command's `descriptionKey`, then looks up its value in the configured i18n resource file and returns it. For example, the built-in `stop` command is configured as:

```java
@CommandProcessor(value = "stop", descriptionKey = "simpleterminal.defaults.stop")
```

When you run `help stop`, Simple-Terminal gets the `descriptionKey` registered with `stop`, determines the current language, and retrieves the value for that key from the matching properties file. For example, under a Chinese locale, the relevant entry in `DefaultDescriptions_zh.properties` is:

```properties
simpleterminal.defaults.stop=关闭进程
```

So running `help stop` shows:

```bash
> help stop
关闭进程                //content looked up from the properties file via descriptionKey

...content omitted
...content omitted
```

For user-defined commands, you must set the location of the properties file yourself via `TerminalBuilder.setDescriptionsPath`. The properties file should be placed in your project's resource directory. This method takes a `resourceDir` parameter and a `filePrefix` parameter: the former is the directory containing the properties file, the latter is the file prefix. Simple-Terminal looks for files with the given prefix in that directory. For example, given:

```
resources
    | io.github.ctimetbukii.demo
        | Descriptions.properties
        | Descriptions_zh.properties
        | Descriptions_fr.properties
```

You should use:

`TerminalBuilder.setDescriptionsPath("io/github/ctimetbukii/demo","Descriptions")`

The first argument `"io/github/ctimetbukii/demo"` indicates the resource files live in the `io/github/ctimetbukii/demo` directory — no leading or trailing slashes. The second argument `"Descriptions"` indicates the file prefix is `Descriptions`; Simple-Terminal attempts to find and load `Descriptions_languageCode.properties` under that directory. In this example, if the user is in a Chinese locale, `Locale.getDefault().getLanguage()` returns `"zh"`, and Simple-Terminal will try to load `Descriptions_zh.properties` from the `io/github/ctimetbukii/demo` directory.

## Async & Custom Streams

`TerminalBuilder` provides a set of optional configurations:

```java
Terminal terminal = TerminalBuilder.builder()
        .setAnchorPackageClassHelper(InfoCommandProcessor.class)
        .runAsync()                              // run asynchronously on a dedicated thread
        .asDamon()                               // mark that thread as a daemon
        .setTerminalThreadName("CONSOLE")        // set the thread name
        .setExecutorService(executorService)     // or use a custom thread pool
        .setTerminalInputStream(in)              // custom input stream
        .setTerminalOutputStream(out)            // custom output stream
        .setCommandDoesntExistProcessor(cmd -> System.err.println("Unknown command: " + cmd))
        .setStopRunnable(() -> System.out.println("Saving data..."))
        .setDescriptionsPath("descriptions", "descriptions")
        .setPropertiesLoadCharset(StandardCharsets.UTF_8)
        .build();
terminal.run();
```

- `runAsync()` makes the terminal listen for input on another thread, so the main thread can keep doing other work; call `terminal.stop()` to stop listening.
- If an `ExecutorService` is provided, the listening task is submitted to it; otherwise Simple-Terminal creates its own thread.
- Input/output streams are all replaceable, making it easy to integrate into GUIs, embedded scenarios, or tests.

## API Reference (Per-Class)

This section walks through every class in `Terminal.Core`, explaining its purpose, applicable scenario, and a short example. Classes marked "internal" are not normally used directly, but understanding them helps you reason about the library's behavior and extend it in advanced scenarios.

### Root package `io.github.ctimetbukii.simpleterminal`

#### `Terminal`

Top-level entry point. Scans and registers commands, listens for input, and maintains context state.

**Scenario**: starting / stopping the command-line interaction.

```java
Terminal terminal = TerminalBuilder.builder()
        .setAnchorPackageClassHelper(App.class)
        .build();
terminal.run();    // blocking listen (default)
// ...business logic...
terminal.stop();   // stop listening (useful in async mode)
```

#### `TerminalBuilder`

Fluent builder that configures every option of `TerminalOptions`.

**Scenario**: when you need async execution, custom streams, a custom thread pool, i18n paths, etc.

```java
Terminal terminal = TerminalBuilder.builder()
        .setAnchorPackageClassHelper(App.class)
        .runAsync()
        .setTerminalThreadName("MY-CONSOLE")
        .setDescriptionsPath("i18n", "descriptions")
        .setStopRunnable(() -> System.out.println("bye"))
        .build();
```

#### `TerminalOptions`

Holds all runtime configuration. Can be passed to `new Terminal(options)`, or read inside a processor via `@InjectTerminalOption`.

**Scenario**: reading the current input/output streams or stop callback inside a processor.

```java
@CommandProcessor("cfg")
public class CfgCommandProcessor {
    @InjectTerminalOption
    private TerminalOptions options;

    @CommandExecutor
    public void showIn() {
        // read the input stream the terminal is currently using
        options.getInputStream().available();
    }
}
```

#### `CommandSign` (internal)

The "signature" of an executor method, determined by `patterns`, parameter types, `stage`, `paraJoin`, and the transform strategy. Simple-Terminal uses it to deduplicate and match user input.

**Scenario**: understanding why overloaded methods are dispatched automatically — each overload maps to a distinct `CommandSign`.

```java
// The two methods below produce two different CommandSigns; "set 8080" hits the int version, "set prod" hits the String version
@CommandExecutor
public void set(int port) { ... }

@CommandExecutor
public void set(String env) { ... }
```

#### `GlobalTransmitter` (internal)

Process-wide static holder storing the registered command set, command metadata, and resource methods to be closed.

**Scenario**: when building a custom `help` / `stop`, you can read all registered commands and their descriptions from it.

```java
@CommandProcessor("myhelp")
public class MyHelpProcessor {
    @CommandExecutor
    public void list() {
        @SuppressWarnings("unchecked")
        Set<String> all = (Set<String>) GlobalTransmitter.getTransmitter()
                .get(TransmitterKey.ALL_COMMANDS);
        all.forEach(System.out::println);
    }
}
```

### Annotations package `annotations`

#### `@CommandProcessor`

Class-level annotation marking a class as the handler for a command.

**Scenario**: registering any custom command.

```java
@CommandProcessor(value = "reload", alias = {"rl"}, descriptionKey = "cmd.reload")
public class ReloadCommandProcessor { ... }
```

#### `@CommandExecutor`

Method-level annotation marking a regular command executor.

**Scenario**: commands of the form `cmd arg1 arg2`.

```java
@CommandExecutor
public void set(String key, String value) {
    config.put(key, value);
}
```

#### `@SubcommandExecutor`

Method-level annotation marking a subcommand executor.

**Scenario**: a command with multiple prefixed sub-options (e.g. `-list`, `-add`).

```java
@SubcommandExecutor("-list")
public void listAll() {
    users.forEach(System.out::println);
}
```

#### `@ContextCommandExecutor`

Method-level annotation marking a specific round of a multi-round context command, distinguished by `stage`.

**Scenario**: wizard-style interaction, e.g. gathering information step by step.

```java
@ContextCommandExecutor(stage = 0)
public void askName(String[] args) {
    out.println("Please enter your age:");
}
```

#### `@ArgsIllegalExecutor`

Method-level annotation triggered when no `CommandSign` matches the given arguments.

**Scenario**: giving users a friendly error message instead of the framework's default.

```java
@ArgsIllegalExecutor
public void onBadArgs(String[] args) {
    err.println("Illegal arguments. Run 'help' for usage. Got: " + Arrays.toString(args));
}
```

#### `@InjectOutPrintStream`

Injects the terminal standard output stream into a `PrintStream` field.

**Scenario**: writing to the terminal stream so output follows redirection.

```java
@InjectOutPrintStream
private PrintStream out;
```

#### `@InjectErrPrintStream`

Injects the terminal error output stream into a `PrintStream` field.

**Scenario**: emitting error messages that follow `TerminalOptions.errOutputStream`.

```java
@InjectErrPrintStream
private PrintStream err;
```

#### `@InjectTerminalOption`

Injects the current `TerminalOptions` into a field.

**Scenario**: reading runtime configuration (custom stop callback, stream objects) inside a processor.

```java
@InjectTerminalOption
private TerminalOptions options;
```

#### `@InjectDefaultDescriptionProperties`

Injects the `Properties` of built-in command descriptions (already loaded by language).

**Scenario**: reusing the built-in command i18n text in a custom help.

```java
@InjectDefaultDescriptionProperties
private Properties defaultDesc;
```

#### `@InjectCommandDescriptionProperties`

Injects the `Properties` of user-defined command descriptions (already loaded by language).

**Scenario**: showing Chinese/English descriptions of user commands in help.

```java
@InjectCommandDescriptionProperties
private Properties cmdDesc;
```

#### `@ParaDsKey`

Parameter-level annotation assigning a description key to a method parameter, shown by `help [command]`.

**Scenario**: letting users see meaningful parameter names in help instead of `arg0`.

```java
@CommandExecutor
public void login(@ParaDsKey("para.username") String username,
                  @ParaDsKey("para.password") String password) { ... }
```

### Defaults package `defaults`

#### `DefaultHelpCommandProcessor`

The built-in `help` command processor. Lists all commands and their details.

**Scenario**: use it as-is; to customize help, register a processor named `help` and the default is skipped.

```java
// once registered, the built-in help won't override your implementation
@CommandProcessor("help")
public class MyHelpProcessor {
    @CommandExecutor
    public void help() {
        System.out.println("Custom help doc");
    }
}
```

#### `DefaultStopCommandProcessor`

The built-in `stop` command processor, supporting delayed / scheduled shutdown, and releasing `ClosableFlag` resources first.

**Scenario**: scheduled exit (e.g. overnight maintenance) or graceful resource release.

```java
// shut down at 2030-02-17 13:14:15.888
// stop -r 2030 02 17 13 14 15 888
```

### Process package `process`

#### `CommandHandler` (internal)

Each processor class corresponds to one `CommandHandler` responsible for matching `CommandSign`s, injecting fields, and reflectively invoking executor methods.

**Scenario**: not used directly. Understanding it helps debug "why isn't my method being called" — usually `patterns` or parameter types don't match any `CommandSign`, so `@ArgsIllegalExecutor` is triggered instead.

#### `ExecuteResult` / `ExecuteState` (internal)

The result carrier and its state enum. `ExecuteState` has the following states, especially important for context commands:

| State | Meaning |
| --- | --- |
| `SUCCEED` | executed successfully, advance to next round |
| `FAIL` | execution failed, clear the context |
| `NEED_REPEAT` | the current round needs to be re-entered |
| `STAGE_JUMP_TO` | jump to a specified stage |
| `CONTEXT_INTERRUPT` | context interrupted, the command exits |

**Scenario**: returning an `int` triggers `STAGE_JUMP_TO`, enabling branching multi-round dialogs.

```java
@ContextCommandExecutor(stage = 1)
public int chooseBranch(String choice) {
    if (choice.equals("advanced")) return 3;  // jump to stage 3
    return 2;                                  // otherwise go to stage 2
}
```

#### `ArgsTypeTransformMethod`

Enum defining four strategies for converting strings to target types.

**Scenario**: specifying a strategy explicitly when you have strict format requirements.

```java
@CommandExecutor(paraTransformMethod = ArgsTypeTransformMethod.CONVERT)
public void setPort(int port) { ... }   // "abc" is rejected, "8080" passes
```

#### `TransformExecutor` / `ConvertExecutor` / `CoerceExecutor` / `InterpretExecutor` / `CoerceOrInterpretExecutor` (internal)

Concrete conversion implementations for each `ArgsTypeTransformMethod` value, delegating to [elegant-convert-checker](https://github.com/CTimet/elegant-convert-checker).

**Scenario**: not used directly. To customize conversion rules, refer to these implementations and extend on top of `elegant-convert-checker`.

### Flags package `flags`

#### `ClosableFlag`

Interface marking processors that need to release resources on shutdown.

**Scenario**: when a processor holds a DB connection, file handle, network socket, etc.

```java
@CommandProcessor("db")
public class DbCommandProcessor implements ClosableFlag {
    private Connection conn;
    @Override
    public void closeResources() throws Exception {
        if (conn != null) conn.close();
    }
}
```

### Exceptions package `exceptions`

#### `NeedRepeatException`

Thrown by the user inside an executor to signal that the current round should be re-entered.

**Scenario**: input is invalid for business reasons (but format-legal) and you want to ask again.

```java
@ContextCommandExecutor(stage = 1)
public void askAge(String age) {
    int a = Integer.parseInt(age);
    if (a < 0 || a > 150) {
        out.println("Unreasonable age, please re-enter:");
        throw new NeedRepeatException();  // roll back this round, ask again
    }
}
```

#### `AliasConflictWithCommandException` (internal use, not recommended to be thrown externally)

Thrown when an alias conflicts with a registered formal command name.

**Scenario**: debugging alias setup when startup fails.

```java
// fails at startup: alias "stop" conflicts with the built-in stop command
@CommandProcessor(value = "exit", alias = {"stop"})
public class ExitProcessor { ... }
```

#### `ContextInterruptException`

Thrown when a context command needs to be interrupted.

**Scenario**: when you need to interrupt a context command.

#### `RepeatAliasException` (internal use, not recommended to be thrown externally)

Thrown when the same alias is registered more than once.

**Scenario**: two processors share the same alias.

```java
// alias "rl" registered twice → throws at startup
@CommandProcessor(value = "reload", alias = {"rl"})
@CommandProcessor(value = "restart", alias = {"rl"})
```

#### `RepeatCommandSignException` (internal use, not recommended to be thrown externally)

Thrown when two executor methods in the same class have identical `patterns` and parameter types.

**Scenario**: overloads whose signatures are not distinguished.

```java
// two set(int) with the same signature → throws
@CommandExecutor public void set(int x) { ... }
@CommandExecutor public void set(int y) { ... }
```

#### `IllegalInjectObjectException` (internal use, not recommended to be thrown externally)

Thrown when an injected field's type doesn't match the expected type.

**Scenario**: putting `@InjectOutPrintStream` on a non-`PrintStream` field.

```java
@InjectOutPrintStream
private String out;  // throws at startup: type must be PrintStream
```

#### `IllegalJumpStageException` (internal use, not recommended to be thrown externally)

Thrown when a context command returns an invalid jump stage (≤ 0).

**Scenario**: the executor returned a non-positive integer.

```java
@ContextCommandExecutor(stage = 1)
public int jump(String s) {
    return 0;  // throws: jump stage must be > 0
}
```

#### `NoAccessibleNecessaryExecutor` (internal use, not recommended to be thrown externally)

Thrown when a processor class has no `@CommandExecutor` or `@SubcommandExecutor` methods at all.

**Scenario**: you wrote only `@ArgsIllegalExecutor` and forgot the actual executors.

```java
@CommandProcessor("oops")
public class OopsProcessor {
    @ArgsIllegalExecutor
    public void onBad(String[] a) { }  // throws: no necessary executor present
}
```

#### `IllogicalPanicException` (internal use, not recommended to be thrown externally)

A branch the library should never reach; throwing it means Simple-Terminal has a bug — please file an issue.

**Scenario**: if you encounter it, open an issue on the repo with the stack trace.

#### `RepeatException` (internal use, not recommended to be thrown externally)

Base class for repeat-related exceptions; `RepeatCommandSignException` extends it. Not thrown directly in practice.

### Utilities package `utils`

#### `CastUtil` (internal)

Converts a string argument array according to a `CommandSign`'s type array.

**Scenario**: internal. Refer to it when building a custom conversion chain.

#### `MethodUtil` (internal)

Factory methods that build a `CommandSign` from an annotated `Method`.

**Scenario**: internal. If you extend the library with a new executor annotation, you can use it to produce `CommandSign`s.

### Records package `utils.records` (internal; usable if you implement your own help method)

| Class | Role |
| --- | --- |
| `CommandInformation` | Full metadata of a processor (class annotation + all executor info), used by help |
| `ExecutorInformation` | Metadata of a single `@CommandExecutor` method, sorted by parameter count |
| `SubExecutorInformation` | Metadata of a single `@SubcommandExecutor` method |
| `RunnableMethod` | A `Method` + instance wrapper, used to reflectively call close-resource methods |
| `TransmitterKey` | Key constants used in `GlobalTransmitter` (e.g. `ALL_COMMANDS`) |

**Scenario**: a custom help or stop may use `CommandInformation` to walk the command structure; the rest are internal support classes.

```java
// custom help: iterate over every command's metadata
Map<String, CommandInformation> map = GlobalTransmitter.getCommandInformationMap();
map.forEach((name, info) -> {
    System.out.println(name + " -> " + info.getProcessorAnn().descriptionKey());
});
```

## License

This project is released under the [MIT License](LICENSE), Copyright (c) 2026 CTimet bukii.
