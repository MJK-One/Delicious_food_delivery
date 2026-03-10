package com.dfdt.delivery.domain.product.domain.port;

import java.util.UUID;

/**
 * Product 도메인이 AI 도메인에 요청하는 연산을 정의하는 포트.
 * 구현체는 AI 도메인 infrastructure 계층에 위치합니다.
 */
public interface AiDescriptionPort {

    /**
     * AI 미리보기 로그를 신규 상품에 연결하고, 상품에 적용할 AI 텍스트를 반환합니다.
     *
     * <p>검증 목록:
     * <ul>
     *   <li>aiLogId 존재 여부</li>
     *   <li>storeId 일치 여부</li>
     *   <li>미적용 상태 (isApplied=false)</li>
     *   <li>미연결 상태 (productId=null)</li>
     *   <li>성공 로그 여부 (isSuccess=true)</li>
     * </ul>
     *
     * @param aiLogId             연결할 AI 로그 ID
     * @param storeId             가게 ID (URL 위변조 방지)
     * @param productId           방금 생성된 상품 ID
     * @param previousDescription 상품 등록 요청의 원본 설명 (롤백 시 복원 대상)
     * @param username            요청자 username
     * @return AI가 생성한 텍스트 (상품 description에 적용할 값)
     * @throws com.dfdt.delivery.common.exception.BusinessException 검증 실패 시
     */
    String validateAndLink(UUID aiLogId, UUID storeId, UUID productId,
                           String previousDescription, String username);
}
