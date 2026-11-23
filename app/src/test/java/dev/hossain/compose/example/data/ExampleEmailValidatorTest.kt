package dev.hossain.compose.example.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExampleEmailValidatorTest {
    private val validator = ExampleEmailValidator()

    @Test
    fun `isValidEmail - returns true when email contains at sign`() {
        assertThat(validator.isValidEmail("user@example.com")).isTrue()
    }

    @Test
    fun `isValidEmail - returns false when email lacks at sign`() {
        assertThat(validator.isValidEmail("invalid-email")).isFalse()
        assertThat(validator.isValidEmail("")).isFalse()
    }
}
