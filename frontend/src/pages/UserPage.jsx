import React, { useEffect, useRef, useState } from 'react';
import axios from 'axios';

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('USER');
  const [editIndex, setEditIndex] = useState(null);
  const userModalRef = useRef();
  const [mUsername, setUser] = useState('');

  useEffect(() => {
      const token = localStorage.getItem('token');
     if (token) {
    try {
      const payloadBase64 = token.split('.')[1];
      const decodedPayload = JSON.parse(atob(payloadBase64));
      const username = decodedPayload?.sub||decodedPayload.username||''
       setUser(username)
    } catch (err) {
      console.error('Error decoding token:', err);
    }
  }

    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/users', { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } });
      setUsers(res.data);
    } catch (err) {
      console.error('Error fetching users:', err);
    }
  };

  const resetForm = () => {
    setUsername('');
    setPassword('');
    setRole('USER');
    setEditIndex(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const user = { username, password, role };
    try {
      if (editIndex !== null) {
        await axios.put(`http://localhost:8080/api/users/${users[editIndex].username}/password`, user, { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } });
      } else {
        await axios.post('http://localhost:8080/api/users', user, { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } });
      }
      bootstrap.Modal.getInstance(userModalRef.current)?.hide();
      resetForm();
      fetchUsers();
    } catch (err) {
      console.error('Error saving user:', err);
    }
  };

  const handleEdit = (index) => {
    const user = users[index];
    setUsername(user.username);
    setPassword('');
    setRole(user.role);
    setEditIndex(index);
    new bootstrap.Modal(userModalRef.current).show();
  };

  const handleDelete = async (username) => {
    try {
      await axios.delete(`http://localhost:8080/api/users/${username}`, { headers: { Authorization: `Bearer ${localStorage.getItem('token')}` } });
      fetchUsers();
    } catch (err) {
      console.error('Error deleting user:', err);
    }
  };

  return (
    <div className="container py-5">
      <h2 className="mb-4 text-center">Manage Users</h2>
      <div className="text-end mb-3">
        <button
          className="btn btn-primary"
          data-bs-toggle="modal"
          data-bs-target="#userModal"
          onClick={resetForm}
        >
          ➕ Add User
        </button>
      </div>

      <div className="table-responsive">
        <table className="table table-bordered table-striped text-center">
          <thead className="table-light">
            <tr>
              <th>Username</th>
              <th>Role</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.length > 0 ? (
              users.map((user, index) => (
                <tr key={user.username}>
                  <td>{user.username}</td>
                  <td>{user.role}</td>
                  {user.username!=mUsername &&
                  <td>
                    <button className="btn btn-sm btn-outline-primary me-2" onClick={() => handleEdit(index)}>
                      Edit
                    </button>
                    <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(user.username)}>
                      Delete
                    </button>
                  </td>
                  }
                   {user.username==mUsername && <td></td>}
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="3" className="text-muted">No users found.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* User Modal */}
      <div className="modal fade" id="userModal" tabIndex="-1" ref={userModalRef}>
        <div className="modal-dialog">
          <form className="modal-content" onSubmit={handleSubmit}>
            <div className="modal-header">
              <h5 className="modal-title">{editIndex !== null ? 'Edit User' : 'Add User'}</h5>
              <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div className="modal-body">
              <div className="mb-3">
                <label className="form-label" htmlFor="username">Username</label>
                <input
                  id="username"
                  type="text"
                  className="form-control"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  disabled={editIndex !== null}
                />
              </div>
              <div className="mb-3">
                <label className="form-label" htmlFor="password">Password</label>
                <input
                  id="password"
                  type="password"
                  className="form-control"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required={editIndex === null}
                />
              </div>
              <div className="mb-3">
                <label className="form-label" htmlFor="role">Role</label>
                <select
                  id="role"
                  className="form-select"
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                  disabled={editIndex !== null}
                >
                  <option value="USER">USER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-success" type="submit">Save</button>
              <button className="btn btn-secondary" data-bs-dismiss="modal" type="button">Cancel</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
