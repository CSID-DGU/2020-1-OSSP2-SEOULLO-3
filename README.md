# 2020-1-OSSP2-SEOULLO-3

  이승환, 박종하, 최정인
  
## SEOULLO(서울로)

  SEOULLO는 SNS(소셜네트워크)와 지도 어플리케이션을 하나의 플랫폼으로 통합한 소셜네트워크 어플리케이션입니다. 
  원하는 장소를 검색하거나, 북마크로 저장하거나, 자신이 가봤던 장소를 새로운 게시글로 등록해보세요.
  SEOULLO에서는 Google Place에 있는 정보를 기반으로 선택한 장소 기반 반경 3km 이내에 있는 관광지를 추천해줍니다.
  저희가 추천한 관광지와 게시글에서 선택하신 관광지는 모두 Naver 지도 기능을 통해서 길찾기를 하실 수 있습니다.
  길찾기는 자동차 기준이니 참고해주시기 바랍니다. 
  
## VERSION
  ```
  <pre>
  <code>
  android {
    compileSdkVersion 28
    buildToolsVersion "28.0.3"

    defaultConfig {
        applicationId "com.seoullo.seoullotour"
        minSdkVersion 21
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
      }
   }
  </code>
  </pre>
  ```
## FUNCTIONS
  ```
  1. Grid-View Fragment
  2. List-View Fragment 
  3. Add picture form gallery
  4. Autocompletion using Google Place API
  5. Ask each Permissions for usage
  6. Camera Activity
  7. Add DataBase to Firebase
  8. Like function
  9. Comment function
  10. Bookmark function
  11. Get Place information from Google Places API
  12. Usage of Viewpager
  13. Drawing Path of Direction
  14. Map Overlay
  15. Search Users
  16. Search Places in Map
  17. Current Location - Navermap
  
  ...more added
  ```


## USED OPEN SOURCE

  ##### BASE OPEN SOURCE PROJECT : https://github.com/stephyswe/Android-Instagram
  
  ##### Bottom Navigation Bar : 'com.github.ittianyu:BottomNavigationViewEx:2.0.4'
  ##### Glide version : 'com.github.bumptech.glide:glide:4.9.0'
  ##### Circle Image View : 'de.hdodenhof:circleimageview:3.1.0'
  
## HOW TO USE API
  
  ### USE YOUR OWN API KEYS
  #### NAVER API KEY : https://console.ncloud.com/mc/solution/naverService/application

      + Client ID
      + Client Secret
      
      * must include Android package name in NAVER CLOUD PLATFORM
      
         Add services to your project
         - Mobile Dynamic Map
         - Directions 5 
        
   #### GOOGLE API KEY : https://console.developers.google.com/
      Add library to your project
      - Places API
      - Directions API ( added in com.seoullo.seollotour.Map )
      
   #### FIREBASE API KEY : https://console.firebase.google.com/
      Add your project to Firebase
      Firebase shows best instructions ! just follow the instructions
      
      * place google-services.json to your project

## USED API

  #### Google
    - Firebase
    - Google Places API
  #### Naver
    - Mobile Dynamic Map API
    - Direction 5
