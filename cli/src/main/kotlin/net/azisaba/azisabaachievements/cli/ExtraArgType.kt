package net.azisaba.azisabaachievements.cli

import kotlinx.cli.ArgType
import kotlinx.cli.ParsingException

object ExtraArgType {
    object Long : ArgType<kotlin.Long>(true) {
        override val description: kotlin.String
            get() = "{ Long }"

        override fun convert(value: kotlin.String, name: kotlin.String): kotlin.Long =
            value.toLongOrNull()
                ?: throw ParsingException("Option $name is expected to be long number. $value is provided.")
    }
}
