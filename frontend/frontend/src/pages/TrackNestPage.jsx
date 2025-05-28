import React, { useEffect, useRef, useState } from 'react';
import axios from 'axios';

export default function TrackNestPage() {
  const [publicGroups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editIndex, setEditIndex] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [visibility, setVisibility] = useState('PRIVATE');

  const modalRef = useRef();

  const fetchGroups = async (query = '') => {
    try {
      const token = localStorage.getItem('token');
      const res = await axios.get(`/api/groups${query}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setGroups(res.data);
    } catch (err) {
      console.error('Error fetching public groups:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGroups();
  }, []);

  const resetForm = () => {
    setName('');
    setDescription('');
    setVisibility('PRIVATE');
    setEditIndex(null);
  };

  const handleEditClick = (index) => {
    const group = publicGroups[index];
    setName(group.name);
    setDescription(group.description);
    setVisibility(group.visibility);
    setEditIndex(index);
    new bootstrap.Modal(modalRef.current).show();
  };

  const handleDeleteClick = async (index) => {
    try {
      const token = localStorage.getItem('token');
      const groupId = publicGroups[index].id;
      await axios.delete(`http://localhost:8080/api/groups/${groupId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchGroups();
    } catch (err) {
      console.error('Error deleting group:', err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('token');
    const group = { name, description, visibility };
    try {
      if (editIndex !== null) {
        const groupId = publicGroups[editIndex].id;
        await axios.put(`http://localhost:8080/api/groups/${groupId}`, group, {
          headers: { Authorization: `Bearer ${token}` }
        });
      } else {
        await axios.post(`http://localhost:8080/api/groups`, group, {
          headers: { Authorization: `Bearer ${token}` }
        });
      }
      bootstrap.Modal.getInstance(modalRef.current)?.hide();
      resetForm();
      fetchGroups();
    } catch (err) {
      console.error('Error saving group:', err);
    }
  };

  const handleSearch = () => {
    if (searchTerm.trim()) {
      fetchGroups(`?search=${searchTerm}`);
    }
  };

  const handleClearSearch = () => {
    setSearchTerm('');
    fetchGroups();
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center min-vh-100">
        <div className="text-secondary fs-5">Loading content...</div>
      </div>
    );
  }

  return (
    <div className="container py-5">
      <h1 className="text-center mb-4">Explore All Groups</h1>

      {/* Search Field */}
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div className="input-group w-50">
          <input
            type="text"
            className="form-control"
            placeholder="Search by name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <button className="btn btn-outline-primary" onClick={handleSearch}>🔍</button>
          <button className="btn btn-outline-secondary" onClick={handleClearSearch}>✖</button>
        </div>
        <button
          className="btn btn-primary"
          data-bs-toggle="modal"
          data-bs-target="#editGroupModal"
          onClick={resetForm}
        >
          ➕ Add Group
        </button>
      </div>

      <div className="table-responsive">
        <table className="table table-bordered table-striped text-center">
          <thead className="table-light">
            <tr>
              <th>Name</th>
              <th>Description</th>
              <th>Visibility</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {publicGroups.length > 0 ? (
              publicGroups.map((group, index) => (
                <tr key={index}>
                  <td>{group.name}</td>
                  <td>{group.description}</td>
                  <td>{group.visibility}</td>
                  <td>
                    <button className="btn btn-sm btn-outline-primary me-2" onClick={() => handleEditClick(index)}>Edit</button>
                    <button className="btn btn-sm btn-outline-danger" onClick={() => handleDeleteClick(index)}>Delete</button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="4" className="text-muted">No groups available.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Edit/Add Group Modal */}
      <div className="modal fade" id="editGroupModal" tabIndex="-1" ref={modalRef}>
        <div className="modal-dialog">
          <form className="modal-content" onSubmit={handleSubmit}>
            <div className="modal-header">
              <h5 className="modal-title">{editIndex !== null ? 'Edit Group' : 'Add Group'}</h5>
              <button type="button" className="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div className="modal-body">
              <div className="mb-3">
                <label className="form-label" htmlFor='group-name'>Name</label>
                <input id="group-name" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
              </div>
              <div className="mb-3">
                <label className="form-label" htmlFor='group-description'>Description</label>
                <input id= "group-description" className="form-control" value={description} onChange={(e) => setDescription(e.target.value)} required />
              </div>
              <div className="mb-3">
                <label className="form-label" htmlFor='group-visibility'>Visibility</label>
                <select id='group-visibility' className="form-select" value={visibility} onChange={(e) => setVisibility(e.target.value)}>
                  <option value="PRIVATE">PRIVATE</option>
                  <option value="PUBLIC">PUBLIC</option>
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
