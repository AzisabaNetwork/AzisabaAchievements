package net.azisaba.azisabaachievements.cli.commands

import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import net.azisaba.azisabaachievements.api.network.Packet
import net.azisaba.azisabaachievements.api.network.PacketRegistry
import net.azisaba.azisabaachievements.cli.CLIMain
import net.azisaba.azisabaachievements.cli.Colors

@OptIn(ExperimentalCli::class)
object CommandDumpProtocol : Subcommand("dumpProtocol", "Shows all registered packets") {
    override fun execute() {
        CLIMain.init()
        val pair = CLIMain.api.packetRegistryPair
        val color1 = Colors.random()
        dumpProtocol(pair.clientRegistry, "${color1}Client ")
        dumpProtocol(pair.serverRegistry, "${Colors.random(color1)}Server ")
    }

    private fun dumpProtocol(registry: PacketRegistry, prefix: String) {
        var id = -1
        var packet: Class<out Packet<*>>?
        do {
            packet = registry.getById(++id)
            if (packet != null) {
                val hex = id.toString(16).padStart(2, '0')
                println("$prefix${Colors.GREEN}$id (0x$hex) ${Colors.RESET}${packet.simpleName}")
            }
        } while (packet != null)
    }
}