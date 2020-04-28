package me.jacoblewis.socialmediaapi.controllers

import io.swagger.annotations.ApiOperation
import me.jacoblewis.socialmediaapi.exceptions.BadRequestException
import me.jacoblewis.socialmediaapi.exceptions.ConflictException
import me.jacoblewis.socialmediaapi.exceptions.NotFoundException
import me.jacoblewis.socialmediaapi.models.ConversationMessageModel
import me.jacoblewis.socialmediaapi.models.ConversationModel
import me.jacoblewis.socialmediaapi.models.MessageModel
import me.jacoblewis.socialmediaapi.models.RequestReceipt
import me.jacoblewis.socialmediaapi.repos.ConversationRepository
import me.jacoblewis.socialmediaapi.repos.MessageRepository
import me.jacoblewis.socialmediaapi.repos.UserRepository
import me.jacoblewis.socialmediaapi.utils.authfilter.filterByAuth
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
class MessengerController {

    @Autowired
    lateinit var userRepo: UserRepository

    @Autowired
    lateinit var conversationRepo: ConversationRepository

    @Autowired
    lateinit var messageRepo: MessageRepository

    @GetMapping("/messages/overview")
    @ApiOperation(
        "Get Messages overview for Current User",
        notes = "Each conversation includes the most recent message"
    )
    fun getMyMessages(): ResponseEntity<RequestReceipt<List<ConversationMessageModel>>> {
        val currentUserId =
            userRepo.findById(currentUserID).value?.id ?: throw NotFoundException("unable to find current user")
        val associatedConversations = conversationRepo.findConversationByMemberId(currentUserId).map {
            val members = userRepo.findAllById(it.memberIds).map { u -> u.filterByAuth() }
            val topMessage =
                messageRepo.findTopByConversationIdOrderByCreationDateDesc(it.id)?.let { m -> listOf(m) } ?: listOf()
            ConversationMessageModel(it.id, members.toList(), it.name, topMessage)
        }
        return ResponseEntity.ok(
            RequestReceipt(
                200,
                "each conversation includes the most recent message",
                associatedConversations
            )
        )
    }

    @GetMapping("/messages/conversations/{conversationID}")
    @ApiOperation("Get Messages from from a Conversation")
    fun getConversation(@PathVariable("conversationID") conversationID: String): ResponseEntity<RequestReceipt<ConversationMessageModel>> {
        val currentUserId =
            userRepo.findById(currentUserID).value?.id ?: throw NotFoundException("unable to find current user")
        val conversation = conversationRepo.findById(conversationID).value
            ?: throw NotFoundException("unable to find conversation with id `$conversationID`")
        if (!conversation.memberIds.contains(currentUserId)) {
            throw ConflictException("your user does not have access to this conversation")
        }
        val messages = messageRepo.findByConversationId(conversation.id).sortedByDescending { it.creationDate }
        val users = userRepo.findAllById(conversation.memberIds).map { u -> u.filterByAuth() }
        val conversationMessageModel =
            ConversationMessageModel(conversation.id, users.toList(), conversation.name, messages)
        return ResponseEntity.ok(RequestReceipt(200, "conversation found", conversationMessageModel))
    }

    @PostMapping("/messages/conversations", consumes = ["multipart/form-data"])
    @ApiOperation("Create a Conversation", notes = "`members` should be comma separated")
    fun createConversation(
        @RequestPart("name", required = false) name: String?,
        @RequestPart("memberIds") memberIds: String
    ): ResponseEntity<RequestReceipt<ConversationMessageModel>> {
        val currentUserId =
            userRepo.findById(currentUserID).value?.id ?: throw NotFoundException("unable to find current user")
        val memberIdsSet = memberIds.split(",").mapNotNull { userRepo.findById(it.trim()).value?.id }.toMutableSet()
        memberIdsSet.add(currentUserId)
        if (memberIdsSet.size < 2) {
            throw BadRequestException("Not enough members. Must have at least 2 members including user requesting conversation creation")
        }
        var convoID = ""
        do {
            convoID = b64EncStr(UUID.randomUUID().toString())
        } while (conversationRepo.existsById(convoID))
        val newConversation = ConversationModel(convoID, memberIdsSet.toList(), name ?: "")
        conversationRepo.save(newConversation)
        // Get newly created conversation and return it
        val conversation = getConversation(convoID).body?.body
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestReceipt(201, "conversation created", conversation))
    }

    @PostMapping("/messages/conversations/{conversationID}/members", consumes = ["multipart/form-data"])
    @ApiOperation("Add a Member to a Conversation")
    fun addMemberToConversation(
        @PathVariable("conversationID") conversationID: String,
        @RequestPart("userID") userID: String
    ): ResponseEntity<RequestReceipt<ConversationModel>> {
        val conversation = conversationRepo.findById(conversationID).value
            ?: throw NotFoundException("unable to find conversation with id `$conversationID`")
        val userIdToAdd =
            userRepo.findById(userID).value?.id ?: throw NotFoundException("unable to find user with id `$userID`")
        if (conversation.memberIds.contains(userIdToAdd)) {
            throw ConflictException("user is already a member of conversation")
        }
        conversation.memberIds = conversation.memberIds + listOf(userIdToAdd)
        conversationRepo.save(conversation)
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestReceipt(201, "member added", conversation))
    }

    @DeleteMapping("/messages/conversations/{conversationID}/members")
    @ApiOperation("Remove yourself as a Member from a Conversation", notes = "messages will remain")
    fun removeMemberFromConversation(
        @PathVariable("conversationID") conversationID: String
    ): ResponseEntity<RequestReceipt<ConversationModel>> {
        val userID =
            userRepo.findById(currentUserID).value?.id ?: throw NotFoundException("unable to find current user")
        val conversation = conversationRepo.findById(conversationID).value
            ?: throw NotFoundException("unable to find conversation with id `$conversationID`")
        val userIdToAdd =
            userRepo.findById(userID).value?.id ?: throw NotFoundException("unable to find user with id `$userID`")
        if (conversation.memberIds.contains(userIdToAdd)) {
            conversation.memberIds = conversation.memberIds - listOf(userIdToAdd)
            conversationRepo.save(conversation)
            return ResponseEntity.ok(RequestReceipt(200, "member `$userID` removed", conversation))
        }
        throw ConflictException("user is already a member of conversation")
    }


    @PostMapping("/messages/conversations/{conversationID}/messages", consumes = ["multipart/form-data"])
    @ApiOperation("Send a Message")
    fun sendMessageToConversation(
        @PathVariable("conversationID") conversationID: String,
        @RequestPart("text") text: String
    ): ResponseEntity<RequestReceipt<MessageModel>> {
        val currentUserId =
            userRepo.findById(currentUserID).value?.id ?: throw NotFoundException("unable to find current user")
        val conversation = conversationRepo.findById(conversationID).value
            ?: throw NotFoundException("unable to find conversation with id `$conversationID`")
        if (!conversation.memberIds.contains(currentUserId)) {
            throw ConflictException("your user does not have access to this conversation")
        }
        var messageID = ""
        do {
            messageID = b64EncStr(UUID.randomUUID().toString())
        } while (conversationRepo.existsById(messageID))
        val newMessage = MessageModel(messageID, conversation.id, Date(), currentUserId, text)
        messageRepo.save(newMessage)
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestReceipt(201, "message sent", newMessage))
    }
}