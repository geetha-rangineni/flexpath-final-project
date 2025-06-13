import React, { useEffect, useRef, useState } from 'react';
import axios from 'axios';

export default function TrackNestPage() {
  const [publicGroups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editIndex, setEditIndex] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedIds, setSelectedIds] = useState([]);

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [visibility, setVisibility] = useState('PRIVATE');

  const [pendingDelete, setPendingDelete] = useState(null);
  const [pendingBulkDelete, setPendingBulkDelete] = useState(null);
  const undoTimeoutRef = useRef(null);
  const [errorMessage, setErrorMessage] = useState('');


  const modalRef = useRef();
  let isAdmin = true;

  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  // Sorting state: key = 'name' or 'visibility', direction = 'asc' or 'desc'
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });

const [showArrow, setShowArrow] = useState(false);

const requestSort = (key) => {
  let direction = 'asc';
  if (sortConfig.key === key && sortConfig.direction === 'asc') {
    direction = 'desc';
  }
  setSortConfig({ key, direction });
  setShowArrow(true);
};

useEffect(() => {
  if (showArrow) {
    const timeout = setTimeout(() => {
      setShowArrow(false);
    }, 2000); // hide after 5 seconds
    return () => clearTimeout(timeout);
  }
}, [sortConfig]);


  const fetchGroups = async (query = '') => {
    try {
      const token = localStorage.getItem('token');
      const res = await axios.get(`/api/groups${query}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setGroups(res.data);
    } catch (err) {
      console.error('Error fetching groups:', err);
      setErrorMessage('Failed to load groups. Please try again.');

    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    try {
      const token = localStorage.getItem('token');
      const payloadBase64 = token?.split('.')[1];
      const decodedPayload = JSON.parse(atob(payloadBase64));
      isAdmin = decodedPayload?.authorities?.includes('ADMIN');
    } catch (error) {
      console.error('Invalid token or decoding failed:', error);
    }
    
    fetchGroups();
  }, []);

  const resetForm = () => {
    setName('');
    setDescription('');
    setVisibility('PRIVATE');
    setEditIndex(null);
  };

  const handleEditClick = (index) => {
    const group = currentGroups[index];
    setName(group.name);
    setDescription(group.description);
    setVisibility(group.visibility);
    setEditIndex((currentPage - 1) * itemsPerPage + index);
    new bootstrap.Modal(modalRef.current).show();
  };

  const handleDeleteClick = async (index) => {
    const confirmed = window.confirm("Are you sure you want to delete this group?");
    if (!confirmed) return;
    const group = currentGroups[index];
    try {
      const token = localStorage.getItem('token');
      await axios.delete(`/api/groups/${group.id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      fetchGroups();
      setPendingDelete({ group });
      undoTimeoutRef.current = setTimeout(() => setPendingDelete(null), 5000);
    } catch (err) {
      console.error('Error deleting group:', err);
      setErrorMessage('Failed to load groups. Please try again.');
    }
  };

  const handleUndoDelete = async () => {
    if (!pendingDelete) return;
    clearTimeout(undoTimeoutRef.current);
    try {
      const token = localStorage.getItem('token');
      await axios.post(`/api/groups`, pendingDelete.group, {
        headers: { Authorization: `Bearer ${token}` },
      });
      fetchGroups();
    } catch (err) {
      console.error('Error undoing delete:', err);
    } finally {
      setPendingDelete(null);
    }
  };

  const handleBulkDelete = async () => {
    // Only delete selected IDs present on current page
    const idsToDelete = selectedIds.filter(id => currentGroups.some(group => group.id === id));
    if (idsToDelete.length === 0) {
      alert("No selected groups on the current page to delete.");
      return;
    }

    const confirmed = window.confirm(`Are you sure you want to delete ${idsToDelete.length} selected group(s) on this page?`);
    if (!confirmed) return;

    try {
      const token = localStorage.getItem('token');
      await Promise.all(
        idsToDelete.map((id) =>
          axios.delete(`/api/groups/${id}`, {
            headers: { Authorization: `Bearer ${token}` },
          })
        )
      );
      const deletedGroups = currentGroups.filter(g => idsToDelete.includes(g.id));
      setPendingBulkDelete(deletedGroups);
      setSelectedIds(prev => prev.filter(id => !idsToDelete.includes(id))); // Remove deleted IDs only
      fetchGroups();
      undoTimeoutRef.current = setTimeout(() => setPendingBulkDelete(null), 5000);
    } catch (err) {
      console.error('Error in bulk delete:', err);
    }
  };

  const handleUndoBulkDelete = async () => {
    if (!pendingBulkDelete) return;
    clearTimeout(undoTimeoutRef.current);
    try {
      const token = localStorage.getItem('token');
      await Promise.all(
        pendingBulkDelete.map((group) =>
          axios.post(`/api/groups`, group, {
            headers: { Authorization: `Bearer ${token}` },
          })
        )
      );
      fetchGroups();
    } catch (err) {
      console.error('Error restoring bulk groups:', err);
    } finally {
      setPendingBulkDelete(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('token');
    const group = { name, description, visibility };
    try {
      if (editIndex !== null) {
        const groupId = publicGroups[editIndex].id;
        await axios.put(`/api/groups/${groupId}`, group, {
          headers: { Authorization: `Bearer ${token}` },
        });
      } else {
        await axios.post(`/api/groups`, group, {
          headers: { Authorization: `Bearer ${token}` },
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

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;

  // Sorting groups before pagination
  const sortedGroups = React.useMemo(() => {
    if (!sortConfig.key) return publicGroups;

    const sorted = [...publicGroups].sort((a, b) => {
      let valA = a[sortConfig.key];
      let valB = b[sortConfig.key];

      // Normalize strings for case-insensitive sort
      if (typeof valA === 'string') valA = valA.toLowerCase();
      if (typeof valB === 'string') valB = valB.toLowerCase();

      if (valA < valB) return sortConfig.direction === 'asc' ? -1 : 1;
      if (valA > valB) return sortConfig.direction === 'asc' ? 1 : -1;
      return 0;
    });
    return sorted;
  }, [publicGroups, sortConfig]);

  const currentGroups = sortedGroups.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(publicGroups.length / itemsPerPage);

  const handlePageChange = (pageNumber) => {
    setCurrentPage(pageNumber);
    // Optionally clear selectedIds on page change
    setSelectedIds([]);
  };

  const handleSort = (key) => {
    setSortConfig(prev => {
      let newConfig;
      if (prev.key === key) {
        newConfig = {
          key,
          direction: prev.direction === 'asc' ? 'desc' : 'asc'
        };
      } else {
        newConfig = { key, direction: 'asc' };
      }
      return newConfig;
    });
    setShowArrow(true);
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

      {errorMessage && (
  <div className="alert alert-danger alert-dismissible fade show" role="alert">
    {errorMessage}
    <button type="button" className="btn-close" onClick={() => setErrorMessage('')} />
  </div>
)}


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

      {isAdmin && selectedIds.length > 0 && (
        <div className="mb-3 text-end">
          <button className="btn btn-danger" onClick={handleBulkDelete}>
            🗑️ Delete Selected ({selectedIds.length})
          </button>
        </div>
      )}

      <div className="table-responsive">
        <table className="table table-bordered table-striped text-center">
          <thead className="table-light">
            <tr>
            {isAdmin && (
                <th>
                  <input
                    type="checkbox"
                    checked={selectedIds.length === currentGroups.length && currentGroups.length > 0}
                    onChange={(e) => {
                      setSelectedIds(e.target.checked ? currentGroups.map(g => g.id) : []);
                    }}
                  />
                </th>
              )}


            <th
              style={{ cursor: 'pointer' }}
              onClick={() => handleSort('name')}
            >
              Name
              {showArrow && sortConfig.key === 'name' && (sortConfig.direction === 'asc' ? ' ▲' : ' ▼')}
            </th>

              <th>Description</th>
              <th 
                style={{ cursor: 'pointer' }}
                onClick={() => requestSort('visibility')}>
                Visibility
                {showArrow && sortConfig.key === 'visibility' && (sortConfig.direction === 'asc' ? ' ▲' : ' ▼')}
              </th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {currentGroups.length > 0 ? (
              currentGroups.map((group, index) => (
                <tr key={group.id}>
                  {isAdmin && (
                    <td>
                      <input
                        type="checkbox"
                        checked={selectedIds.includes(group.id)}
                        onChange={() => {
                          setSelectedIds(prev =>
                            prev.includes(group.id)
                              ? prev.filter(id => id !== group.id)
                              : [...prev, group.id]
                          );
                        }}
                      />
                    </td>
                  )}
                  <td>{group.name}</td>
                  <td>{group.description}</td>
                  <td>{group.visibility}</td>
                  <td>
                    <button className="btn btn-sm btn-outline-primary me-2" onClick={() => handleEditClick(index)}>Edit</button>
                    {!selectedIds.includes(group.id) && (
                      <button className="btn btn-sm btn-outline-danger" onClick={() => handleDeleteClick(index)}>Delete</button>
                    )}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={isAdmin ? 5 : 4} className="text-muted">No groups available.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <nav className="d-flex justify-content-between align-items-center">
        <div>Showing rows {indexOfFirstItem + 1} - {Math.min(indexOfLastItem, publicGroups.length)} of {publicGroups.length}</div>
        <ul className="pagination">
          <li className={`page-item ${currentPage === 1 && 'disabled'}`}>
            <button className="page-link" onClick={() => handlePageChange(1)}>&laquo;</button>
          </li>
          <li className={`page-item ${currentPage === 1 && 'disabled'}`}>
            <button className="page-link" onClick={() => handlePageChange(currentPage - 1)}>&lsaquo;</button>
          </li>
          <li className="page-item active">
            <span className="page-link">{currentPage}</span>
          </li>
          <li className={`page-item ${currentPage === totalPages && 'disabled'}`}>
            <button className="page-link" onClick={() => handlePageChange(currentPage + 1)}>&rsaquo;</button>
          </li>
          <li className={`page-item ${currentPage === totalPages && 'disabled'}`}>
            <button className="page-link" onClick={() => handlePageChange(totalPages)}>&raquo;</button>
          </li>
        </ul>
      </nav>

      {pendingDelete && (
        <div className="alert alert-warning alert-dismissible fade show position-fixed bottom-0 end-0 m-3 shadow" style={{ zIndex: 9999 }}>
          Group "{pendingDelete.group.name}" deleted.
          <button className="btn btn-sm btn-outline-dark ms-2" onClick={handleUndoDelete}>Undo</button>
        </div>
      )}

      {pendingBulkDelete && (
        <div className="alert alert-warning alert-dismissible fade show position-fixed bottom-0 end-0 m-3 shadow" style={{ zIndex: 9999 }}>
          {pendingBulkDelete.length} group(s) deleted.
          <button className="btn btn-sm btn-outline-dark ms-2" onClick={handleUndoBulkDelete}>Undo</button>
        </div>
      )}

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
                <input id="group-description" className="form-control" value={description} onChange={(e) => setDescription(e.target.value)} required />
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
