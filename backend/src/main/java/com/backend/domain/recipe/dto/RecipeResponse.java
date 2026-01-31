package com.backend.domain.recipe.dto;

import java.util.List;

/**
 * AI가 응답하는 레시피 정보 DTO
 *
 * @param recipeName    요리 이름
 * @param description   요리 설명
 * @param ingredients   필요한 재료 목록
 * @param instructions  조리 단계
 * @param estimatedTime 예상 조리 시간 (분)
 * @param difficulty    난이도 (쉬움, 보통, 어려움)
 * @param tips          요리 팁
 */
public record RecipeResponse(
    String recipeName,
    String description,
    List<String> ingredients,
    List<String> instructions,
    int estimatedTime,
    String difficulty,
    String tips) {
}
