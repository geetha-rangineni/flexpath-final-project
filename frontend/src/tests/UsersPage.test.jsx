import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';
import UsersPage from '../pages/UserPage';

jest.mock('axios');

// Mock Bootstrap Modal
global.bootstrap = {
  Modal: class {
    static getInstance() {
      return { hide: jest.fn() };
    }
    constructor() {
      return { show: jest.fn() };
    }
  }
};

describe('UsersPage', () => {
  const mockUsers = [
    { username: 'john', role: 'USER' },
    { username: 'admin', role: 'ADMIN' }
  ];

  beforeEach(() => {
    axios.get.mockResolvedValue({ data: mockUsers });
    axios.post.mockResolvedValue({});
    axios.put.mockResolvedValue({});
    axios.delete.mockResolvedValue({});
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('renders user table after fetching users', async () => {
    render(<UsersPage />);
    await waitFor(() => {
      expect(screen.getByText('john')).toBeInTheDocument();
      expect(screen.getByText('admin')).toBeInTheDocument();
    });
  });

  it('opens modal to add a new user', async () => {
    render(<UsersPage />);
    const addButton = screen.getByRole('button', { name: /add user/i });
    fireEvent.click(addButton);

    // Expect username and password inputs to be present
    await waitFor(() => {
      expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    });
  });

  it('submits the form to add a new user', async () => {
    render(<UsersPage />);
    fireEvent.click(screen.getByRole('button', { name: /add user/i }));

    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'newuser' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'pass123' } });
    fireEvent.change(screen.getByLabelText(/role/i), { target: { value: 'ADMIN' } });

    fireEvent.click(screen.getByText(/save/i));

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        'http://localhost:8080/api/users',
        {
          username: 'newuser',
          password: 'pass123',
          role: 'ADMIN'
        },
        expect.any(Object) // headers
      );
    });
  });

  it('opens modal to edit a user and submits updated password', async () => {
    render(<UsersPage />);
    await waitFor(() => screen.getByText('john'));

    const editButton = screen.getAllByRole('button', { name: /edit/i })[0];
    fireEvent.click(editButton);

    const passwordInput = await screen.findByLabelText(/password/i);
    fireEvent.change(passwordInput, { target: { value: 'updated123' } });

    fireEvent.click(screen.getByText('Save'));

    await waitFor(() => {
      expect(axios.put).toHaveBeenCalledWith(
        'http://localhost:8080/api/users/john/password',
        {
          username: 'john',
          password: 'updated123',
          role: 'USER'
        },
        expect.any(Object)
      );
    });
  });

  it('deletes a user', async () => {
    render(<UsersPage />);
    await waitFor(() => screen.getByText('admin'));

    const deleteButton = screen.getAllByRole('button', { name: /delete/i })[1];
    fireEvent.click(deleteButton);

    await waitFor(() => {
      expect(axios.delete).toHaveBeenCalledWith(
        'http://localhost:8080/api/users/admin',
        expect.any(Object)
      );
    });
  });

  it('logs error when fetching users fails', async () => {
    const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
    axios.get.mockRejectedValueOnce(new Error('Fetch failed'));
  
    render(<UsersPage />);
    await waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining('Error fetching users:'), expect.any(Error));
    });
  
    consoleSpy.mockRestore();
  });

  it('resets form fields when Add User is clicked', async () => {
    render(<UsersPage />);
    fireEvent.click(screen.getByRole('button', { name: /add user/i }));
  
    await waitFor(() => {
      expect(screen.getByLabelText(/username/i)).toHaveValue('');
      expect(screen.getByLabelText(/password/i)).toHaveValue('');
      expect(screen.getByLabelText(/role/i)).toHaveValue('USER');
    });
  });
  

  it('does not render edit/delete buttons for logged-in user', async () => {
    const payload = { sub: 'john' }; // same as mock user
    const encoded = btoa(JSON.stringify(payload));
    localStorage.setItem('token', `header.${encoded}.sig`);
  
    render(<UsersPage />);
    await waitFor(() => {
      expect(screen.queryAllByRole('button', { name: /edit/i }).length).toBe(1); // Only 'admin' is editable
      expect(screen.queryAllByRole('button', { name: /delete/i }).length).toBe(1);
    });
  
    localStorage.removeItem('token');
  });

  it('hides edit/delete buttons for the logged-in user from the token', async () => {
    const mockPayload = { sub: 'john' }; // logged-in user
    const encodedPayload = btoa(JSON.stringify(mockPayload));
    localStorage.setItem('token', `header.${encodedPayload}.signature`);
  
    const mockUsers = [
      { username: 'john', role: 'USER' },
      { username: 'admin', role: 'ADMIN' }
    ];
    axios.get.mockResolvedValueOnce({ data: mockUsers });
  
    render(<UsersPage />);
  
    await waitFor(() => {
      expect(screen.getByText('john')).toBeInTheDocument();
      expect(screen.getByText('admin')).toBeInTheDocument();
    });
  
    // john's edit/delete buttons should be hidden
    const editButtons = screen.getAllByRole('button', { name: /edit/i });
    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
  
    expect(editButtons.length).toBe(1); // Only for admin
    expect(deleteButtons.length).toBe(1);
  
    localStorage.removeItem('token');
  });
  
  it('shows fallback message when no users are present', async () => {
    axios.get.mockResolvedValueOnce({ data: [] }); // no users
    render(<UsersPage />);
    expect(await screen.findByText(/No Users/i)).toBeInTheDocument();
  });
  it('opens edit modal when Edit is clicked', async () => {
    const mockUsers = [{ id: 1, name: 'John Doe', email: 'john@example.com' }];
    axios.get.mockResolvedValueOnce({ data: mockUsers });
  
    render(<UsersPage />);
    const editButton = await screen.findByText('Edit');
    fireEvent.click(editButton);
  
    expect(await screen.findByText('Edit User')).toBeInTheDocument();
  });
  it('opens edit modal when Edit is clicked', async () => {
  const mockUsers = [{ id: 1, name: 'John Doe', email: 'john@example.com' }];
  axios.get.mockResolvedValueOnce({ data: mockUsers });

  render(<UsersPage />);
  const editButton = await screen.findByText('Edit');
  fireEvent.click(editButton);

  expect(await screen.findByText('Edit User')).toBeInTheDocument();
});
it('shows fallback message when no users are present', async () => {
  axios.get.mockResolvedValueOnce({ data: [] }); // simulate empty user list
  render(<UsersPage />);
  expect(await screen.findByText(/No Users Found/i)).toBeInTheDocument();
});

it('does not render edit/delete buttons for logged-in user', async () => {
  const token = {
    sub: 'john',
    authorities: ['ADMIN']
  };
  localStorage.setItem('token', btoa(JSON.stringify({})) + '.' + btoa(JSON.stringify(token)) + '.');

  axios.get.mockResolvedValueOnce({ data: [{ id: 1, username: 'john' }] });

  render(<UsersPage />);

  await waitFor(() => {
    expect(screen.queryByText('Edit')).not.toBeInTheDocument();
    expect(screen.queryByText('Delete')).not.toBeInTheDocument();
  });
});

});
