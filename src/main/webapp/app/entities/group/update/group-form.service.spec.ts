import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../group.test-samples';

import { GroupFormService } from './group-form.service';

describe('Group Form Service', () => {
  let service: GroupFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GroupFormService);
  });

  describe('Service methods', () => {
    describe('createGroupFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createGroupFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            organizerId: expect.any(Object),
          }),
        );
      });

      it('passing IGroup should create a new form with FormGroup', () => {
        const formGroup = service.createGroupFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            organizerId: expect.any(Object),
          }),
        );
      });
    });

    describe('getGroup', () => {
      it('should return NewGroup for default Group initial value', () => {
        const formGroup = service.createGroupFormGroup(sampleWithNewData);

        const group = service.getGroup(formGroup) as any;

        expect(group).toMatchObject(sampleWithNewData);
      });

      it('should return NewGroup for empty Group initial value', () => {
        const formGroup = service.createGroupFormGroup();

        const group = service.getGroup(formGroup) as any;

        expect(group).toMatchObject({});
      });

      it('should return IGroup', () => {
        const formGroup = service.createGroupFormGroup(sampleWithRequiredData);

        const group = service.getGroup(formGroup) as any;

        expect(group).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IGroup should not enable id FormControl', () => {
        const formGroup = service.createGroupFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewGroup should disable id FormControl', () => {
        const formGroup = service.createGroupFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
