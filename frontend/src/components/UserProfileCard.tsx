import React from 'react';
import { User as UserIcon, MapPin } from 'lucide-react';
import type { User } from '../types';

interface UserProfileCardProps {
  user: User;
}

export const UserProfileCard: React.FC<UserProfileCardProps> = ({ user }) => {

  return (
    <div className="bg-gradient-to-r from-green-600 to-green-700 rounded-lg p-6 text-white shadow-xl">
      <div className="flex items-center">
        <div className="flex-shrink-0">
          {user.images && user.images.length !== 0 ? (
            <img 
              src={user.images[0].url} 
              alt={user.display_name}
              className="w-20 h-20 rounded-full object-cover border-4 border-white shadow-lg"
            />
          ) : (
            <div className="w-20 h-20 bg-white bg-opacity-20 rounded-full flex items-center justify-center border-4 border-white">
              <UserIcon className="w-10 h-10 text-white" />
            </div>
          )}
        </div>
        
        <div className="ml-6 flex-1">
          <h2 className="text-2xl font-bold mb-2">{user.display_name}</h2>
          <p className="text-green-100 mb-3">{user.email}</p>
          
          <div className="flex justify-center space-x-4 text-sm text-center">
            {user.country && (
              <div className="flex items-center">
                <MapPin className="w-4 h-4 mr-1" />
                <span>{user.country}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};