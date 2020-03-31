package me.jacoblewis.socialmediaapi.controllers

import io.swagger.annotations.ApiOperation
import me.jacoblewis.socialmediaapi.exceptions.ConflictException
import me.jacoblewis.socialmediaapi.exceptions.NotFoundException
import me.jacoblewis.socialmediaapi.models.PostModel
import me.jacoblewis.socialmediaapi.models.RequestReceipt
import me.jacoblewis.socialmediaapi.repos.PostRepository
import me.jacoblewis.socialmediaapi.repos.UserRepository
import me.jacoblewis.socialmediaapi.utils.b64EncStr
import me.jacoblewis.socialmediaapi.utils.currentUserID
import me.jacoblewis.socialmediaapi.utils.value
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import java.util.*

@Component
@RestController
class PostsController {

    @Autowired
    lateinit var userRepo: UserRepository

    @Autowired
    lateinit var postRepo: PostRepository

    @GetMapping("/posts/myfeed")
    @ApiOperation("Get Feed for Current User")
    fun getMyFeed(): ResponseEntity<RequestReceipt<List<PostModel>>> {
        val currentUser =
            userRepo.findById(currentUserID).value ?: throw NotFoundException("unable to find current user")
        val followingUsers = currentUser.followingUsers ?: listOf()
        val posts =
            followingUsers.flatMap { postRepo.findByCreatorId(it.id ?: "") }.sortedByDescending { it.creationDate }
        return ResponseEntity.ok(RequestReceipt(200, "available posts in feed", posts))
    }

    @GetMapping("/posts/users/{userID}")
    @ApiOperation("Get Posts from a Specified User")
    fun getPostsForUser(@PathVariable("userID") userID: String): ResponseEntity<RequestReceipt<List<PostModel>>> {
        userRepo.findById(userID).value ?: throw NotFoundException("unable to find user with ID: `$userID`")
        val posts = postRepo.findByCreatorId(userID).sortedByDescending { it.creationDate }
        return ResponseEntity.ok(RequestReceipt(200, "available posts", posts))
    }

    @PostMapping("/posts", consumes = ["multipart/form-data"])
    @ApiOperation("Create a Post")
    fun createPost(
        @RequestPart("title") title: String,
        @RequestPart("content") content: String
    ): ResponseEntity<RequestReceipt<PostModel>> {
        val currentUserId =
            userRepo.findById(currentUserID).value?.id ?: throw NotFoundException("unable to find current user")
        var postID = ""
        do {
            postID = b64EncStr(UUID.randomUUID().toString())
        } while (postRepo.existsById(postID))

        val newPost = PostModel(postID, currentUserId, Date(), title, content, listOf())
        postRepo.save(newPost)
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestReceipt(201, "created post", newPost))
    }

    @PostMapping("/posts/likes/{postId}")
    @ApiOperation("Like a Post")
    fun likePost(@PathVariable("postId") postId: String): ResponseEntity<RequestReceipt<PostModel>> {
        val currentUserId =
            userRepo.findById(currentUserID).value?.id ?: throw NotFoundException("unable to find current user")
        val post = postRepo.findById(postId).value ?: throw NotFoundException("unable to find post with id: `$postId`")
        if (post.likes?.contains(currentUserId) == true) {
            throw ConflictException("already liked post")
        }
        post.likes = (post.likes ?: listOf()) + listOf(currentUserId).toSet().toList()
        postRepo.save(post)
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestReceipt(201, "post liked", post))
    }

    @DeleteMapping("/posts/likes/{postId}")
    @ApiOperation("Unlike a Post")
    fun unlikePost(@PathVariable("postId") postId: String): ResponseEntity<RequestReceipt<PostModel>> {
        val currentUserId =
            userRepo.findById(currentUserID).value?.id ?: throw NotFoundException("unable to find current user")
        val post = postRepo.findById(postId).value ?: throw NotFoundException("unable to find post with id: `$postId`")
        if (post.likes?.contains(currentUserId) == true) {
            post.likes = (post.likes ?: listOf()) - listOf(currentUserId).toSet().toList()
            postRepo.save(post)
            return ResponseEntity.ok(RequestReceipt(200, "post like removed", post))
        }
        throw ConflictException("you have not liked this post")
    }

}