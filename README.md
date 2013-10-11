aRevelation
===========
Android password manager based on Revelation Password Manager file format.

Introduction
===========
Hello!
This project is android implementation of Revelation password manager. Right now it is in it's prototype life stage. I find it very easy to use this password manager with Dropbox or another sync software and I find it very hard to use it without android application. This is the main reason of this app development. If you have any questions/suggestions please mail me to alexey.kislin@gmail.com.

Build
===========

###Ubuntu Linux

####Install JDK
```
apt-get install openjdk-6-jdk
```

####Download and extract android SDK
Go to http://developer.android.com/sdk/index.html
You don't need to download ADT Bundle, just sdk. For example "android-sdk_r22.2.1-linux.tgz".
After that you need to extract file by using tar:
```
tar -xzvf android-sdk_r22.2.1-linux.tgz
```
It will extract content of the archive to the current directory.


####Update your sdk
```
cd android-sdk-linux/tools
./android update sdk --no-ui
./android
```
Check "Android SDK Build-tools" (18.0.1) and click "Install"


####Install Gradle
```
add-apt-repository ppa:cwchien/gradle
apt-get update
apt-get install gradle
```

####Install git
```
apt-get install git
```

####Clone aRevelation project
```
git clone https://github.com/MarmaladeSky/aRevelation.git
```

####Install ia-32-libs(x86_64 OS)
```
sudo apt-get install ia-32-libs
```

####Go to project directory and run build
```
cd aRevelation
export ANDROID_HOME="path/to/android-sdk-linux"
gradle build
```
You can find .apk files in aRevelation/build/apk