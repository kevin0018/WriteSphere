# WriteSphere - Modelo Conceptual

## Descripción General

WriteSphere es una plataforma que permite a los usuarios crear, compartir e interactuar con poemas o posts.

## 1. Modelo conceptual

### 1.1. Enlace público al esquema
[WriteSphere Esquema](https://drive.google.com/file/d/1P5WSrQpkGgYdlQE5F2g8DgK-zxQxlb8R/view?usp=sharing)

### 1.2. Esquema conceptual (EC ó ER)
![WriteSphere Esquema Conceptual](Project%20WriteSphere.png)

## 2. Modelo lógico relacional

### 2.1. Esquema lógico

**User**(<ins>id</ins>, username, email, passwordHash, createdAt, status)

**Post**(<ins>id</ins>, title, content, createdAt, updatedAt, status, type, userId)

**Comment**(<ins>id</ins>, content, createdAt, postId, userId)

**LikePost**(<ins>id</ins>, userId, postId, createdAt)

**LikeComment**(<ins>id</ins>, userId, commentId, createdAt)

**Follow**(<ins>id</ins>, followerId, followingId, createdAt)

**UserPost**(<ins>id</ins>, userId, postId)

**Notification**(<ins>id</ins>, type, entityId, entityType, userId, triggeredByUserId, createdAt, readAt, content)

**Report**(<ins>id</ins>, userId, postId, reportedComment, createdAt)

### 2.2. Diagrama referencial

Relación referencial | Clave ajena | Relación referida
-|-|-
Post | userId | User
Comment | postId | Post
Comment | userId | User
LikePost | userId | User
LikePost | postId | Post
LikeComment | userId | User
LikeComment | commentId | Comment
Follow | followerId | User
Follow | followingId | User
UserPost | userId | User
UserPost | postId | Post
Notification | userId | User
Notification | triggeredByUserId | User
Report | userId | User
Report | postId | Post
