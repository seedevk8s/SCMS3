# 푸름대학교 학생성장지원센터 CHAMP

본 저장소는 학생역량강화 프로그램을 구현하는 곳입니다.
참고 사이트 1과 유사한 UX 화면 구성을 레퍼런스 합니다.

## 🚀 빠른 시작

### 사전 요구사항
- Java 17 이상
- Maven 3.6 이상
- Docker & Docker Compose (권장)
- MySQL 8.0 (Docker 사용 시 불필요)

### 설치 및 실행

1. **저장소 클론**
   ```bash
   git clone <repository-url>
   cd SCMS2
   ```

2. **데이터베이스 설정**

   Docker 사용 (권장):
   ```bash
   docker-compose up -d
   ```

   상세한 데이터베이스 설정 방법은 [database/README.md](database/README.md)를 참고하세요.

3. **애플리케이션 실행**
   ```bash
   # 개발 모드로 실행
   mvn spring-boot:run -Dspring-boot.run.profiles=dev

   # 또는 JAR 빌드 후 실행
   mvn clean package
   java -jar target/scms2-0.0.1-SNAPSHOT.jar
   ```

4. **브라우저에서 접속**
   ```
   http://localhost:8080
   ```

### 테스트 계정 🔑

애플리케이션 첫 실행 시 자동으로 생성되는 테스트 계정:

#### 학생 계정
- 학번: `2024001` / 비밀번호: `030101` (김철수)
- 학번: `2024002` / 비밀번호: `040215` (이영희)
- 학번: `2023001` / 비밀번호: `020310` (박민수)
- 학번: `2022001` / 비밀번호: `010620` (정우진)
- 기타 7개 계정 (상세 정보는 [database/README.md](database/README.md) 참고)

#### 관리자 계정
- 학번: `9999999` / 비밀번호: `admin123`

> 💡 **초기 비밀번호는 생년월일 6자리** (예: 2003-01-01 → `030101`)
> 최초 로그인 시 비밀번호 변경이 필요합니다.

---

## 📚 프로젝트 문서

모든 프로젝트 문서는 **[`doc/`](./doc/)** 폴더에 체계적으로 정리되어 있습니다.

### 주요 문서 바로가기

#### 🚀 시작하기
- [환경 설정 가이드](./doc/guides/90_SETUP_GUIDE.md) - 개발 환경 구축 방법
- [프로젝트 개요](./doc/development-logs/01_PROJECT_OVERVIEW.md) - 프로젝트 전체 이해
- [개발 현황](./doc/guides/91_DEVELOPMENT_STATUS.md) - 현재 구현 상태

#### 📖 개발 로그
- [프로그램 기능 개발](./doc/development-logs/04_PROGRAM_FEATURE_DEVELOPMENT_LOG.md)
- [프로그램 신청 기능](./doc/development-logs/10_PROGRAM_APPLICATION_FEATURE_DEVELOPMENT_LOG.md)
- [관리자 신청 관리 기능](./doc/development-logs/13_ADMIN_APPLICATION_MANAGEMENT_DEVELOPMENT_LOG.md) ⭐ 최신
- [전체 문서 목록](./doc/README.md) - 모든 문서 인덱스

#### 🔄 세션 재개
- [세션 재개 가이드](./doc/guides/92_SESSION_RESUME_GUIDE.md) - 작업 재개 방법
- [다음 구현 계획](./doc/development-logs/11_NEXT_IMPLEMENTATION_PLAN.md) - 앞으로 해야 할 작업

---

## 🌐 외부 참고 자료

### 핵심 문서
- 📄 [프로젝트 수행 계획서](https://docs.google.com/document/d/1LPxYcGUIk_J7sn4BlCQeZrpfCZGavj8dZMRhIfEAAh4/edit?tab=t.0)
- 📊 [요구사항 정리 문서](https://docs.google.com/spreadsheets/d/104q5eTg701of5WxGEBqalPelumekEeqCLGjKmC2mPG4/edit?gid=1838870076#gid=1838870076)

### UI 레퍼런스
- 🌐 [푸름대학교 학생성장지원센터 CHAMP](https://champ.woosuk.ac.kr/ko/)
- 🧰 [TOAST UI](https://ui.toast.com/)
- 📁 [UI 디자인 파일들](./doc/ui/) - 프로젝트 내 UI 참고 자료

> 💡 외부 링크 접근 불가 시 프로젝트 담당자에게 문의하세요.

---

