import dayjs from 'dayjs/esm';

import { IEvent, NewEvent } from './event.model';

export const sampleWithRequiredData: IEvent = {
  id: 18832,
};

export const sampleWithPartialData: IEvent = {
  id: 649,
  event_date: dayjs('2024-09-28T05:48'),
  event_group_name: 'triumphantly inasmuch',
};

export const sampleWithFullData: IEvent = {
  id: 28058,
  event_date: dayjs('2024-09-28T18:15'),
  event_location: 'lock',
  event_description: 'hydrolyze',
  event_group_name: 'mostly baggy',
};

export const sampleWithNewData: NewEvent = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
