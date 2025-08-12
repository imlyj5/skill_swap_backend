# SkillSwap Backend

Learning new skills can be lonely, costly, and hard to stay motivated. Especially without guidance and support. While many people are eager to share what they know, there’s no simple, peer-based way to connect based on shared interests and skills.

Most platforms are built for content delivery, not human connection.
But what if learning felt more like meeting a friend than taking a class?

This project aims to solve that by creating a user-friendly Skill Exchange App, where users match by what they want to learn and teach, then connect, chat, and support each other. 

It makes learning social, fun, and free. Inspired by social matching platforms, the app helps people find partners with complementary skills and shared interests. By fostering mutual support and peer-to-peer learning, it turns learning into a social, rewarding experience.

## 📺 Deployment
- [SkillSwap Backend](https://skill-swap-backend-35l1.onrender.com)
- [SkillSwap Frontend](https://skill-swap-frontend-dyhq.onrender.com)

## 🚀 Features

- **User Management**: Registration, authentication, and profile management
- **Skill Management**: Create and manage skills with AI-generated tags
- **Smart Matching**: 3-tier ranking system to find compatible skill partners
- **AI Tagging**: Google Gemini-powered skill categorization

## 🏗️ Tech Stack

- **Backend**: Spring Boot
- **Database**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Migration**: Flyway
- **Testing**: JUnit 5
- **Build**: Maven
- **AI**: Google Gemini API

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL
- Google Gemini API key

## 🛠️ Setup & Installation

### 1. Clone the Repository
```bash
git clone <https://github.com/imlyj5/skill_swap_backend>
cd skill_swap_backend
```

### 2. Environment Variables
Set the following environment variables:
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/skill_swap
export DATABASE_USERNAME=your_db_username
export DATABASE_PASSWORD=your_db_password
export GEMINI_API_KEY=your_gemini_api_key
```

### 3. Build and Run
```bash
mvn clean compile
mvn flyway:migrate
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

## 🔌 API Endpoints

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - User login

### Users & Profiles  
- `GET /profiles` - Get all users
- `GET /profiles/{id}` - Get user by ID
- `PATCH /profiles/{id}` - Update user profile
- `DELETE /profiles/{id}` - Delete user

### Skills
- `GET /skills` - Get all skills
- `POST /skills` - Create new skill
- `GET /skills/{id}` - Get skill by ID
- `GET /skills/tags/{id}` - Get AI-generated tag suggestions for a user based on their skills

### Matching
- `GET /matches/{userId}` - Get ranked matches for user

## 🎯 Matching Algorithm

The system uses a 3-tier ranking system:

1. **Perfect Match (Rank 1)**: Exact skill name matches
2. **Good Match (Rank 2)**: Tag overlap between users
3. **Potential Match (Rank 3)**: One-way tag overlap (Skills that offer what you want to learn)

## 🗄️ Database Schema

### Core Tables
- `users` - User profiles and authentication
- `skill` - Skills with AI-generated tags
- `user_offers` - Skills users can teach
- `user_wants` - Skills users want to learn

## 📁 Project Structure

```
src/
├── main/java/com/example/SkillSwap/
│   ├── controller/          # REST controllers
│   ├── service/            # Business logic
│   ├── repository/         # Data access layer
│   ├── model/             # Entity classes
│   └── config/            # Configuration
├── main/resources/
│   ├── application.properties
│   └── db/migration/      # Flyway migrations
└── test/                  # Test suite
```

## 🔧 Configuration

### Environment Variables
- `DATABASE_URL` - PostgreSQL connection string
- `DATABASE_USERNAME` - Database username  
- `DATABASE_PASSWORD` - Database password
- `GEMINI_API_KEY` - Google Gemini API key for AI tagging

## 🔗 Related Projects

- **Frontend**: [SkillSwap Frontend Repository](https://github.com/jennylearncoding/skill_swap_frontend)

---

**Happy Skill Swapping!** 🎓✨
