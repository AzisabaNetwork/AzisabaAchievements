# AzisabaAchievements

## Player progress

Achievement definitions (criteria, names, rewards) live in this plugin. Progress counts come from a `ProgressProvider`
registered with Bukkit Services.

By default, a built-in provider stores progress in the `player_achievements` table (through the proxy). It is
registered with `ServicePriority.Lowest`, so it is only used when no other provider is registered. It supports
`addProgress` and `setProgress`; stored counts are clamped to `0..required count`.

To manage progress elsewhere, register your own `ProgressProvider` with a higher priority:

```java
Bukkit.getServicesManager().register(
        ProgressProvider.class, impl, plugin, ServicePriority.Normal);
```

`getProgress(uuid)` must be keyed by this plugin's achievement keys (for example `azisaba:general/foo`).
Missing keys are treated as 0.

After progress changes, fire `ProgressUpdatedEvent` so unlocks and the GUI can refresh without a rejoin:

```java
new ProgressUpdatedEvent(player.getUniqueId()).callEvent();
```

Providers may optionally override `addProgress(uuid, key, amount)` and `setProgress(uuid, key, count)` to accept
writes. When they do, `AchievementManager#progressAchievement` / `AchievementManager#setAchievementProgress` and the
following Spigot commands write through the provider, then refresh the cache and send unlocks:

```text
/achievements progress <player> <key> <amount>
/achievements setprogress <player> <key> <count>
```

## Achievement rewards

Achievement rewards are stored in MariaDB and executed by the Spigot server where the player is online.
Set a logical server id on every Spigot server:

```yaml
rewards:
  server-id: server-a
```

Configure rewards from Velocity:

```text
/vachievements achievement <key> reward add @origin <command...>
/vachievements achievement <key> reward add server-a,server-b <command...>
/vachievements achievement <key> reward list
/vachievements reward delivery list <player>
/vachievements reward delivery requeue <delivery-id>
```

Commands run as the console and support `{player}`, `{uuid}`, `{achievement}`, and `{server}`.
Reward delivery is at-most-once: a delivery is claimed in the database before its command runs. Update Velocity
and every Spigot server together when the network protocol id changes.

## License

`api` and `common` directories are licensed under MIT License.

Everything else, like `spigot` and `velocity` directories, are licensed under GNU General Public License v3.0.
