package com.github.senocak.auth.service

import com.github.senocak.domain.Category
import com.github.senocak.domain.Post
import com.github.senocak.domain.User
import com.github.senocak.factory.CategoryFactory.createCategory
import com.github.senocak.factory.PostFactory.createPost
import com.github.senocak.factory.UserFactory.createUser
import com.github.senocak.repository.PostRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for PostService")
class PostServiceTest {
    private val postRepository = Mockito.mock(PostRepository::class.java)
    private val postService = PostService(postRepository)

    @Test
    fun givenUserCategoryNextPageMaxNumber_whenGetAllByUser_thenAssertResult() {
        // Given
        val user: User = createUser()
        val category: Category = createCategory()
        val post = createPost()
        val postList: MutableList<Post> = ArrayList()
        postList.add(post)
        val postPage: Page<Post> = PageImpl(postList)
        Mockito.doReturn(postPage).`when`(postRepository).findAll(any(), any<Pageable>())
        // When
        val getAllByUser = postService.getAllByUser(user, category, 1, 1)
        // Then
        Assertions.assertEquals(postPage, getAllByUser)
    }

    @Test
    fun givenStringSlug_whenFindPostBySlugOrId_thenAssertResult() {
        // Given
        val post = createPost()
        Mockito.doReturn(post).`when`(postRepository).findPostBySlugOrId("slug")
        // When
        val findPostBySlugOrId = postService.findPostBySlugOrId("slug")
        // Then
        Assertions.assertEquals(post, findPostBySlugOrId)
    }

    @Test
    fun givenPost_whenPersist_thenAssertResult() {
        // Given
        val post = createPost()
        Mockito.doReturn(post).`when`(postRepository).save(post)
        // When
        val persist = postService.persist(post)
        // Then
        Assertions.assertEquals(post, persist)
    }
}
