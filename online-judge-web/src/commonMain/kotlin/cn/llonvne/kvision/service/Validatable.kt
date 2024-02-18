package cn.llonvne.kvision.service

import kotlinx.serialization.Serializable

interface Validatable {
    fun validate(): ValidateResult

    companion object {
        @Serializable
        sealed interface ValidateResult {
            fun toBoolean(): Boolean {
                return when (this) {
                    Ok -> true
                    is Failed -> false
                }
            }
        }

        @Serializable
        data object Ok : ValidateResult

        @Serializable
        data class Failed(val message: String) : ValidateResult

        fun interface Validator {
            fun validate(): ValidateResult
        }

        interface ValidatorDsl {
            fun add(validator: Validator)
        }

        inline fun <reified T> ValidatorDsl.on(
            value: T,
            failMessage: String,
            crossinline predicate: T.() -> Boolean
        ) {
            add {
                return@add if (predicate.invoke(value)) {
                    Ok
                } else {
                    Failed(failMessage)
                }
            }
        }

        data class ValidatorDslImpl(private val validators: MutableList<Validator> = mutableListOf()) : ValidatorDsl {
            override fun add(validator: Validator) {
                validators.add(validator)
            }

            fun result(): ValidateResult {
                validators.forEach {
                    when (val result = it.validate()) {
                        Ok -> {}
                        is Failed -> {
                            return result
                        }
                    }
                }
                return Ok
            }
        }

        fun validate(action: ValidatorDsl.() -> Unit): ValidateResult {
            val validatorDsl = ValidatorDslImpl()

            validatorDsl.action()

            return validatorDsl.result()
        }
    }
}