import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Search, TrendingUp, LogOut, Music } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Button } from './ui/Button';

export const Navigation: React.FC = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const { logout } = useAuth();

    const navItems = [
        { path: '/search', label: 'Search', icon: Search },
        { path: '/dashboard', label: 'Dashboard', icon: TrendingUp },
    ];

    const handleLogout = () => {
        logout();
    };

    return (
        <header className="bg-black border-b border-gray-800 p-4 sticky top-0 z-50">
            <div className="max-w-7xl mx-auto flex items-center justify-between">
                <div className="flex items-center space-x-8">
                    <div className="flex items-center">
                        <Music className="w-8 h-8 text-green-500 mr-3" />
                        <h3 className="font-bold text-white">BT2</h3>
                    </div>

                    <nav className="flex space-x-4">
                        {navItems.map((item) => {
                            const Icon = item.icon;
                            const isActive = location.pathname === item.path;

                            return (
                                <button
                                    key={item.path}
                                    onClick={() => navigate(item.path)}
                                    className={`flex items-center space-x-2 px-3 py-2 rounded-lg transition-colors duration-200 ${isActive
                                            ? 'bg-green-600 text-white'
                                            : 'text-gray-400 hover:text-white hover:bg-gray-700'
                                        }`}
                                >
                                    <Icon className="w-4 h-4" />
                                    <span>{item.label}</span>
                                </button>
                            );
                        })}
                    </nav>
                </div>
                
                <div className='flex items-center gap-3'>
                    <h3 className="font-bold text-white">{user?.display_name}</h3>
                    {user?.images &&
                        <img
                        src={user?.images[0].url}
                        alt={user?.display_name}
                        className="w-10 h-10 rounded-full object-cover shadow-lg"
                    />}
                    <Button
                        onClick={handleLogout}
                        size="sm"
                        className="text-gray-400 hover:text-white cursor-pointer"
                    >
                        <LogOut className="w-4 h-4 mr-2" />
                        Logout
                    </Button>
                </div>
            </div>
        </header>
    );
};