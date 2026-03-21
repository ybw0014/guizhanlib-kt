# Kommand Manual

Kommand is a Kotlin DSL for building Bukkit and Paper commands with Adventure components, nested subcommands, automatic help output, cooldowns, and tab completion helpers.

## Quick Start

```kotlin
baseCommand(plugin, "admin") {
    description = Component.text("Admin commands")
    permission = "example.admin"
    aliases = listOf("adm")

    execute {
        sender.sendMessage(Component.text("Available subcommands: reload, give"))
    }

    subCommand("reload") {
        description = Component.text("Reload the plugin")
        inheritParentPermission = true

        execute {
            sender.sendMessage(Component.text("Reloaded."))
        }
    }
}
```

And that's it, Kommand handles the registering for you.

## Root Commands and Subcommands

Use `subCommand(name)` to create nested command nodes.

```kotlin
val tools = baseCommand(plugin, "tools") {
    description = Component.text("Tool commands")
    permission = "example.tools"

    subCommand("give") {
        description = Component.text("Give a tool")
        usage = "<player> <material>"
        inheritParentPermission = true

        execute {
            val targetName = args[0]
            val materialName = args[1]
            sender.sendMessage(Component.text("Giving $materialName to $targetName"))
        }
    }

    subCommand("list") {
        description = Component.text("List available tools")
        inheritParentPermission = true

        execute {
            sender.sendMessage(Component.text("Hammer, Wrench, Scanner"))
        }
    }
}
```

Subcommands are recursive, so you can nest them as deeply as you want.

```kotlin
subCommand("config") {
    subCommand("reload") {
        execute {
            sender.sendMessage(Component.text("Config reloaded"))
        }
    }
}
```

## How Execution Works

Each command node can:

- execute its own handler
- contain subcommands
- do both at the same time

When a player or console runs a command, Kommand processes it in this order:

1. sender-type restriction
2. permission check
3. cooldown check
4. subcommand lookup
5. executor invocation if the usage matches
6. automatic help output if nothing matches

That means you usually do not need to build your own fallback help command.

## Descriptions

`description` accepts any `ComponentLike`.

```kotlin
description = Component.text("Admin commands")
```

For simple legacy-color text, use the string helper:

```kotlin
description("&aAdmin commands")
```

For translatable text, use `descriptionTranslatable`:

```kotlin
descriptionTranslatable("example.command.admin.description")
```

## Usage Strings

`usage` is a plain string that describes the arguments accepted by a command node.

```kotlin
usage = "<player> [amount]"
```

Kommand validates usage with these rules:

- arguments inside `<...>` are required
- arguments inside `[...]` are optional
- the total argument count cannot exceed the number of usage parts

If the arguments do not match the declared usage, Kommand shows help for the current command path.

## Executors

Kommand supports reusable `KommandExecutor` handlers as well as inline lambdas.

### Context Receiver Style

```kotlin
execute {
    sender.sendMessage(Component.text("Label: $label"))
    sender.sendMessage(Component.text("Args: ${args.joinToString()}"))
}
```

### Explicit Sender and Args Style

```kotlin
execute { sender, args ->
    sender.sendMessage(Component.text("Received ${args.size} args"))
}
```

### Reusable `KommandExecutor`

This is useful when you want handlers in separate files.

```kotlin
val reloadHandler = KommandExecutor {
    it.sender.sendMessage(Component.text("Reloaded."))
}

baseCommand(plugin, "admin") {
    subCommand("reload") {
        execute(reloadHandler)
    }
}
```

Executors return `Unit`.

## KommandContext

Inside `execute {}` and `tabComplete {}`, you work with `KommandContext`.

Available properties:

- `kommand`: the current `Kommand` node
- `sender`: the `CommandSender`
- `command`: the Bukkit `Command`
- `label`: the label used to run the command
- `args`: the remaining arguments as `List<String>`
- `argsArray`: the same arguments as `Array<String>`
- `size`: argument count

Useful helpers:

```kotlin
val first = argOrNull(0)
val second = argOrNull(1)
val sameAsArgs0 = this[0]
```

For nested subcommands, `args` always represent the remaining arguments for the current node.

## Permissions

Set a permission directly:

```kotlin
permission = "example.admin.reload"
```

Set a custom permission message:

```kotlin
permissionMessage = Component.text("You are not allowed to do that.")
```

Or use the string helper:

```kotlin
permissionMessage("&cYou are not allowed to do that.")
```

### Permission Inheritance

If a subcommand should follow the common `parent.child` naming convention, enable `inheritParentPermission`.

```kotlin
baseCommand(plugin, "admin") {
    permission = "example.admin"

    subCommand("reload") {
        inheritParentPermission = true
        execute {
            sender.sendMessage(Component.text("Reloaded"))
        }
    }
}
```

