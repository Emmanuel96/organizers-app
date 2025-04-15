export interface IGroup {
  excluded?: boolean;
  id: number;
  name?: string | null;
  meetup_group_name?: string | null;
  eventSource?: 'MEET_UP' | 'EVENTBRITE';
  eventbriteOrganizerId?: string | null;
}

export type NewGroup = Omit<IGroup, 'id'> & { id: null };
