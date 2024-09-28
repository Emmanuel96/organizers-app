import { IAuthority, NewAuthority } from './authority.model';

export const sampleWithRequiredData: IAuthority = {
  name: '07c765e6-51a3-40c6-adf6-4838c830d0f2',
};

export const sampleWithPartialData: IAuthority = {
  name: '21410a2a-656e-4a4e-9347-2b6dcde94bb6',
};

export const sampleWithFullData: IAuthority = {
  name: '3fb62d29-2d27-4ee0-9bb0-6ad78b1a0df7',
};

export const sampleWithNewData: NewAuthority = {
  name: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
