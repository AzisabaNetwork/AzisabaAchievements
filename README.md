# AzisabaAchievements

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
