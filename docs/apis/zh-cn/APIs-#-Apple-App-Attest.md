# Apple App Attest 接入文档

本文档描述 Apple 客户端如何利用 [App Attest](https://developer.apple.com/documentation/devicecheck/establishing-your-app-s-integrity) 能力完成服务端的 Device Attest 注册与认证流程, 实现无账号登录并获取用户级 OAuth2 Token.

> **术语说明**: 服务端的 **Device Attest** 是一个泛化的设备证明机制, 不限于特定平台; Apple **App Attest** 是其客户端实现之一, 基于 `DCAppAttestService` 提供硬件级设备证明. 因此服务端 API 路径使用 `/device/*` 命名, 而请求头 `OAuth-Client-Attestation-PoP-Type: app-attest` 标识具体的客户端证明方式.

整个流程分为两个阶段:
1. **设备注册 (Attestation)**: 首次使用时, 将设备密钥注册到服务端
2. **获取/续期 Token (Device Assertion)**: 通过设备私钥签名证明身份, 获取用户级 Token

> **安全须知**: 所有敏感数据(Key ID、Token等)均应使用 Keychain 存储,
> **切勿**使用 `UserDefaults`、`plist` 或其他明文方式存储任何敏感信息, 因为这些存储方式在越狱设备上可被轻易读取.

---

## 阶段一: 设备注册 (Attestation)

> 仅在首次使用时执行一次. 设备注册成功后, 后续直接进入阶段二获取Token.
>
> 以下 `/device/*` 端点是服务端 Device Attest 的通用接口, Apple 客户端通过 App Attest 与之交互.

### 1.1 获取 Challenge

```http
POST /device/challenge
```

无需认证, 无需请求体.

**Response:**
```json
{"challenge": "dGhpcyBpcyBhIHJhbmRvbSBjaGFsbGVuZ2U"}
```

### 1.2 生成 Attestation

客户端调用 Apple API 生成 Attestation Object:

```swift
let challengeData = Data(SHA256.hash(data: challenge.data(using: .utf8)!))
let attestation = try await DCAppAttestService.shared.attestKey(keyId, clientDataHash: challengeData)
```

### 1.3 注册设备

```http
POST /device/attest
Content-Type: application/x-www-form-urlencoded

key_id={keyId}&attestation={Base64编码的Attestation Object}&challenge={challenge}
```

|参数名|类型|说明|是否必填|
|---|---|---|---|
|key_id|string|`DCAppAttestService.generateKey()` 生成的 Key Identifier|是|
|attestation|string|Base64 编码的 Attestation Object|是|
|challenge|string|上一步获取的 challenge 原始值|是|

**Success Response (200):**
```json
{"key_id": "...", "username": "apple_app_..."}
```

**Error Response (401):**
```json
{"error": "registration_failed", "error_description": "..."}
```

---

## 阶段二: 获取 OAuth2 Token (Device Assertion)

> 设备注册成功后, 通过 Assertion 获取初始 Token.
>
> **续期策略**: 该 grant type 不签发 `refresh_token`. Token 过期后直接重新执行 Assertion 流程获取新 Token. 原因: 每次请求都需要完整的 attestation 验证 (kid + assertion + challenge), `refresh_token` 不提供额外安全价值.

### 2.1 获取 Challenge

```http
POST /oauth2/challenge
```

无需认证, 无需请求体.

**Response:**
```json
{"challenge": "dGhpcyBpcyBhIHJhbmRvbSBjaGFsbGVuZ2U"}
```

### 2.2 生成 Assertion

客户端调用 Apple API 生成 Assertion Object:

```swift
let challengeData = Data(SHA256.hash(data: challenge.data(using: .utf8)!))
let assertion = try await DCAppAttestService.shared.generateAssertion(keyId, clientDataHash: challengeData)
```

### 2.3 请求 Token

```http
POST /oauth2/token
OAuth-Client-Attestation-PoP-Type: app-attest
Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:device-assertion&kid={keyId}&assertion={Base64编码的Assertion Object}&challenge={challenge}&scope=openid
```

**请求头:**

|头部|值|说明|
|---|---|---|
|OAuth-Client-Attestation-PoP-Type|`app-attest`|指定使用 Apple App Attest 作为客户端证明方式|

**请求体参数:**

|参数名|类型|说明|是否必填|
|---|---|---|---|
|grant_type|enum|固定为 `urn:ietf:params:oauth:grant-type:device-assertion`|是|
|kid|string|设备 Key Identifier (与注册时的 `key_id` 相同)|是|
|assertion|string|Base64 编码的 Assertion Object|是|
|challenge|string|步骤 2.1 获取的 challenge 原始值|是|
|scope|string|申请的权限范围, 多个空格分隔|否|

**Success Response (200):**
```json
{
    "access_token": "eyJ...",
    "token_type": "Bearer",
    "expires_in": 299
}
```

> Token响应格式详见 [OAuth2 Token 接口文档](APIs-%23-OAuth2-Grant.md#response)

---

## 完整时序图

```mermaid
sequenceDiagram
    participant App as iOS App
    participant Server as Authorization Server
    participant Apple as Apple Attest Service

    Note over App,Server: 阶段一: 设备注册 (仅首次)
    App->>App: DCAppAttestService.generateKey()
    App->>Server: POST /device/challenge
    Server-->>App: {"challenge": "..."}

    App->>App: SHA256(challenge) 计算 clientDataHash
    App->>Apple: attestKey(keyId, clientDataHash)
    Apple-->>App: Attestation Object (CBOR)

    App->>Server: POST /device/attest
    Note right of App: key_id=...&attestation=Base64(...)&challenge=...
    Server->>Server: 消费 challenge
    Server->>Server: 验证 Attestation (证书链、Nonce、AAGUID等)
    Server->>Server: 存储设备公钥和 sign count
    Server-->>App: {"key_id": "...", "username": "..."}

    Note over App: 设备注册完成, 后续直接进入阶段二

    Note over App,Server: 阶段二: 获取 Token (每次需要Token时)
    App->>Server: POST /oauth2/challenge
    Server-->>App: {"challenge": "..."}

    App->>App: SHA256(challenge) 计算 clientDataHash
    App->>Apple: generateAssertion(keyId, clientDataHash)
    Apple-->>App: Assertion Object (CBOR)

    App->>Server: POST /oauth2/token
    Note right of App: OAuth-Client-Attestation-PoP-Type: app-attest<br/>grant_type=urn:ietf:params:oauth:grant-type:device-assertion<br/>kid=...&assertion=Base64(...)&challenge=...
    Server->>Server: 消费 challenge
    Server->>Server: 验证 Assertion (签名验证、sign count 检查)
    Server->>Server: 解析/创建匿名用户
    Server-->>App: {access_token, token_type, expires_in}

    Note over App: Token过期后, 重新执行阶段二 Assertion 流程
    Note over App: (不使用refresh_token, 因每次请求均需完整attestation验证)
```

---

## Token 续期策略

 重新执行阶段二 Assertion 流程 (获取 challenge → 生成 Assertion → 请求 Token)

> 该 grant type **不签发 `refresh_token`**. 因为每次 token 请求都需要完整的 attestation 验证 (kid + assertion + challenge), `refresh_token` 不提供额外安全价值, 反而增加存储和验证开销.

---

## 注意事项

* 每个 challenge 只能使用一次, 有效期5分钟, 过期或已使用的 challenge 会被拒绝
* `AccessToken` 有效期较短 (目前5分钟), 过期后应重新执行阶段二 Assertion 流程获取新 Token
* 阶段一的注册只需执行一次, App 应在 Keychain 中持久化 Key ID
* 如果设备密钥丢失或需要重新注册, 需重新执行完整的阶段一流程

## Apple 官方文档

* [Establishing Your App's Integrity](https://developer.apple.com/documentation/devicecheck/establishing-your-app-s-integrity)
* [Validating Apps That Connect to Your Server](https://developer.apple.com/documentation/devicecheck/validating-apps-that-connect-to-your-server)
