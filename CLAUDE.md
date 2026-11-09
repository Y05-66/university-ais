# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

University Educational Administration System (UEAS) — a full-stack academic management system for Chinese universities. Built with Java 21 + Spring Boot 3.2.5 (backend), Vue 3 + Vite + Element Plus (frontend), MySQL 8, and Redis.

**Technology Stack:**
- Backend: Java 21, Spring Boot 3.2.5, MyBatis-Plus 3.5.5, Sa-Token (auth), Redis, Knife4j 4.4.0 (API docs), Hutool, Lombok
- Frontend: Vue 3.4, Vite 5, Pinia, Vue Router 4, Element Plus, Axios, dayjs, ECharts
- Infrastructure: Docker (MySQL, Redis; RabbitMQ removed 2026-11-07 as unused)

**Run status:** Backend on `:8080`, Frontend on `:5173` (Vite proxy rewrites `/api` → backend root).

## Commands

### Backend (`university-ais-server/`)

```bash
# From project root
cd university-ais-server

# Build all modules (required before running)
mvn install -DskipTests

# Run dev server (runs from ueas-admin subproject)
cd ueas-admin
mvn spring-boot:run

# Or from root with full paths
cd university-ais-server && JAVA_HOME=D:/jdk/jdk-21 /d/maven/apache-maven-3.9.12/bin/mvn.cmd spring-boot:run -pl ueas-admin -am
```

Java runtime: `D:\jdk\jdk-21\bin\java.exe`. Maven: `D:\maven\apache-maven-3.9.12\bin\mvn.cmd`.

**Always run `mvn install -DskipTests` before `spring-boot:run`** — the plugin cannot find sibling module classes otherwise.

### Tests (added 2026-11-07)

```bash
cd university-ais-server

# Run all tests across all modules
mvn test

# Run tests in a specific module
mvn test -pl ueas-common

# Run a single test class
mvn test -pl ueas-system -Dtest=LoginDTOTest
```

**Test coverage:** 23 unit tests across 4 modules — `R.java`, `ResultCode`, `PageResult`, `BizException`, `LoginDTO` validation, and `Course` entity fields. All tests are pure unit tests (no Spring context, no DB/Redis dependency).

### Frontend (`university-ais-web/`)

```bash
cd university-ais-web
pnpm install    # required — prefer pnpm-lock.yaml (package-lock.json also exists but is unused)
pnpm dev        # localhost:5173, proxies /api/** → http://localhost:8080/**
pnpm build      # outputs to dist/
pnpm lint       # ESLint with auto-fix
```

If Vite has stale cache (blank page, "系统加载中..." forever):
```bash
rm -rf node_modules/.vite && pnpm dev
```

### Docker Infrastructure

```bash
"/c/Users/Administration/AppData/Local/Programs/DockerDesktop/resources/bin/docker.exe" ps
"/c/Users/Administration/AppData/Local/Programs/DockerDesktop/resources/bin/docker.exe" compose -f docker/docker-compose.yml up -d
"/c/Users/Administration/AppData/Local/Programs/DockerDesktop/resources/bin/docker.exe" compose -f docker/docker-compose.yml down
```

Services: MySQL `:3307` (named volume: `mysql_data`), Redis `:6380` (named volume: `redis_data`).
DB credentials: `ais` / `ais@2026`, database `ueas`. Redis password: `ais@2026`.

**MySQL access:**
```bash
"/c/Users/Administration/AppData/Local/Programs/DockerDesktop/resources/bin/docker.exe" exec ais-mysql mysql -u ais -p'ais@2026' ueas -e "SHOW TABLES;"
```

**Docker compose file location:** `docker/docker-compose.yml`

## Architecture

### Backend Module Graph

Maven multi-module project (159+ Java files across 11 active modules). `ueas-admin` is the Spring Boot fat-jar entry point. All modules share parent POM `university-ais` (v1.0.0). The parent POM declares 12 `<module>` entries including `ueas-schedule`, whose directory has been deleted from disk — Maven will fail if you rebuild without removing it from the POM first.

```
university-ais (parent pom, packaging=pom)
  ueas-admin     (Spring Boot app, packaging=jar)
    ├── ueas-common     (BaseEntity, R<T>, PageResult, BizException, GlobalExceptionHandler, CacheManager, CacheHelper, MyBatisPlusMetaHandler, RushConfig, RushLimiter)
    ├── ueas-security   (JwtAuthenticationFilter — Spring Security NOT used; Sa-Token session populator)
    ├── ueas-system     (auth, RBAC, dicts, logs, SysConfigController; includes LogAspect AOP for audit logging)
    ├── ueas-basic      (college, major, class, student, teacher, course, dashboard)
    ├── ueas-enrollment (course offering / enrollment management, depends on ueas-basic)
    ├── ueas-score      (grade entry, queries; depends on ueas-basic + ueas-enrollment)
    ├── ueas-exam       (exam scheduling, room allocation, invigilation; depends on ueas-basic)
    ├── ueas-evaluation (teaching evaluations, depends on ueas-basic)
    ├── ueas-notice     (announcements; includes NoticeCleanupTask scheduled at 2 AM daily)
    └── ueas-ai         (AI services; uses RestTemplate to call OpenAI-compatible API directly)
```

**Dependency rule:** lower-level modules never depend on higher-level ones. `ueas-common` has no internal dependencies. Business modules depend on `ueas-common` + `ueas-security` + their own needs. `StudentCourse` entity lives in `ueas-basic` but is referenced by both `ueas-enrollment` (CRUD + conflict detection) and `ueas-score` (via `ScoreInputMapper`).

### Key Patterns

- **Package base:** `com.ais.*` — all Java code lives under `com.ais`. The `@MapperScan("com.ais.*.mapper")` annotation confirms this convention.
- **ORM:** MyBatis-Plus 3.5.5 with hybrid approach:
  - `@TableName` on all entities
  - `@TableField(exist = false)` for computed/JOIN fields not persisted
  - Some mappers use XML files (see locations below), others use `@Select`/`@Update` annotations directly
  - `LambdaQueryWrapper` preferred for dynamic queries (never plain `@Select` with `<if>` tags without `<script>` wrapping)
  - Logical delete configured: `logic-delete-field: deleted`, value `1`=deleted, `0`=active
  - `map-underscore-to-camel-case: true`
