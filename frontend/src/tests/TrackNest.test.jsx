import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
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
    { id: 1, name: 'Alpha Group', description: 'Desc A', visibility: 'PUBLIC' },
    { id: 2, name: 'Beta Group', description: 'Desc B', visibility: 'PRIVATE' },
  ];

  beforeEach(() => {
    const mockPayload = btoa(JSON.stringify({ authorities: ['ADMIN'] }));
    localStorage.setItem('token', `header.${mockPayload}.signature`);
  
    axios.get.mockResolvedValue({ data: mockGroups });
    axios.post.mockResolvedValue({});
    axios.put.mockResolvedValue({});
    axios.delete.mockResolvedValue({});
  });
  

  it('renders table after loading', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Explore All Groups'));
    expect(screen.getByText('Alpha Group')).toBeInTheDocument();
    expect(screen.getByText('Beta Group')).toBeInTheDocument();
  });

  it('displays error message on fetch failure', async () => {
    axios.get.mockRejectedValueOnce(new Error('Fetch failed'));
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Failed to load groups. Please try again.'));
  });

  it('performs a search and calls filtered API', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));
    fireEvent.change(screen.getByPlaceholderText('Search by name...'), {
      target: { value: 'Alpha' },
    });
    fireEvent.click(screen.getByText('🔍'));
    expect(axios.get).toHaveBeenCalledWith('/api/groups?search=Alpha', expect.anything());
  });

  it('clears search and reloads all data', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));
    fireEvent.click(screen.getByText('✖'));
    expect(axios.get).toHaveBeenCalledWith('/api/groups', expect.anything());
  });

  it('opens modal on edit and populates data', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));
    fireEvent.click(screen.getAllByText('Edit')[0]);
    expect(document.querySelector('#editGroupModal')).toBeInTheDocument();
  });

  it('calls delete API on delete click', async () => {
    global.confirm = () => true;
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));
    fireEvent.click(screen.getAllByText('Delete')[0]);
    await waitFor(() =>
      expect(axios.delete).toHaveBeenCalledWith('/api/groups/1', expect.anything())
    );
  });

  it('adds new group and hides modal', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Explore All Groups'));

    fireEvent.click(screen.getByText('➕ Add Group'));
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'New Group' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'New Desc' } });
    fireEvent.change(screen.getByLabelText('Visibility'), { target: { value: 'PUBLIC' } });
    fireEvent.click(screen.getByText('Save'));

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        '/api/groups',
        expect.objectContaining({
          name: 'New Group',
          description: 'New Desc',
          visibility: 'PUBLIC',
        }),
        expect.anything()
      );
    });
  });

  it('displays empty message when no groups exist', async () => {
    axios.get.mockResolvedValueOnce({ data: [] });
    render(<TrackNestPage />);
    await waitFor(() => {
      expect(screen.getByText('No groups available.')).toBeInTheDocument();
    });
  });

  it('handles sort by name toggle', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));
  
    const table = screen.getByRole('table');
    const nameHeader = within(table).getByText('Name', { exact: false });
  
    fireEvent.click(nameHeader);
    expect(nameHeader.textContent).toContain('▲');
  
    fireEvent.click(nameHeader);
    expect(nameHeader.textContent).toContain('▼');
  });
  

  it('selects and bulk deletes items', async () => {
    global.confirm = () => true;
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));

    // Select item
    fireEvent.click(screen.getAllByRole('checkbox')[1]); // first checkbox
    fireEvent.click(screen.getByText(/Delete Selected/));

    await waitFor(() => {
      expect(axios.delete).toHaveBeenCalled();
    });
  });

  it('displays undo message after single delete', async () => {
    global.confirm = () => true;
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));

    fireEvent.click(screen.getAllByText('Delete')[0]);

    await waitFor(() => {
      expect(screen.getByText(/deleted/)).toBeInTheDocument();
    });
  });

  it('undoes single delete', async () => {
    global.confirm = () => true;
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));
    fireEvent.click(screen.getAllByText('Delete')[0]);

    await waitFor(() => {
      const undoButton = screen.getByText('Undo');
      fireEvent.click(undoButton);
      expect(axios.post).toHaveBeenCalledWith('/api/groups', expect.anything(), expect.anything());
    });
  });

  it('displays correct pagination summary', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Explore All Groups'));
    expect(screen.getByText('Showing rows 1 - 2 of 2')).toBeInTheDocument();
  });

  it('submits edited group using PUT', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));
  
    fireEvent.click(screen.getAllByText('Edit')[0]);
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'Updated Alpha' } });
  
    axios.put.mockResolvedValueOnce({});
    fireEvent.click(screen.getByText('Save'));
  
    await waitFor(() =>
      expect(axios.put).toHaveBeenCalledWith(
        '/api/groups/1',
        expect.objectContaining({ name: 'Updated Alpha' }),
        expect.anything()
      )
    );
  });

  it('undoes bulk delete by reposting all deleted groups', async () => {
    global.confirm = () => true;
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));
  
    fireEvent.click(screen.getAllByRole('checkbox')[1]);
    fireEvent.click(screen.getByText(/Delete Selected/));
  
    const undoButton = await screen.findByText('Undo');
    fireEvent.click(undoButton);
  
    await waitFor(() =>
      expect(axios.post).toHaveBeenCalledWith('/api/groups', expect.objectContaining({ name: 'Alpha Group' }), expect.anything())
    );
  });

  it('navigates pages using › and »', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));
  
    fireEvent.click(screen.getByText('›'));
    fireEvent.click(screen.getByText('»'));
    expect(screen.getByText('Showing rows 1 - 2 of 2')).toBeInTheDocument(); // still same because of 2 entries
  });
  it('clears error message when close button is clicked', async () => {
    axios.get.mockRejectedValueOnce(new Error('Network Error'));
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText(/Failed to load/));
  
    const closeButton = document.querySelector('.btn-close');
    fireEvent.click(closeButton);
  
    expect(screen.queryByText(/Failed to load/)).not.toBeInTheDocument();
  });

  it('handles visibility column sorting', async () => {
    render(<TrackNestPage />);
    await waitFor(() => screen.getByText('Alpha Group'));
  
    const headers = screen.getAllByText('Visibility');
    const tableHeader = headers.find(el => el.tagName === 'TH') || headers[0];
  
    fireEvent.click(tableHeader);
    await waitFor(() => expect(tableHeader.textContent).toMatch(/▲|▼/));
  
    fireEvent.click(tableHeader);
    await waitFor(() => expect(tableHeader.textContent).toMatch(/▲|▼/));
  });
  
  
  
  
  
  
  
});
