# CampaignPro Backend

Email Campaign Manager System - Spring Boot Backend Application

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data MongoDB**
- **Spring Security**
- **JWT Authentication**
- **SendGrid (Email Service)**
- **Maven**

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/campaignpro/
│   │   │   ├── CampaignProApplication.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── ContactController.java
│   │   │   │   ├── TemplateController.java
│   │   │   │   ├── CampaignController.java
│   │   │   │   ├── EmailController.java
│   │   │   │   ├── AnalyticsController.java
│   │   │   │   ├── CampaignActivityController.java
│   │   │   │   ├── ActivityTrackingController.java
│   │   │   │   └── AdminController.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Contact.java
│   │   │   │   ├── ContactGroup.java
│   │   │   │   ├── EmailTemplate.java
│   │   │   │   ├── Campaign.java
│   │   │   │   ├── EmailDelivery.java
│   │   │   │   ├── CampaignActivity.java
│   │   │   │   └── Analytics.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ContactRepository.java
│   │   │   │   ├── ContactGroupRepository.java
│   │   │   │   ├── EmailTemplateRepository.java
│   │   │   │   ├── CampaignRepository.java
│   │   │   │   ├── EmailDeliveryRepository.java
│   │   │   │   ├── CampaignActivityRepository.java
│   │   │   │   └── AnalyticsRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ContactService.java
│   │   │   │   ├── TemplateService.java
│   │   │   │   ├── CampaignService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── AnalyticsService.java
│   │   │   │   ├── AdminService.java
│   │   │   │   └── ScheduledTaskService.java
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── UserDTO.java
│   │   │   │   ├── ContactDTO.java
│   │   │   │   ├── ContactGroupDTO.java
│   │   │   │   ├── TemplateDTO.java
│   │   │   │   ├── CampaignDTO.java
│   │   │   │   └── AnalyticsDTO.java
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── UnauthorizedException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── security/
│   │   │       ├── JwtUtil.java
│   │   │       ├── UserDetailsServiceImpl.java
│   │   │       └── JwtAuthenticationFilter.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/campaignpro/
├── pom.xml
└── README.md
```

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB (local or MongoDB Atlas)
- SendGrid Account (for email services)

### Environment Variables

Create a `.env` file or set the following environment variables:

```properties
# MongoDB
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/campaignpro

# Security
JWT_SECRET=your-super-secret-jwt-key-minimum-256-bits

# Email Service (SendGrid)
SENDGRID_API_KEY=your-sendgrid-api-key
EMAIL_FROM=noreply@campaignpro.com

# Frontend URL (for CORS)
FRONTEND_URL=https://your-frontend-domain.netlify.app

# Application URL (for tracking)
APP_URL=https://your-backend-domain.onrender.com
```

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd CampaignPro/backend
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

3. **Configure application**
   - Update `application.properties` with your configuration
   - Or use environment variables (recommended for production)
   - Available environment variables:
     - `MONGODB_URI` - MongoDB connection string
     - `JWT_SECRET` - JWT signing key
     - `SENDGRID_API_KEY` - SendGrid API key
     - `EMAIL_FROM` - Default from email address
     - `FRONTEND_URL` - Frontend URL for CORS
     - `APP_URL` - Backend URL for tracking

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`

### Building for Production

```bash
mvn clean package
java -jar target/campaignpro-backend-1.0.0.jar
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `PUT /api/users/password` - Change password
- `GET /api/users/all` - Get all users (Admin)
- `PUT /api/users/{id}/role` - Update user role (Admin)
- `PUT /api/users/{id}/status` - Update user status (Admin)
- `DELETE /api/users/{id}` - Delete user (Admin)

### Contact Management
- `GET /api/contacts` - Get all contacts
- `POST /api/contacts` - Create contact
- `GET /api/contacts/{id}` - Get contact by ID
- `PUT /api/contacts/{id}` - Update contact
- `DELETE /api/contacts/{id}` - Delete contact
- `GET /api/contacts/search` - Search contacts
- `GET /api/contacts/groups` - Get all groups
- `POST /api/contacts/groups` - Create group
- `PUT /api/contacts/groups/{id}` - Update group
- `DELETE /api/contacts/groups/{id}` - Delete group

### Email Templates
- `GET /api/templates` - Get all templates
- `POST /api/templates` - Create template
- `GET /api/templates/{id}` - Get template by ID
- `PUT /api/templates/{id}` - Update template
- `DELETE /api/templates/{id}` - Delete template
- `POST /api/templates/{id}/preview` - Preview template

### Campaign Management
- `GET /api/campaigns` - Get all campaigns
- `POST /api/campaigns` - Create campaign
- `GET /api/campaigns/{id}` - Get campaign by ID
- `PUT /api/campaigns/{id}` - Update campaign
- `DELETE /api/campaigns/{id}` - Delete campaign
- `POST /api/campaigns/{id}/send` - Send campaign
- `POST /api/campaigns/{id}/schedule` - Schedule campaign
- `POST /api/campaigns/{id}/cancel` - Cancel scheduled campaign

### Analytics
- `GET /api/analytics/overview` - Get overview analytics
- `GET /api/analytics/campaigns` - Get campaign analytics
- `GET /api/analytics/{campaignId}` - Get specific campaign analytics

### Admin (Admin Role Required)
- `GET /api/admin/dashboard` - Get admin dashboard
- `GET /api/admin/users` - Get all users with stats
- `GET /api/admin/campaigns` - Get all campaigns with stats
- `GET /api/admin/analytics` - Get platform analytics

## Features Implemented

✅ User Authentication & Authorization (JWT)
✅ User Management (Profile, Roles, Status)
✅ Contact Management (CRUD, Groups, Search)
✅ Email Template Management (Rich text, Placeholders)
✅ Campaign Management (Create, Schedule, Send)
✅ Email Delivery System (Bulk sending, Tracking)
✅ Campaign Activity Tracking (Opens, Clicks)
✅ Analytics Dashboard (Performance metrics)
✅ Admin Dashboard (Platform overview)
✅ Security (Spring Security, CORS, RBAC)
✅ Scheduled Tasks (Campaign processing)
✅ Email Integration (SendGrid)

## Security Features

- JWT-based authentication
- Role-based access control (USER, ADMIN)
- Password encryption (BCrypt)
- CORS configuration
- Input validation
- Exception handling
- Rate limiting (can be added)

## Database Schema

The application uses MongoDB with the following collections:
- `users` - User accounts
- `contacts` - Contact information
- `contact_groups` - Contact groups
- `email_templates` - Email templates
- `campaigns` - Email campaigns
- `email_deliveries` - Email delivery tracking
- `campaign_activities` - Campaign activity tracking
- `analytics` - Analytics data

## Deployment

### Render (Recommended)
1. Create a new web service on Render
2. Connect your GitHub repository
3. Set environment variables
4. Deploy

### Railway
1. Create a new service on Railway
2. Connect your GitHub repository
3. Set environment variables
4. Deploy

### AWS EC2
1. Launch EC2 instance
2. Install Java, Maven, MongoDB
3. Clone repository
4. Build and run the application
5. Configure Nginx as reverse proxy
6. Set up SSL certificate

## Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.
