package me.jacoblewis.socialmediaapi.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import me.jacoblewis.socialmediaapi.utils.Authorities
import me.jacoblewis.socialmediaapi.utils.authfilter.AuthFilter
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
@JsonInclude(NON_NULL)
data class UserModel(
    @Id
    @AuthFilter([Authorities.STANDARD])
    var id: String?,
    var firstName: String?,
    var lastName: String?,
    var email: String?,
    @AuthFilter([Authorities.ADMIN])
    var passwordHash: String?,
    @AuthFilter([Authorities.ADMIN])
    var token: String?,
    @AuthFilter([Authorities.ADMIN])
    var authorities: List<String>?,
    var followingUsers: List<UserModel>?
) {

    fun createFollowCopy(): UserModel = UserModel(id, null, null, null, null, null, null, null)
}