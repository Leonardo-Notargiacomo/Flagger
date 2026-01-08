# backend

This application is generated using [LoopBack 4 CLI](https://loopback.io/doc/en/lb4/Command-line-interface.html) with the
[initial project layout](https://loopback.io/doc/en/lb4/Loopback-application-layout.html).

## Install dependencies

By default, dependencies were installed when this application was generated.
Whenever dependencies in `package.json` are changed, run the following command:

```sh
npm install
```

To only install resolved dependencies in `package-lock.json`:

```sh
npm ci
```

## Run the application

```sh
npm start
```

You can also run `node .` to skip the build step.

Open http://127.0.0.1:3000 in your browser.

## Password endpoints

The backend exposes two authenticated endpoints for changing a user password. Both require a valid JWT access token.

```sh
# Change the currently authenticated user's password
curl -X PATCH http://127.0.0.1:3000/go-users/me/password \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword":"OldPassword123","newPassword":"NewPassword123"}'

# Change password by user id (only allowed for your own id)
curl -X PATCH http://127.0.0.1:3000/go-users/<id>/password \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword":"OldPassword123","newPassword":"NewPassword123"}'
```

## Rebuild the project

To incrementally build the project:

```sh
npm run build
```

To force a full build by cleaning up cached artifacts:

```sh
npm run rebuild
```

## Fix code style and formatting issues

```sh
npm run lint
```

To automatically fix such issues:

```sh
npm run lint:fix
```

## Other useful commands

- `npm run migrate`: Migrate database schemas for models
- `npm run openapi-spec`: Generate OpenAPI spec into a file
- `npm run docker:build`: Build a Docker image for this application
- `npm run docker:run`: Run this application inside a Docker container

## Tests

```sh
npm test
```

## What's next

Please check out [LoopBack 4 documentation](https://loopback.io/doc/en/lb4/) to
understand how you can continue to add features to this application.

[![LoopBack](https://github.com/loopbackio/loopback-next/raw/master/docs/site/imgs/branding/Powered-by-LoopBack-Badge-(blue)-@2x.png)](http://loopback.io/)

## Fix Summary: `relation 'public.user' does not exist`

### **Root Cause**

The error occurred due to two primary issues:

1. **Missing Database Tables:**
   The required PostgreSQL tables (`go_user`, `go_user_credentials`, and `flag`) were not created yet. Without running the migration, LoopBack could not find the `user` table when handling login requests.

2. **JWT Binding Issue:**
   The default LoopBack JWT component was still referencing its internal `User` model instead of the project’s custom `GoUser` model. This caused mismatches between the authentication service and the database schema.

---

### **Fix Implemented**

#### 1. **Database Migration**

Executed the migration command to create all necessary tables:

```bash
npm run migrate
```

This generated the following tables in PostgreSQL:

* `go_user`
* `go_user_credentials`
* `flag`

#### 2. **Custom JWT Bindings Configuration**

Updated the **`application.ts`** file to bind custom user service and repository.

**Added Imports (lines 20–21):**

```typescript
import {MyUserService} from './services';
import {GoUserRepository} from './repositories';
```

**Added JWT Bindings (lines 61–63):**

```typescript
this.bind(UserServiceBindings.USER_SERVICE).toClass(MyUserService as any);
this.bind(UserServiceBindings.USER_REPOSITORY).toClass(GoUserRepository as any);
```

These bindings ensure the JWT component uses the custom `GoUser` and `GoUserRepository` instead of the default LoopBack `User` model.

---

### **Test Results**

After applying the fixes, the `/login` endpoint successfully authenticated users.

**Test Command:**

```bash
curl -X POST http://127.0.0.1:3000/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"TestPassword123"}'
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**JWT endpoints are completly functional now**

## Root Cause Analysis: Relation Metadata Error

### **Problem Overview**

The relation error was caused by inconsistent and missing relation definitions between the `GoUser` and `GoUserCredentials` models, leading LoopBack to fail when resolving relation metadata during application startup.

---

### **Root Causes**

1. **Missing `@hasOne()` Decorator (`go-user.model.ts:36–37`):**
   The relation decorator was commented out, so LoopBack could not register the metadata linking `GoUser` to `GoUserCredentials`.

2. **Invalid Repository Factory (`go-user.repository.ts:31`):**
   The repository attempted to create a relation factory for a non-existent property (`gUCToGU`).

3. **Missing `@belongsTo()` Decorator:**
   The `GoUserCredentials` model imported `belongsTo` but did not define the decorator, leaving the foreign key undefined.

4. **Conflicting Relation Definitions:**
   Multiple conflicting `hasOne` factories were defined in the repository (`userCredentials`, `goUserCredentials`, and `gUCToGU`), which caused metadata ambiguity.

When LoopBack tried to resolve the metadata for `gUCToGU` in `go-user.repository.ts:31`, it failed because the corresponding property did not exist in the model.

---

### **Solution Plan**

#### **Clean Up GoUserRepository**

* Retained only one `hasOne` relation: **`goUserCredentials`**.
* Removed duplicate and conflicting factories (`userCredentials`, `gUCToGU`).
* Fixed the relation factory injection:

  ```typescript
  this.goUserCredentials = this.createHasOneRepositoryFactoryFor(
    'goUserCredentials',
    getGoUserCredentialsRepository,
  );
  ```

#### **Use Consistent Naming**

* **Relation property:** `goUserCredentials`
* **Foreign key:** `goUserId`

---

 **Outcome:**
After cleaning up the repository and enforcing consistent naming, LoopBack was able to correctly resolve relation metadata. The migration executed successfully, and the relation between `GoUser` and `GoUserCredentials` now works as intended.
