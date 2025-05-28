import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

export default function Navbar() {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  let isAdmin = false;
  if (token) {
    try {
      const payloadBase64 = token.split('.')[1];
      const decodedPayload = JSON.parse(atob(payloadBase64));
      isAdmin = decodedPayload?.authorities?.includes('ADMIN');
      const username = decodedPayload?.sub||decodedPayload.username||''
    } catch (err) {
      console.error('Error decoding token:', err);
    }
  }
  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <nav className="bg-white border-b shadow-sm px-6 py-4 sticky top-0 z-50">
      <div className="max-w-6xl mx-auto flex justify-end items-center">
        <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
          {token && (
            <>
              <Link to="/tracknest" className="text-xl font-bold text-blue-600 hover:underline">
                TrackNest App
              </Link>
              <Link to="/dashboard" className="text-gray-700 hover:text-blue-600 transition" style={{ marginLeft: '20px' }}>
                Dashboard
              </Link>
              {isAdmin && (
                <Link to="/user" className="text-gray-700 hover:text-blue-600 transition" style={{ marginLeft: '20px' }}>
                  User
                </Link>
              )}
              <button
                onClick={handleLogout}
                className="text-gray-700 hover:text-red-600 transition"
                style={{ marginLeft: '20px', marginRight: '10px', background: 'none', border: 'none' }}
              >
                Logout
              </button>
            </>
          )}

        </div>
      </div>
    </nav>
  );
}
