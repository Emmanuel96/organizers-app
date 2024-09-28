import dayjs from 'dayjs/esm';

export interface IEvent {
  id: number;
  event_date?: dayjs.Dayjs | null;
  event_location?: string | null;
  event_description?: string | null;
  event_group_name?: string | null;
}

export type NewEvent = Omit<IEvent, 'id'> & { id: null };
