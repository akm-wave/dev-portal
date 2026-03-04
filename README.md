# Dev Tracking Portal

An enterprise-grade Dev Tracking Portal to track **Features → Microservices → Checklists** with real-time progress tracking, strong relationships, and admin control panel.

## 🚀 Features

### Core Functionality
- **Feature Management**: Track features with release versions, target dates, and status
- **Microservice Management**: Manage microservices with version control and ownership
- **Checklist Management**: Base entity for tracking tasks with status and priority
- **Progress Tracking**: Real-time progress calculation based on checklist completion
- **Relationship Management**: Features → Microservices → Checklists hierarchy

### User Management
- JWT-based authentication
- Role-based access control (ADMIN/USER)
- Secure password encryption with BCrypt

### UI Features
- Modern dashboard with statistics and charts
- Feature list with "Impacted Microservices" and "Checklist Check" modals
- Search, filter, and pagination for all entities
- Progress bars and status indicators
- Responsive design with Ant Design

## 🛠 Tech Stack

### Backend
- **Framework**: Spring Boot 3.2
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Security**: Spring Security + JWT
- **Migration**: Flyway
- **Mapping**: MapStruct
- **Documentation**: OpenAPI/Swagger

### Frontend
- **Framework**: React 18 with TypeScript
- **UI Library**: Ant Design 5
- **Build Tool**: Vite
- **HTTP Client**: Axios
- **Routing**: React Router 6

## 📋 Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Maven 3.8+

## 🚀 Quick Start

### 1. Database Setup

```bash
# Create PostgreSQL database
createdb devportal_dev
```

### 2. Backend Setup

```bash
# Navigate to project root
cd dev-team-tracking-portal

# Run with Maven
./mvnw spring-boot:run
```

The backend will start at `http://localhost:8080/api`

### 3. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will start at `http://localhost:3000`

## 🐳 Docker Deployment

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

## 📚 API Documentation

Once the backend is running, access Swagger UI at:
`http://localhost:8080/api/swagger-ui.html`

## 🔐 Default Credentials

```
Username: admin
Password: Admin@123
Role: ADMIN
```

## 📁 Project Structure

```
dev-team-tracking-portal/
├── src/main/java/com/devportal/
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   ├── domain/
│   │   ├── entity/       # JPA entities
│   │   └── enums/        # Enumerations
│   ├── dto/
│   │   ├── request/      # Request DTOs
│   │   └── response/     # Response DTOs
│   ├── exception/        # Exception handling
│   ├── mapper/           # MapStruct mappers
│   ├── repository/       # JPA repositories
│   ├── security/         # Security configuration
│   └── service/          # Business logic
├── src/main/resources/
│   ├── db/migration/     # Flyway migrations
│   └── application*.yml  # Configuration files
├── frontend/
│   ├── src/
│   │   ├── components/   # React components
│   │   ├── contexts/     # React contexts
│   │   ├── layouts/      # Layout components
│   │   ├── pages/        # Page components
│   │   ├── services/     # API services
│   │   └── types/        # TypeScript types
│   └── package.json
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | localhost |
| `DB_USERNAME` | Database username | postgres |
| `DB_PASSWORD` | Database password | postgres |
| `JWT_SECRET` | JWT signing key | (generated) |
| `SPRING_PROFILES_ACTIVE` | Active profile | dev |

## 📊 Entity Relationships

```
Feature (1) ──── (N) Microservice (N) ──── (N) Checklist
         └── feature_microservices ──┘    └── microservice_checklists ──┘
```

## 🔒 Security

- All API endpoints except `/auth/**` require authentication
- ADMIN role: Full CRUD access
- USER role: Read-only access
- JWT tokens expire after 24 hours
- Passwords encrypted with BCrypt

## 📈 Business Rules

1. A microservice cannot exist without at least one checklist
2. A feature cannot exist without at least one microservice
3. Feature progress = average completion of all checklists under its microservices
4. Microservice progress = percentage of completed checklists
5. Cannot delete checklist if linked to active microservice
6. Auto-update feature status when all microservices are completed

## 🧪 Testing

```bash
# Run backend tests
./mvnw test

# Run frontend tests
cd frontend && npm test
```

## 📝 License

MIT License
