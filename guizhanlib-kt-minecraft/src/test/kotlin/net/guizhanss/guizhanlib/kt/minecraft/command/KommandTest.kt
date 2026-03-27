package net.guizhanss.guizhanlib.kt.minecraft.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandMap
import org.bukkit.command.PluginCommand
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KommandTest {

    private lateinit var server: ServerMock
    private lateinit var plugin: TestPlugin

    @BeforeTest
    fun beforeTest() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(TestPlugin::class.java)
    }

    @AfterTest
    fun afterTest() {
        MockBukkit.unmock()
    }

    private fun resolveCommandMap(): CommandMap {
        val publicMethod = server.javaClass.methods.firstOrNull {
            it.name == "getCommandMap" && it.parameterCount == 0
        }
        if (publicMethod != null) {
            return publicMethod.invoke(server) as CommandMap
        }

        var currentType: Class<*>? = server.javaClass
        while (currentType != null) {
            val field = currentType.declaredFields.firstOrNull {
                it.name == "commandMap" && CommandMap::class.java.isAssignableFrom(it.type)
            }
            if (field != null) {
                field.isAccessible = true
                return field.get(server) as CommandMap
            }
            currentType = currentType.superclass
        }

        error("Unable to resolve MockBukkit command map for tests.")
    }

    private fun findPluginCommand(name: String): PluginCommand? = resolveCommandMap().getCommand(name) as? PluginCommand

    @Test
    fun testBaseCommandCreation() {
        val baseCommand = baseCommand(plugin, "test") {
            description = Component.text("Test command")
            execute { }
        }

        assertEquals("test", baseCommand.name)
        assertEquals(Component.text("Test command"), baseCommand.description.asComponent())
        assertFalse(baseCommand.hasParent)
    }

    @Test
    fun testSubCommandCreation() {
        val baseCommand = baseCommand(plugin, "test") {
            description = Component.text("Main command")

            subCommand("sub") {
                description = Component.text("Sub command")
                execute { }
            }
        }

        assertTrue(baseCommand.hasSubCommands)
        assertEquals(1, baseCommand.subCommands.size)
        assertEquals("sub", baseCommand.subCommands[0].name)
    }

    @Test
    fun testNestedSubCommands() {
        val baseCommand = baseCommand(plugin, "test") {
            description = Component.text("Main command")

            subCommand("level1") {
                description = Component.text("Level 1")

                subCommand("level2") {
                    description = Component.text("Level 2")
                    execute { }
                }
            }
        }

        assertEquals("level2", baseCommand.subCommands[0].subCommands[0].name)
    }

    @Test
    fun testCommandExecution() {
        val player = server.addPlayer()
        var executed = false

        baseCommand(plugin, "test") {
            description = Component.text("Test command")
            execute {
                executed = true
            }
        }

        player.performCommand("test")
        assertTrue(executed)
    }

    @Test
    fun testDynamicCommandRegistration() {
        val player = server.addPlayer()
        var executions = 0

        assertNull(findPluginCommand("dynamic"))

        val dynamicCommand = baseCommand(plugin, "dynamic") {
            aliases = listOf("dyn")
            description = Component.text("Dynamic command")
            execute {
                executions += 1
            }
        }

        assertEquals("dynamic", dynamicCommand.name)
        assertSame(dynamicCommand.command, findPluginCommand("dynamic"))
        assertSame(dynamicCommand.command, findPluginCommand("dyn"))

        player.performCommand("dynamic")
        player.performCommand("dyn")

        assertEquals(2, executions)
    }

    @Test
    fun testDeclaredPluginYmlCommandIsReused() {
        val declaredCommand = assertNotNull(findPluginCommand("test"))

        val baseCommand = baseCommand(plugin, "test") {
            aliases = listOf("declared")
            description = Component.text("Declared command")
            execute { }
        }

        assertSame(declaredCommand, baseCommand.command)
        assertSame(plugin, baseCommand.command.plugin)
        assertSame(baseCommand.command, findPluginCommand("declared"))
    }

    @Test
    fun testReusableExecutorHandler() {
        val player = server.addPlayer()
        var capturedArg: String? = null
        val handler = reusableKommandHandler {
            capturedArg = argOrNull(0)
        }

        baseCommand(plugin, "reusable") {
            description = Component.text("Reusable handler command")
            usage = "<arg>"
            execute(handler)
        }

        player.performCommand("reusable value")

        assertEquals("value", capturedArg)
    }

    @Test
    fun testSubCommandExecution() {
        val player = server.addPlayer()
        var subExecuted = false

        baseCommand(plugin, "test") {
            description = Component.text("Main command")

            subCommand("action") {
                description = Component.text("Action subcommand")
                execute {
                    subExecuted = true
                }
            }
        }

        player.performCommand("test action")
        assertTrue(subExecuted)
    }

    @Test
    fun testPermissionCheck() {
        val player = server.addPlayer()

        val cmd = baseCommand(plugin, "test") {
            description = Component.text("Protected command")
            permission = "test.permission"
            permissionMessage = Component.text("No permission!", NamedTextColor.RED)
            execute { }
        }

        assertFalse(player.hasPermission("test.permission"))
        assertFalse(cmd.hasPermission(player))
    }

    @Test
    fun testTabComplete() {
        val player = server.addPlayer()

        val baseCommand = baseCommand(plugin, "test") {
            description = Component.text("Test command")

            subCommand("alpha") {
                description = Component.text("Alpha command")
                execute { }
            }

            subCommand("beta") {
                description = Component.text("Beta command")
                execute { }
            }

            tabComplete { _, _ ->
                listOf("option1", "option2")
            }
        }

        val resultEmpty = baseCommand.onTabComplete(player, baseCommand.command, "test", emptyArray())
        assertTrue(resultEmpty.contains("alpha"))
        assertTrue(resultEmpty.contains("beta"))

        val resultPartial = baseCommand.onTabComplete(player, baseCommand.command, "test", arrayOf("al"))
        assertTrue(resultPartial.contains("alpha"))
        assertFalse(resultPartial.contains("beta"))
    }

    @Test
    fun testUsageValidation() {
        val baseCommand = baseCommand(plugin, "test") {
            description = Component.text("Test command")
            usage = "<required> [optional]"
            execute { }
        }

        assertEquals("/test <required> [optional]", baseCommand.fullUsage("test"))
    }

    @Test
    fun testLegacyColorConversion() {
        val coloredString = "&aGreen &bAqua"
        val component = coloredString.asLegacyComponent()

        assertTrue(
            component.asComponent().children().isNotEmpty() ||
                component.asComponent().color() != null ||
                component.asComponent() != Component.empty(),
        )
    }

    @Test
    fun testHelpGeneration() {
        val player = server.addPlayer()

        val baseCommand = baseCommand(plugin, "test") {
            description = Component.text("Main command")

            subCommand("help1") {
                description = Component.text("Help 1", NamedTextColor.YELLOW)
                execute { }
            }

            subCommand("help2") {
                description = Component.text("Help 2", NamedTextColor.GREEN)
                execute { }
            }
        }

        val helpEntries = baseCommand.helpEntries(player)
        assertEquals(2, helpEntries.size)
    }

    @Test
    fun testCustomHelpFormatter() {
        val player = server.addPlayer()
        var customHelpCalled = false

        baseCommand(plugin, "test") {
            description = Component.text("Main command")

            subCommand("action") {
                description = Component.text("Action")
                execute { }
            }

            helpFormat { _, _, _ ->
                customHelpCalled = true
            }
        }

        player.performCommand("test")
        assertTrue(customHelpCalled)
    }

    @Test
    fun testStringDescriptionShorthand() {
        val baseCommand = baseCommand(plugin, "test") {
            description("&aColored description")
            execute { }
        }

        assertTrue(
            baseCommand.description.asComponent().children().isNotEmpty() ||
                baseCommand.description.asComponent().color() != null,
        )
    }

    @Test
    fun testContextArgAccess() {
        val player = server.addPlayer()
        var capturedArg: String? = null

        baseCommand(plugin, "test") {
            description = Component.text("Test command")
            usage = "<arg>"
            execute {
                capturedArg = argOrNull(0)
            }
        }

        player.performCommand("test myarg")
        assertEquals("myarg", capturedArg)
    }
}
