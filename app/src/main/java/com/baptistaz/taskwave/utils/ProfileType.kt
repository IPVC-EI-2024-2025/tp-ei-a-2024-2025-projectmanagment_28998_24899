package com.baptistaz.taskwave.utils

/**
 * User profile types with associated database values and display labels.
 *
 * @property db Value stored in the database.
 * @property label Readable label used in the UI.
 */
enum class ProfileType(val db: String, val label: String) {
    ADMIN ("ADMIN",  "Admin"),
    GESTOR("GESTOR", "Gestor"),
    USER  ("USER",   "User");

    companion object {
        /**
         * ProfileType that matches a given label.
         *
         * @param label UI label ("Admin", "Gestor").
         * @return Corresponding ProfileType enum.
         */
        fun fromLabel(label: String): ProfileType =
            values().first { it.label.equals(label, true) }
    }
}
