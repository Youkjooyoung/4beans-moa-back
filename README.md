# MOA Backend

MOA 백엔드는 OTT 구독 공유 서비스의 인증, 파티, 결제, 정산, 관리자 API를 제공하는 Spring Boot 애플리케이션입니다.

## 기술 스택

- Java 17
- Spring Boot 3.5
- Spring Security
- MyBatis
- MySQL
- Maven Wrapper

## 로컬 설정

로컬 전용 설정은 `application-local.properties`에 둡니다. 실제 값은 커밋하지 않습니다.

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

필요한 값:

- DB URL, 사용자, 비밀번호
- 프론트엔드 URL과 CORS origin
- Kakao/Google OAuth redirect URI
- 로컬 SSL keystore 설정

## 로컬 실행

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Windows PowerShell에서는 다음 명령을 사용할 수 있습니다.

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

## 검증

```bash
./mvnw -B clean verify
```

CI는 `main`, `dev`, pull request에서 Maven verify를 실행하고 WAR 산출물을 보관합니다. 운영 배포는 `main` push에서만 실행합니다.

## 배포 흐름

1. GitHub Actions에서 Maven verify와 WAR package를 실행합니다.
2. `main` 브랜치 push인 경우 WAR를 서버로 전송합니다.
3. `moa-backend.service`를 재시작합니다.
4. 로컬 health endpoint 또는 루트 endpoint로 smoke check를 실행합니다.

## 보안 원칙

- `application-local.properties`, API key, 계정 메모, 인증서는 커밋하지 않습니다.
- 외부 API key와 DB 비밀번호는 GitHub Secrets 또는 서버 환경변수로 관리합니다.
- 테스트용 계정도 평문 파일로 추적하지 않습니다.

## 관련 문서

- [작업 이력 및 고도화 우선순위](./docs/PROJECT_ACTIVITY_AND_PRIORITIES.md)
