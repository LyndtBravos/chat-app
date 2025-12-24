💬 Chat App API
📌 Overview

The Chat App API is a Spring Boot backend that supports real-time messaging between users.
It includes authentication, one-on-one and group chats, message delivery, and read/typing indicators.

This version is fully Dockerized using PostgreSQL and Redis, so you can run the entire backend locally without installing Java or databases.

⚠️ A Vue.js front-end UI is currently in development and will integrate with this backend to provide a complete chat experience.

✨ Features

🔐 User registration & JWT-based authentication

💬 One-on-one and group chats

📨 Send, update, read, and delete messages

⌨️ Typing indicators & read/delivered tracking (WebSocket)

👤 Basic user management

🐳 Fully containerized with Docker

🔌 Ready for front-end integration

🛠️ Technologies Used

Java 17+, Spring Boot

PostgreSQL 18 – primary database

Redis 7 – message delivery & caching

Spring WebSocket / STOMP – real-time communication

Hibernate / JPA – ORM

Docker & Docker Compose

🚀 Setup & Running the App
Prerequisites

Make sure you have:

Docker

Docker Compose

Ports 5432, 6379, and 8080 available

▶️ Run with Docker

Clone the repository:

git clone <repo-url>
cd chat-app


Build and start all services:

docker-compose up --build


⏳ PostgreSQL may take a few seconds to become healthy.

Once ready, the API will be available at:

http://localhost:8080

🔐 Environment Variables (Docker)

The Docker setup already provides:

POSTGRES_DB=chat_app_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=1017#Thugger
SPRING_PROFILES_ACTIVE=docker

🧪 API Endpoints & Sample Flow

Below is a step-by-step testing story using a fresh Docker instance.

1️⃣ Authentication
Register a user
POST /auth/register
Content-Type: application/json

{
  "username": "alice",
  "email": "alice@example.com",
  "phoneNumber": "0712345678",
  "password": "password123"
}


Repeat this step to create more users (e.g. bob, charlie).

Login
POST /auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "password123"
}


Response:

{
  "token": "<JWT_TOKEN>"
}


🔐 Use this token for all protected endpoints:

Authorization: Bearer <JWT_TOKEN>

2️⃣ Users

Get all users:

GET /api/users


Get user by ID:

GET /api/users/{id}


Get currently logged-in user:

GET /api/users/me


Update user:

PUT /api/users/{id}
Content-Type: application/json

{
  "username": "alice",
  "email": "alice@yahoo.com",
  "phoneNumber": "0625316849",
  "password": "password123"
}


Delete user:

DELETE /api/users/{id}

3️⃣ Chats
Create a group chat
POST /api/chats
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "name": "Project Discussion",
  "isGroup": true,
  "participantIds": [1, 2, 3]
}

Update a chat
PUT /api/chats/{id}
Content-Type: application/json

{
  "name": "Alice & Bob",
  "isGroup": false,
  "participantIds": [2]
}

Delete a chat
DELETE /api/chats/{id}

Get all chats for a user
GET /api/chats/user/{userId}

4️⃣ Messages
Send a message (text only)
POST /api/messages
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "chatId": 1,
  "content": "Hello team!"
}

Send a message with attachment
POST /api/messages
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "chatId": 1,
  "content": "Check this out!",
  "attachment": {
    "type": "IMAGE",
    "url": "https://images.unsplash.com/photo-1765775356123-f5bd734f9de3"
  }
}

Get messages in a chat
GET /api/messages/chat/{chatId}

Mark messages as read
POST /api/messages/read/{chatId}

Update a message
PUT /api/messages/update/{id}
Content-Type: application/json

{
  "content": "Updated message content"
}

Delete a message
DELETE /api/messages/{id}

🔄 Real-Time Messaging (WebSocket)

The backend supports WebSocket events via STOMP for:

💬 Real-time message delivery

⌨️ Typing indicators

👀 Read & delivered status

Handled by: ChatWsController

⚠️ Manual WebSocket testing is optional.
These endpoints are designed primarily for front-end integration.

🚧 Enhancements / Next Steps

🎨 Vue.js front-end UI (in progress)

🔔 Notifications & unread message counts

🔍 Message search & advanced attachments

🛡️ Role-based access & permissions

☁️ Production-ready Docker Compose setup

📝 Notes for Reviewers

The backend API is fully functional and Dockerized

All endpoints can be tested via Postman

WebSocket functionality is implemented for real-time features

JWT authentication ensures secure access

Made with ❤️ by Brian Mthembu
