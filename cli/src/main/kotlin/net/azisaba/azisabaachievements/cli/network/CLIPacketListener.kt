package net.azisaba.azisabaachievements.cli.network

import net.azisaba.azisabaachievements.api.network.ProxyPacketListener
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyAddAchievementTranslation
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyCreateAchievement
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyFetchAchievement
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyProgressAchievement
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestData
import net.azisaba.azisabaachievements.api.network.packet.PacketProxyRequestPlayerData

object CLIPacketListener : ProxyPacketListener {
    override fun handle(packet: PacketProxyRequestData) {}
    override fun handle(packet: PacketProxyCreateAchievement) {}
    override fun handle(packet: PacketProxyFetchAchievement) {}
    override fun handle(packet: PacketProxyProgressAchievement) {}
    override fun handle(packet: PacketProxyAddAchievementTranslation) {}
    override fun handle(packet: PacketProxyRequestPlayerData) {}
}
