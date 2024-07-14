package online.mempool.fatline.data

import MessageOuterClass.UserDataType
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import userDataBody

@Serializable
@Entity
data class Profile(
    @PrimaryKey
    val fid: Long,
    val username: String?,
    val display_name: String?,
    val profile_picture: String?,
    val bio: String?,
    val url: String?
) {
    val displayTag: String
        get() = "@${username ?: ("~$fid")}"


    companion object {
        enum class UpdatedField {
            DISPLAY_NAME,
            BIO,
            URL
        }

        private fun UpdatedField.toType() = when (this) {
            UpdatedField.DISPLAY_NAME -> UserDataType.USER_DATA_TYPE_DISPLAY
            UpdatedField.BIO -> UserDataType.USER_DATA_TYPE_BIO
            UpdatedField.URL -> UserDataType.USER_DATA_TYPE_URL
        }

        fun profileUpdateBody(updateType: UpdatedField, newValue: String) = userDataBody {
            type = updateType.toType()
            value = newValue
        }
    }

}