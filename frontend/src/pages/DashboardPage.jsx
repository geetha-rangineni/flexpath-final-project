import React, { useEffect, useRef, useState } from 'react';
import axios from 'axios';

export default function DashboardPage() {
  const [privateItems, setPrivateItems] = useState([]);
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchBy, setSearchBy] = useState('');
  const [editIndex, setEditIndex] = useState(null);
  const [selectedIds, setSelectedIds] = useState([]);
  const isAdmin = true;

  // Form states
  const [title, setTitle] = useState('');
  const [type, setType] = useState('Workout');
  const [description, setDescription] = useState('');
  const [visibility, setVisibility] = useState('PRIVATE');
  const [date, setDate] = useState('');
  const [createdBy, setCreatedBy] = useState('');
  const [group, setGroup] = useState('');

  // Undo states
  const [lastDeletedItems, setLastDeletedItems] = useState([]);
  const [undoVisible, setUndoVisible] = useState(false);
  const undoTimerRef = useRef(null);

  const itemModalRef = useRef();
  const [pendingDeleteItems, setPendingDeleteItems] = useState([]);

  

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const token = localStorage.getItem('token');
      const [itemsRes, groupsRes] = await Promise.all([
        axios.get('/api/entries', { headers: { Authorization: `Bearer ${token}` } }),
        axios.get('/api/groups', { headers: { Authorization: `Bearer ${token}` } }),
      ]);
      setPrivateItems(itemsRes.data);
      setGroups(groupsRes.data);
    } catch (err) {
      console.error('Error fetching data:', err);
    } finally {
      setLoading(false);
    }
  };

  const resetItemForm = () => {
    setTitle('');
    setType('Workout');
    setDescription('');
    setVisibility('PRIVATE');
    setDate('');
    setCreatedBy('');
    setGroup('');
    setEditIndex(null);
  };

  const handleItemSubmit = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem('token');
      const config = { headers: { Authorization: `Bearer ${token}` } };
      const item = { title, type, description, visibility, date, createdBy, group: { id: group } };

      if (editIndex !== null) {
        await axios.put(`/api/entries/${privateItems[editIndex].id}`, item, config);
      } else {
        await axios.post('/api/entries', item, config);
      }

      window.bootstrap.Modal.getInstance(itemModalRef.current)?.hide();
      resetItemForm();
      fetchData();
    } catch (err) {
      console.error('Error saving item:', err);
    }
  };

  const handleEditItem = (index) => {
    const item = privateItems[index];
    setTitle(item.title);
    setType(item.type);
    setDescription(item.description);
    setVisibility(item.visibility);
    setDate(item.date);
    setCreatedBy(item.createdBy);
    setGroup(item.group?.id || '');
    setEditIndex(index);
    new window.bootstrap.Modal(itemModalRef.current).show();
  };

  const handleDeleteItem = (index) => {
    const item = privateItems[index];
    if (!window.confirm(`Delete "${item.title}"?`)) return;
  
    setPendingDeleteItems([item]);
    setPrivateItems(prev => prev.filter(i => i.id !== item.id));
    setUndoVisible(true);
  
    undoTimerRef.current = setTimeout(async () => {
      const token = localStorage.getItem('token');
      try {
        await axios.delete(`/api/entries/${item.id}`, { headers: { Authorization: `Bearer ${token}` } });
      } catch (err) {
        console.error('Delete error:', err);
        fetchData();
      }
      setUndoVisible(false);
      setPendingDeleteItems([]);
    }, 5000);
  };
  
  const handleBulkDelete = () => {
    const itemsToDelete = privateItems.filter(i => selectedIds.includes(i.id));
    if (!itemsToDelete.length || !window.confirm(`Delete ${itemsToDelete.length} items?`)) return;
  
    setPendingDeleteItems(itemsToDelete);
    setPrivateItems(prev => prev.filter(i => !selectedIds.includes(i.id)));
    setSelectedIds([]);
    setUndoVisible(true);
  
    undoTimerRef.current = setTimeout(async () => {
      const token = localStorage.getItem('token');
      try {
        await Promise.all(itemsToDelete.map(item =>
          axios.delete(`/api/entries/${item.id}`, { headers: { Authorization: `Bearer ${token}` } })
        ));
      } catch (err) {
        console.error('Bulk delete error:', err);
        fetchData();
      }
      setUndoVisible(false);
      setPendingDeleteItems([]);
    }, 5000);
  };
  const handleUndo = () => {
    clearTimeout(undoTimerRef.current);
    setPrivateItems(prev => [...pendingDeleteItems, ...prev]);
    setPendingDeleteItems([]);
    setUndoVisible(false);
  };
    
  
  const handleSearch = async () => {
    if (!searchBy || !searchTerm) return;
    try {
      const token = localStorage.getItem('token');
      const res = await axios.get('/api/entries/search', {
        headers: { Authorization: `Bearer ${token}` },
        params: { field: searchBy, query: searchTerm },
      });
      setPrivateItems(res.data);
    } catch (err) {
      console.error('Search failed:', err);
    } finally {
      setSearchBy('');
      setSearchTerm('');
    }
  };

  return (
    <div className="min-vh-100 bg-light py-5">
      <div className="container">
        <h1 className="text-center mb-4">My Dashboard</h1>

        <div className="d-flex justify-content-between align-items-center mb-3">
          <div className="d-flex gap-2">
            <select className="form-select w-auto" value={searchBy} onChange={(e) => setSearchBy(e.target.value)}>
              <option value="">Search By</option>
              <option value="title">Title</option>
              <option value="type">Type</option>
            </select>
            <input
              type="text"
              className="form-control"
              placeholder="Search..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              disabled={!searchBy}
            />
            <button className="btn btn-outline-secondary" onClick={handleSearch} disabled={!searchBy || !searchTerm}>🔍</button>
          </div>

          <button className="btn btn-primary" data-bs-toggle="modal" data-bs-target="#itemModal" onClick={resetItemForm}>
            ➕ Add Item
          </button>
        </div>

        {isAdmin && selectedIds.length > 0 && (
          <div className="mb-3 d-flex justify-content-end">
            <button className="btn btn-danger" onClick={handleBulkDelete}>
              Delete Selected ({selectedIds.length})
            </button>
          </div>
        )}

        {undoVisible && (
        <div className="alert alert-warning alert-dismissible fade show position-fixed bottom-0 end-0 m-3 shadow" style={{ zIndex: 9999 }}>
            <span>Item(s) deleted. <strong>Undo?</strong></span>
            <button className="btn btn-sm btn-outline-dark" onClick={handleUndo}>Undo</button>
          </div>
        )}

        <div className="table-responsive">
          <table className="table table-bordered table-striped">
            <thead className="table-light">
              <tr>
                {isAdmin && (
                  <th>
                    <input
                      type="checkbox"
                      checked={selectedIds.length === privateItems.length && privateItems.length > 0}
                      onChange={(e) => setSelectedIds(e.target.checked ? privateItems.map(item => item.id) : [])}
                    />
                  </th>
                )}
                {['Title', 'Type', 'Description', 'Visibility', 'Date', 'Created By', 'Group', 'Actions'].map((head) => (
                  <th key={head}>{head}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {privateItems.length ? (
                privateItems.map((item, index) => (
                  <tr key={item.id}>
                    {isAdmin && (
                      <td>
                        <input
                          type="checkbox"
                          checked={selectedIds.includes(item.id)}
                          onChange={() => {
                            setSelectedIds(prev =>
                              prev.includes(item.id) ? prev.filter(id => id !== item.id) : [...prev, item.id]
                            );
                          }}
                        />
                      </td>
                    )}
                    <td>{item.title}</td>
                    <td>{item.type}</td>
                    <td>{item.description}</td>
                    <td>{item.visibility}</td>
                    <td>{item.date ? new Date(item.date).toLocaleDateString() : '—'}</td>
                    <td>{item.createdBy}</td>
                    <td>{item.group?.name || '—'}</td>
                    <td>
                      <button className="btn btn-sm btn-outline-primary me-2" onClick={() => handleEditItem(index)}>Edit</button>
                      {!selectedIds.includes(item.id) && (
                        <button className="btn btn-sm btn-outline-danger" onClick={() => handleDeleteItem(index)}>Delete</button>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={isAdmin ? 9 : 8} className="text-center text-muted">
                    No private items available.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Modal */}
        <div className="modal fade" id="itemModal" tabIndex="-1" ref={itemModalRef}>
          <div className="modal-dialog">
            <form className="modal-content" onSubmit={handleItemSubmit}>
              <div className="modal-header">
                <h5 className="modal-title">{editIndex !== null ? 'Edit Item' : 'Add New Item'}</h5>
                <button type="button" className="btn-close" data-bs-dismiss="modal" />
              </div>
              <div className="modal-body">
                {[
                  { id: "title", label: 'Title', value: title, onChange: setTitle },
                  { id: "description", label: 'Description', value: description, onChange: setDescription }
                ].map(({ id, label, value, onChange }) => (
                  <div className="mb-3" key={id}>
                    <label htmlFor={id} className="form-label">{label}</label>
                    <input
                      id={id}
                      className="form-control"
                      type="text"
                      value={value}
                      onChange={(e) => onChange(e.target.value)}
                      required
                    />
                  </div>
                ))}

                <div className="mb-3">
                  <label htmlFor="group" className="form-label">Group</label>
                  <select id="group" className="form-select" value={group} onChange={(e) => setGroup(e.target.value)} required>
                    <option value="">Select a group</option>
                    {groups.map((grp) => (
                      <option key={grp.id} value={grp.id}>{grp.name}</option>
                    ))}
                  </select>
                </div>

                <div className="mb-3">
                  <label htmlFor="type" className="form-label">Type</label>
                  <select id="type" className="form-select" value={type} onChange={(e) => setType(e.target.value)}>
                    {['Workout', 'Diet', 'Symptom', 'Other'].map((opt) => (
                      <option key={opt}>{opt}</option>
                    ))}
                  </select>
                </div>

                <div className="mb-3">
                  <label htmlFor="visibility" className="form-label">Visibility</label>
                  <select id="visibility" className="form-select" value={visibility} onChange={(e) => setVisibility(e.target.value)}>
                    <option>PRIVATE</option>
                    <option>PUBLIC</option>
                  </select>
                </div>

                <div className="mb-3">
                  <label htmlFor="date" className="form-label">Date</label>
                  <input id="date" className="form-control" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
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
    </div>
  );
}