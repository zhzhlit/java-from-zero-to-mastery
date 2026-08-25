---
status: learning-ready
javaVersion: "21"
verifiedAt: "2026-08-25"
---

# Typora 图片上传与图床配置：PicList、Cloudflare R2 与 Markdown 迁移

Typora 本身不直接提供图床。它通过第三方上传工具或自定义命令，把本地图片上传到对象存储、Git 仓库或图床服务，再把 Markdown 中的本地路径替换成公网 URL。

常见链路是：

```text
Typora 插入图片 → 上传工具 → 图床/对象存储 → 返回图片 URL → 写入 Markdown
```

## Typora 支持的上传方式

Typora 官方支持的上传工具包括：

- PicGo.app：图形界面，教程多，上手简单
- PicGo-Core：命令行版，适合脚本化和轻量运行
- PicList：基于 PicGo，增加远程图片管理、删除、重命名等能力
- iPic、uPic、Picsee：macOS 图片上传工具
- Upgit：适合上传到 GitHub
- 自定义命令：接入自己的脚本或其他上传工具

配置入口：

```text
Typora → 设置/偏好设置 → 图像 → 上传服务
```

常用操作：

- 插入图片时自动上传
- 右键单张图片选择“上传图片”
- `格式 → 图像 → 上传所有本地图片`

Typora 能显示的图片格式通常包括 PNG、JPEG/JPG、GIF、SVG、WebP 等；最终能否上传取决于所选图床的文件限制。

## 推荐上传工具

如果在 macOS 上长期使用 Typora，推荐顺序如下：

| 工具 | 适合场景 | 说明 |
|---|---|---|
| PicList | 长期写博客、文档、管理大量图片 | 支持上传、相册管理、远程删除、重命名、压缩和多图床 |
| PicGo.app | 只需要粘贴图片后自动生成链接 | 用户多、教程丰富、配置简单 |
| PicGo-Core | 命令行和自动化上传 | 无常驻图形界面，占用低，配置门槛更高 |
| uPic | macOS 原生备选 | 体验轻量，但生态和教程少于 PicGo 系列 |

直接建议：**优先使用 PicList，并接入自己控制的存储服务**。不要长期依赖免费公共图床，也不要把私人截图放进公开 GitHub 仓库。

## PicList 常见图床选择

PicList 内置支持的主要图床包括：

- 阿里云 OSS
- 腾讯云 COS V5
- 七牛云 Kodo
- 又拍云
- AWS S3，以及兼容 S3 的 Cloudflare R2、Backblaze B2、MinIO 等
- GitHub
- S.EE（原 SM.MS）
- Imgur
- WebDAV
- SFTP
- 本地目录
- 多吉云
- 兰空图床 Lsky Pro
- AList
- 自定义 API

也可以通过 PicGo 插件继续扩展华为云 OBS、Gitee 等平台。

## 图床选型建议

| 场景 | 推荐 |
|---|---|
| 国内个人长期使用 | 阿里云 OSS 或腾讯云 COS |
| 全球博客、开源文档 | Cloudflare R2 |
| 公开项目 README | GitHub |
| 已有 NAS 或服务器 | AList、WebDAV、Lsky Pro、SFTP、MinIO |
| 只想快速体验 | S.EE 或 PicGo 默认配置 |

免费方案要分清“完全免费”和“有免费额度”：

- Cloudflare R2：有长期免费额度，适合个人图床；国内访问一般，配置稍复杂。
- Backblaze B2：有长期免费存储额度，可作为 R2 备选。
- GitHub 公共仓库：适合公开技术笔记，不适合大量图片和私人截图。
- 本地目录：完全免费，但分享 Markdown 时需要同时复制图片目录。
- 阿里云 OSS、腾讯云 COS：通常不是永久免费，但国内访问稳定，正式使用更省心。

无论选择哪种服务，都建议创建权限受限的子账号密钥，只允许操作图片 Bucket；不要把云账号主 AccessKey 填进上传工具。

## Cloudflare R2 配置流程

