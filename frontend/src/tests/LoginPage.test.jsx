import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import axios from 'axios';
import LoginPage from '../pages/LoginPage';
import '@testing-library/jest-dom';

jest.mock('axios');

// ✅ Mock useNavigate
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockedNavigate,
}));

const renderLoginPage = () =>
  render(
    <BrowserRouter>
      <LoginPage />
    </BrowserRouter>
  );

describe('LoginPage', () => {
  beforeEach(() => {
    localStorage.clear();
    mockedNavigate.mockReset();
  });

  it('renders login form inputs', () => {
    renderLoginPage();
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /log in/i })).toBeInTheDocument();
  });

  it('stores token and navigates on successful login', async () => {
    // ✅ Match component's expectation: res.data.accessToken.token
    axios.post.mockResolvedValue({
      data: {
        accessToken: { token: 'mockToken123' },
      },
    });

    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'testpass' } });
    fireEvent.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => {
      expect(localStorage.getItem('token')).toBe('mockToken123');
      expect(mockedNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('displays error on failed login', async () => {
    axios.post.mockRejectedValue(new Error('Login failed'));

    renderLoginPage();

    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'wrong' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'invalid' } });
    fireEvent.click(screen.getByRole('button', { name: /log in/i }));

    await waitFor(() => {
      expect(screen.getByText(/invalid username or password/i)).toBeInTheDocument();
    });
  });
});
