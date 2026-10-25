# CampaignPro Backend

## About the Project

CampaignPro is a backend application used to manage email campaigns. Users can manage contacts, create email templates, send emails, schedule campaigns, and check campaign performance.

It also has an admin section to manage users and view statistics.

## Technologies Used

- Java 17
- Spring Boot 3.2.0
- Spring Data MongoDB
- Spring Security
- JWT Authentication
- SendGrid
- Maven

## Requirements

Before running the project, install the following:

- Java 17 or above
- Maven 3.6 or above
- MongoDB or MongoDB Atlas account
- SendGrid account for sending emails

## Configuration

Set the following environment variables before running the application.

```properties
MONGODB_URI=your_mongodb_connection_string
JWT_SECRET=your_secret_key
SENDGRID_API_KEY=your_sendgrid_api_key
EMAIL_FROM=your_email_address
FRONTEND_URL=your_frontend_url
APP_URL=your_backend_url
```

Replace these values with your actual configuration. Do not share your secret keys publicly.

## How to Run the Project

**1. Clone the repository**

```bash
git clone <repository-url>
```

**2. Go to the backend folder**

```bash
cd CampaignPro/backend
```

**3. Install the dependencies**

```bash
mvn clean install
```

**4. Run the application**

```bash
mvn spring-boot:run
```

The application will run at `http://localhost:8080`.

## Features

### User Management
- Register and login
- View and update user profile
- Change password
- Manage user roles

### Contact Management
- Add, update, and delete contacts
- Search contacts
- Create and manage contact groups

### Email Templates
- Create and edit email templates
- Delete templates
- Preview templates
- Use placeholders in emails

### Campaign Management
- Create and update campaigns
- Send email campaigns
- Schedule campaigns
- Cancel scheduled campaigns

### Email Tracking
- Track email delivery
- Track email opens and clicks
- View campaign activity

### Analytics
- View campaign performance
- Check email delivery statistics
- View campaign reports

### Admin
- View and manage users
- Update user roles and status
- View campaigns
- Check overall statistics

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Login |

### Users

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users/profile` | Get user profile |
| PUT | `/api/users/profile` | Update profile |
| PUT | `/api/users/password` | Change password |

### Contacts

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/contacts` | Get all contacts |
| POST | `/api/contacts` | Add a contact |
| GET | `/api/contacts/{id}` | Get a contact |
| PUT | `/api/contacts/{id}` | Update a contact |
| DELETE | `/api/contacts/{id}` | Delete a contact |

### Email Templates

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/templates` | Get all templates |
| POST | `/api/templates` | Create a template |
| PUT | `/api/templates/{id}` | Update a template |
| DELETE | `/api/templates/{id}` | Delete a template |

### Campaigns

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/campaigns` | Get all campaigns |
| POST | `/api/campaigns` | Create a campaign |
| PUT | `/api/campaigns/{id}` | Update a campaign |
| DELETE | `/api/campaigns/{id}` | Delete a campaign |
| POST | `/api/campaigns/{id}/send` | Send a campaign |
| POST | `/api/campaigns/{id}/schedule` | Schedule a campaign |
| POST | `/api/campaigns/{id}/cancel` | Cancel a campaign |

### Analytics

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/analytics/overview` | View overall analytics |
| GET | `/api/analytics/campaigns` | View campaign analytics |
| GET | `/api/analytics/{campaignId}` | View campaign details |

## Security

The application uses Spring Security and JWT for authentication. Passwords are encrypted using BCrypt. User roles are used to control access to admin features.

CORS is configured to allow requests from the frontend.

## Database

MongoDB is used to store application data.

The main collections are users, contacts, contact groups, email templates, campaigns, email deliveries, campaign activities, and analytics.

## Deployment

The application can be deployed using Render, Railway, or AWS EC2.

Configure the required environment variables on the deployment platform before running the application.

## Testing

Run the following command to test the application:

```bash
mvn test
```

## License

This project is licensed under the MIT License.
