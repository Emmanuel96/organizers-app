import { IGroup, NewGroup } from './group.model';

export const sampleWithRequiredData: IGroup = {
  id: 21970,
};

export const sampleWithPartialData: IGroup = {
  id: 23261,
  name: 'unbalance poor little',
  meetup_group_name: 'lest fairly',
};

export const sampleWithFullData: IGroup = {
  id: 32281,
  name: 'so',
  meetup_group_name: 'offset',
};

export const sampleWithNewData: NewGroup = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
