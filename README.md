# 2020-1-OSSP2-SEOULLO-3


## DEVELOPERS
    
    동국대학교 컴퓨터공학과   
     이승환, 박종하, 최정인   
     
    Dongguk University Computer Science
     Seunghwan Lee, Jongha Park, Jungin Choi   
  
## SEOULLO(서울로)

     SEOULLO는 SNS(소셜네트워크)와 지도 어플리케이션을 하나의 플랫폼으로 통합한 소셜네트워크 어플리케이션입니다.   
     원하는 장소를 검색하거나, 북마크로 저장하거나, 자신이 가봤던 장소를 새로운 게시글로 등록해보세요.   
     SEOULLO에서는 Google Place에 있는 정보를 기반으로 선택한 장소 기반 반경 3km 이내에 있는 관광지를 추천해줍니다.   
     저희가 추천한 관광지와 게시글에서 선택하신 관광지는 모두 Naver 지도 기능을 통해서 길찾기를 하실 수 있습니다.   
     길찾기는 자동차 기준이니 참고해주시기 바랍니다. 
    
     SEOULLO is an integrated application, that SNS and Map application merged.    
     SEOULLO will be able to perform both application's role in one platform.     
     You can search a place you want, or add to your bookmark, or add a new post of place you want to share.   
     SEOULLO shows some of the recommendations, which is provided by Google.   
     The places of recommendation will be selected at the point, you've chosen within 3km.   
     You can enjoy the search for directions which are provided by Naver.   
     Please refer to it since directions are based on car standards.
     
     Firebase + Google Places API + Naver Map API : SNS application
  
  ### SCREENSHOTS
  
  <div display="block">
    <img width="200" src="https://user-images.githubusercontent.com/22142225/85195206-8ecc7e80-b30b-11ea-8fbb-c68a509790b3.jpeg">
        <img width="200" src="https://user-images.githubusercontent.com/22142225/85195215-955af600-b30b-11ea-8d1b-d9d1a44e8dbf.jpeg">
    <img width="200" src="https://user-images.githubusercontent.com/22142225/85195214-955af600-b30b-11ea-8489-ddbb9aded080.jpeg">
    <img width="200" src="https://user-images.githubusercontent.com/22142225/85195216-95f38c80-b30b-11ea-9b58-2edbcfc4e8f9.jpeg">
  </div>
  
## VERSION
  ```
  ...
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
  ...
  
  ```
  ## HOW TO USE API
  
  ### 🔑 **USE YOUR OWN API KEYS** 
    
     본 프로젝트는 안드로이드 스튜디오 3.6.3 에서 진행되었습니다.
     The project was carried out at Android Studio 3.6.3.
    
  #### NAVER API KEY : <https://console.ncloud.com/mc/solution/naverService/application>
  
  ![image](https://user-images.githubusercontent.com/22142225/85913606-34826f00-b871-11ea-91b8-59769ac6101c.png)

      화면에 보이는 ClientID와 Client Secret 키 값을 복사해서 프로젝트에 추가해주세요 !
      copy and add your own API key (ClientID and Client Secret) to your project !
      
      수정 경로 / Path : /app/res/values/keys.xml 
      
      + Client ID
      + Client Secret
      
      * must include Android package name in NAVER CLOUD PLATFORM
      
         Add services to your project
         - Mobile Dynamic Map
         - Directions 5 
         
   #### GOOGLE API KEY : <https://console.developers.google.com/>
   
   ![image](https://user-images.githubusercontent.com/22142225/85913648-8cb97100-b871-11ea-9a83-0e16841333c9.png)
   
      화면에 보이는 키를 복사해서 프로젝트에 추가해주세요 !
      copy and add your own API key to your project !
      
      수정 경로 / Path : /app/res/values/keys.xml
   
      Add library to your project
      - Places API
      - Directions API ( added in com.seoullo.seollotour.Map )
      
   #### FIREBASE API KEY : <https://console.firebase.google.com/>
   
   ![image](https://user-images.githubusercontent.com/22142225/85913802-b8892680-b872-11ea-9a66-c113dfd14e93.png)
   
      google-services.json 파일을 다운 받아서 프로젝트 /app 에 추가해주세요 !
   
      수정 경로 / Path : /app/google-services.json
   
      Add your project to Firebase
      Firebase shows best instructions ! just follow the instructions
      
      * place google-services.json to your project



#### /app/res/values/keys.xml
![image](https://user-images.githubusercontent.com/22142225/85913694-e91c9080-b871-11ea-91dd-a33d433997e6.png)
![image](https://user-images.githubusercontent.com/22142225/85913714-149f7b00-b872-11ea-9f11-b377867ff667.png)
#### /app/google-services.json
![image](https://user-images.githubusercontent.com/22142225/85913838-16b60980-b873-11ea-916d-3d013bc41f95.png)



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
  18. Remove Post
  19. Edit Profile
  20. Add Post
  21. File Search
  22. Login / Logout Activity
  23. Signin Activity
  ...more added
  ```


## USED OPEN SOURCE

   ##### BASE OPEN SOURCE PROJECT : <https://github.com/stephyswe/Android-Instagram>
  
    + Bottom Navigation Bar : 'com.github.ittianyu:BottomNavigationViewEx:2.0.4'
    + Glide ImageLoader : 'com.github.bumptech.glide:glide:4.9.0'
    + Circle Image View : 'de.hdodenhof:circleimageview:3.1.0'
  
## USED API

  #### Google
    - Google Places API
  #### Naver
    - Mobile Dynamic Map API
    - Direction 5
  #### Firebase
    - Firebase Authentication
    - Firebase Database
    - Firebase Storage 
   
## LICENSE

   #### [Apache License 2.0](https://github.com/CSID-DGU/2020-1-OSSP2-SEOULLO-3/blob/master/LICENSE)
