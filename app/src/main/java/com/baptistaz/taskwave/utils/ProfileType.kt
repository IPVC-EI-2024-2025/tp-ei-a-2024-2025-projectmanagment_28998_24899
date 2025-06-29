package com.baptistaz.taskwave.utils

enum class ProfileType(val db: String, val label: String) {
    ADMIN ("ADMIN",  "Admin"),
    GESTOR("GESTOR", "Gestor"),
    USER  ("USER",   "User");

    companion object {
        fun fromLabel(label: String) =
            values().first { it.label.equals(label, true) }
    }
}
