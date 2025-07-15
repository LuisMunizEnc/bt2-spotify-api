import './App.css'
import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';
import SearchPage from './pages/Search';

function App() {
  return (
      <Router>
        <Routes>
          <Route 
            path="/search" 
            element={
                <SearchPage />
            } 
          />
        </Routes>
      </Router>
  );
}

export default App
