# Campus Book Share - Server

校园书籍分享平台后端服务，基于 Go 语言开发。

## 技术栈

- **Go 1.25.0**
- **Gin** - Web 框架
- **GORM** - ORM 框架
- **MySQL** - 数据库
- **JWT** - 用户认证

## 项目结构

```
server/
├── common/          # 公共组件
│   ├── database.go  # 数据库连接与初始化
│   └── jwt.go       # JWT 工具函数
├── controller/      # 控制器层，处理业务逻辑
│   ├── AdminController.go
│   ├── PostController.go
│   ├── UploadController.go
│   └── UserController.go
├── middleware/      # 中间件
│   └── AuthMiddleware.go  # JWT 认证中间件
├── model/           # 数据模型
│   ├── comment.go
│   ├── notification.go
│   ├── post.go
│   ├── user.go
│   └── user_behavior.go
├── routes/          # 路由定义
│   └── routes.go
├── uploads/         # 上传文件存储目录
├── main.go          # 入口文件
├── go.mod
└── go.sum
```

## API 接口

### 认证接口 `/api/auth`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/admin/login | 管理员登录 |

### 帖子接口 `/api`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/posts | 获取帖子列表 |
| GET | /api/posts/:id | 获取帖子详情 |
| GET | /api/posts/:id/comments | 获取帖子评论 |

### 评论接口 `/api/comment` (需认证)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/comment/create | 创建评论 |
| DELETE | /api/comment/:id | 删除评论 |

### 帖子接口 `/api/post` (需认证)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/post/create | 创建帖子 |
| GET | /api/post/my | 获取我的帖子 |
| PUT | /api/post/:id | 更新帖子 |
| DELETE | /api/post/:id | 删除帖子 |

### 用户接口 `/api/user` (需认证)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/user/info | 获取用户信息 |
| PUT | /api/user/info | 更新用户信息 |
| POST | /api/user/change-password | 修改密码 |
| GET | /api/user/notifications | 获取通知列表 |
| PUT | /api/user/notifications/:id | 标记通知已读 |

### 文件上传 `/api`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/upload | 上传图片 (需认证) |

### 管理后台 `/api/admin` (需认证)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/stats/dashboard | 仪表盘统计 |
| GET | /api/admin/stats/post-trend | 帖子趋势 |
| GET | /api/admin/stats/trade-distribution | 交易分布 |
| GET | /api/admin/users | 用户列表 |
| DELETE | /api/admin/users/:id | 删除用户 |
| GET | /api/admin/posts | 帖子列表 |
| DELETE | /api/admin/posts/:id | 删除帖子 |
| GET | /api/admin/comments | 评论列表 |
| DELETE | /api/admin/comments/:id | 删除评论 |
| GET | /api/admin/notifications | 通知列表 |
| DELETE | /api/admin/notifications/:id | 删除通知 |

## 启动说明

1. 确保 MySQL 数据库运行中，并创建 `campus_book_db` 数据库
2. 修改 `common/database.go` 中的数据库连接配置
3. 运行服务：

```bash
go run main.go
```

服务将在 `http://localhost:8080` 启动

## 数据库

数据库连接配置位于 `common/database.go`，默认连接本地 MySQL：

- 主机：127.0.0.1:3306
- 用户名：root
- 密码：zhangxun4436
- 数据库：campus_book_db

程序启动时会自动迁移数据表结构。