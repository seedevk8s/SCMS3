package com.scms.app.controller;

import com.scms.app.dto.PortfolioItemRequest;
import com.scms.app.dto.PortfolioItemResponse;
import com.scms.app.model.PortfolioItem;
import com.scms.app.service.PortfolioItemService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 포트폴리오 항목 REST API Controller
 */
@RestController
@RequestMapping("/api/portfolios/{portfolioId}/items")
@RequiredArgsConstructor
@Slf4j
public class PortfolioItemController {

    private final PortfolioItemService portfolioItemService;

    /**
     * 포트폴리오의 항목 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getPortfolioItems(@PathVariable Long portfolioId) {
        try {
            List<PortfolioItemResponse> items = portfolioItemService.getPortfolioItems(portfolioId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "items", items,
                    "count", items.size()
            ));
        } catch (Exception e) {
            log.error("포트폴리오 항목 목록 조회 실패: portfolioId={}", portfolioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "항목 목록 조회에 실패했습니다"));
        }
    }

    /**
     * 포트폴리오 항목 생성
     */
    @PostMapping
    public ResponseEntity<?> createPortfolioItem(
            @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioItemRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            PortfolioItem item = portfolioItemService.createPortfolioItem(portfolioId, userId, request);
            PortfolioItemResponse response = PortfolioItemResponse.from(item);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "포트폴리오 항목이 추가되었습니다",
                            "item", response
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("포트폴리오 항목 생성 실패: portfolioId={}", portfolioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "항목 추가에 실패했습니다"));
        }
    }

    /**
     * 포트폴리오 항목 수정
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updatePortfolioItem(
            @PathVariable Long portfolioId,
            @PathVariable Long itemId,
            @Valid @RequestBody PortfolioItemRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            PortfolioItem item = portfolioItemService.updatePortfolioItem(itemId, userId, request);
            PortfolioItemResponse response = PortfolioItemResponse.from(item);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "포트폴리오 항목이 수정되었습니다",
                    "item", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("포트폴리오 항목 수정 실패: itemId={}", itemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "항목 수정에 실패했습니다"));
        }
    }

    /**
     * 포트폴리오 항목 삭제
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deletePortfolioItem(
            @PathVariable Long portfolioId,
            @PathVariable Long itemId,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            portfolioItemService.deletePortfolioItem(itemId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "포트폴리오 항목이 삭제되었습니다"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("포트폴리오 항목 삭제 실패: itemId={}", itemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "항목 삭제에 실패했습니다"));
        }
    }

    /**
     * 항목 순서 변경
     */
    @PutMapping("/reorder")
    public ResponseEntity<?> reorderItems(
            @PathVariable Long portfolioId,
            @RequestBody Map<String, List<Long>> request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            List<Long> itemIds = request.get("itemIds");
            if (itemIds == null || itemIds.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "항목 ID 목록이 필요합니다"));
            }

            portfolioItemService.reorderPortfolioItems(portfolioId, userId, itemIds);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "항목 순서가 변경되었습니다"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("항목 순서 변경 실패: portfolioId={}", portfolioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "항목 순서 변경에 실패했습니다"));
        }
    }

    /**
     * 프로그램 신청으로부터 항목 추가
     */
    @PostMapping("/from-program/{applicationId}")
    public ResponseEntity<?> createFromProgramApplication(
            @PathVariable Long portfolioId,
            @PathVariable Long applicationId,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            PortfolioItem item = portfolioItemService.createFromProgramApplication(portfolioId, userId, applicationId);
            PortfolioItemResponse response = PortfolioItemResponse.from(item);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "프로그램 활동이 포트폴리오에 추가되었습니다",
                            "item", response
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("프로그램 활동 추가 실패: portfolioId={}, applicationId={}", portfolioId, applicationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "프로그램 활동 추가에 실패했습니다"));
        }
    }
}