Cloudflare R2 适合面向全球访问的个人博客、开源文档和 Typora 图床。推荐链路：

```text
Cloudflare R2 → PicList AWS S3 配置 → Typora PicList 上传服务
```

### 1. 创建 R2 存储桶

在 Cloudflare 控制台进入：

```text
存储和数据库 → R2 对象存储 → 创建存储桶
```

示例名称：

```text
typora-images
```

存储桶名称只能包含小写字母、数字和连字符。

### 2. 设置公开访问

R2 存储桶默认私有。Typora 图片需要通过 URL 显示，因此必须配置公开读取。

测试阶段可以启用 R2 的公共开发 URL：

```text
R2 → 存储桶 → 设置 → 公共开发 URL
```

它会生成类似：

```text
https://pub-xxxxxxxxxxxxxxxx.r2.dev
```

正式使用推荐绑定自己的图片域名，例如：

```text
https://img.example.com
```

### 3. 创建 R2 API 令牌

进入：

```text
R2 → 概述 → 管理 R2 API 令牌
```

建议设置：

- 权限：对象读取和写入
- 范围：仅应用于指定存储桶
- 存储桶：刚创建的 `typora-images`

创建后保存：

- Access Key ID
- Secret Access Key
- S3 API Endpoint

Secret Access Key 只显示一次，需要立即保存到安全位置。

### 4. 配置 PicList

打开：

```text
PicList → 图床 → 添加图床 → AWS S3
```

按下面填写：

| PicList 字段 | 填写内容 |
|---|---|
| 配置名称 | `Cloudflare R2` |
| Access Key ID | Cloudflare 生成的 Access Key ID |
| Secret Access Key | Cloudflare 生成的 Secret Access Key |
| Bucket Name / 存储桶名称 | `typora-images` |
| Upload Path / 上传路径 | `typora/` |
| Region | `auto` |
| Endpoint / 自定义节点 | `https://账户ID.r2.cloudflarestorage.com` |
| URL Prefix / 自定义域名 | `https://pub-xxx.r2.dev` 或 `https://img.example.com` |
| Proxy / 代理 | 留空 |
| s3ForcePathStyle | 通常保持 `No` |
| Reject Unauthorized / 拒绝无效 TLS 证书 | 建议开启 |
| ACL / 上传资源访问策略 | 若没有空选项，可保留 `public-read` 先测试 |

关键点：

- Endpoint 是上传接口，不能填 `r2.dev` 或图片域名。
- URL Prefix 是最终图片访问地址，不能填 S3 Endpoint。
- Region 必须填 `auto`。
- Bucket Name 必须与 R2 控制台中的存储桶名称完全一致。
- Upload Path 建议固定为 `typora/`。
- 为避免同名覆盖，建议在 PicList 开启时间戳、哈希或 ULID 重命名。

保存后，将该配置设为默认图床，并在 PicList 内先上传一张普通图片测试。

成功返回应类似：

```text
https://img.example.com/typora/xxx.png
```

把 URL 粘贴到浏览器，确认无需登录即可显示图片。

### 5. 连接 Typora

保持 PicList 在后台运行，然后打开：

```text
Typora → 设置 → 图像
```

设置：

- 上传服务：PicList
- 点击“验证图片上传选项”
- 测试成功后，将“插入图片时”设为“上传图片”

之后在 Typora 中粘贴截图，Markdown 会自动生成：

```markdown
![图片](https://img.example.com/typora/xxx.png)
```

## 自定义域名与 DNS 注意事项

域名不需要转移注册商，但如果要免费绑定 R2 自定义域名，通常需要把 DNS 解析托管到 Cloudflare。

推荐做法：

1. 在 Cloudflare 添加根域名。
2. 检查并迁移原 DNS 记录。
3. 邮箱相关 MX、SPF、DKIM、DMARC 记录保持“仅 DNS”。
4. 如果原域名开启 DNSSEC，先处理旧 DS 记录，再切换名称服务器。
5. Cloudflare 激活域名后，在 R2 存储桶设置中绑定 `img.example.com`。

