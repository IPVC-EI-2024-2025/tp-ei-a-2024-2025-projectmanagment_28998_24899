import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id_user") val id_user: String? = null,
    @SerializedName("name") val name: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("password") val password: String = "",
    @SerializedName("profiletype") val profileType: String = "",
    @SerializedName("photo") val photo: String = "",
    @SerializedName("phonenumber") val phoneNumber: String = "",
    @SerializedName("auth_id") val authId: String = ""
)
