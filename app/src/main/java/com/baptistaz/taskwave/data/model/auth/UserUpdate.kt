package com.baptistaz.taskwave.data.model.auth

/**
 * Used for partial updates of a user profile.
 *
 * @property name Updated full name.
 * @property email Updated email address.
 * @property username Updated username.
 * @property phonenumber Updated phone number.
 * @property profiletype Updated user role/profile type.
 */
data class UserUpdate(
    val name: String? = null,
    val email: String? = null,
    val username: String? = null,
    val phonenumber: String? = null,
    val profiletype: String? = null
)
