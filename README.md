<div align="center">

# Simple-Terminal

☕一个轻量、简单、易用的 Java 终端库
🥰通过注解驱动的方式，用极少的代码为自己的程序接入一套完整的命令行交互能力

</div>

[English](README_EN.md) | 简体中文

## 项目特点

- 注解驱动：在类/方法上**打上注解即可注册命令**。Simple-Terminal自动解析用户输入，自动做指令分发，自动处理方法参数。**告别手写指令解析**
- 自动参数转换：支持将用户输入的字符串**参数自动转换为方法所需的参数类型**，不需要再手动写try-catch
- 支持正则表达式：使用**正则表达式对用户输入的参数进行自动校验**，把90%的if判断交给Simple-Terminal
- 支持子命令：将某个字符串设置为子命令，传入处理方法的参数时自动省去
- 上下文命令：支持需要多轮对话的命令，还可以实现**上下文跳转，实现不同的处理分支**
- 内置 `help` 与 `stop`：**开箱即用**，并支持自定义替换
- 国际化（i18n）支持：根据系统语言自动加载对应的 properties 描述文件，**搭配内置的help命令，轻松实现命令帮助**。你只管提供国际化资源，剩下的交给Simple-Terminal
- 自定义输入输出流：**可以接入网络输入输出流，实现远程终端**
- 注入获取参数：通过@InjectOutPrintStream，@InjectErrPrintStream 注解标记字段以获取标准输出流PrintStream与错误输出流PrintStream，避免写死System.out; System.err，自由接入网络输出流
- 支持命令别名：可以为一个命令设置多个**别名**，想怎么使用就怎么使用
- 优雅地关闭资源：任何一个命令处理器，如果内部有需要在终端关闭时关闭的资源，只需要实现ClosableFlag接口，实现该接口下的closeResource方法，内置的stop命令执行时，就可以调用该方法，从而**优雅地关闭资源**
- 4种参数转换策略：底层使用 [ElegantConvertChecker](https://github.com/CTimetbukii/ElegantConvertChecker) 库，可以自由地将用户的输入以4种不同的策略转换为处理方法的入参。还可以**自定义布尔标记**，Y/y/T/t/1/是，任何你希望的字符串都可以被转换为方法入参时的true
- 极简的使用：通过TerminalBuilder的链式调用，逐步设置好一个Terminal对象，返回后，调用run方法即可运行。**使用简直不要太简单**
- 异步执行：支持在独立线程或自定义线程池中运行终端监听
- 常驻式交互：Simple-Terminal是一个常驻的交互式控制台，你不需要自己写循环，Simple-Terminal天然就支持常驻式

## 目录

- [安装](#安装)
- [快速开始](#快速开始)
- [核心概念](#核心概念)
- [注解一览](#注解一览)
- [命令处理器](#命令处理器)
- [子命令](#子命令)
- [上下文命令](#上下文命令)
- [参数校验与转换](#参数校验与转换)
- [字段注入](#字段注入)
- [内置命令](#内置命令)
- [国际化](#国际化)
- [异步与自定义流](#异步与自定义流)
- [API 详解（逐类说明）](#api-详解逐类说明)
- [许可证](#许可证)

## 安装

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

> Simple-Terminal 核心模块编译目标为 Java 1.8。

## 快速开始

最简用法——只需两行代码即可启动一个带有内置 `help` 与 `stop` 命令的终端：

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

运行后即可在控制台输入 `help` 查看可用命令，输入 `stop` 退出进程。

注册自己的命令也很简单，定义一个处理器类，并通过锚定类告诉 Simple-Terminal 在哪个包下扫描你的命令处理器类。命令处理器类需要放在同一个包下：

```java
// 1. 定义命令处理器
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

// 2. 启动终端，使用锚定类指定扫描包
Terminal terminal = TerminalBuilder.builder()
        .setAnchorPackageClassHelper(InfoCommandProcessor.class) //其他命令处理器类需要和InfoCommandProcessor放在同一个包下
        .build();
terminal.run();
```

随后输入 `info` 会打印 `Simple-Terminal Demo`，输入 `info CTimet` 会打印 `Hello, CTimet!`。

## 核心概念

Simple-Terminal 在运行时会通过反射扫描你指定的锚定类所处的包，找到所有带有 `@CommandProcessor` 注解的类并完成注册。每一行用户输入会按空格切分，首段被视为命令名（或别名），剩余部分作为参数传入匹配到的执行方法。

一个命令处理器类内部可以包含以下几种执行方法：

| 执行方法注解 | 作用 |
| --- | --- |
| `@CommandExecutor` | 普通命令执行方法，处理 `cmd arg1 arg2` 形式的输入 |
| `@SubcommandExecutor` | 子命令执行方法，处理 `cmd -s arg1` 形式的输入，传入方法的参数中将不再包含 `-s`，<br/>任何不带空格形式的字符串都可以设置为子命令 |
| `@ContextCommandExecutor` | 上下文执行方法，处理需要多轮对话的命令，由 `stage` 区分轮次 |
| `@ArgsIllegalExecutor` | 当传入参数不匹配任何执行方法时触发的兜底方法 |

## 注解一览

### `@CommandProcessor`（类级别）

标记一个类为某个命令的处理器。

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `value` | `String` | — | 命令名（必填） |
| `alias` | `String[]` | `{}` | 命令别名 |
| `matchAllCases` | `boolean` | `true` | 是否同时匹配大小写混写的命令 |
| `hasContext` | `boolean` | `false` | 是否含有上下文（多轮对话） |
| `descriptionKey` | `String` | `""` | 在 help 中展示的描述对应的 properties key 。<br/> Simple-Terminal根据这个descriptionKey从国际化资源文件中获取该命令的描述，help指令会将其打印出来|

### `@CommandExecutor`（方法级别）

标记普通命令执行方法。

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `patterns` | `String[]` | `{}` | 每个参数位置的正则约束 |
| `paraJoin` | `boolean` | `false` | 是否把所有参数以空格拼接为单个字符串传入 |
| `paraTransformMethod` | `ArgsTypeTransformMethod` | `COERCE_WITH_INTERPRET` | 参数类型转换策略 |
| `descriptionKey` | `String` | `""` | 该执行方法的描述 key |

当用户输入命令时，Simple-Terminal会依次比对不同的@CommandExecutor标记的方法，比对它们的方法入参，正则约束等等。比如

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

在这个例子中，假如你使用指令 `hello` ，则控制台会输出 `hello`，此时hello方法被触发。若你输入`hello a`，则控制台会输出`hello a`，此时helloUser方法被触发。若你输入`hello 1`，则控制台会输出`1`，此时helloId方法被触发。Simple-Terminal会根据用户传递的参数类型自动查找并调用合适的方法。

你也可以在方法参数列表中加入多个参数。例如

```java
@CommandExecutor
public void helloId(int id1, int id2) {
    out.println(id1);
    out.println(id2);
}
```

此时使用指令`hello 1 2`，控制台会依次打印`1`,`2`。参数1被解析为id1，参数2被解析为id2。方法参数的顺序与类型和指令参数的顺序与类型是吻合的。

你也可以使用正则表达式约束用户传入的参数

```java
@CommandExecutor(patterns={"[0-9]", "[0-9]"})
public void helloId(int id1, int id2) {
    out.println(id1);
    out.println(id2);
}
```

现在使用 `hello` 命令时，传入的参数只能为0到9的数字。若执行 `hello 100 100` 等有参数不符合正则约束的命令，或者执行 `hello a b` 等有参数不符合传参类型约束的命令，Simple-Terminal 在解析指令时就会找不到合适的方法调用，于是判定命令参数不合法。此时 Simple-Terminal 将查找有无被 @ArgsIllegalExecutor 标记的方法。如果有，则调用。比如

```java
@CommandExecutor(patterns={"[0-9]", "[0-9]"})
public void helloId(int id1, int id2) {
    out.println(id1);
    out.println(id2);
}

@ArgsIllegalExecutor
public void fail() {
    out.println("参数不合法！");
}
```

此时执行 `hello 100 100` ，便会打印`参数不合法！`

被ArgsIllegalExecutor标记的方法可以有参也可以无参。有参时，参数只能有1个且类型必须为String[]，此时Simple-Terminal会将命令参数传入进去，比如


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

此时执行`hello 100 100` 会打印 `[100, 100]`

### `@SubcommandExecutor`（方法级别）

标记子命令执行方法，用法与 `@CommandExecutor` 类似，额外包含 `value`（子命令名）、`alias` (别称)、`matchAllCases` （是否同时匹配大小写，默认为true）。

被子命令标记的方法，传参时将不再会传入子命令。例如

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

此时执行 `info -r 1`，会调用 info4 方法，入参a="1"。子命令"-r"不再入参。最终控制台将输出`1`

有子命令时，Simple-Terminal会优先尝试子命令，故此时info2方法不会被触发。当执行`info -s 1`时，由于`-s`不是一个子命令，此时Simple-Terminal会回退到查找对应的CommandExecutor，info2方法才会被触发，入参a="-s", b="1"。

任何不含空格的字符串都可以作为子命令，并不必须以`-`开头。比如

```java
@SubcommandExecutor("at")
public void info4(String a) {
    out.println(a);
}
```

此时`at`便被设置为了子命令。使用`info at 1`，将会触发info4方法，传参a="1"。

还可以使用`alias`配置子命令的别称，譬如

```java
@SubcommandExecutor(value="at", alias={"@","a"})
public void info4(String a) {
    out.println(a);
}
```

此时使用`info @ 1`, `info a 1`，一样可以触发info4方法。

默认情况下，`matchAllCases` 值为true，代表大小写都兼容。在前面`info at`的例子中，如果使用`info AT`，一样可以触发。但是alias不存在大小写兼容，若alias为`at`，则使用`AT`将不会被触发。

你可以手动设置`matchAllCases`为false，此时必须严格按照子命令输入才能触发，比如

```java
@SubcommandExecutor(value="at", alias={"@","a"}, matchAllCases=false)
public void info4(String a) {
    out.println(a);
}
```

此时`info AT`不再触发info4


### `@ContextCommandExecutor`（方法级别）

标记上下文命令的某一轮处理方法。

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `stage` | `int` | — | 第几轮输入，`0` 代表初次输入，从 `1` 开始递增代表后续上下文输入 |
| `patterns` | `String[]` | `{}` | 参数正则约束 |
| `paraJoin` | `boolean` | `false` | 是否拼接参数 |
| `paraTransformMethod` | `ArgsTypeTransformMethod` | `COERCE_WITH_INTERPRET` | 参数转换策略 |

> 注意：不要在同一个方法上同时使用 `@CommandExecutor` 和 `@ContextCommandExecutor(stage = 0)`，否则该方法可能被调用两次，且两次传入的参数并不相同。

ContextCommandExecutor 用于标记需要多轮用户输入才能完成的指令。这样的指令，每轮输入时带有上下文信息，故我们称作上下文指令。

ContextCommandExecutor 相较于前两个注解，多了 stage 这一个独有的参数。它代表第几轮上下文会话输入。不要使用@ContextCommandExecutor(stage=0)来处理初次输入，相反，使用@CommandExecutor来处理初次输入。两者不要共用。下面给出一个上下文指令的例子

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

    @ContextCommandExecutor(stage = 1, paraJoin = true) //paraJoin =true处理用户名中包含空格的情况
    public void acceptUser(String user) {
        out.println("Send to '" + user + "' done.");
        out.println("Your feeling rate: ");
    }

    @ContextCommandExecutor(stage = 2)
    public void acceptUserFeelingRate(int rate) {
        out.println("Rate = " + rate);
        out.println("Your feeling float rate:");
    }

    @ContextCommandExecutor(stage = 3, paraTransformMethod = ArgsTypeTransformMethod.CONVERT) //在该模式下，输入1.111111111111111等超出float精度的数据会提示参数不合法
    public void acceptUserFeelingFloatRate(float rate) {
        out.println("F Rate = " + rate);
        out.println("Your feeling float rate 2:");
    }

    @ContextCommandExecutor(stage = 4) //这一轮对话中输入1.1111111111111111就不会提示参数不合法，而是传入float=1.1112，这是强制转换的结果
    public void acceptUserFeelingFloatRate2(float rate) {
        out.println("F Rate = " + rate);
    }

    @ArgsIllegalExecutor
    public void fail(String[] args) {
        out.println("参数不合法");
        out.println(Arrays.toString(args));
    }
}

```

这个示例程序包含在SimpleTerminal.Demo中。这个程序演示了如何使用ContextCommandExecutor注解来处理多轮用户输入。下面给出控制台内容

```bash
> send A                         //触发CommandExecutor标记的方法
Send 'A' to whom?
> CTimet bukii                   //触发stage=1标记的方法
Send to 'CTimet bukii' done.
Your feeling rate: 
> 10                             //stage=2，第二轮用户输入
Rate = 10
Your feeling float rate:
> 10.0                           //stage=3
F Rate = 10.0
Your feeling float rate 2:
> 21                             //stage=4
F Rate = 21.0
>                                //send命令结束，此时输入的内容将不再被解析为send命令的上下文输入
```

有些时候，你可能希望输入之间能够实现跳转。比如，当密码存在时，要求用户输入密码，不存在则跳过这一环节。@ContextCommandExecutor同样支持这一点。我们称之为上下文跳转

实现上下文跳转的方法非常简单。只需要让被@ContextCommandExecutor标记的方法返回一个int值即可。下面给出上下文跳转的示例

```java
package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.ContextCommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.InjectOutPrintStream;
import io.github.ctimetbukii.simpleterminal.exceptions.NeedRepeatException;

import java.io.PrintStream;

@CommandProcessor(value = "repeat", hasContext = true)
public class RepeatCommandProcessor { //这个指令处理类没有implement DescriptionFlag。使用默认help指令不会显示它的描述
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
            throw new NeedRepeatException();  //当方法中抛出NeedRepeatException时，下一轮输入的内容还会进入当前stage。只有抛出NeedRepeatException才会这样，其他Exception则正常报错
        }
        out.println("Stop. Jump to which stage OR try fail repeat by entering true/false");
    }

    @ContextCommandExecutor(stage = 2)
    public void failedRepeat2(boolean repeat) {
        if (repeat) {
            out.println("Failed repeat example, throw not NeedRepeatException");
            throw new RuntimeException(); // 控制台会报错，相当于命令执行失败。再次键入时不会再走这个方法了
        }
        out.println("Meow@");
    }

    @ContextCommandExecutor(stage = 2, patterns = "^[1-4]\\d*$") //patterns限制了jumpTo只能为1-4的正整数
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

这个示例程序同样在SimpleTerminal.Demo中。这个示例的jumpTo方法演示了如何进行上下文跳转 —— 只需要让方法返回一个int值就好了，返回的值会被作为stage，于是下一轮用户的输入将被这个stage的方法捕获，下面给出控制台内容：

```bash
> repeat 1                                                              //输入被repeat方法捕获，进入CommandExecutor中处理
arg = 1
> false                                                                 //输入被stage=1标记的方法捕获，即repeat1方法
Stop. Jump to which stage OR try fail repeat by entering true/false
> 4                                                                     //输入被stage=2标记的方法捕获，进入jumpTo方法
Jump to 4.
Type stage 4's args:                                                    //jumpTo方法return 4
> c                                                                     //于是这里输入将被stage=4标记的方法捕获
Stage 4                                                                 //成功执行stage4方法
> 
```

在上面的示例程序中，还演示了，如果你想要让某一个stage重复执行应该怎么做 —— 抛出NeedRepeatException。例如，当用户输入密码错误时，我们可能希望用户重新输入密码，此时即需要重复当前stage的输入。这个时候只需要抛出NeedRepeatException，Simple-Terminal就会将下一轮的输入重新指向同一个stage，下面给出控制台示例

```bash
> repeat true                                                           //输入被repeat方法捕获，进入CommandExecutor中处理
arg = true
Repeat?
> true                                                                  //输入被stage=1的方法捕获，stage=1中，若用户输入true，则会抛出NeedRepeatException，于是接下来的输入依然进入了stage=1
Repeated. Again?
> true
Repeated. Again?
> true
Repeated. Again?
> 
```

有些时候，你可能希望中途退出stage方法。比如用户输入密码，密码错误则直接退出这个指令，后面的多轮输入不再进行，此时，此时你可以抛出 ContextInterruptException，命令就会自然退出了。


### `@ArgsIllegalExecutor`（方法级别）

当传入参数不符合任何已注册的方法时触发。可无参，也可只接收一个 `String[]` 参数。接受`String[]`参数时，调用时会向里面传入用户输入的参数。

## 命令处理器

一个完整的命令处理器示例：

```java
@CommandProcessor(value = "greet", alias = {"hi", "hello"}, descriptionKey = "cmd.greet")
public class GreetCommandProcessor {

    @CommandExecutor(descriptionKey = "cmd.greet.default")
    public void greet(String name) {
        System.out.println("Hello, " + name + "!");
    }

    @ArgsIllegalExecutor
    public void onIllegal(String[] args) {
        System.err.println("参数不合法：" + Arrays.toString(args));
    }
}
```

- `alias` 允许为命令配置多个别名，输入别名等价于输入正式命令名。
- 若 `matchAllCases = false`，则只有完全匹配大小写的命令名才会被触发。
- 别名不能与已注册的正式命令名冲突，否则启动时会抛出 `AliasConflictWithCommandException`。

## 子命令

子命令适用于同一命令下有多个子选项的场景，例如内置的 `stop` 命令：

```java
@CommandProcessor(value = "stop", descriptionKey = "simpleterminal.defaults.stop")
public class DefaultStopCommandProcessor {

    @CommandExecutor(descriptionKey = "simpleterminal.defaults.stop.normal")
    public void stop() {
        // 立即关闭
    }

    @SubcommandExecutor(value = "-s", descriptionKey = "simpleterminal.defaults.stop.secs")
    public void stopAfterSecs(@ParaDsKey("simpleterminal.defaults.stop.para.secsDelay") long secsDelay) {
        // 在指定秒数后关闭
    }
}
```

输入 `stop` 立即关闭，输入 `stop -s 2` 则在 2 秒后关闭。当首个参数命中已注册的子命令时，会走子命令通道，否则回退到普通 `@CommandExecutor`。

## 上下文命令

上下文命令用于需要多轮对话的场景。例如一个 `send` 命令，先输入消息内容，再询问发送给谁：

```java
@CommandProcessor(value = "send", hasContext = true, descriptionKey = "cmd.send")
public class SendCommandProcessor {

    // stage = 0：初次输入，args 包含顶部命令，即 {"send", "hello"}
    @ContextCommandExecutor(stage = 0)
    public void start(String[] args) {
        System.out.println("To whom?");
    }

    // stage = 1：第一次上下文输入，args 为用户的后续输入
    @ContextCommandExecutor(stage = 1, paraJoin = true)
    public void finish(String target) {
        System.out.println("Send '" + args[1] + "' to " + target + ", done.");
    }
}
```

交互过程：

```
> send hello
To whom?
> CTimet bukii
Send 'hello' to CTimet bukii, done.
```

进阶控制：

- **重新执行当前轮**：在执行方法中抛出 `NeedRepeatException`，本轮 `contextTimes` 会被回退，用户可重新输入。
- **跳转到指定 stage**：让执行方法返回一个 `int` 值，Simple-Terminal 会将上下文跳转到该 stage（必须大于 0），从而实现分支式的多轮对话。
- **提前终止**: 让执行方法抛出`ContextInterruptException`，上下文指令便会正常终止

## 参数校验与转换

### 参数正则约束 `patterns`

`patterns` 是一个按位置对应的正则数组，依次校验每个参数。例如内置 `stop -r` 对日期的约束：

```java
@SubcommandExecutor(value = "-r",
        patterns = {"-?\\d+", "^(0[1-9]|1[0-2])$", "^(0[1-9]|[12][0-9]|3[01])$"})
public void stopAtRealTime(int targetYear, int targetMon, int targetDay) { ... }
```

### 参数拼接 `paraJoin`

当用户输入中需要包含空格时（例如一句完整的话），可以开启 `paraJoin`，此时所有参数会以空格拼接为单个字符串传入。开启后方法参数类型必须为单个 `String`。

### 参数类型转换 `paraTransformMethod`

Simple-Terminal 底层依赖 [elegant-convert-checker](https://github.com/CTimet/elegant-convert-checker) 完成字符串到目标类型的转换，提供四种策略：

| 策略 | 说明 |
| --- | --- |
| `CONVERT` | 严格转换，所有参数都必须严格符合目标类型的形式 |
| `COERCE` | 强制转换，尽量把字符串塞进目标类型 |
| `INTERPRET` | 解释转换，对字符串进行语义解释后转换 |
| `COERCE_WITH_INTERPRET` | 默认策略，先尝试解释转换，失败后再尝试强制转换 |

借助方法重载与不同的 `patterns`，你可以为同一个命令注册多个签名，Simple-Terminal 会根据输入自动匹配最合适的那一个。

## 字段注入

Simple-Terminal 支持向命令处理器类中的字段自动注入运行时对象，免去手动传参：

| 注解 | 注入内容 |
| --- | --- |
| `@InjectOutPrintStream` | 终端标准输出流（默认 `System.out`） |
| `@InjectErrPrintStream` | 终端错误输出流（默认 `System.err`） |
| `@InjectTerminalOption` | 当前的 `TerminalOptions` 对象 |
| `@InjectDefaultDescriptionProperties` | 内置默认命令的描述 `Properties` |
| `@InjectCommandDescriptionProperties` | 用户自定义命令的描述 `Properties`（已按语言自动加载） |

示例：

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

此外，方法参数上可以使用 `@ParaDsKey("xxx")` 为该参数指定一个描述 key，在 `help [命令]` 中会展示对应的中/英文参数名。下文关于国际化的部分将详细解释。

## 内置命令

Simple-Terminal 默认注册两个命令，若你已自定义同名的 `help` 或 `stop`，则默认实现不会覆盖你的实现。

### `help`

- `help`：列出所有已注册命令及其描述
- `help [命令名]`：展示指定命令的用法、参数说明与子命令

### `stop`

关闭进程，关闭前会依次调用所有实现了 `ClosableFlag` 接口的处理器的 `closeResources()`，再执行 `TerminalOptions` 中配置的 `stopRunnable`，最后调用 `System.exit`。

支持的子命令：

| 子命令 | 示例 | 说明 |
| --- | --- | --- |
| `-ms` | `stop -ms 200` | 200 毫秒后关闭 |
| `-s` | `stop -s 2` | 2 秒后关闭 |
| `-min` | `stop -min 2` | 2 分钟后关闭 |
| `-h` | `stop -h 2` | 2 小时后关闭 |
| `-d` | `stop -d 2` | 2 天后关闭 |
| `-mo` | `stop -mo 2` | 2 个月后关闭（按 30 天/月估算） |
| `-y` | `stop -y 2` | 2 年后关闭（按 365 天/年估算） |
| `-r` | `stop -r 2030 2 17 13 14 15 888` | 在 2030-02-17 13:14:15.888 关闭，参数从年到毫秒可省略 |

如果你的命令处理器持有需要释放的资源，实现 `ClosableFlag` 接口即可：

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

## 国际化

Simple-Terminal 会根据 `Locale.getDefault().getLanguage()` 自动选择描述文件：

- 内置命令的描述随包提供 `DefaultDescriptions.properties`（英文）与 `DefaultDescriptions_zh.properties`（中文）。
- 用户自定义命令的描述通过 `TerminalBuilder.setDescriptionsPath(resourceDir, filePrefix)` 指定资源目录与文件前缀。查找顺序为：
  1. `文件前缀_语言编码.properties`（如 `descriptions_zh.properties`）
  2. `文件前缀.properties`（如 `descriptions.properties`）

```java
Terminal terminal = TerminalBuilder.builder()
        .setAnchorPackageClassHelper(InfoCommandProcessor.class)
        .setDescriptionsPath("descriptions", "descriptions")
        .build();
terminal.run();
```

可通过 `setPropertiesLoadCharset` 设置读取 properties 时使用的编码，默认为 UTF-8。

设置好国际化资源文件后，需要配置descriptionKey。descriptionKey参数在@CommandProcessor，@CommandExecutor，@SubcommandExecutor中都有，具体的作用是

当用户输入help指令时，会获取对应指令的descriptionKey，然后拿着这个descriptionKey去配置好的国际化资源文件中查找值，找到后将值返回。例如内置stop命令配置的是

```java
@CommandProcessor(value = "stop", descriptionKey = "simpleterminal.defaults.stop")
```

当输入help stop时，Simple-Terminal将获取stop命令注册时携带的descriptionKey，然后获取当前语言，从对应的配置文件中获取该descriptionKey对应的值。例如在中文语言下，DefaultDescription_zh.properties中相关配置为

```properties
simpleterminal.defaults.stop=关闭进程
```

于是输入`help stop`时，就能看到

```bash
> help stop
关闭进程                //根据descriptionKey从配置文件中查出的内容

...内容省略
...内容省略
```

对于用户自定义的命令，需要用户自己通过TerminalBuilder.setDescriptionsPath方法设置配置文件的位置。配置文件应当放在项目的Resource目录下。该方法接受一个resourceDir参数和一个filePrefix参数。前者代表配置文件所处的目录，后者代表配置文件的文件前缀。Simple-Terminal会在配置文件所处目录下查找指定前缀的配置文件。例如在

```
resources
    | io.github.ctimetbukii.demo
        | Descriptions.properties
        | Descriptions_zh.properties
        | Descriptions_fr.properties
```

这样的项目结构中，应使用

`TerminalBuilder.setDescriptionsPath("io/github/ctimetbukii/demo","Descriptions")`

这样的代码。第一个传参"io/github/ctimetbukii/demo"代表资源文件在"io/github/ctimetbukii/demo"目录下，前后不要加斜杠。第二个传参"Descriptions"代表资源文件前缀为"Descriptions"，Simple-Terminal会尝试在"io/github/ctimetbukii/demo"目录下查找 Descriptions_语言编码.properties 文件并尝试加载。在这个例子中，若用户在中文语言区域使用help命令，Locale.getDefault().getLanguage()返回"zh"后，Simple-Terminal将会尝试在io/github/ctimetbukii/demo目录下加载Description_zh.properties文件。

## 异步与自定义流

`TerminalBuilder` 提供了一系列可选配置：

```java
Terminal terminal = TerminalBuilder.builder()
        .setAnchorPackageClassHelper(InfoCommandProcessor.class)
        .runAsync()                              // 在独立线程异步运行
        .asDamon()                               // 将该线程设为守护线程
        .setTerminalThreadName("CONSOLE")        // 设置线程名
        .setExecutorService(executorService)     // 或使用自定义线程池
        .setTerminalInputStream(in)              // 自定义输入流
        .setTerminalOutputStream(out)            // 自定义输出流
        .setCommandDoesntExistProcessor(cmd -> System.err.println("未知命令：" + cmd))
        .setStopRunnable(() -> System.out.println("正在保存数据..."))
        .setDescriptionsPath("descriptions", "descriptions")
        .setPropertiesLoadCharset(StandardCharsets.UTF_8)
        .build();
terminal.run();
```

- `runAsync()` 让终端在另一线程监听输入，主线程可继续执行其它逻辑；调用 `terminal.stop()` 可停止监听。
- 若提供了 `ExecutorService`，则监听任务会被 submit 到该线程池；否则 Simple-Terminal 会自行创建一个线程。
- 输入/输出流均可替换，便于在 GUI、嵌入式场景或测试中接入。

## API 详解（逐类说明）

本节遍历 `Terminal.Core` 中的每一个类，说明它的用途、适用场景，并给出一段简短示例。标注为「内部实现」的类一般不需要直接使用，但了解它们有助于你理解库的行为并在高级场景下进行扩展。

### 根包 `io.github.ctimetbukii.simpleterminal`

#### `Terminal`

终端顶层入口。负责扫描注册命令、监听输入、维护上下文状态。

**场景**：启动 / 停止命令行交互。

```java
Terminal terminal = TerminalBuilder.builder()
        .setAnchorPackageClassHelper(App.class)
        .build();
terminal.run();    // 阻塞监听（默认）
// ...业务逻辑...
terminal.stop();   // 停止监听（异步模式下可用）
```

#### `TerminalBuilder`

链式构造器，集中配置 `TerminalOptions` 的所有可选项。

**场景**：需要异步、自定义流、自定义线程池、i18n 路径等配置时。

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

承载全部运行时配置的对象。既可以传给 `new Terminal(options)`，也可以通过 `@InjectTerminalOption` 注入到处理器中读取。

**场景**：在处理器中读取当前输入输出流、stop 回调等运行时配置。

```java
@CommandProcessor("cfg")
public class CfgCommandProcessor {
    @InjectTerminalOption
    private TerminalOptions options;

    @CommandExecutor
    public void showIn() {
        // 读取当前终端使用的输入流
        options.getInputStream().available();
    }
}
```

#### `CommandSign`（内部实现）

一个命令执行方法的「签名」，由 `patterns`、参数类型、`stage`、`paraJoin`、转换策略共同决定。Simple-Terminal 用它去重并匹配用户输入。

**场景**：理解方法重载为何能被自动分发——每个重载对应一个独立的 `CommandSign`。

```java
// 下面两个方法会生成两个不同的 CommandSign，输入 "set 8080" 走 int 版，输入 "set prod" 走 String 版
@CommandExecutor
public void set(int port) { ... }

@CommandExecutor
public void set(String env) { ... }
```

#### `GlobalTransmitter`（内部实现）

进程级静态传递器，存放已注册命令集合、命令元信息、待关闭的资源方法。

**场景**：自定义 `help` / `stop` 时，可从中读取所有已注册命令及其描述。

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

### 注解包 `annotations`

#### `@CommandProcessor`

类级别注解，把一个类标记为某个命令的处理器。

**场景**：注册任意自定义命令。

```java
@CommandProcessor(value = "reload", alias = {"rl"}, descriptionKey = "cmd.reload")
public class ReloadCommandProcessor { ... }
```

#### `@CommandExecutor`

方法级别注解，标记处理普通命令的方法。

**场景**：`cmd arg1 arg2` 形式的命令。

```java
@CommandExecutor
public void set(String key, String value) {
    config.put(key, value);
}
```

#### `@SubcommandExecutor`

方法级别注解，标记子命令执行方法。

**场景**：同一命令下有多个带前缀的子选项（如 `-list`、`-add`）。

```java
@SubcommandExecutor("-list")
public void listAll() {
    users.forEach(System.out::println);
}
```

#### `@ContextCommandExecutor`

方法级别注解，标记多轮对话命令的某一轮处理方法，由 `stage` 区分。

**场景**：向导式交互，如分步收集信息。

```java
@ContextCommandExecutor(stage = 0)
public void askName(String[] args) {
    out.println("请输入年龄：");
}
```

#### `@ArgsIllegalExecutor`

方法级别注解，当传入参数不匹配任何 `CommandSign` 时触发。

**场景**：给用户友好的错误提示，而非让框架打印默认错误。

```java
@ArgsIllegalExecutor
public void onBadArgs(String[] args) {
    err.println("参数不合法，请使用 help 查看用法。实际收到：" + Arrays.toString(args));
}
```

#### `@InjectOutPrintStream`

向 `PrintStream` 字段注入终端标准输出流。

**场景**：在处理器中向终端输出，便于重定向时统一走终端流。

```java
@InjectOutPrintStream
private PrintStream out;
```

#### `@InjectErrPrintStream`

向 `PrintStream` 字段注入终端错误输出流。

**场景**：输出错误信息，使其跟随 `TerminalOptions.errOutputStream`。

```java
@InjectErrPrintStream
private PrintStream err;
```

#### `@InjectTerminalOption`

向 `TerminalOptions` 字段注入当前终端配置。

**场景**：在处理器内读取运行时配置（如自定义的 stop 回调、流对象）。

```java
@InjectTerminalOption
private TerminalOptions options;
```

#### `@InjectDefaultDescriptionProperties`

注入内置命令的描述 `Properties`（已按语言加载）。

**场景**：自定义 help 时，复用内置命令的 i18n 文案。

```java
@InjectDefaultDescriptionProperties
private Properties defaultDesc;
```

#### `@InjectCommandDescriptionProperties`

注入用户自定义命令的描述 `Properties`（已按语言加载）。

**场景**：在 help 中展示用户命令的中/英文描述。

```java
@InjectCommandDescriptionProperties
private Properties cmdDesc;
```

#### `@ParaDsKey`

参数级别注解，为方法参数指定描述 key，用于 `help [命令]` 展示参数名。

**场景**：让用户在 help 中看到有意义的参数名而非 `arg0`。

```java
@CommandExecutor
public void login(@ParaDsKey("para.username") String username,
                  @ParaDsKey("para.password") String password) { ... }
```

### 默认实现包 `defaults`

#### `DefaultHelpCommandProcessor`

内置 `help` 命令处理器。列出所有命令及详情。

**场景**：直接使用；若想自定义 help，注册一个名为 `help` 的处理器即可覆盖。

```java
// 注册后，内置 help 不会覆盖你的实现
@CommandProcessor("help")
public class MyHelpProcessor {
    @CommandExecutor
    public void help() {
        System.out.println("自定义帮助文档");
    }
}
```

#### `DefaultStopCommandProcessor`

内置 `stop` 命令处理器，支持延迟 / 定时关闭，并会先释放 `ClosableFlag` 资源。

**场景**：需要定时退出（如凌晨维护）或需要优雅释放资源时。

```java
// 在 2030-02-17 13:14:15.888 关闭进程
// stop -r 2030 02 17 13 14 15 888
```

### 处理包 `process`

#### `CommandHandler`（内部实现）

每个命令处理器类对应一个 `CommandHandler`，负责匹配 `CommandSign`、注入字段、反射调用执行方法。

**场景**：一般不直接使用。理解它有助于排查「为什么我的方法没被调用」——通常是 `patterns` 或参数类型不匹配导致没有任何 `CommandSign` 命中，进而走了 `@ArgsIllegalExecutor`。

#### `ExecuteResult` / `ExecuteState`（内部实现）

执行结果的载体与状态枚举。`ExecuteState` 包含四种状态，对上下文命令尤其重要：

| 状态 | 含义 |
| --- | --- |
| `SUCCEED` | 执行成功，进入下一轮 |
| `FAIL` | 执行失败，清空上下文 |
| `NEED_REPEAT` | 需要重新输入当前轮 |
| `STAGE_JUMP_TO` | 跳转到指定 stage |
| `CONTEXT_INTERRUPT` | 上下文中断，指令退出 |

**场景**：通过返回 `int` 触发 `STAGE_JUMP_TO`，实现分支式多轮对话。

```java
@ContextCommandExecutor(stage = 1)
public int chooseBranch(String choice) {
    if (choice.equals("advanced")) return 3;  // 跳到 stage 3
    return 2;                                  // 否则走 stage 2
}
```

#### `ArgsTypeTransformMethod`

枚举，定义四种字符串到目标类型的转换策略。

**场景**：对参数格式有严格要求时显式指定策略。

```java
@CommandExecutor(paraTransformMethod = ArgsTypeTransformMethod.CONVERT)
public void setPort(int port) { ... }   // "abc" 会被拒绝，"8080" 才通过
```

#### `TransformExecutor` / `ConvertExecutor` / `CoerceExecutor` / `InterpretExecutor` / `CoerceOrInterpretExecutor`（内部实现）

`ArgsTypeTransformMethod` 各枚举值对应的具体转换实现，底层调用 [elegant-convert-checker](https://github.com/CTimet/elegant-convert-checker)。

**场景**：一般不直接使用。需要自定义转换规则时，可参考它们的实现并基于 `elegant-convert-checker` 自行扩展。

### 标记包 `flags`

#### `ClosableFlag`

接口，标记需要在进程关闭时释放资源的处理器。

**场景**：处理器持有数据库连接、文件句柄、网络 socket 等资源时。

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

### 异常包 `exceptions`

#### `NeedRepeatException`

用户在执行方法中抛出，表示当前轮输入需要重新输入。

**场景**：用户输入不符合业务要求（但格式合法），希望重新询问。

```java
@ContextCommandExecutor(stage = 1)
public void askAge(String age) {
    int a = Integer.parseInt(age);
    if (a < 0 || a > 150) {
        out.println("年龄不合理，请重新输入：");
        throw new NeedRepeatException();  // 本轮回退，重新询问
    }
}
```

#### `AliasConflictWithCommandException` (内部使用，不建议外部抛出)

别名与已注册的正式命令名冲突时抛出。

**场景**：启动失败时排查别名设置。

```java
// 启动时报错：别名 "stop" 与内置命令 stop 冲突
@CommandProcessor(value = "exit", alias = {"stop"})
public class ExitProcessor { ... }
```

#### `ContextInterruptException`

需要中断上下文指令时抛出

**场景**：需要中断上下文指令时

#### `RepeatAliasException` (内部使用，不建议外部抛出)

同一个别名被重复注册时抛出。

**场景**：两个处理器用了相同别名。

```java
// 别名 "rl" 被注册两次 → 启动抛异常
@CommandProcessor(value = "reload", alias = {"rl"})
@CommandProcessor(value = "restart", alias = {"rl"})
```

#### `RepeatCommandSignException` (内部使用，不建议外部抛出)

同一类中存在 `patterns` 与参数类型完全相同的两个执行方法时抛出。

**场景**：方法重载时签名未区分开。

```java
// 两个 set(int) 签名相同 → 抛异常
@CommandExecutor public void set(int x) { ... }
@CommandExecutor public void set(int y) { ... }
```

#### `IllegalInjectObjectException` (内部使用，不建议外部抛出)

被注入注解标记的字段类型与期望不符时抛出。

**场景**：把 `@InjectOutPrintStream` 标在了非 `PrintStream` 字段上。

```java
@InjectOutPrintStream
private String out;  // 启动抛异常：类型必须是 PrintStream
```

#### `IllegalJumpStageException` (内部使用，不建议外部抛出)

上下文命令返回的跳转 stage 不合法（≤ 0）时抛出。

**场景**：执行方法返回了非正整数。

```java
@ContextCommandExecutor(stage = 1)
public int jump(String s) {
    return 0;  // 抛异常：跳转 stage 必须 > 0
}
```

#### `NoAccessibleNecessaryExecutor` (内部使用，不建议外部抛出)

处理器类中没有任何 `@CommandExecutor` 或 `@SubcommandExecutor` 方法时抛出。

**场景**：只写了 `@ArgsIllegalExecutor` 却忘了写真正的执行方法。

```java
@CommandProcessor("oops")
public class OopsProcessor {
    @ArgsIllegalExecutor
    public void onBad(String[] a) { }  // 抛异常：没有任何必要执行方法
}
```

#### `IllogicalPanicException` (内部使用，不建议外部抛出)

库自身不应到达的分支，抛出即代表 Simple-Terminal 出 bug，请提交 issue。

**场景**：遇到时请到仓库提交 issue 并附堆栈。

#### `RepeatException` (内部使用，不建议外部抛出)

重复异常的基类，`RepeatCommandSignException` 继承自它。一般不直接抛出。

### 工具包 `utils`

#### `CastUtil`（内部实现）

将字符串参数数组按 `CommandSign` 的类型数组进行转换。

**场景**：内部使用。需要自定义转换链路时可参考其实现。

#### `MethodUtil`（内部实现）

从带注解的 `Method` 构造 `CommandSign` 的工厂方法。

**场景**：内部使用。若你扩展了新的执行器注解，可借助它生成 `CommandSign`。

### 记录包 `utils.records`（内部实现，若您希望自己实现help方法，则可以使用）

| 类 | 作用 |
| --- | --- |
| `CommandInformation` | 一个命令处理器的完整元信息（类注解 + 所有执行方法信息），供 help 使用 |
| `ExecutorInformation` | 单个 `@CommandExecutor` 方法的元信息，按参数数量排序 |
| `SubExecutorInformation` | 单个 `@SubcommandExecutor` 方法的元信息 |
| `RunnableMethod` | 包装 `Method` 与实例的可运行对象，用于关闭资源时反射调用 |
| `TransmitterKey` | `GlobalTransmitter` 中使用的 key 常量（如 `ALL_COMMANDS`） |

**场景**：自定义 help 或 stop 时可能用到 `CommandInformation` 来遍历命令结构；其余为内部支撑类。

```java
// 自定义 help：遍历所有命令的元信息
Map<String, CommandInformation> map = GlobalTransmitter.getCommandInformationMap();
map.forEach((name, info) -> {
    System.out.println(name + " → " + info.getProcessorAnn().descriptionKey());
});
```

## 许可证

本项目基于 [MIT License](LICENSE) 发布，Copyright (c) 2026 CTimet bukii。
