## Request

### Url

```http
POST /oauth2/token
```

### Authorization

支持以下客户端认证方式:

#### 机密客户端 (仅 Attestation-Based Client Authentication)

通过设备证明(Apple App Attest)进行客户端认证, 详见 [Apple App Attest 接入文档](APIs-%23-Apple-App-Attest.md).

#### 机密客户端 (仅 Client Basic Auth)

在```Authorization```头传入验证信息, 验证信息格式: ```Basic Base64({clientId}:{clientSecret})```

例如
```http
Authorization: Basic ZGVmYXVsdDoxMjM=
```

#### 增强机密客户端 (Client Basic Auth + Attestation-Based Client Authentication)

在```Authorization```头传入验证信息, 验证信息格式: ```Basic Base64({clientId}:{clientSecret})```

例如
```http
Authorization: Basic ZGVmYXVsdDoxMjM=
```

#### 公共客户端 (Public Client)

在请求体中传入```client_id```参数, 无需```client_secret```:

```
client_id=demo
```

> 公共客户端适用于无法安全存储密钥客户端, 如 WEB 应用
> ```client_id```与其他参数一起放在```application/x-www-form-urlencoded```请求体中.

### Content Type

```
application/x-www-form-urlencoded
```

---

### 1. 通过微信授权码获取Token

|参数名|类型|说明|是否必填|默认值|
|---|---|---|---|---|
|grant_type|enum|验证模式, 此处固定为```wechat_authorization_code```|是|无|
|code|string|```wx.login```接口返回的```code```|是|无|
|scope|string|申请的权限范围, 多个空格分隔|否|无|

> **微信小程序**作为公共客户端通常不会收到```refresh_token```, 如果没有```refresh_token```, 则在 Token 到期前重复执行上述流程获取新 Token


### 2. 通过 Apple App Attest 获取Token

使用 Apple App Attest 进行设备证明并获取用户级 Token, 涉及设备注册和Token申请两个阶段.

完整流程、请求参数和时序图请参考: **[Apple App Attest 接入文档](APIs-%23-Apple-App-Attest.md)**

### 3. Token 续期 (Refresh Token)

> 仅**机密客户端**会收到```refresh_token```, 如果持有```refresh_token```, 应优先使用此方式续期.
> **公共客户端**不会收到```refresh_token```, 请直接参考对应的定制化认证流程中的续期方式.

|参数名|类型|说明|是否必填|默认值|
|---|---|---|---|---|
|grant_type|enum|验证模式, 此处固定为```refresh_token```|是|无|
|refresh_token|string|尚未过期的```refresh_token```|是|无|
|scope|string|续期的权限范围, 多个空格分隔, 且不可超出初次获取时的权限范围|否|无|

---

## Response

```json
{
    "access_token": "3aebdf85-27fb-4455-80d5-009626b6d6b0",
    "token_type": "Bearer",
    "refresh_token": "a872f3a8-d44a-4dc2-a495-bb1a63b33a09",
    "expires_in": 299,
    "scope": "DEFAULT"
}
```

|属性名|类型|说明|
|---|---|---|
|access_token|string|用于调用业务接口的```AccessToken```|
|token_type|enum|```AccessToken```类型, 调用业务接口时作为```Authorization```头的前缀, 例如: ```Authorization: Bearer 3aebdf85-27fb-4455-80d5-009626b6d6b0```|
|refresh_token|string|用于续期的```RefreshToken```, 仅机密客户端会收到|
|expires_in|int|```AccessToken```的有效期, 单位: 秒|
|scope|string|```AccessToken```的权限范围, 多个用空格分隔, 如果申请Token时没有传入```scope```参数, 则响应中也不会有此属性|

* ```AccessToken```的有效期很短, 目前设置为5分钟; ```RefreshToken```的有效期很长, 目前设置为7天. 所以每次使用```AccessToken```前都应检查有效期, 若已过期或剩余有效时间小于1分钟, 则应及时续期.
* 更多说明请参考[RFC6749 Section-4 Obtaining Authorization](https://datatracker.ietf.org/doc/html/rfc6749#section-4)