- **Pagination:** `MybatisPlusInterceptor` with `PaginationInnerInterceptor(DbType.MYSQL)` — use `new Page<>(current, size)` in service layer.
- **Auth:** Sa-Token (token stored in Redis, 24h TTL, 30min idle timeout). Public endpoints use path exclusion in `SaTokenConfig.addInterceptors()`. A `JwtAuthenticationFilter` (extends `OncePerRequestFilter`) populates `currentUserId`/`currentUserType` request attributes by reading the active Sa-Token session — **despite its name, it does NOT use JWT**. `SecurityConfig.java` is intentionally an empty placeholder — Spring Security is **not** used. Token sent as `Bearer <token>` in `Authorization` header. Frontend stores token in `sessionStorage` key `ueas_access_token`.
- **Backend public endpoints:** The following endpoints are excluded from Sa-Token auth (see `SaTokenConfig.java`):
  - Auth: `/auth/login`, `/auth/info`, `/auth/logout`
  - Basic data: `/basic/*`, `/student/list`, `/teacher/list`, `/course/page`, `/course-offer/page`
  - Query-only: `/score/my`, `/score/input/list`, `/enrollment/my`, `/enrollment/available`, `/exam/page`, `/exam/invigilate/my`, `/notice/page`, `/sys-user/page`, `/sys-role/list`, `/sys-menu/tree`, `/sys-log/page`, `/sys-dict/list`
  - AI chat, captcha, upload, static resources
  - Dashboard: `/dashboard/stats`, `/sys-config/enrollment/enabled`
  - Note: Most write/admin endpoints still require login + role check. When adding new frontend endpoints that should be public, add them here AND in Vite proxy exclusion list if needed.
- **API path double-prefix quirk（Vite 代理重写规则）:** Vite dev server 的 proxy 配置为：
  ```js
  '/api': {
    target: 'http://localhost:8080',
    rewrite: (path) => path.replace(/^\/api/, ''),
  }
  ```
  前端发 `/api/auth/login`，代理去掉 `/api` 前缀后后端实际收到 `/auth/login`。因此后端 `SaTokenConfig` 只需排除裸路径（如 `/auth/login`），无需同时排除带 `/api` 的形式。新增公开接口也只排除裸路径即可。
- **CORS:** `CorsConfig.java` in `ueas-admin` handles cross-origin requests. Allowed origins controlled by `cors.allowed-origins` property (default: `http://localhost:5173,http://localhost:5174,http://localhost:3000`). Max age: 3600s.
- **Sa-Token exception handling:** `SaTokenExceptionHandler.java` (ueas-admin) catches Sa-Token `NotLoginException` and returns HTTP 401 with `R.fail()` — prevents raw exception messages from leaking to clients.
- **AI config profiles:** AI API Key (`${AI_API_KEY:sk-placeholder}`) and Base URL (`${AI_BASE_URL:https://apihub.agnes-ai.com}`) are in `ueas-admin/src/main/resources/application.yml` under `spring.ai.openai.*`. A separate `application-ai.yml` also exists with overriding AI defaults (points to deepseek by default instead of agnes-ai). Production uses `application-prod.yml` with `${ENV_VAR}` placeholders for all secrets.
- **Caching:** `CacheManager` and `CacheHelper` in `ueas-common` provide Redis-based caching for high-frequency read data (colleges, semesters, majors, classes, courses, teachers, dicts, roles). Keys prefixed with `ueas:cache:`. TTL is 30min for business data, 5h for auth/menu data. `CacheHelper` is annotated `@Lazy` to avoid circular dependency issues during startup. Cache is invalidated on write operations (save/update/delete). `CacheWarmup` in `ueas-admin` implements `CommandLineRunner` to preload all cached data on startup.
- **Scheduled tasks:** `NoticeCleanupTask` (`@Scheduled(cron = "0 0 2 * * ?")`) deletes expired notices daily at 2 AM. `AsyncConfig` enables `@Async` and `@Scheduled`. Note: no `@Async` method is actually used in the codebase yet.
- **AOP audit logging:** `LogAspect` in `ueas-system` intercepts all controller methods and writes execution logs to `sys_log` table.
- **API Docs:** Knife4j at `http://localhost:8080/doc.html`. Configured via `Knife4jConfig.java` with `@EnableKnife4j`. No `@Operation` annotations exist on admin-module controllers; basic-module controllers carry the annotations.
- **Application profiles:** `application.yml` (main, active profile=`dev`), `application-dev.yml` (dev overrides), `application-prod.yml` (production, all secrets via `${ENV_VAR}`). AI config (`spring.ai.openai.api-key` / `spring.ai.openai.base-url`) is in the main `application.yml` with `${ENV_VAR:default}` fallbacks. HikariCP pool: 5 min idle, 20 max, 30s timeout.
- **Password hashing:** Passwords are stored as BCrypt hashes. Use `cn.dev33.satoken.secure.BCrypt.hashpw()` and `BCrypt.checkpw()`. Never use SHA-256 for new passwords.
- **Transaction management:** `@Transactional(rollbackFor = Exception.class)` used on multi-step write operations in `EnrollmentServiceImpl`, `ScoreServiceImpl`, `EvaluationServiceImpl`, `SysUserServiceImpl`, and `SysRoleController`.
- **Error codes:** `ResultCode` enum in `ueas-common/result/` provides named constants for standard HTTP-like codes (200, 401, 500) and domain-specific codes. Controllers return `R.fail(ResultCode.XXX)` or `R.fail("string message")`.
- **Exception handling:** `BizException` thrown for business logic errors; caught by `GlobalExceptionHandler` (`@RestControllerAdvice` in `ueas-common`).

### Mapper XML Locations

| Module | XML Mapper Files |
|--------|-----------------|
| `ueas-system` | `mapper/SysUserMapper.xml`, `SysMenuMapper.xml`, `SysLogMapper.xml`, `DictDataMapper.xml`, `SysRoleMenuMapper.xml` |
| `ueas-basic` | `mapper/StudentMapper.xml`, `CourseMapper.xml`, `CourseOfferMapper.xml`, `TeacherMapper.xml` |
| `ueas-exam` | `mapper/ExamMapper.xml` |
| `ueas-score` | `mapper/ScoreInputMapper.xml` |

All other mappers use pure Java annotations (`@Select`, `@Update`, `LambdaQueryWrapper`).

**Score module VOs** (`ueas-score/src/main/java/com/ais/score/vo/`): `StudentScoreInputVo` (nested `CourseScoreItem` with score status/modify count), `ScoreInputStudentVo`, `CourseStudentVo`. These are used by `ScoreServiceImpl` for the teacher score input page — not persisted entities.

