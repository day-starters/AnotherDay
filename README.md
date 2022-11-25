# 일기 예보 및 뉴스 기능을 탑재한 그룹 알람 애플리케이션
본 프로젝트는 연속적인 알람 여러 개를 설정하는 불편함을 해소하기 위해 알람 그룹화 기능을 제공하며, 추가적으로 <br>
뉴스와 일기 예보에 대한 정보까지 알려주어, 하루를 시작하기 위해 필요한 기본적인 것들을 구현한 프로젝트이다.

<br>

# 개발 환경
<table>
  <tr>
    <td>운영체제</td>
    <td>Android 10</td>
  </tr>
  <tr>
    <td>개발 언어</td>
    <td>Kotlin</td>
  </tr>
  <tr>
    <td>IDE</td>
    <td>Android Studio</td>
  </tr>
  <tr>
    <td>데이터베이스</td>
    <td>SQLite(내장)</td>
  </tr>
</table>

<br>

# 시스템 구성도

<br>

# 화면 구성
![image](https://user-images.githubusercontent.com/61930770/203972697-9fe618f2-1228-460e-a4d6-c3c71d2fe5eb.png)

<br>

# 실행 화면
* <a href="https://youtube.com/shorts/WMFBkaiYrM0">알람 추가, 수정, 삭제</a>
* <a href="https://youtube.com/shorts/HCubVaHeYF8">알람 울림, 해제</a>
* <a href="https://youtube.com/shorts/9DHvyL_fgwU">일기 예보</a>

<br>

# 역할 분담
* <a href="https://github.com/rlaxodud214">김태영</a> (29woskxm@tukorea.ac.kr)
  * 조장
  * 뉴스 크롤링
  * 뉴스 DB 및 UI
  
* <a href="https://github.com/nhk1657">노현경</a> (nhk1657@tukorea.ac.kr)
  * 사용자 위치에 해당하는 일기 예보 크롤링
  * 일기 예보 UI
  
* <a href="https://github.com/ddubidubap">박성연</a> (2019150019@tukorea.ac.kr)
  * 사용자 위치 DB 및 위치 설정 UI
  * 알람 DB 및 알람 설정 UI
  
* <a href="https://github.com/LHY00y">이하영</a> (leeha0507@tukorea.ac.kr)
  * 알람 그룹화 기능
  
* <a href="https://github.com/hhh-one">이혜원</a> (904lhw@kpu.ac.kr)
  * 알람 울림, 해제 UI
  * TTS 기능
