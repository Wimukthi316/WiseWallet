# 💰 WiseWallet - Smart Financial Management App

<div align="center">
  <img src="<img width="1344" height="2992" alt="Screen01" src="https://github.com/user-attachments/assets/562df9d9-fdeb-4b81-a15d-e3dba4501568" />
" alt="WiseWallet Logo" width="300"/>
  
  **Manage Money, Achieve Goals, Control Finances**
  
  [![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
  [![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
  [![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
</div>

## 📱 About

WiseWallet is a comprehensive personal finance management application built for Android. It empowers users to take control of their financial journey by providing intuitive tools for expense tracking, budget planning, and financial goal setting.

> *"Your path to financial freedom starts with every penny saved"*

## ✨ Features

### 🏠 **Smart Dashboard**
- Personalized greetings based on time of day
- Real-time monthly budget overview
- Dynamic spending analytics with month-over-month comparisons
- Quick access to all essential features

### 💳 **Expense Management**
- Easy expense entry with categorization
- Multiple categories: Food, Transport, Bills, Entertainment, Rent
- Date-based expense tracking
- Edit and delete functionality
- Visual expense summaries

### 📊 **Budget Planning**
- Set monthly budget goals
- Category-wise budget allocation
- Real-time spending progress tracking
- Budget exceeded notifications
- Visual progress indicators

### 👤 **User Management**
- Secure user registration and login
- Personalized user experience
- Data persistence across sessions

## 📷 Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center">
        <img src="![image3](image3)" alt="Onboarding" width="200"/>
        <br/>
        <b>Onboarding</b>
      </td>
      <td align="center">
        <img src="![image4](image4)" alt="Sign Up" width="200"/>
        <br/>
        <b>Registration</b>
      </td>
      <td align="center">
        <img src="![image5](image5)" alt="Dashboard" width="200"/>
        <br/>
        <b>Dashboard</b>
      </td>
    </tr>
  </table>
</div>

## 🛠️ Technology Stack

- **Language:** Kotlin
- **Framework:** Android SDK
- **Architecture:** MVVM with LiveData
- **UI:** Material Design Components
- **Data Storage:** SharedPreferences
- **Data Serialization:** Gson
- **Design Pattern:** Observer Pattern with LiveData

## 🏗️ Architecture

WiseWallet follows the MVVM (Model-View-ViewModel) architecture pattern:

```
├── Activities (View Layer)
│   ├── Screen06 (Dashboard)
│   ├── Screen07 (Add Expense)
│   ├── Screen08 (Expense Summary)
│   └── Screen09 (Budget Planner)
├── Data Management
│   ├── DataManager (Business Logic)
│   ├── Expense (Data Model)
│   └── User (User Model)
└── UI Components
    ├── ExpenseAdapter (RecyclerView)
    └── SharedPreferencesLiveData
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: API 34

### Installation
1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/WiseWallet.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Run the app on an emulator or physical device

## 📋 Key Components

### DataManager
The core component that handles:
- Expense CRUD operations
- Budget management
- User data persistence
- LiveData updates for real-time UI synchronization

### Activities Overview
- **Screen06**: Main dashboard with budget overview and quick actions
- **Screen07**: Add/Edit expense form with validation
- **Screen08**: Expense list with filtering and summary
- **Screen09**: Budget planning and progress tracking

## 🎨 Design Features

- **Material Design 3** principles
- **Dynamic theming** with blue accent color
- **Responsive layouts** with ConstraintLayout
- **Smooth animations** and transitions
- **Card-based UI** for better content organization

## 📊 Data Flow

1. **User Registration/Login** → Stored in SharedPreferences
2. **Expense Entry** → Validated → Stored → LiveData Update
3. **Budget Setting** → Compared with expenses → Notifications triggered
4. **Real-time Updates** → LiveData observers → UI refresh

## 🔔 Smart Notifications

- Budget exceeded alerts
- Category-wise spending warnings
- Monthly summary notifications
- Customizable notification preferences

## 🛡️ Security Features

- Input validation on all forms
- Secure local data storage
- Password confirmation during registration
- Data integrity checks

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Wimukthi316**
- GitHub: [@yourusername](https://github.com/yourusername)

## 🙏 Acknowledgments

- Material Design guidelines by Google
- Android Developer documentation
- Kotlin community for excellent language support

---

<div align="center">
  <p><strong>📱 Take control of your finances with WiseWallet! 💰</strong></p>
  
  <p>
    <a href="#top">⬆️ Back to top</a>
  </p>
</div>
