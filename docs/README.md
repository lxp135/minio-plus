## 安装yarn
```
npm install -g yarn
```
## 安装vitepress
```
yarn add --dev vitepress
```
## 启动
```
yarn dev
```

## 常见问题

### 安装 vitepress 提示 node 版本错误，如何切换 node 版本

访问 nvm-windows 的 [GitHub](https://github.com/coreybutler/nvm-windows) 页面。
下载并安装nvm-windows。
安装完成后，打开命令行（例如PowerShell或CMD）。
查看所有可用的Node.js版本：
```
nvm list available
```
安装你想要的Node.js版本，例如：
```
nvm install 18.18.0
```
使用你刚刚安装的版本：
```
nvm use 18.18.0
```
验证是否成功切换到新版本：
```
node -v
```