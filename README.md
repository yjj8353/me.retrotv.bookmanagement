# 도서 관리 웹 어플리케이션

# 주의사항
* 프로젝트 기동 시, 필요한 키 값들이 보안상의 이유로 제거 되어 있기 때문에 빌드해도 동작하지 않습니다.

# 프로젝트 설명
## 이 프로젝트는?
### 개발 범위
백엔드 + 프론트엔드 모든 범위 직접 개발

### 사용 대상자
이 프로젝트 개발자를 포함한 주변의 책 관리로 힘든 사람들 (약 3명 정도)

### 만들게 된 이유
퇴사 후, 새로운 기술에 대해 독학 하면서 지금까지 구매한 책을 관리할 애플리케이션이 있으면 좋을 것 같아서 시작하게 된 프로젝트입니다. 마침, 주변에 책 관리용 어플리케이션이 있으면 좋겠다고 생각하는 사람이 몇명 더 있어서 '바로 이거다!'라는 생각이 들었습니다. 이 프로젝트에서는 지난 약 3개월 간 독학하면서 배운 내용에 대한 집대성 같은 존재라고 볼 수 있습니다.

# 기술 스택
## 백엔드
* Java
* Gradle
* Spring Boot 2 (Spring Boot Devtools/Spring Boot Web/Spring Data JPA/Spring Security)
* JavaDoc

## 프론트엔드
* TypeScript/HTML5/CSS3
* Vue.js 3 (Quasar/Vue Router/Vite/Pinia)
* Axios
* JSDoc

## 데이터베이스
* PostgreSQL

## 테스팅
* JUnit 5
* Mockito
* Postman

## 데브옵스
* Github
* Jenkins
* Docker

# 어플리케이션 구조
![어플리케이션 구조](https://3571514308-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FD6dAOCAQfCwTaIXfPRSa%2Fuploads%2FFZVnbVShR0XP9OyIAT18%2F%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8%20%EA%B5%AC%EC%A1%B0.png?alt=media&token=4d47b351-90fa-49a2-a09b-6ffa88a5186c)

# 데이터베이스 ERD
![데이터베이스 ERD](https://3571514308-files.gitbook.io/~/files/v0/b/gitbook-x-prod.appspot.com/o/spaces%2FD6dAOCAQfCwTaIXfPRSa%2Fuploads%2FpzSmsXzwLrf3mWbtpPZ0%2Fpostgres-1663133979221.png?alt=media&token=eb2ad741-18eb-4a70-aadf-b5ae688dcfa4)

# API 문서
https://yjj8353.gitbook.io/bookmanagement/api

# 프로젝트 동작과정
https://yjj8353.gitbook.io/bookmanagement/undefined-4

# 데모
https://bookmanagement.retrotv.me:8443

# 진행하면서 아쉬웠던 점
프로젝트를 진행하면서 크게 아쉬웠던 점을 꼽으라면 두가지가 있었습니다. 하나는 권한 분리를 제대로 하지 못해 관리자만의 기능을 웹으로 구현하지 못하고 직접 서버상에서 작업해줘야 한다 것과 OSIV(Open Session In View)에 기능으로 인한 지연로딩 오류 였습니다. 관리자 기능의 웹 구현은 현재 만드는 프로젝트의 문제점이나 부족한 점을 파악하고 차기버전 개발 시 해당 내용을 토대로 재개발 예정이며 OSIV로 인한 문제는 JPA의 영속과 준영속에 대한 개념의 이해로 해결이 가능했습니다.

# 앞으로의 계획
현재 프로젝트 진행 시 사용한 개념이나 기술의 이해의 부족으로 완성되지 못하거나 미흡했던 부분을 점검해, 해당 프로젝트의 차기 버전 구상 계획에서 좀 더 꼼꼼히 반영하고 개발할 예정입니다.
