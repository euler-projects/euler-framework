## Request

### Url

```http
POST /oauth2/challenge
```

### Authorization

无需认证, 任何客户端均可调用.

### Content Type

无要求, 请求体为空即可.

## Response

```json
{"challenge": "dGhpcyBpcyBhIHJhbmRvbSBjaGFsbGVuZ2U"}
```

|属性名|类型|说明|
|---|---|---|
|challenge|string|一次性challenge字符串(Base64URL编码), 有效期5分钟, 使用后立即失效|

响应包含以下缓存控制头:
```http
Cache-Control: no-store
Pragma: no-cache
```

> **注意**: challenge是一次性的, 使用后立即失效, 请勿缓存或复用.
