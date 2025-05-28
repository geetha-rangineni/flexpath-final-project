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
});
