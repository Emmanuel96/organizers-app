import { IUser } from './user.model';

export const sampleWithRequiredData: IUser = {
  id: 10306,
  login: 'Z',
};

export const sampleWithPartialData: IUser = {
  id: 7867,
  login: 'm5e2q',
};

export const sampleWithFullData: IUser = {
  id: 21710,
  login: 'y',
};
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
