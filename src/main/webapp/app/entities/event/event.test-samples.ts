import dayjs from 'dayjs/esm';

import { IEvent, NewEvent } from './event.model';

export const sampleWithRequiredData: IEvent = {
  id: 22719,
};

export const sampleWithPartialData: IEvent = {
  id: 10775,
  event_description: 'ew tiny absentmindedly',
  event_group_name: 'unless muscat',
};

export const sampleWithFullData: IEvent = {
  id: 23474,
  event_date: dayjs('2024-09-28T16:48'),
  event_location: 'unless bidet over',
  event_description: 'cleaner hourly',
  event_group_name: 'where',
};

export const sampleWithNewData: NewEvent = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
