# University Educational Administration System (UEAS)

高校教务管理系统 —— 基于 Spring Boot + Vue 3 的 full-stack 教务管理平台。

## 技术栈

**后端:** Java 21 · Spring Boot 3.2.5 · MyBatis-Plus · Sa-Token · Redis · Knife4j

**前端:** Vue 3 · Vite · Element Plus · Pinia · Vue Router · ECharts

**基础设施:** Docker (MySQL 8 · Redis)

## 模块结构

```
university-ais-server/
├── ueas-admin          # Spring Boot 启动入口
├── ueas-common         # 公共组件 (R, PageResult, BizException, CacheHelper...)
├── ueas-security       # Sa-Token 认证 + JwtAuthenticationFilter
├── ueas-system         # 系统管理 (用户、角色、菜单、字典、日志)
├── ueas-basic          # 基础数据 (学院、专业、班级、学生、教师、课程)
├── ueas-enrollment     # 选课管理 (含抢课功能)
├── ueas-score          # 成绩管理
├── ueas-exam           # 考试安排与监考
├── ueas-evaluation     # 教学评价
├── ueas-notice         # 通知公告
└── ueas-ai             # AI 助教服务
```

## 快速开始

### 前置条件
- JDK 21+
- Maven 3.9+
- Node.js 18+ / pnpm
- Docker & Docker Compose

### 1. 启动基础设施

```bash
cd university-ais-server/docker
docker compose up -d
```

默认服务：MySQL `:3307`, Redis `:6380`

### 2. 构建并启动后端

```bash
cd university-ais-server
mvn install -DskipTests
cd ueas-admin
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`

### 3. 启动前端

```bash
cd university-ais-web
pnpm install
pnpm dev
```

前端运行在 `http://localhost:5173`

### 4. 访问 API 文档

浏览器打开 `http://localhost:8080/doc.html`

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 系统管理员 |
| teacher01 | teacher123 | 教师 |
| student01 | student123 | 学生 |

## 功能模块

- 🔐 **身份认证**: Sa-Token + Redis 会话管理，24h Token TTL
- 👥 **RBAC 权限**: 用户/角色/菜单三级权限控制
- 🏫 **基础数据**: 学院/专业/班级/学生/教师/课程 CRUD
- 📝 **选课管理**: 在线选课 + 抢课（限速器防刷）
- 📊 **成绩管理**: 教师录入 / 学生查询
- 📅 **考试安排**: 考场分配 + 监考任务
- 💬 **AI 助教**: 浮动聊天窗口，支持多角色
- 🔔 **通知公告**: 按角色定向发布
- 📈 **仪表盘**: 各角色独立数据分析面板

## 配置说明

关键环境变量（通过 `application-prod.yml` 或 `.env` 注入）：
- `AI_API_KEY` — AI 接口密钥
- `AI_BASE_URL` — AI API 地址
- `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` — 阿里云 OSS 凭证
- `OSS_ENDPOINT` — OSS 端点

## License

内部项目，仅供教学使用。
