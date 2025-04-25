export interface IGroup {
  excluded?: boolean;
  id: number;
  name?: string | null;
  organizerId?: string | null;
  eventSource?: 'MEET_UP' | 'EVENTBRITE';
}

export type NewGroup = Omit<IGroup, 'id'> & { id: null };
