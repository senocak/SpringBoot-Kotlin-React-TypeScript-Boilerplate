package com.github.senocak.auth.service

import com.github.senocak.TestConstants
import com.github.senocak.domain.Comment
import com.github.senocak.factory.CommentFactory.createComment
import com.github.senocak.repository.CommentRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.Optional

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for CommentService")
class CommentServiceTest {
    private val commentRepository = Mockito.mock(CommentRepository::class.java)
    private var commentService =  CommentService(commentRepository)

    @Test
    fun given_whenGetAll_thenAssertResult() {
        // Given
        val paging: Pageable = PageRequest.of(1, 1)
        val comment: Comment = createComment()
        val commentList: MutableList<Comment> = ArrayList<Comment>()
        commentList.add(comment)
        val commentPage: Page<Comment> = PageImpl(commentList)
        Mockito.doReturn(commentPage).`when`(commentRepository).findAll(paging)
        // When
        val getAll: Page<Comment> = commentService.getAll(1, 1)
        // Then
        Assertions.assertEquals(commentPage, getAll)
    }

    @Test
    fun givenStringIdWhenFindByIdOrNameOrSlugThenAssertResult() {
        // Given
        val comment: Optional<Comment> = Optional.of(createComment())
        Mockito.doReturn(comment).`when`(commentRepository).findById(TestConstants.COMMENT_TITLE)
        // When
        val findByIdOrNameOrSlug: Comment? = commentService.findById(TestConstants.COMMENT_TITLE)
        // Then
        Assertions.assertEquals(comment.get(), findByIdOrNameOrSlug)
    }

    @Test
    fun givenCategoryWhenSaveCategoryThenAssertResult() {
        // Given
        val comment: Comment = createComment()
        Mockito.doReturn(comment).`when`(commentRepository).save<Comment>(comment)
        // When
        val savedComment: Comment = commentService.persist(comment)
        // Then
        Assertions.assertEquals(comment, savedComment)
    }
}