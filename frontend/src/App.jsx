import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import TrackNestPage from './pages/TrackNestPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import Navbar from './components/Navbar.jsx';
import UsersPage from './pages/UserPage.jsx';


function PrivateRoute({ children }) {
  const token = localStorage.getItem('token');
  return token ? children : <Navigate to="/login" />;
}

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        {/* Redirect root to login */}
        <Route path="/" element={<Navigate to="/login" />} />

        {/* Login page with auto-redirect handled in component */}
        <Route path="/login" element={<LoginPage />} />

        {/* Protected Dashboard */}
        <Route
          path="/dashboard"
          element={
            <PrivateRoute>
              <DashboardPage />
            </PrivateRoute>
          }
        />
        <Route path="/user" element={
          <PrivateRoute>
            <UsersPage />
          </PrivateRoute>

        } />

        <Route path="/tracknest" element={
          <PrivateRoute>
            <TrackNestPage />
          </PrivateRoute>

        } />
      </Routes>
    </Router>
  );
}

export default App;
