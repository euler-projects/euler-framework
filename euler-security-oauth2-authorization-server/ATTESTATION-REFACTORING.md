# Attestation-Based Client Authentication 改造记录

基于 [draft-ietf-oauth-attestation-based-client-auth-08](https://www.ietf.org/archive/id/draft-ietf-oauth-attestation-based-client-auth-08.html)，为 APP 类 Client 引入 Attestation-Based Client Authentication。

## 改造概述

- Attestation 支持两种使用模式：作为独立的 `ClientAuthenticationMethod`（`attest_jwt_client_auth`，草案 Section 6.3/13.4），或作为现有 Client Authentication 之上的**增强层**（草案 Section 6.4）
- 新增 `ClientAttestationAuthenticationConverter` + `ClientAttestationAuthenticationProvider` 在 `OAuth2ClientAuthenticationFilter` 中完成 `attest_jwt_client_auth` 客户端认证
- `ClientAttestationFilter` 后置过滤器仅负责 Scenario A 增强验证和 `attest_jwt_client_auth` 客户端的 keyId 提取
- 自定义 `OAuth-Client-Attestation-PoP-Type` 扩展机制，支持 `jwt`（草案标准）和 `App-Attest`（Apple Assertion）两种互斥的 PoP 类型
- `apple_app_attest_assertion` Grant Type 精简为仅负责匿名用户解析和令牌签发
- `attest_jwt_client_auth` 客户端不需要请求携带 `client_id`，由 attestation 数据推导
- 移除 Core 层 Assertion 认证组件（未来非 OAuth2 场景将集成 WebAuthn）

## 已完成的工作

### Task 1: 新增常量定义

- `EulerOAuth2ParameterNames` — 新增 Header 名称常量（`OAuth-Client-Attestation`、`OAuth-Client-Attestation-PoP`、`OAuth-Client-Attestation-PoP-Type`）和 PoP 类型常量（`jwt`、`App-Attest`）
- `EulerOAuth2ErrorCodes` — 新增草案 Section 6.2 定义的错误码（`use_attestation_challenge`、`use_fresh_attestation`、`invalid_client_attestation`）

### Task 2: APP-to-OAuth Client 映射

- `AppAttestRegistration` — 新增 `clientId` 字段
- `JdbcAppAttestRegistrationService` — `app_attest_registration` 表新增 `client_id` 列，更新 INSERT/SELECT SQL

### Task 3: ClientAttestationVerifier 扩展接口

- 新建 `ClientAttestationVerifier.java`（euler-security-core）— 可选接口，验证 Client Attestation JWT 并返回 confirmation key

### Task 4: ClientAttestationFilter 核心组件

- 新建 `ClientAttestationFilter.java`（euler-security-oauth2-authorization-server）— `OncePerRequestFilter`
- 支持场景 A（标准 Client Auth + Attestation 增强层）
- `attest_jwt_client_auth` 客户端由 `ClientAttestationAuthenticationConverter` + `ClientAttestationAuthenticationProvider` 在 `OAuth2ClientAuthenticationFilter` 中完成认证，Filter 仅提取 keyId
- PoP 验证按 `OAuth-Client-Attestation-PoP-Type` 分发：`jwt`（Nimbus JWT 验签）或 `App-Attest`（Apple Assertion）
- 通过 request attribute `attestation.verified.key_id` 向下游 Converter 传递已验证的 keyId
- 可选依赖：`ClientAttestationVerifier`、`ChallengeService`、`NonceService`

### Task 5: 移除 EulerPublicClientAuthentication

- 删除 `EulerPublicClientAuthenticationConverter.java`
- 删除 `EulerPublicClientAuthenticationProvider.java`
- `attest_jwt_client_auth` 客户端认证由 `ClientAttestationAuthenticationConverter` + `ClientAttestationAuthenticationProvider` 接管

### Task 6: 精简 Grant Type（匿名用户模式）

- `OAuth2AppleAppAttestAssertionAuthenticationToken` — 移除 `assertion`/`challenge` 字段，仅保留 `keyId` 和 `scopes`
- `OAuth2AppleAppAttestAssertionAuthenticationConverter` — 改为从 request attribute 读取 `keyId`
- `OAuth2AppleAppAttestAssertionAuthenticationProvider` — 移除 `AuthenticationManager`/`ChallengeService` 依赖，新增 `EulerAppleAppAttestUserDetailsService` 依赖，直接加载/创建匿名用户
- 删除 Core 层组件：`AppleAppAttestAssertionAuthenticationProvider.java`、`AppleAppAttestAssertionAuthenticationToken.java`、`InitializeAppleAppAttestBeanManagerConfigurer.java`
- 清理 `EulerBootSecurityAutoConfiguration` 中对应 Bean 定义
- 删除 Jackson 序列化支持：`AppleAppAttestAssertionAuthenticationTokenDeserializer.java`、`AppleAppAttestAssertionAuthenticationTokenMixin.java`
- 清理 `EulerSecurityJacksonModule` 中对已删除 Token 的 polymorphic type 和 mixin 注册

### Task 7: 配置集成

- `EulerAuthorizationServerConfiguration.configAttestationBasedClientAuthentication()` — 注册 `ClientAttestationFilter`（addFilterAfter）+ 精简版 Grant Type + Challenge 端点（`/oauth2/challenge`）
- `EulerOAuth2ConfigurerUtils` — 新增 `getAppAttestRegistrationService`、`getAppleAppAttestValidationService`、`getAppleAppAttestUserDetailsService`、`getClientAttestationVerifier`、`getNonceService` 方法
- `OAuth2ConfigurerUtilsAccessor` — 新增 `getAuthorizationServerSettings` 方法

### Task 9: Challenge 端点适配

- `ChallengeEndpointFilter`（`euler-security-web`）— 通用 challenge 端点过滤器，响应返回 `challenge` 字段
- 添加 `Cache-Control: no-store` 和 `Pragma: no-cache` 响应头
- 原 `AppAttestChallengeEndpointFilter` 和 `OAuth2AttestationChallengeEndpointFilter` 合并为此通用实现

### Task 11: PoP JWT jti 重放检测

- 新建 `NonceService.java` 接口（euler-security-core）— 追踪客户端生成的 nonce/jti 值
- 新建 `InMemoryNonceService.java` 实现（euler-security-core）— `ConcurrentHashMap` + 懒过期清理
- `ClientAttestationFilter` — 集成 `NonceService`，在 PoP JWT 验签成功后检查 `jti` claim（草案 Section 12.1）

### Task 12: AS Metadata 支持

- OIDC Provider Configuration (`.well-known/openid-configuration`) 和 OAuth2 AS Metadata (`.well-known/oauth-authorization-server`) 两个端点同时声明：
  - `token_endpoint_auth_methods_supported`: 追加 `attest_jwt_client_auth`（草案 Section 13.4 IANA 注册）
  - `attestation_challenge_endpoint`
  - `client_attestation_signing_alg_values_supported`: `["ES256"]`
  - `client_attestation_pop_signing_alg_values_supported`: `["ES256"]`

### Task 14: 引入 `attest_jwt_client_auth` 认证方式（草案 Section 6.3/13.4）

遵循 RFC 7591 一个客户端仅一种 `token_endpoint_auth_method` 的原则，引入正式的 `attest_jwt_client_auth` 认证方式，取代之前 Scenario B 中使用 `none` 的权宜方案。

- `EulerOAuth2ParameterNames` — 新增 `ATTEST_JWT_CLIENT_AUTH` 常量（已迁移至 `EulerClientAuthenticationMethod`）
- 新建 `EulerClientAuthenticationMethod.java`（euler-security-oauth2-core）— `ATTEST_JWT_CLIENT_AUTH` 常量定义
- `ClientAttestationFilter` — Scenario B 入口条件改为校验 `contains(ATTEST_JWT_CLIENT_AUTH)`，认证 Token 使用 `ATTEST_JWT_CLIENT_AUTH` 方法（已在 Task 15 中进一步重构为 Converter+Provider）
- `EulerOAuth2ClientRegistrationRegisteredClientConverter` — 新增识别 `attest_jwt_client_auth` 认证方式，无需 `client_secret`
- `EulerAuthorizationServerConfiguration` — AS Metadata 两个端点追加 `tokenEndpointAuthenticationMethod("attest_jwt_client_auth")`

三种合法场景：

| 场景 | 客户端 auth method | 处理路径 |
|------|-------------------|----------|
| 纯 Attestation 认证 | `attest_jwt_client_auth` | `ClientAttestationAuthenticationConverter` + `ClientAttestationAuthenticationProvider` → `ClientAttestationFilter` 提取 keyId |
| 传统认证 + Attestation 增强 | `client_secret_basic` 等 | 标准认证 → `ClientAttestationFilter` Scenario A |
| PKCE 公共客户端 + Attestation 增强 | `none` | `PublicClientAuthenticationProvider` → `ClientAttestationFilter` Scenario A |

### Task 15: Converter + Provider 重构（将 Scenario B 迁移至 `OAuth2ClientAuthenticationFilter`）

将 `attest_jwt_client_auth` 客户端认证从 `ClientAttestationFilter` Scenario B 迁移至标准的 `OAuth2ClientAuthenticationFilter` 中，通过新增 Converter + Provider 对实现。

- 新建 `ClientAttestationAuthenticationConverter.java` — 提取 attestation headers，解析 keyId，通过 `AppAttestRegistrationService` 解析 clientId，创建未认证的 `OAuth2ClientAuthenticationToken`
- 新建 `ClientAttestationAuthenticationProvider.java` — 验证 Client Attestation JWT（可选）和 PoP（JWT 或 App-Attest），返回已认证 Token（keyId 存入 credentials）
- 简化 `ClientAttestationFilter` — 移除整个 Scenario B，仅保留：
  - `attest_jwt_client_auth` 客户端：从 `credentials` 提取 keyId 设置 request attribute
  - Scenario A：标准认证 + Attestation 增强验证
- 移除 `ClientAttestationFilter` 对 `RegisteredClientRepository` 的依赖
- `EulerAuthorizationServerConfiguration` — 通过 `clientAuthentication()` 注册 Converter + Provider

### Task 16: 合并 `ClientAttestationVerifier` 与 `PopJwtVerifier`

将原 `PopJwtVerifier`（PoP JWT 验证）和原 `ClientAttestationVerifier` 接口（euler-security-core，可选的 Client Attestation JWT 验证扩展点）合并为统一的 `ClientAttestationVerifier` 具体类（euler-security-oauth2-authorization-server），提供两种验证入口：

- `verify(attestationJwt, popJwt)` — 标准草案流程（暂未实现 attestation JWT 验证，降级为 kid 反查）
- `verify(popJwt)` — kid 反查模式，从 PoP JWT header 取 kid 查公钥验证

同时彻底简化 Converter，将所有解析和验证职责后移到 Provider：

- 删除 `PopJwtVerifier.java`（euler-security-oauth2-authorization-server）
- 删除旧 `ClientAttestationVerifier.java` 接口（euler-security-core）
- 新建 `ClientAttestationVerifier.java`（euler-security-oauth2-authorization-server）— 合并 PoP JWT 验证逻辑和 kid 反查，返回 `PopVerificationResult(keyId, clientId, registration)`
- 简化 `ClientAttestationAuthenticationConverter` — 移除所有依赖（`AppAttestRegistrationService`）和解析逻辑（JWT 解析、kid 提取、clientId 解析），变为纯数据搬运：仅收集原始 header/参数，用占位符 `__attestation__` 作为临时 principal
- 重构 `ClientAttestationAuthenticationProvider` — 承担全部解析和验证职责：kid 提取和 clientId 解析由 `ClientAttestationVerifier` 完成（JWT PoP），或由 `AppAttestRegistrationService` 完成（App-Attest PoP）；验证后再查 RegisteredClient
- 更新 `ClientAttestationFilter` — 替换旧的 `oauth2ClientAttestationVerifier`（接口类型）和 `popJwtVerifier` 为新的 `ClientAttestationVerifier`，移除 `extractSubFromAttestationJwt` 方法
- 更新 `EulerAuthorizationServerConfiguration` — 创建 `ClientAttestationVerifier` 替代 `PopJwtVerifier`，Converter 改为无参构造
- 清理 `EulerOAuth2ConfigurerUtils` — 移除 `getClientAttestationVerifier` 方法（旧接口）

## 文件变更摘要

| 操作 | 文件路径 |
|------|----------|
| 新建 | `euler-security-oauth2-core/.../core/EulerClientAuthenticationMethod.java` |
| 新建 | `euler-security-oauth2-authorization-server/.../web/authentication/ClientAttestationAuthenticationConverter.java` |
| 新建 | `euler-security-oauth2-authorization-server/.../authentication/ClientAttestationAuthenticationProvider.java` |
| 新建 | `euler-security-oauth2-core/.../core/EulerOAuth2ErrorCodes.java` |
| 新建 | `euler-security-core/.../authentication/ClientAttestationVerifier.java` |
| 新建 | `euler-security-core/.../authentication/NonceService.java` |
| 新建 | `euler-security-core/.../authentication/InMemoryNonceService.java` |
| 新建 | `euler-security-oauth2-authorization-server/.../authentication/ClientAttestationVerifier.java` |
| 新建 | `euler-security-oauth2-authorization-server/.../web/ClientAttestationFilter.java` |
| 修改 | `euler-security-oauth2-core/.../endpoint/EulerOAuth2ParameterNames.java` |
| 修改 | `euler-security-core/.../apple/AppAttestRegistration.java` |
| 修改 | `euler-security-core/.../apple/JdbcAppAttestRegistrationService.java` |
| 新建 | `euler-security-web/.../web/authentication/ChallengeEndpointFilter.java` |
| 重写 | `euler-security-oauth2-authorization-server/.../authentication/OAuth2AppleAppAttestAssertionAuthenticationProvider.java` |
| 重写 | `euler-security-oauth2-authorization-server/.../authentication/OAuth2AppleAppAttestAssertionAuthenticationToken.java` |
| 重写 | `euler-security-oauth2-authorization-server/.../web/authentication/OAuth2AppleAppAttestAssertionAuthenticationConverter.java` |
| 修改 | `euler-security-oauth2-authorization-server/.../EulerAuthorizationServerConfiguration.java` |
| 修改 | `euler-security-oauth2-authorization-server/.../EulerOAuth2ConfigurerUtils.java` |
| 修改 | `euler-security-oauth2-authorization-server/.../OAuth2ConfigurerUtilsAccessor.java` |
| 修改 | `euler-boot-autoconfigure/.../EulerBootSecurityAutoConfiguration.java` |
| 删除 | `euler-security-core/.../authentication/ClientAttestationVerifier.java` |
| 删除 | `euler-security-oauth2-authorization-server/.../authentication/PopJwtVerifier.java` |
| 删除 | `euler-security-oauth2-authorization-server/.../web/authentication/EulerPublicClientAuthenticationConverter.java` |
| 删除 | `euler-security-oauth2-authorization-server/.../authentication/EulerPublicClientAuthenticationProvider.java` |
| 删除 | `euler-security-core/.../apple/AppleAppAttestAssertionAuthenticationProvider.java` |
| 删除 | `euler-security-core/.../apple/AppleAppAttestAssertionAuthenticationToken.java` |
| 删除 | `euler-boot-autoconfigure/.../InitializeAppleAppAttestBeanManagerConfigurer.java` |
| 删除 | `euler-security-core/.../jackson/AppleAppAttestAssertionAuthenticationTokenDeserializer.java` |
| 删除 | `euler-security-core/.../jackson/AppleAppAttestAssertionAuthenticationTokenMixin.java` |
| 修改 | `euler-security-core/.../jackson/EulerSecurityJacksonModule.java` |
| 修改 | `euler-security-oauth2-authorization-server/.../converter/EulerOAuth2ClientRegistrationRegisteredClientConverter.java` |

## 遗留问题

### 1. 注册端点 clientId 绑定（Task 2.3）

设备注册时如何绑定 `clientId` 需要额外讨论。当前 `AppAttestRegistrationEndpointFilter` 的注册流程不涉及 OAuth2 Client。可能的方案：

- **方案 A**: 注册请求携带 `client_id` 参数，Filter 校验后写入 `AppAttestRegistration`
- **方案 B**: 注册端点要求 OAuth2 Bearer Token 认证，从 token 中提取 `client_id`
- **方案 C**: 配置时指定默认 `client_id`，注册时自动绑定

### 2. Refresh Token 绑定 Client Instance Key（Task 10，草案 Section 10.2）

暂不实现。内置 Provider（`OAuth2AuthorizationCodeAuthenticationProvider` 等）无法扩展以注入 `keyId` 到 `OAuth2Authorization` attributes。需要装饰 `OAuth2AuthorizationService` 才能实现。当前阶段仅保证匿名用户模式下 Client 必须通过 Attestation 验证。

### 3. ClientAttestationVerifier 实际启用（草案 Section 5.1）

当前 `ClientAttestationVerifier.verify(attestationJwt, popJwt)` 中的 attestation JWT 验证暂未实现，降级为 kid 反查。要启用需要：
- App 后端（Client Attester）部署签发 JWT 的密钥对
- Authorization Server 配置 Attester 的公钥以验签
- 在 `ClientAttestationVerifier` 中实现 attestation JWT 验证逻辑

### 4. 非 OAuth2 场景 Assertion 认证迁移至 WebAuthn

已移除 Core 层的 `AppleAppAttestAssertionAuthenticationProvider` 和 `AppleAppAttestAssertionAuthenticationToken`。非 OAuth2 场景（如表单登录）的 assertion 认证待集成 WebAuthn 标准。

### 5. Grant Type 重命名考虑

`apple_app_attest_assertion` grant type 现在仅负责匿名用户解析，不再包含 assertion 验证逻辑。名称可能需要重命名为更通用的标识（如 `device_attestation`），但需考虑客户端兼容性。

### 6. PoP-Type 扩展（Play-Integrity 等）

`OAuth-Client-Attestation-PoP-Type` 扩展机制已预留，当前支持 `jwt` 和 `App-Attest`。未来可添加 `Play-Integrity`（Google）等新类型，仅需在 `ClientAttestationFilter` 的 switch 分支中添加处理逻辑。

### 7. RedisNonceService 实现

`InMemoryNonceService` 仅适用于单实例部署。集群部署需要 Redis 实现以确保跨节点的 jti 重放检测一致性。实现方式：`SET key "" NX EX ttl`。

### 8. Challenge 端点路径与 AS Metadata 同步

当前 AS Metadata 中硬编码 `attestation_challenge_endpoint` 路径为 `{issuer}/app/attest/challenge`（默认值）。如果通过 `AppAttestSecurityConfigurer.challengeEndpointUri()` 自定义了路径，metadata 声明不会自动同步，需要手动调整。
