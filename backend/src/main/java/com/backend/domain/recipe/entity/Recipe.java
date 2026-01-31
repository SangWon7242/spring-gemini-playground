package com.backend.domain.recipe.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 레시피 엔티티 - 추천된 레시피를 저장
 */
@Entity
@Table(name = "recipes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recipe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String recipeName;

  @Column(length = 1000)
  private String description;

  @Column(length = 2000)
  private String ingredients; // JSON 또는 콤마 구분 문자열

  @Column(length = 4000)
  private String instructions; // JSON 또는 콤마 구분 문자열

  private int estimatedTime;

  private String difficulty;

  @Column(length = 1000)
  private String tips;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Builder
  public Recipe(String recipeName, String description, String ingredients,
      String instructions, int estimatedTime, String difficulty, String tips) {
    this.recipeName = recipeName;
    this.description = description;
    this.ingredients = ingredients;
    this.instructions = instructions;
    this.estimatedTime = estimatedTime;
    this.difficulty = difficulty;
    this.tips = tips;
    this.createdAt = LocalDateTime.now();
  }
}