如果不能更换 DNS 服务器，免费版通常不能只在原 DNS 服务商添加一条 CNAME 来稳定绑定 R2。此时可选：

- 继续使用 `r2.dev`，仅用于测试或低频个人使用。
- 升级 Cloudflare Business 使用部分 CNAME 设置。
- 改用允许外部 DNS 直接绑定的阿里云 OSS 或腾讯云 COS。

不要把上传 Endpoint 改成图片域名；Endpoint 始终保持：

```text
https://账户ID.r2.cloudflarestorage.com
```

## 常见问题排查

### 报错：No value provided for input HTTP label: Bucket

含义：PicList 的 R2 配置没有填写或没有保存 Bucket Name。

处理：

1. 打开 `PicList → 图床 → AWS S3 → 当前 Cloudflare R2 配置`。
2. 检查 `Bucket Name / 存储桶名称`。
3. 确保它只填写 R2 存储桶名称，例如 `typora-images`。
4. 保存配置并设为默认图床。
5. 若仍失败，删除当前 AWS S3 配置后重新创建。

不要把以下内容填进 Bucket Name：

- Cloudflare 账户 ID
- `r2.dev` 地址
- S3 Endpoint
- 自定义图片域名

### PicList 显示 is listening at 36677

这表示 PicList 上传服务已经运行，Typora 可以通过本地服务调用它。它不是错误。

### PicList 显示 Web server is not enabled

这通常与 Typora 上传失败无关，不是 R2 上传的关键问题。

### ACL、x-amz-acl 或 InvalidArgument 报错

Cloudflare R2 的公开访问由公共开发 URL 或自定义域名控制，不依赖传统 S3 ACL。

如果 PicList 上传时报 ACL 相关错误，尝试清空或移除 ACL 配置。如果界面没有空选项，可以先保留 `public-read` 测试；只在确实出现 ACL 报错时再处理。

### 上传成功但 Markdown 中还是本地路径

检查 Typora：

```text
设置 → 图像 → 上传服务 → PicList → 验证图片上传选项
```

并确认“插入图片时”选择了“上传图片”。也可以右键图片手动选择“上传图片”。

## 已有 Markdown 文档如何迁移图片

已有 Markdown 文档可以用 Typora 迁移到 R2：

1. 先备份 `.md` 文件和原图片目录；如果文档在 Git 中，先提交一次。
2. 用 Typora 打开 Markdown。
3. 确认所有本地图片都能正常显示。
4. 点击：

```text
格式 → 图像 → 上传所有本地图片
```

Typora 会调用 PicList，把本地图片上传到 R2，并将原链接：

```markdown
![示例](./images/test.png)
```

自动替换为：

```markdown
![示例](https://img.example.com/typora/xxx.png)
```

注意：

- “上传所有本地图片”只处理本地路径，不会迁移已有的 `https://...` 网络图片。
- 路径失效、Typora 中无法显示的图片无法上传，需要先修复本地路径。
- 不建议上传成功后马上删除本地图片，至少保留一次完整备份。
- 不要上传含密码、Token、客户数据或内部系统信息的截图。
- 少量文档用 Typora 菜单最安全；大量文档适合写批量迁移脚本。

## 参考资料

- [Typora 图片上传说明](https://support.typora.io/Upload-Image/)
- [Typora 图片使用说明](https://support.typora.io/Images/)
- [PicList 应用概述](https://piclist.cn/app)
- [PicList 配置手册](https://piclist.cn/configure)
- [Cloudflare R2 创建存储桶](https://developers.cloudflare.com/r2/buckets/create-buckets/)
- [Cloudflare R2 公共存储桶](https://developers.cloudflare.com/r2/buckets/public-buckets/)
- [Cloudflare R2 S3 API 凭证](https://developers.cloudflare.com/r2/get-started/s3/)
- [Cloudflare R2 价格](https://developers.cloudflare.com/r2/pricing/)
