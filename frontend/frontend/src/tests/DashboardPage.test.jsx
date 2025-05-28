import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import DashboardPage from '../pages/DashboardPage';
import axios from 'axios';
import '@testing-library/jest-dom';


// Mock axios
jest.mock('axios');

// Mock bootstrap.Modal to prevent jsdom error
global.bootstrap = {
  Modal: function () {
    return { show: jest.fn(), hide: jest.fn() };
  }
};

describe('DashboardPage', () => {
  beforeEach(() => {
    axios.get.mockResolvedValueOnce({
      data: [
        {
          id: 1,
          title: 'Test Title',
          type: 'Workout',
          description: 'Test Desc',
          visibility: 'PRIVATE',
          date: '2023-01-01',
          createdBy: 'admin',
          group: { id: 1, name: 'Group A' }
        }
      ]
    }).mockResolvedValueOnce({
      data: [
        { id: 1, name: 'Group A' },
        { id: 2, name: 'Group B' }
      ]
    });

    localStorage.setItem('token', 'fake-jwt-token');
  });

  it('renders dashboard title and table', async () => {
    render(<DashboardPage />);
    await waitFor(() => expect(screen.getByText('Test Title')).toBeInTheDocument());
    expect(screen.getByText('My Dashboard')).toBeInTheDocument();
  });

  it('opens add item modal and fills form', async () => {
    render(<DashboardPage />);
    fireEvent.click(screen.getByText('➕ Add Item'));

    await waitFor(() => expect(screen.getByLabelText('Title')).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'New Title' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'New Desc' } });
    fireEvent.change(screen.getByLabelText('Group'), { target: { value: '1' } });
    fireEvent.change(screen.getByLabelText('Type'), { target: { value: 'Diet' } });
    fireEvent.change(screen.getByLabelText('Visibility'), { target: { value: 'PUBLIC' } });
    fireEvent.change(screen.getByLabelText('Date'), { target: { value: '2023-12-01' } });

    fireEvent.click(screen.getByText('Save'));

    await waitFor(() => expect(axios.post).toHaveBeenCalled());
  });

  it('edits an existing item', async () => {
    render(<DashboardPage />);
    await waitFor(() => screen.getByText('Test Title'));

    fireEvent.click(screen.getAllByText('Edit')[0]);

    await waitFor(() => expect(screen.getByLabelText('Title')).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'Edited Title' } });
    fireEvent.click(screen.getByText('Save'));

    await waitFor(() => expect(axios.put).toHaveBeenCalled());
  });

  it('deletes an item', async () => {
    render(<DashboardPage />);
    await waitFor(() => screen.getByText('Test Title'));

    fireEvent.click(screen.getAllByText('Delete')[0]);

    await waitFor(() => expect(axios.delete).toHaveBeenCalled());
  });
});
