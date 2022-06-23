# sjs

src-main-java-tripe-sjs SjsApplication.java 파일 실행 하여,
POST로 데이터 입력
http://localhost:8080/ 에 접속하여 포인트 조회버튼을 클릭하여 포인트 조회가 가능합니다.

DDL은 src-main-resources-schema.sql 파일에 있으며, http://localhost:8080/h2-console/ 에서

URL : jdbc:h2:mem:userdb;DB_CLOSE_DELAY=-1
ID : sa 로 접속하여 DB Data 확인 가능합니다.
