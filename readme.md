# ESJ 轻阅 / ESJ Read

ESJ 轻阅是一个简化的 ESJ Zone Android 阅读客户端，界面只保留登录页、书架和阅读页。

## 功能

- 登录 ESJ Zone，并持久化登录状态。
- 从书架读取收藏列表，显示标题、最新章节、最后观看章节和更新日期。
- 阅读页支持章节目录、上一章、下一章、暗色模式和阅读进度保存。
- 章节内容支持文本和图片，图片可放大查看并保存到相册。
- 已访问章节会缓存到本地，阅读下一章时会预加载后续章节。
- 支持在魔法线路 `www.esjzone.cc` 和直连线路 `www.esjzone.one` 之间切换。

## 项目地址

[SlimeNull/CommunityESJ](https://github.com/SlimeNull/CommunityESJ)

## 构建

```powershell
.\gradlew.bat :app:assembleDebug
```
