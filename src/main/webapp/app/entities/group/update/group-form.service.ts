import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IGroup, NewGroup } from '../group.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IGroup for edit and NewGroupFormGroupInput for create.
 */
type GroupFormGroupInput = IGroup | PartialWithRequiredKeyOf<NewGroup>;

type GroupFormDefaults = Pick<NewGroup, 'id'>;

type GroupFormGroupContent = {
  id: FormControl<IGroup['id'] | NewGroup['id']>;
  name: FormControl<IGroup['name']>;
  organizerId: FormControl<IGroup['organizerId']>;
  eventSource: FormControl<IGroup['eventSource']>;
  eventSourceUrl: FormControl<IGroup['eventSourceUrl']>;
};

export type GroupFormGroup = FormGroup<GroupFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class GroupFormService {
  createGroupFormGroup(group: GroupFormGroupInput = { id: null }): GroupFormGroup {
    const groupRawValue = {
      ...this.getFormDefaults(),
      ...group,
    };
    return new FormGroup<GroupFormGroupContent>({
      id: new FormControl(
        { value: groupRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(groupRawValue.name),
      organizerId: new FormControl(groupRawValue.organizerId),
      eventSource: new FormControl(groupRawValue.eventSource ?? 'MEET_UP', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      eventSourceUrl: new FormControl(groupRawValue.eventSourceUrl, {
        validators: [],
      }),
    });
  }

  getGroup(form: GroupFormGroup): IGroup | NewGroup {
    return form.getRawValue() as IGroup | NewGroup;
  }

  resetForm(form: GroupFormGroup, group: GroupFormGroupInput): void {
    const groupRawValue = { ...this.getFormDefaults(), ...group };
    form.reset({
      ...groupRawValue,
      id: { value: groupRawValue.id, disabled: true },
      eventSource: groupRawValue.eventSource ?? 'MEET_UP',
      eventSourceUrl: groupRawValue.eventSourceUrl,
    } as any);
  }

  private getFormDefaults(): GroupFormDefaults & Pick<IGroup, 'eventSource' | 'organizerId'> {
    return {
      id: null,
      eventSource: 'MEET_UP',
      organizerId: null,
    };
  }
}
