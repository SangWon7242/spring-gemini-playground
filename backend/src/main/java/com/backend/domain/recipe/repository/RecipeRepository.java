package com.backend.domain.recipe.repository;

import com.backend.domain.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 레시피 Repository
 * MVP에서는 기본 CRUD만 제공, 향후 확장 가능
 */
@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

  /**
   * 레시피 이름으로 검색
   */
  List<Recipe> findByRecipeNameContainingIgnoreCase(String recipeName);
}
