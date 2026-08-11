# MOA 백엔드 작업 이력 및 고도화 우선순위

문서 기준일: 2026-06-19

## 2026-06-19 고도화 구현 결과

- 순차 고도화 추가 결과: 공통 `ApiResponse` 성공/실패 계약 테스트와 `/api/subscription` 생성 응답 계약 테스트를 추가했다.
- 구독 생성 계약: 프론트가 의존하는 `success: true` 응답 필드와 `message` 필드를 단위 테스트로 고정했다.
- 검증 결과: 네트워크 접근 승인 후 `.\mvnw.cmd -B clean verify`가 통과했다. 테스트는 12개 통과했고 WAR 빌드가 완료됐다.

- 적용 Skill: `senior-enhancement-lead`를 확인했고, 저장소의 `AGENTS.md` 지침을 우선 적용했다.
- 생성 Skill/Agent 검색 결과: 프로젝트 루트에 `.agents`, `.codex`, 별도 `SKILL.md`는 없었다. 현재 작업은 기존 Skill로 충분하므로 새 Skill은 생성하지 않았다.
- AI 도구 흔적 정리: 기존 외부 AI 도구 전용 설정 파일 삭제 상태와 `AGENTS.md` 추가 상태를 유지했다. 새 지침은 Codex 기준으로 통일한다.
- 개발 유틸 분리: `JwtSecretGenerator`, `UserDummyGenerator`를 운영 코드인 `src/main/java`에서 제거하고 `src/test/java/com/moa/tools`로 이동했다. WAR 산출물에는 포함하지 않고, 테스트/수동 실행 목적의 도구로만 취급한다.
- 더미 데이터 표시화: `UserDummyGenerator`의 닉네임과 CI 값을 `test_user_`, `TEST_CI_` 접두어로 변경해 실제 사용자 인증 데이터로 오해하지 않게 했다.
- 정적 산출물 책임 경계: 현재 추적 중인 `src/main/resources/static/assets`, `src/main/resources/assets`는 레거시 WAR 호환 리소스로만 본다. 신규 프론트 배포 기준은 `moa-frontend`의 S3/CloudFront 산출물이며, 새 빌드 결과를 백엔드 리소스에 수동 복사하지 않는다.
- 업로드 샘플 책임 경계: 현재 추적 중인 `uploads` 파일은 데모/seed 성격의 샘플로만 본다. 운영 사용자 업로드는 Git에 추가하지 않고 외부 저장소 또는 서버 업로드 경로에서 관리한다.
- 검증 결과: 네트워크 접근 승인 후 `.\mvnw.cmd -B clean verify`가 통과했다. 테스트는 12개 통과했고 WAR 빌드가 완료됐다.
- 후속 품질 이슈: `PartyServiceImpl` unchecked warning과 Mockito dynamic agent warning은 기능 변경과 분리된 P1 품질 작업으로 추적한다.

## 현재 고도화 우선순위

| 우선순위 | 작업 | 상태 | 다음 액션 |
| --- | --- | --- | --- |
| P0 | AI 도구 전용 흔적 Codex 기준 통일 | 진행 중 | 외부 AI 도구 전용 설정 파일 삭제와 `AGENTS.md` 추가 상태를 커밋에 포함 |
| P0 | 운영 코드의 개발 유틸 분리 | 완료 | 필요 시 실행 방법을 README 또는 ops 문서에 별도 추가 |
| P0 | 프론트 정적 산출물 책임 경계 정리 | 기준 확정 | 레거시 WAR fallback 제거 여부가 결정되면 추적 파일 정리 |
| P0 | 업로드 샘플/운영 업로드 경계 정리 | 기준 확정 | 운영 업로드는 Git 미추적 원칙 유지 |
| P1 | Maven 의존성 캐시/미러 정리 | 대기 | 네트워크 제한 환경의 Maven mirror/cache 절차 문서화 |
| P1 | health check 표준화 | 대기 | actuator 또는 고정 health endpoint 결정 |
| P1 | 테스트 프로파일 정리 | 대기 | `application-test.properties`와 mock DB 전략 확정 |
| P2 | 배포 rollback/runbook 문서화 | 대기 | artifact 보관, 이전 WAR 복구, journal 확인 절차 작성 |
| P2 | Java warning 정리 | 대기 | `PartyServiceImpl` unchecked warning과 Mockito agent warning 분리 처리 |

## 검증 명령

- `.\mvnw.cmd -B clean verify`

## 변경 원칙

- 기존 미커밋 변경분은 사용자 또는 이전 자동화 작업으로 보고 되돌리지 않는다.
- 민감 파일 내용은 공개하지 않고, 추적 여부와 관리 방식만 점검한다.
- 기능 변경과 운영/문서 정리는 검증 가능한 작은 단위로 나눈다.
