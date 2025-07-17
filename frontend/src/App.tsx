import './App.css'
import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import SearchPage from './pages/Search';
import { LoginPage } from './pages/Login';
import { OAuthCallback } from './pages/OAuthCallback';
import { PrivateRoute } from './components/PrivateRoute';
import { AuthProvider } from './context/AuthContext';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/get-code" element={<OAuthCallback />} />
          <Route path="/search" element={
            <PrivateRoute>
              <SearchPage />
            </PrivateRoute>
          } />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App
