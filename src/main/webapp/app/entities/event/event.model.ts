import dayjs from 'dayjs/esm';

export interface IEvent {
  id: number;
  event_date?: dayjs.Dayjs | null;
  event_location?: string | null;
  event_description?: string | null;
  eventGroupName?: string | null;
  event_url?: string | null;
  event_group_display_name?: string | null;
  eventGroupDisplayName?: string | null;
  eventTitle?: string | null;
  eventbriteOrganizerId?: string | null;
}

export type NewEvent = Omit<IEvent, 'id'> & { id: null };