### Annotation-Based SQL with Dynamic Conditions

MyBatis supports `<if>`, `<foreach>`, and other dynamic SQL tags inside `@Select`/`@Update` when wrapped with `<script>`...`</script>`. The codebase uses this pattern extensively (e.g. `StudentCourseMapper` and `ScoreInputMapper`). It works correctly — only *unwrapped* `<if>` tags in plain `@Select` cause SQL syntax errors.

## Entity & Service Patterns

- All entities extend `BaseEntity` (auto-fill `createTime`/`updateTime`/`deleted` via `MyBatisPlusMetaHandler`). All entities use `@Data` (Lombok) and `@TableName`.
- Service implementations follow four patterns:
  1. **Simple CRUD:** extends `ServiceImpl<M, E>`, no custom logic (e.g. `ExamRoomServiceImpl`, `ExamServiceImpl`, `NoticeServiceImpl`, `EvaluationServiceImpl`)
  2. **Cache-aware:** extends `ServiceImpl<M, E>` + uses `CacheHelper` for cache operations (e.g. `CollegeServiceImpl`, `StudentServiceImpl`, `CourseServiceImpl`, `MajorServiceImpl`, `TeacherServiceImpl`, `SemesterServiceImpl`, `ClassInfoServiceImpl`, `CourseOfferServiceImpl`, `EnrollmentServiceImpl`)
  3. **Complex logic:** no `ServiceImpl` inheritance, uses constructor-injected mappers directly with transactions (e.g. `SysUserServiceImpl`)
  4. **Mixed:** extends `ServiceImpl<M, E>` but adds custom queries via injected mapper (e.g. `ScoreServiceImpl` extends `ServiceImpl<StudentCourseMapper, StudentCourse>` and uses `ScoreInputMapper` for score input)
- Controller pattern: RESTful — `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping`. Standard CRUD pages follow: `/page` (paginated list), `/list` (flat list for dropdowns), `/{id}` (single), `POST` (create), `PUT` (update), `DELETE /{id}` (delete).
- Response pattern: All APIs return `R<T>` wrapper. Pagination returns `PageResult<T>`.

## Frontend Structure

- **Auto-imports:** `unplugin-auto-import` auto-imports Vue, Vue Router, Pinia, and Element Plus composables/functions. `unplugin-vue-components` auto-registers Element Plus components. DTS generated at `src/auto-imports.d.ts` and `src/components.d.ts`. This means Vue files do **not** need explicit `import { ref, computed } from 'vue'` — they are available globally. Note: `@vueuse/core` is installed (as `vue-demi`) but not explicitly auto-imported; use full imports when needed.
- **Router:** All routes statically defined in `src/router/index.js` (`constantRoutes`, ~45 routes). No per-route permission checks in the router guard. Guard is intentionally simplified: only calls `/auth/info` when `userInfo` is null (first load / session restore), then relies on cached data. Vue Router 4 with `createWebHistory()` (HTML5 history mode). Token-invalidated sessions are handled by 401 responses triggering logout + redirect. `pendingNav`/`navQueue` variables exist in exported `navState` but are unused — remnants of an abandoned race-condition fix.
- **Layout:** `src/layouts/MainLayout.vue` — sidebar (server-driven menus), topbar, tags-view, main content with `keep-alive` caching. Theme toggle button switches between light/dark mode. **AI Floating Assistant** (`AiFloatingAvatar.vue`) is embedded in MainLayout at bottom-right corner — draggable chat widget visible on all pages.
- **Stores:** Pinia stores in `src/store/`: `user.js` (auth state), `app.js` (theme/sidebar), `tagsView.js` (tabs). **Critical:** Always clear `tagsViewStore.visitedViews` and `tagsViewStore.cachedViews` on logout or auth failure to prevent stale tabs from the previous user leaking into the new session.
- **API layer:** `src/api/index.js` (barrel export, 102 API functions covering all modules), `src/api/auth.js` (7 auth functions). Base URL: `/api`, timeout: 15000ms. `src/api/request.js` has a unified interceptor — 401 responses trigger logout/redirect via `handleLogout()`. Use `markNavigation()` before any navigation call to suppress transient 401s from in-flight background requests during page transitions. See also `src/composables/useNavLock.js` for an alternative AbortController-based nav lock mechanism.
- **Pages:** `src/views/` mirrors backend modules (basic/, enrollment/, score/, exam/, evaluation/, notice/, system/, program/). View files include student/teacher-specific pages like `exam/my-exam/index.vue` and `exam/my-invigilate/index.vue`.
- **Components:** `src/components/AiFloatingAvatar.vue` — draggable AI chat widget. All views are hand-built; no reusable CRUD component templates exist. Dashboard uses unified `navigateTo(path)` helper for quick link navigation (validates token before routing). Student exam schedule path is `/exam/my-exam` (not `/exam/my`).
- **Global icons:** `main.js` registers all `@element-plus/icons-vue` icons globally, so they can be used in templates without explicit import. However, some views (e.g. `dashboard`) also import specific icons individually — both approaches work.
- **Path alias:** `@` → `src/` (configured in `vite.config.js`).
- **Tags-view (tabs):** `tagsView.js` maintains `visitedViews` and `cachedViews`. Routes with `meta.affix: true` (currently only the dashboard/home page) stay pinned and are never closable.
- **Login redirect:** After login, frontend always navigates to `/dashboard` regardless of userType (see `getDefaultPath` in `login/index.vue`). From there, each role's dashboard drives them to their relevant pages via quick links.
- **Auth state recovery:** `user.js` initializes all auth state from `sessionStorage` on page load (`token`, `userInfo`, `roles`, `menus`, `userType`) to avoid re-fetching `/auth/info` on every route change. Router guard only calls `/auth/info` when `userInfo` is null (initial page load or session restore). If token becomes invalid mid-session, subsequent API calls return 401 → logout flow.
- **Navigation 401 suppression:** `src/api/request.js` exports `markNavigation()` — call it before any navigation (`router.replace`) to suppress transient 401s from background requests during page transitions. Used by `navigateTo()` in `dashboard/index.vue` and `handleMenuSelect()` in `MainLayout.vue`.

## Key Menus & Roles (Current State)

