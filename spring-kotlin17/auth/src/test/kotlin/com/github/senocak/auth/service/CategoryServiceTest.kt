package com.github.senocak.auth.service

import com.github.senocak.domain.Category
import com.github.senocak.domain.dto.category.CategoryCreateRequestDto
import com.github.senocak.domain.dto.category.CategoryUpdateRequestDto
import com.github.senocak.exception.ServerException
import com.github.senocak.factory.CategoryFactory.createCategory
import com.github.senocak.repository.CategoryRepository
import com.github.senocak.util.AppConstants.toSlug
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for CategoryService")
class CategoryServiceTest {
    private val categoryRepository = Mockito.mock(CategoryRepository::class.java)
    private val categoryService = CategoryService(categoryRepository)

    @Nested
    internal inner class FindTest {
        @Test
        fun givenNotExist_WhenFindCategory_ThenThrowServerException() {
            // When
            val closureToTest = Executable { categoryService.findCategory("idOrSlug") }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_WhenFindCategory_ThenAssertResult() {
            // Given
            val category: Optional<Category> = Optional.of(createCategory())
            Mockito.doReturn(category).`when`(categoryRepository).findByIdOrSlug("idOrSlug")
            // When
            val saveCategory: Category = categoryService.findCategory("idOrSlug")
            // Then
            Assertions.assertEquals(category.get(), saveCategory)
        }
    }

    @Nested
    internal inner class CreateTest {
        var categoryCreateRequestDto: CategoryCreateRequestDto = CategoryCreateRequestDto()
        @Test
        fun givenExist_WhenCreateCategory_ThenThrowServerException() {
            // Given
            categoryCreateRequestDto.name = "idOrSlug"
            val category: Optional<Category> = Optional.of(createCategory())
            Mockito.doReturn(category).`when`(categoryRepository).findByIdOrSlug(toSlug("idOrSlug"))
            // When
            val closureToTest = Executable { categoryService.createCategory(categoryCreateRequestDto) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_WhenCreateCategory_ThenAssertResult() {
            // Given
            categoryCreateRequestDto.name = "name"
            categoryCreateRequestDto.image = "image"
            val category: Category = createCategory()
            Mockito.doReturn(category).`when`(categoryRepository).save(Mockito.any(Category::class.java))

            // When
            val saveCategory: Category = categoryService.createCategory(categoryCreateRequestDto)
            // Then
            Assertions.assertEquals(category.name, saveCategory.name)
            Assertions.assertEquals(category.slug, saveCategory.slug)
        }
    }

    @Nested
    internal inner class DeleteTest {
        @Test
        fun givenCategoryWhenDeleteCategoryThenAssertResult() {
            // Given
            val category: Category = createCategory()
            // When
            categoryService.deleteCategory(category)
            // Then
            Mockito.verify(categoryRepository).delete(category)
        }
    }

    @Nested
    internal inner class GetAllTest {
        @Test
        fun givenCategoryWhenSaveCategoryThenAssertResult() {
            // Given
            val categoryList: MutableList<Category> = ArrayList<Category>()
            val category: Category = createCategory()
            categoryList.add(category)
            val categories: Page<Category> = PageImpl(categoryList)
            Mockito.doReturn(categories).`when`(categoryRepository).findAll(PageRequest.of(0, 1))
            // When
            val getAll: Page<Category?>? = categoryService.getAll(0, 1)
            // Then
            Assertions.assertEquals(categories, getAll)
        }
    }

    @Nested
    internal inner class UpdateTest {
        @Test
        fun givenCategoryWhenSaveCategoryThenAssertResult() {
            // Given
            val category: Category = createCategory()
            Mockito.doReturn(category).`when`(categoryRepository).save<Category>(category)
            // When
            val saveCategory: Category = categoryService.updateCategory(category, CategoryUpdateRequestDto(category.name, category.image))
            // Then
            Assertions.assertEquals(category, saveCategory)
        }
    }
}