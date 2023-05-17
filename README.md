# WebGAL Android
[![example workflow](https://github.com/nini22p/WebGAL-Android/actions/workflows/android.yml/badge.svg)](https://github.com/nini22P/WebGAL-Android/actions)

一个将 WebGAL 游戏打包到安卓平台的简易模板  
A simple template for packaging WebGAL games to Android platform

本模板使用的图标来自：[MakinoharaShoko/WebGAL](https://github.com/MakinoharaShoko/WebGAL)  
The icons used in this template are from: [MakinoharaShoko/WebGAL](https://github.com/MakinoharaShoko/WebGAL)

## 如何打包 WebGAL 游戏

* 安装 [android studio](https://developer.android.com/studio) 并导入本项目
* 将游戏移动到 `app\src\main\assets\webgal`。默认加载 `app\src\main\assets\webgal\index.html`，如有需要自定义加载链接请修改 `app\src\main\res\values\values.xml` 文件里面的 `load_url` 字段
* 更改包名以及游戏名和图标
* 点击菜单栏 `Build` -> `Generate Signed Bundle or APK` 构建 apk

## How to package WebGAL games

* Install [android studio](https://developer.android.com/studio) and import this project
* Move the game to `app\src\main\assets\webgal`. Load `app\src\main\assets\webgal\index.html` by default, if you need to customize the load link, please modify the `load_url` field inside the `app\src\main\res\values\values.xml` file
* Change the package name and the game name and icon
* Click `Build` -> `Generate Signed Bundle or APK` in the menu bar to build the apk

### 更多信息 / More Info

[https://docs.openwebgal.com/guide/android](https://docs.openwebgal.com/guide/android)
