# 📚 프로젝트 문서 목록

이 폴더는 SCMS3 (푸름대학교 학생성장지원센터 CHAMP) 프로젝트의 모든 문서를 체계적으로 관리합니다.

---

## 📂 문서 구조

### 🔢 개발 로그 (01-19)
순차적인 개발 과정을 기록한 문서들

| 번호 | 파일명 | 설명 |
|------|--------|------|
| 01 | `01_PROJECT_OVERVIEW.md` | 프로젝트 전체 개요 |
| 02 | `02_DEVELOPMENT_LOG.md` | 초기 개발 로그 |
| 03 | `03_IMPLEMENTATION_SUMMARY.md` | 구현 요약 |
| 04 | `04_PROGRAM_FEATURE_DEVELOPMENT_LOG.md` | 프로그램 기능 개발 로그 |
| 05 | `05_ADMIN_HEADER_FUNCTIONALITY_DEVELOPMENT_LOG.md` | 관리자 헤더 기능 개발 |
| 06 | `06_FILTER_AND_CAROUSEL_DEVELOPMENT_LOG.md` | 필터 및 캐러셀 개발 |
| 07 | `07_PAGINATION_AND_DATA_LOADER_DEVELOPMENT_LOG.md` | 페이징 및 데이터 로더 |
| 08 | `08_PROGRAM_DETAIL_AND_IMAGES_DEVELOPMENT_LOG.md` | 프로그램 상세 페이지 및 이미지 |
| 09 | `09_IMAGE_STRATEGY_PICSUM_PHOTOS.md` | 이미지 전략 (picsum.photos) |
| 10 | `10_PROGRAM_APPLICATION_FEATURE_DEVELOPMENT_LOG.md` | 프로그램 신청 기능 |
| 11 | `11_NEXT_IMPLEMENTATION_PLAN.md` | 다음 구현 계획 |
| 12 | `12_ADMIN_TAB_AND_CLICKABLE_TITLES_DEVELOPMENT_LOG.md` | 관리자 탭 및 클릭 가능한 제목 ⭐ NEW |

---

### 📖 가이드 문서 (90-99)
프로젝트 설정 및 사용 가이드

| 번호 | 파일명 | 설명 |
|------|--------|------|
| 90 | `90_SETUP_GUIDE.md` | 환경 설정 가이드 |
| 91 | `91_DEVELOPMENT_STATUS.md` | 전체 개발 현황 |
| 92 | `92_SESSION_RESUME_GUIDE.md` | 세션 재개 가이드 |
| 93 | `93_CURRENT_SESSION.md` | 현재 세션 작업 기록 |
| 94 | `94_PR_DESCRIPTION_TEMPLATE.md` | PR 설명 템플릿 |

---

### 🎨 UI 참고 자료 (`ui/` 폴더)
UI 디자인 참고 자료 및 버전별 화면 설계

| 번호 | 파일명 | 설명 |
|------|--------|------|
| 01 | `01_ui_base.pptx` | 기본 UI 디자인 |
| 02-13 | `02_ui_v1.pptx` ~ `13_ui_v12.pptx` | UI 디자인 버전별 진화 |
| 14-16 | `14_ui_reference.pptx` ~ `16_ui_reference_11.pptx` | UI 참고 자료 |

---

## 📌 주요 문서 바로가기

### 처음 시작하는 경우
1. [`01_PROJECT_OVERVIEW.md`](./01_PROJECT_OVERVIEW.md) - 프로젝트가 무엇인지 이해
2. [`90_SETUP_GUIDE.md`](./90_SETUP_GUIDE.md) - 개발 환경 설정
3. [`91_DEVELOPMENT_STATUS.md`](./91_DEVELOPMENT_STATUS.md) - 현재 개발 상태 확인

### 세션을 재개하는 경우
1. [`92_SESSION_RESUME_GUIDE.md`](./92_SESSION_RESUME_GUIDE.md) - 세션 재개 방법
2. [`93_CURRENT_SESSION.md`](./93_CURRENT_SESSION.md) - 최근 작업 내용
3. [`11_NEXT_IMPLEMENTATION_PLAN.md`](./11_NEXT_IMPLEMENTATION_PLAN.md) - 다음 작업 계획

### 특정 기능 개발 참고
- **프로그램 관리**: `04_PROGRAM_FEATURE_DEVELOPMENT_LOG.md`
- **프로그램 신청**: `10_PROGRAM_APPLICATION_FEATURE_DEVELOPMENT_LOG.md`
- **관리자 기능**: `12_ADMIN_TAB_AND_CLICKABLE_TITLES_DEVELOPMENT_LOG.md`
- **UI/UX**: `06_FILTER_AND_CAROUSEL_DEVELOPMENT_LOG.md`, `08_PROGRAM_DETAIL_AND_IMAGES_DEVELOPMENT_LOG.md`

---

## 🔄 문서 업데이트 규칙

### 개발 로그 작성 규칙
1. **파일명**: `{번호}_FEATURE_NAME_DEVELOPMENT_LOG.md`
2. **번호**: 순차적으로 증가 (다음은 13번)
3. **내용 포함 사항**:
   - 작업 개요
   - 문제 상황 및 원인 분석
   - 해결 방법 (코드 포함)
   - 테스트 결과
   - Git 커밋 히스토리
   - 다음 단계

### 가이드 문서 작성 규칙
1. **파일명**: `{90대 번호}_DOCUMENT_NAME.md`
2. **90대 번호**: 가이드/참고 문서용 예약 범위
3. **용도**: 설정, 사용법, 참고 자료 등

---

## 📊 문서 통계

- **총 개발 로그**: 12개
- **가이드 문서**: 5개
- **UI 참고 자료**: 16개
- **마지막 업데이트**: 2025-11-17

---

## 💡 문서 작성 팁

### Good Practice ✅
- 코드 변경 전/후 비교 포함
- 스크린샷 또는 다이어그램 추가
- 에러 메시지와 해결 방법 명시
- Git 커밋 해시 참조
- 테스트 절차 명시

### Bad Practice ❌
- 모호한 설명 ("문제를 수정했습니다")
- 코드 없이 결과만 나열
- 날짜/버전 정보 누락
- 링크 깨짐
- 넘버링 중복

---

## 🔗 외부 참고 문서

- **프로젝트 수행 계획서**: [Google Docs 링크](https://docs.google.com/document/d/1LPxYcGUIk_J7sn4BlCQeZrpfCZGavj8dZMRhIfEAAh4/edit)
- **요구사항 정리**: [Google Sheets 링크](https://docs.google.com/spreadsheets/d/104q5eTg701of5WxGEBqalPelumekEeqCLGjKmC2mPG4/edit)
- **TOAST UI**: https://ui.toast.com/

---

**관리자**: Claude AI
**최종 업데이트**: 2025-11-17
**버전**: 1.0