The subcommand above gets the effective permission `example.admin.reload`.

## Sender Restrictions

Kommand supports sender-type checks before execution.

### Player Only

```kotlin
subCommand("kit") {
    playerOnly()

    execute {
        val player = sender as org.bukkit.entity.Player
        player.sendMessage(Component.text("Kit received"))
    }
}
```

### Console Only

```kotlin
subCommand("sync") {
    consoleOnly()

    execute {
        sender.sendMessage(Component.text("Console sync complete"))
    }
}
```

You can override the rejection message with `senderTypeMessage`.

## Cooldowns

Cooldowns are only enforced for players. Console senders bypass them.

Use seconds:

```kotlin
cooldown(10)
```

Use milliseconds:

```kotlin
cooldownMs(1500)
```

Set a custom cooldown message:

```kotlin
cooldownMessage = Component.text("Please slow down.")
```

Cooldowns are tracked per player and per full command path.

## Aliases

Root commands support aliases through `BaseKommandBuilder.aliases`.

```kotlin
baseCommand(plugin, "admin") {
    aliases = listOf("adm", "a")
    description = Component.text("Admin commands")
}
```

Aliases are applied automatically during root command registration. You do not need a separate alias registration call.

## Help Output

Kommand automatically generates help lines from accessible commands.

Default output format:

```text
/<path> <usage> - <description>
```

### Custom Help Formatter

Use `helpFormat` or `customHelp` to replace the default formatter.

```kotlin
baseCommand(plugin, "admin") {
    helpFormat { sender, label, commands ->
        sender.sendMessage(Component.text("Available commands:"))
        commands.forEach { command ->
            sender.sendMessage(Component.text("- ${command.fullUsage(label)}"))
        }
    }
}
```

A formatter defined on a parent command is inherited by child commands unless a deeper node overrides it.

## Tab Completion

Kommand supports both direct tab completion and a position-based DSL.

### Direct `tabComplete`

```kotlin
subCommand("mode") {
    tabComplete {
        when (size) {
            1 -> listOf("survival", "creative", "adventure", "spectator")
            else -> emptyList()
        }
    }
}
```

Or:

```kotlin
subCommand("mode") {
    tabComplete { _, args ->
        if (args.size == 1) listOf("survival", "creative") else emptyList()
    }
}
```

### Position-Based `tab {}` DSL

```kotlin
subCommand("give") {
    usage = "<player> <material> [amount]"

    tab {
        arg(0, suggestPlayers())
        arg(1, suggestMaterials())
        arg(2, suggest("1", "16", "64"))
    }
}
```

Kommand filters executor suggestions by the current partial input automatically.

## Full Example

```kotlin
val admin = baseCommand(plugin, "admin") {
    description = Component.text("Administrative commands")
    permission = "example.admin"
    aliases = listOf("adm")

    helpFormat { sender, label, commands ->
        sender.sendMessage(Component.text("Admin help"))
        commands.forEach { command ->
            sender.sendMessage(
                Component.text(command.fullUsage(label))
                    .append(Component.text(" - "))
                    .append(command.description.asComponent())
            )
        }
    }

    subCommand("reload") {
        description = Component.text("Reload the plugin")
        inheritParentPermission = true
        cooldown(3)

        execute {
            sender.sendMessage(Component.text("Reloaded."))
        }
    }

    subCommand("give") {
        description = Component.text("Give an item to a player")
        usage = "<player> <material> [amount]"
        inheritParentPermission = true
        playerOnly()
        senderTypeMessage = Component.text("This subcommand is player-only.")

        tab {
            arg(0, suggestPlayers())
            arg(1, suggestMaterials())
            arg(2, suggest("1", "16", "64"))
        }

        execute {
            val target = args[0]
            val material = args[1]
            val amount = argOrNull(2) ?: "1"
            sender.sendMessage(Component.text("Giving $amount of $material to $target"))
        }
    }

    execute {
        sendHelp(sender, label)
    }
}
```

## Practical Notes

- `description`, `permissionMessage`, `senderTypeMessage`, and `cooldownMessage` all use Adventure-compatible values.
- Help only includes commands that pass the permission check for the sender.
- Subcommand lookup is case-insensitive.
- If a command node has both subcommands and an executor, the matching subcommand wins when the first remaining argument matches a child name.
- Cooldowns are stored in memory.
- Usage validation is argument-count based. It does not validate types or argument semantics.

## Summary

Kommand gives you a compact Kotlin DSL for:

- root commands via `baseCommand(plugin, name)`
- automatic root command registration
- nested subcommands
- Adventure-based descriptions and messages
- permission checks
- sender restrictions
- cooldowns
- automatic help
- reusable `KommandExecutor` handlers
- tab completion helpers

If you stay within those primitives, you can build most plugin command trees without writing your own command dispatcher or manual registration code.