| Role | userType | Key Menus |
|------|----------|-----------|
| ROLE_ADMIN | 3 | All menus (基础管理, 学生/教师管理, 教务/选课/成绩/考试/通知公告/系统管理) |
| ROLE_ACADEMIC | — | 基础管理, 学生/教师管理, 教务/选课/成绩/考试管理, 通知公告 |
| ROLE_TEACHER | 2 | 首页, 成绩录入, 我的通知, **我的监考** (/exam/my-invigilate), 成绩管理, 通知公告 |
| ROLE_STUDENT | 1 | 首页, 我的选课, **选择课程** (/enrollment/select), **我的考试安排** (/exam/my-exam), 我的成绩, 我的通知, 成绩管理, 通知公告 |

**Important:**
- Teacher role has been cleaned up — removed access to 选课管理/我的选课 (teachers don't need to select courses), 考试安排, 考场分配, and other admin-only menus. Teacher now only sees "我的监考" page showing their assigned invigilation duties.
- Student role has been updated — students see "我的考试安排" page and "选择课程" page. "选择课程" is gated by the enrollment switch (`sys:config:enrollment_enabled` in Redis).
- AI chat is accessible via the floating robot emoji (🤖) in bottom-right corner on all pages — no separate menu item needed. An unused `src/views/ai/assistant.vue` exists but is not referenced in the router.

When adding new menus, add to `sys_menu` and grant to roles via `sys_role_menu`. Both parent (type=0) and child (type=1) menus need inclusion.

## Menu System

Menus are server-driven: after login, `/auth/info` returns the user's menus as a tree. Frontend `buildMenuTree()` in `MainLayout.vue` filters by `visible !== 0` and sorts by `sort`. A `DEFAULT_MENUS` fallback exists in case the server returns no menus.

**Route parent paths:** Every parent menu path in the DB (e.g. `/basic`, `/score`, `/notice`) must have a corresponding redirect route in `src/router/index.js` so `el-menu`'s router mode doesn't navigate to a non-existent path. Child routes must use the exact `path` from the database. In the router config, these parent paths appear as `{ path: 'basic', meta: { title: '基础管理' }, children: [...] }` — the `redirect` property was removed to prevent flash on first click; the parent path acts purely as a grouping container.

**Menu index matching:** `el-menu` uses `@select="handleMenuSelect"` event handler (NOT router mode). Menu `:index` values must match route paths exactly. Child path resolution: `child.path.startsWith('/') ? child.path : (menu.path + '/' + child.path)`. Parent menu items with children do NOT navigate (only toggle expand/collapse). Clicking an already-active page skips navigation. A nav-lock (`isNavigating`) prevents double-click race conditions — set to true before push, reset after 200ms timeout in `.finally()`.

**Router parent routes:** Every parent menu path MUST have a `redirect` pointing to its first child route so clicking the parent navigates to a real page instead of a blank redirect. Configured in `src/router/index.js`:
- `/basic` → `college`, `/student` → `list`, `/teacher` → `list`
- `/program` → `course`, `/enrollment` → `my`
- `/score` → `student-input`, `/exam` → `arrange`
- `/evaluation` → `submit`, `/notice` → `list`, `/system` → `user`
- Root `/` → `dashboard`

**Menu SQL reference:**
```sql
-- Check role-menu assignments
SELECT r.code as role, m.name, m.path FROM sys_role_menu rm
JOIN sys_role r ON rm.role_id = r.id
JOIN sys_menu m ON rm.menu_id = m.id
ORDER BY r.code, m.sort;

-- Grant all menus to a role
INSERT INTO sys_role_menu (role_id, menu_id) SELECT <role_id>, id FROM sys_menu;
```

Seed data is in `docker/sql/init/02-data.sql`. Database initialization SQL is in `docker/sql/init/01-schema.sql` and `02-data.sql`.

## Default Login Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | 系统管理员 (ROLE_ADMIN, userType=3) |
| teacher01 | teacher123 | 教师 (ROLE_TEACHER, userType=2) |
| student01 | student123 | 学生 (ROLE_STUDENT, userType=1) |

Passwords are BCrypt hashes (`$2a$` format). If passwords stop working after DB re-initialization, regenerate them:

```bash
# Generate BCrypt hashes using Anaconda Python
/d/anaconda3/python -c "
import bcrypt
for pwd in ['admin123', 'teacher123', 'student123']:
    salt = bcrypt.gensalt(rounds=10, prefix=b'2a')
    print(f'{pwd}: {bcrypt.hashpw(pwd.encode(), salt).decode()}')
"

# Update DB
"/c/Users/Administration/AppData/Local/Programs/DockerDesktop/resources/bin/docker.exe" exec ais-mysql mysql -u ais -p'ais@2026' ueas -e "UPDATE sys_user SET password = '<hash>' WHERE username='<user>';"
```

## Code Style Conventions

### Backend Java
- All files use Chinese Javadoc comments (`/** ... */`)
- Controllers have class-level doc + `@Operation` annotations for Knife4j
- Services use `CacheHelper` for cache-aware operations
- Use `LambdaQueryWrapper` for simple dynamic queries; use `@Select("<script>...")` for complex JOINs with conditional filtering
- `removeById` override must use `Serializable` parameter, not `Long`
- Add `@Slf4j` to controllers/services that log; remove it from classes that don't use `log.` calls
- **DTOs live in** `ueas-system/src/main/java/com/ais/system/dto/` (auth DTOs) **and** `ueas-basic/src/main/java/com/ais/basic/dto/` (DashboardStats, course/major/teacher create DTOs): `LoginDTO`, `LoginResult`, `UserInfo`, `UserCreateDTO`, `UserUpdateDTO`, `UserQueryDTO`, `DashboardStats`, `CourseCreateDTO`, `MajorCreateDTO`, `TeacherCreateDTO`
- **Entity base class**: `BaseEntity` (ueas-common) provides `id`, `createTime`, `updateTime`, `deleted` fields with auto-fill via `MyBatisPlusMetaHandler`

### Frontend Vue/JS
- API functions documented with Chinese JSDoc comments
- Stores use Pinia composition API with `ref()` and `defineStore()`
- Router guards check token and fetch user info on navigation
- Error handling uses `ElMessage` for user feedback
- **AI Floating Component**: `AiFloatingAvatar.vue` is a draggable chat widget positioned fixed at bottom-right. It uses `position: fixed` with JS-driven drag via `mousedown/mousemove/mouseup`. Chat window appears to the left of the avatar. Size: 420px wide × 520px tall.
- **Logout hygiene**: Always clear `tagsViewStore.visitedViews` and `tagsViewStore.cachedViews` in both `user.js/logoutAsync()` and router guard auth-failure paths to prevent stale tabs from persisting across user sessions.

## Common Issues & Fixes

### Dashboard showing zeros for student/teacher accounts
**Root cause:** Two bugs combined to cause zero data for non-admin users:
1. `JwtAuthenticationFilter` was annotated `@Component` but never registered in the servlet filter chain — request attributes were never set
2. `SaTokenDaoRedisImpl.getObject()` used `ObjectMapper.readValue(json, Object.class)` which deserializes SaSession JSON into a plain `Map`, not a `SaSession` instance. The `instanceof SaSession` check in `getSession()` always failed, so `StpUtil.getSession().get("userType")` returned null
3. `StudentCourseMapper.countByStudentId()` had an un-wrapped `<if>` tag in `@Select` annotation

**Fixes applied:**
- Created `SecurityFilterConfig.java` to register `JwtAuthenticationFilter` with `Ordered.LOWEST_PRECEDENCE` (after Sa-Token interceptor)
- Fixed `SaTokenDaoRedisImpl.getObject()` to properly reconstruct `SaSession` objects from JSON by detecting SaSession-specific fields (`id`, `type`, `loginType`)
- Wrapped `<if>` tag in `<script>`...`</script>` in `StudentCourseMapper.countByStudentId()`
- Added Sa-Token session fallback in `UserContextUtil.getCurrentUserId()` and `getUserType()`

**Verification:** Student01 dashboard now shows 8 courses, avg score 92.25, pass rate 100%. Teacher01 dashboard shows 2 students, avg score 95.5, pass rate 100%.

### Login fails after password hash corruption
**Symptom:** Correct credentials return "账号或密码错误" (code 1006).
**Root cause:** The password hash in `sys_user` doesn't match the plaintext password. This can happen if DB init scripts run out of order or if a migration is partially applied.
**Fix:** Regenerate BCrypt hashes and update the DB directly (see "Default Login Credentials" section above for the Python one-liner).

### Backend won't start after code changes
Run `mvn install -DskipTests` first to rebuild all modules, then restart. The `spring-boot:run` goal only runs the `ueas-admin` module.

### Menu Display Issues (empty sidebar after login)
1. `sys_menu` table empty — check seed data
2. Role-menu assignments missing — check `sys_role_menu`
3. **Stale sessionStorage** — `user.js` caches menus in `sessionStorage` on first login. If menus are added to DB after a user logs in, that user must **clear sessionStorage and re-login** to see the new menus. Clear with: browser DevTools → Application → sessionStorage → clear all → refresh page → re-login.
4. Backend not sorting menus — verify `SysUserServiceImpl.buildMenuTree()` sorts by `sort` field
5. **UTF-8 encoding issue**: When adding Chinese menu names via SQL, always use `SET NAMES utf8mb4;` before INSERT/UPDATE to avoid garbled text in database and browser

### Login returns 500 "系统内部错误"
If login fails after DB re-initialization, check that the `password` column contains BCrypt hashes, not SHA-256. Generate fresh hashes and update as shown above.

### Menu click flickering on first click
When clicking a parent menu (e.g. `/score`), the redirect chain causes a flash then logout.

**Root cause:** Parent routes had no `redirect`, so navigation to `/score` hit a non-component route, triggering auth revalidation → 401 → redirect to login → back to dashboard = flash.

**Fix applied (2026-11-09):**
1. Added `redirect` to all parent routes in `src/router/index.js` pointing to first child
2. `handleMenuSelect` uses nav-lock (`isNavigating`) with `router.push(cleanIndex)`, skips if already on target path
3. Root `/` route gets `redirect: 'dashboard'`
4. Child paths in `DEFAULT_MENUS` must use full paths (e.g. `/basic/college` not `college`)

### Missing database tables
The initial schema SQL may not have created all tables if Docker was started after init. Check with:
```bash
"/c/Users/Administration/AppData/Local/Programs/DockerDesktop/resources/bin/docker.exe" exec ais-mysql mysql -u ais -p'ais@2026' ueas -e "SHOW TABLES;"
```

### SQL syntax error in @Select annotations
Unwrapped `<if>` tags (without `<script>` wrapping) in `@Select` cause SQL syntax errors. With `<script>` wrapping, dynamic SQL tags work correctly — the codebase uses this pattern in `StudentCourseMapper` and `ScoreInputMapper`.

### Student/Teacher creation fails with "user_id doesn't have a default value"
The `student` and `teacher` tables have `user_id` as NOT NULL, but when creating via API without a linked user, it fails. Fix:
```bash
"/c/Users/Administration/AppData/Local/Programs/DockerDesktop/resources/bin/docker.exe" exec ais-mysql mysql -u ais -p'ais@2026' ueas -e "ALTER TABLE student MODIFY COLUMN user_id BIGINT NULL;"
"/c/Users/Administration/AppData/Local/Programs/DockerDesktop/resources/bin/docker.exe" exec ais-mysql mysql -u ais -p'ais@2026' ueas -e "ALTER TABLE teacher MODIFY COLUMN user_id BIGINT NULL;"
```

### App.vue JSDoc comment causes Vue compiler error
JSDoc comments containing HTML tags like `<p>` will be parsed as HTML by Vue SFC compiler, causing "Element is missing end idea" errors. **Never put JSDoc blocks outside `<template>` in `.vue` files.** Move comments inside `<script setup>` or use HTML comments `<!-- -->` before `<template>`.

### Frontend API file structure
API functions live in two files:
- `src/api/index.js` — main barrel export (102 API functions covering all modules)
- `src/api/auth.js` — auth functions (7 functions: login, logout, info, captcha)

When adding new API functions, add them to `src/api/index.js`. Frontend imports must match export names exactly.

### Vue-demi peer dependency warning
`node_modules` contains `vue-demi@0.14.10_vue@3.5.43` which may cause peer dependency warnings. This is harmless — `vue-demi` is a compatibility layer managed by `@vueuse/core`. It does not affect functionality.

### Avatar storage path changed
Avatar local cache directory was moved from `C:/uploads/avatars/` to `D:/university academic system/uploads/avatars/`. Update `application.yml`:
```yaml
ueas:
  avatar:
    dir: D:/university academic system/uploads/avatars/
```

### Student Exam Schedule Feature
Students now see their own exam schedule via `/exam/my-exam` page. Backend API `GET /exam/my` returns exams for student's enrolled courses (JOINs student_course → course_offer → exam). Filtered by student ID from `UserContextUtil.getStudentId()`.

### Teacher Invigilation Feature
Teachers now see only their assigned invigilation duties via `/exam/my-invigilate` page. Backend API `GET /exam/invigilate/my` returns exams where teacher is assigned via `exam_invigilate` table. Filtered by teacher ID from `UserContextUtil.getTeacherIds()`.

### 抢课功能 (Enrollment/Rush Course Feature)
Admin-controlled feature that allows students to select courses in a limited-time window.

**Backend:**
- `SysConfigController` (`ueas-system`): `GET/PUT /sys-config/enrollment/enabled` — admin toggle. When enabled, auto-creates a pinned notice to all users.
- `RushConfig` + `RushLimiter` (`ueas-common`): Rate limiter (default: 3 requests/sec per student) using Redis INCR+EXPIRE with Lua script. Time window control via `sys:config:enrollment_start` / `sys:config:enrollment_end` (epoch millis).
- `EnrollmentController.select()`: Checks `sys:config:enrollment_enabled`, time window, and rate limit before allowing course selection.
- `ResultCode`: `ENROLLMENT_DISABLED(4004)`, `ENROLLMENT_NOT_IN_WINDOW(4005)`, `RATE_LIMIT_EXCEEDED(4006)`
- `UserContextUtil.getTeacherIds()`: Admin (userType=3) calls `teacherMapper.selectAllTeacherIds()` to get ALL teachers (not just their own record).
- `TeacherMapper`: Added `selectAllTeacherIds()` for admin full-query.
- `ueas-system/pom.xml`: Added `ueas-notice` dependency (SysConfigController creates notices directly).

**Frontend:**
- `src/api/index.js`: Added `getEnrollmentEnabled()`, `setEnrollmentEnabled()`
- `src/views/dashboard/index.vue`: Admin dashboard shows enrollment switch card (toggle enables auto-notice)
- `src/views/enrollment/select/index.vue`: Loads switch state; if disabled, shows warning and disables submit button
- `src/layouts/MainLayout.vue`: Added `选择课程` to `DEFAULT_MENUS` for `ROLE_STUDENT`; fixed menu flickering

**Important:** `notice.publisher_id` is NOT NULL — always set it when creating notices programmatically.

### Bug Fixes (2026-10-30 ~ 2026-11-07)

| File | Issue | Fix |
|------|-------|-----|
| `ScoreController.java:114` | `teacherIds.get(0)` when list empty → IndexOutOfBoundsException | Added `isEmpty()` check |
| `DashboardServiceImpl.java` | `selectCount(null)` counted deleted records | Added `deleted=0` filter |
| `UserContextUtil.java` | Duplicate DB query in `getStudentId()` | Cached single query result |
| `CourseOfferController.java` | `teachers.get(0)` NPE when null | Added null check |
| `EnrollmentController.java` | Dead code `studentId < 0` | Removed redundant checks |
| `SysUserServiceImpl.java` | `StpUtil.getLoginIdAsLong()` without login check | Added `isLogin()` guard |
| `UploadController.java` | `Paths.get(null)` NPE in constructor | Added null-safe path handling |
| `CaptchaController.java` | AWT graphics exception | Wrapped in try-catch with fallback |
| `EnrollmentServiceImpl.java` | N+1 query in `getAvailableCourses` | Replaced with batch query + Java filtering |
| `EnrollmentServiceImpl.java` | `.apply("enrolled < max_students")` SQL injection | Moved to Java filter |
| `LogAspect.java` | Hardcoded IP `"127.0.0.1"` | Added real IP detection via `X-Forwarded-For`/`X-Real-IP` |
| `ExamController.java` | Teacher sees all exams | Added `getExamIdsByTeacherId()` filter, `_emptyFilter` for empty results |
| `ExamInvigilateController.java` | Teacher can edit/delete invigilation | Added `my` endpoint, removed write permissions for teachers |
| `ExamMapper.xml` | SQL OGNL syntax error with `size()` | Changed to `size > 0` |
| `src/store/user.js` | TagsView not cleared on logout → stale tabs persist across sessions | Clear `visitedViews` and `cachedViews` in `logoutAsync()` |
| `src/router/index.js` | Same issue in auth-failure paths | Clear `visitedViews` and `cachedViews` in 401 handler |
| DB `sys_user` | Admin password hash corrupted (did not match "admin123") | Regenerated BCrypt `$2a$` hashes via Python and updated all test accounts |
| `UserContextUtil.java` | Admin `getTeacherIds()` returned empty (only matched own teacher record, not all) | Admin now calls `selectAllTeacherIds()` for full query |
| `MainLayout.vue` | Parent menu click flickers → clicks first time trigger redirect then logout | Use nav-lock (`isNavigating`) in `handleMenuSelect`; add `redirect` to all parent routes; check `route.path !== cleanIndex` before navigating |
| `MainLayout.vue` | DEFAULT_MENUS missing 成绩录入 for ROLE_ADMIN | Added ROLE_ADMIN to 成绩录入 child roles |
| `MainLayout.vue` | "教务系统" logo text color differs from menu items | Set explicit `color: rgba(255,255,255,0.85) !important` on `#sidebar-logo .el-icon` and `.logo-text` in both light and dark theme styles |
| `SysConfigController.java` | Auto-notice creation failed (publisher_id NOT NULL) | Set `publisherId` from `SaTokenUtil.getCurrentUserId()` |
| `TeacherMapper.java` | Missing admin full-query method | Added `selectAllTeacherIds()` |
| `ResultCode.java` | Missing error codes for rush feature | Added ENROLLMENT_DISABLED(4004), ENROLLMENT_NOT_IN_WINDOW(4005), RATE_LIMIT_EXCEEDED(4006) |
| `dashboard/index.vue` | Quick links use backend API paths (e.g., `/student/list`) instead of frontend router paths → navigation fails or goes to wrong page | Fixed all admin/teacher/student quick links to use correct frontend route paths that match both router config and backend API endpoints |
| `dashboard/index.vue` | Teacher view: 5 stat cards all use `:span="6"` → total 30 > 24, 5th card wraps to next line | Changed to `:span="5"` for first 4 cards and `:span="4"` for the 5th (5+5+5+5+4=24) |
| `ExamInvigilateController.java:60` | `selectBatchIds(emptyList)` → SQL `WHERE id IN ()` → 500 | Guard with `examIds.isEmpty()` returning emptyMap |
| `MainLayout.vue` | Parent menu click triggers duplicate navigation when already in child page | Added check: if current path equals redirectTarget or its child, skip router.push |
| `StudentMapper.java` | `selectByUserId` uses `LIMIT 1` without ORDER BY → returns wrong student when duplicate `user_id` exists | Added `ORDER BY id DESC` to always return the latest record |
| `dashboard/index.vue` | Teacher view: 5 stat cards all use `:span="6"` → total 30 > 24, 5th card wraps to next line | Changed to `:span="5"` for first 4 cards and `:span="4"` for the 5th (5+5+5+5+4=24) |
| `dashboard/index.vue` | Quick links use wrong paths (e.g., `/exam/my` instead of `/exam/my-exam`) | Fixed student quick link path from `/exam/my` to `/exam/my-exam` |
| `dashboard/index.vue` | Clicking quick links causes flash then logout | Added `navigateTo()` function that validates token before navigation, uses consistent click handling |
| `src/router/index.js` | Router guard race condition: async `getInfo()` allowed concurrent navigations causing state issues | Guard refactored to simplified pattern — only calls `/auth/info` when `userInfo` is null (first load), relies on sessionStorage cache otherwise. `pendingNav`/`navQueue` exported but unused. |
| `src/router/index.js` | Stale sessionStorage userInfo could bypass token validation | Simplified guard: still calls `/auth/info` only when `userInfo` is null, but 401 from any API call triggers logout + redirect via `window.location.replace()` to prevent stuck states |
| `AiAssistantService.java` | AI introduced itself ("我是Agnes...") and answered non-educational questions | Updated all 4 system prompts: no self-introduction, restrict to academic affairs topics only |
| DB `sys_role_menu` | Admin account had extra ROLE_TEACHER assignment causing it to see teacher menus | Removed ROLE_TEACHER from admin's role assignments |

## Debugging

When menus don't display:
1. Check browser console for errors
2. Verify `userStore.menus` has data: `console.log(userStore.menus)`
3. Check `menuList` computed result in DevTools
4. Verify backend API: `curl http://localhost:8080/auth/info -H "Authorization: Bearer <token>"`
5. **Clear sessionStorage** if menus changed after login: DevTools → Application → sessionStorage → clear → refresh → re-login
6. Ensure no stale Vite cache: `rm -rf node_modules/.vite`
7. Check role-menu assignments in database

When AI assistant doesn't work:
1. Check IDEA 控制台日志 — 应出现 `Calling AI API: https://apihub.agnes-ai.com/v1/chat/completions`
2. 如出现 `AI chat error:`，查看完整堆栈定位问题
3. 确认后端已重启（IDEA 中 Stop → Run，或修改代码后 Run → Restart）

When timetable shows empty for students:
1. Check `student` table for duplicate `user_id` records: `SELECT id, user_id FROM student WHERE user_id = X;`
2. If multiple records exist, `selectByUserId` (LIMIT 1) may return the wrong one — check `student_course` enrollment data per student_id
3. Fix: delete empty duplicate records, or add `ORDER BY id DESC` to `selectByUserId` query

## Security Note

- **Hard-coded cloud credentials:** `application.yml` contains hard-coded Aliyun OSS access key ID and secret for avatar uploads. Move to environment variables before external deployment.
- **Passwords stored as BCrypt:** Use `cn.dev33.satoken.secure.BCrypt.hashpw()` for new passwords and `BCrypt.checkpw()` for verification.
- **`@Autowired` vs constructor injection:** A few config classes (`StpInterfaceImpl`, `SaTokenDaoConfig`, `SaTokenDaoRedisImpl`) use `@Autowired` field injection rather than constructor injection — inconsistent with the rest of the codebase which prefers constructor injection.
- **Legacy `springfox` config:** `application-dev.yml` contains a dead `springfox.documentation.enabled: true` property from a previous Swagger version. It has no effect — Knife4j is the active API docs tool.

## Docker Initialization Notes

Docker mounts `./sql/init` into the MySQL container's `/docker-entrypoint-initdb.d`. SQL files execute in **alphabetical order** on first startup:

1. `01-schema.sql` — creates all tables
2. `02-data.sql` — inserts seed data (users, roles, menus, basic data)
3. `03-add-indexes.sql` — adds performance indexes
4. `04-optimize.sql` — BCrypt password migration and additional indexes
5. `add-course-offer-version.sql` — adds optimistic lock version field to course_offer
6. `add-deleted-columns.sql` — adds missing logical delete columns

If you need to reinitialize the database from scratch, stop Docker, delete the `mysql_data` volume, and restart.

## Known Incomplete Areas

- **CrudPage.vue & PageContainer.vue:** These files were deleted on 2026-11-07 as completely unused. `src/components/` currently only contains `AiFloatingAvatar.vue`. No reusable CRUD component templates exist — all views are hand-built.
- **Aliyun OSS credentials:** `application.yml` uses `${ENV_VAR:default}` syntax for OSS keys (see line 63-66). Production deployments must override via environment variables; the embedded defaults are development-only. Move to env-only before external deployment.
- **SQL logging enabled in dev:** `application.yml` sets `mybatis-plus.configuration.log-impl: org.apache.ibatis.logging.stdout.StdOutImpl`, printing every SQL statement to stdout. Disable or change the log level before production builds.
- **`use-generated-keys: false`:** MyBatis-Plus is configured with `use-generated-keys: false` in `application.yml`; this means auto-incremented IDs are not returned to Java after insert — relevant if code needs the generated ID post-insert.
- **Docker named volumes:** `mysql_data`, `redis_data` — data persists across container restarts. To fully reset all services, stop Docker, run `docker volume rm mysql_data redis_data`, then restart. **RabbitMQ removed** (unused); `rabbitmq_data` volume declaration remains in docker-compose.yml as an orphan — harmless but can be cleaned up.
- **Removed unused items (2026-11-07):** `spring-boot-starter-amqp` dependency (RabbitMQ auto-config was excluded anyway), `easyexcel` dependency (zero usages), RabbitMQ service from docker-compose.yml. Also updated `UniversityAisApplication.java` to drop explicit `exclude = {RabbitAutoConfiguration.class}` since Spring Boot now just skips it when no AMQP starter is present.
- **ueas-schedule module status:** The `ueas-schedule/` directory no longer exists on disk, but its `<module>` entry remains in the parent POM. This causes Maven build failures (`Could not find the specified project`) until the entry is removed from the POM. If scheduling features are needed, create the directory and add code under `ueas-basic` or `ueas-enrollment` instead.
- **Hard-coded credentials moved to env vars (2026-11-07):** `application.yml` `oss.*` and `ai.openai.api-key/base-url` now use `${ENV_VAR:default}` syntax; regenerate secrets for production via environment variables or `application-prod.yml`.
- **Spring AI 已集成（ueas-ai 模块）：**
  - 使用 **RestTemplate 直接调用** OpenAI 兼容接口（绕过 Spring AI ChatClient 的反序列化问题）
  - Agnes AI 返回 `reasoning_content` 和 `provider_specific_fields` 等非标准字段，Spring AI M5 的 Jackson 会拒绝导致反序列化失败
  - 已配置 `fail-on-unknown-properties: false`（application.yml），但 Spring AI 内部使用独立 ObjectMapper 不受影响
  - API Key 和 Base URL 配置在 `ueas-admin/src/main/resources/application.yml` 中
  - 提供 4 个 AI 接口：`/ai/chat`（通用）、`/ai/chat/student`（学生）、`/ai/chat/teacher`（教师）、`/ai/chat/admin`（管理员）
  - **前端AI入口改为浮动按钮**：`src/components/AiFloatingAvatar.vue` 是可拖拽的卡通机器人（🤖 emoji），固定在页面右下角，点击展开420×520px聊天窗口
  - AI 菜单项已从数据库和路由中移除（不再显示在侧边栏），仅通过浮动按钮访问
  - 配置示例：
    ```yaml
    spring:
      ai:
        openai:
          api-key: your-api-key
          base-url: https://api.example.com/v1
          chat:
            options:
              model: agnes-2.5-flash
              temperature: 0.7
    ```
  - **注意：** 如果返回 "AI 服务暂时不可用"，检查：
    1. IDEA 是否已重新编译并重启后端（日志中应出现 `Calling AI API:` 信息）
    2. Agnes AI 账户的 API 配额是否充足（免费版有限流）
    3. 直接测试 API：`curl https://apihub.agnes-ai.com/v1/chat/completions` 检查返回错误信息
  - **配置文件位置：** AI API Key 和 Base URL 配置在 `ueas-admin/src/main/resources/application.yml`（`spring.ai.openai.api-key` / `spring.ai.openai.base-url`），通过 `${AI_API_KEY:sk-placeholder}` / `${AI_BASE_URL:https://apihub.agnes-ai.com}` 环境变量覆盖。另有独立的 `application-ai.yml`，提供不同的默认值（默认指向 deepseek 而非 agnes-ai）。生产环境使用 `application-prod.yml` 中的 `${ENV_VAR}` 占位符注入。


### Dashboard Quick Links & API Path Issues (2026-11-07)

**Problem:** Clicking dashboard stat cards or quick link grid items caused "闪一下退出再进入" or no response.

**Root Cause:** 
1. Frontend quick links used route-like paths but some didn't exist as direct routes — they needed parent redirect traversal
2. `sessionStorage` cached stale `userInfo` while backend session expired → homepage loaded fine, but API calls on click returned 401 → redirect to login → back to dashboard = flash
3. Concurrent navigation requests during async `getInfo()` caused race conditions
4. Student exam schedule path was wrong: `/exam/my` instead of `/exam/my-exam`

**Fix Applied:**
1. Added unified `navigateTo(path)` helper in dashboard that validates token before routing
2. Fixed student quick link path from `/exam/my` to `/exam/my-exam`
3. Simplified router guard — only calls `/auth/info` when `userInfo` is null (first load)
4. Login page now always redirects to `/dashboard` regardless of userType

**Important patterns:**
- All quick links use `@click="navigateTo(item.path)"` pattern (never inline `$router.push()`)
- `navigateTo()` checks `userStore.token` first, then pushes route
- Router guard uses simplified synchronous callback — no queue mechanism

### Schedule Module Naming Mismatch (Ongoing)

The `ueas-schedule/` directory exists on disk with a `pom.xml` declaring dependencies on `ueas-common` + `ueas-basic` + `ueas-enrollment`, but **zero Java source files**. It IS declared in the root parent POM's `<modules>` list — this will cause Maven build failures (`Could not find the specified project`) until either:
- The module entry is removed from the POM, OR
- The directory is populated with source code

Do not add code to this module; schedule-related features belong in `ueas-basic` or `ueas-enrollment`.

**Menu path drift:** `02-data.sql` seed data creates four menu entries under the `排课管理` parent with paths `/schedule/manual`, `/schedule/auto`, `/schedule/adjust`, `/schedule/offering` — none of which have matching frontend router entries or backend controllers. The actual implementation lives at `program/offering` (开课管理) and `program/course` (课程管理) in both the router and the view layer. Clicking those dead admin menus produces blank/error pages. When adding new menus, use existing working paths as reference and ensure `router/index.js` + view components match.

**Seed data student role bug:** `02-data.sql` line 153 grants ROLE_STUDENT (role_id=4) the path `/exam/arrange` — an admin-only exam scheduling page that students should not see. The correct student exam path is `/exam/my-exam` (student self-service schedule). Fix: change the SQL to `path IN ('/dashboard', '/enrollment/my', '/score/my', '/evaluation/submit', '/exam/my-exam')`. Re-running init from scratch would overwrite any manual DB edits, so this must be fixed in the source SQL file going forward.



**Note:** Vite proxy rewrites `/api/*` → backend root, so frontend calls `request.get('/course/page')` which becomes `GET /course/page` on backend :8080.

### AI 助手 prompt 优化 (2026-11-07)

AI 助手的四个接口（`/ai/chat`, `/ai/chat/student`, `/ai/chat/teacher`, `/ai/chat/admin`）的 systemPrompt 已更新：
- **禁止自我介绍**：不再输出"我是 Agnes..."等开场白
- **范围限制**：只回答教务系统和学习相关问题，无关问题回复"这个问题跟我当前的工作不太相关哦，我只负责教务系统相关的问题~"
- **限流保护**：同一 2.5 秒窗口内最多调用一次 AI API，避免耗尽免费配额触发 429 错误

配置通过环境变量注入：`AI_API_KEY`, `AI_BASE_URL`, `OSS_ENDPOINT`, `OSS_ACCESS_KEY_ID`, `OSS_ACCESS_KEY_SECRET`。
