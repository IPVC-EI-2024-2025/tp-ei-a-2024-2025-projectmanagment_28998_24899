package com.baptistaz.taskwave.utils

enum class ProfileType(val db: String, val label: String) {
    ADMIN ("ADMIN",  "Admin"),
    GESTOR("GESTOR", "Gestor"),
    USER  ("USER",   "com.baptistaz.taskwave.data.model.auth.User");

    companion object {
        fun fromLabel(label: String) =
            values().first { it.label.equals(label, true) }
    }
}
