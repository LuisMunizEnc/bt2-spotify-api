import './App.css'
import { Navigate, Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import SearchPage from './pages/Search';
import { LoginPage } from './pages/Login';
import { OAuthCallback } from './pages/OAuthCallback';
import { PrivateRoute } from './components/PrivateRoute';
import { AuthProvider } from './context/AuthContext';
import { ArtistPage } from './pages/ArtistPage';
import { Dashboard } from './pages/Dashboard';
import { AlbumPage } from './pages/AlbumPage';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/get-code" element={<OAuthCallback />} />
          <Route path="/search" element={
            <PrivateRoute>
              <SearchPage />
            </PrivateRoute>
          } />
          <Route path="/artist/:id" element={
            <PrivateRoute>
              <ArtistPage />
            </PrivateRoute>
          } />
          <Route path="/dashboard" 
            element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            } 
          />
          <Route path="/album/:id" 
            element={
              <PrivateRoute>
                <AlbumPage />
              </PrivateRoute>
            } 
          />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App
