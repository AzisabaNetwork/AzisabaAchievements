package net.azisaba.azisabaachievements.cli

fun Boolean?.color() = if (this == true) Colors.GREEN else Colors.RED

fun Boolean?.toColored() = "${color()}$this${Colors.RESET}"
