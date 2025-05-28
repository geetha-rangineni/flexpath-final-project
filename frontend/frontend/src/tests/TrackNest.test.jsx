import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import axios from 'axios';
import TrackNestPage from '../pages/TrackNestPage';

jest.mock('axios');

global.bootstrap = {
  Modal: class {
    show() {}
    hide() {}
    static getInstance() {
      return { hide: jest.fn() };
    }
  }
};

describe('TrackNestPage', () => {
  const mockGroups = [
    { id: 1, name: 'Group 1', description: 'Desc 1', visibility: 'PUBLIC' },
    { id: 2, name: 'Group 2', description: 'Desc 2', visibility: 'PRIVATE' },
  ];

  beforeEach(() => {
    axios.get.mockResolvedValue({ data: mockGroups });
    axios.post.mockResolvedValue({});
    axios.put.mockResolvedValue({});
    axios.delete.mockResolvedValue({});
    localStorage.setItem('token', 'mock-token');
  });

  it('renders table after loading', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Explore All Groups'));
    expect(screen.getByText('Group 1')).toBeInTheDocument();
    expect(screen.getByText('Group 2')).toBeInTheDocument();
  });

  it('displays error message if fetch fails', async () => {
    axios.get.mockRejectedValueOnce(new Error('Fetch error'));
    render(<TrackNestPage />);
    await waitFor(() => {
      expect(screen.getByText('Loading content...')).toBeInTheDocument();
    });
  });

  it('performs a search and displays filtered results', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Group 1'));
    fireEvent.change(screen.getByPlaceholderText('Search by name...'), {
      target: { value: 'Group 1' },
    });
    fireEvent.click(screen.getByText('🔍'));
    expect(axios.get).toHaveBeenCalledWith('/api/groups?search=Group 1', expect.anything());
  });

  it('clears search and reloads all data', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Group 1'));
    fireEvent.click(screen.getByText('✖'));
    expect(axios.get).toHaveBeenCalledWith('/api/groups', expect.anything());
  });

  it('handles edit click and opens modal with data', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Group 1'));
    fireEvent.click(screen.getAllByText('Edit')[0]);
    expect(document.querySelector('#editGroupModal')).toBeInTheDocument();
  });

  it('handles delete click and calls API', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Group 1'));
    fireEvent.click(screen.getAllByText('Delete')[0]);
    await waitFor(() => {
      expect(axios.delete).toHaveBeenCalledWith('http://localhost:8080/api/groups/1', expect.anything());
    });
  });

  it('submits new group and closes modal', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Explore All Groups'));

    fireEvent.click(screen.getByText('➕ Add Group'));
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'New Group' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'New Desc' } });
    fireEvent.change(screen.getByLabelText('Visibility'), { target: { value: 'PUBLIC' } });
    fireEvent.click(screen.getByText('Save'));

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        'http://localhost:8080/api/groups',
        expect.objectContaining({ name: 'New Group', description: 'New Desc', visibility: 'PUBLIC' }),
        expect.anything()
      );
    });
  });

  it('shows empty message if no groups are available', async () => {
    axios.get.mockResolvedValueOnce({ data: [] });
    render(<TrackNestPage />);
    await waitFor(() => {
      expect(screen.getByText('No groups available.')).toBeInTheDocument();
    });
  });
});
