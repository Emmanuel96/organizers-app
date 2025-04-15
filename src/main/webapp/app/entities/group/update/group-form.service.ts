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
  meetup_group_name: FormControl<IGroup['meetup_group_name']>;
  eventSource: FormControl<IGroup['eventSource']>;
  eventbriteOrganizerId: FormControl<IGroup['eventbriteOrganizerId']>;
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
      meetup_group_name: new FormControl(groupRawValue.meetup_group_name),
      eventSource: new FormControl(groupRawValue.eventSource ?? 'MEET_UP', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      eventbriteOrganizerId: new FormControl(groupRawValue.eventbriteOrganizerId),
    });
  }

  getGroup(form: GroupFormGroup): IGroup | NewGroup {
    const raw = form.getRawValue() as IGroup | NewGroup;
    if (raw.eventSource === 'MEET_UP') {
      raw.eventbriteOrganizerId = null;
    } else if (raw.eventSource === 'EVENTBRITE') {
      raw.meetup_group_name = null;
    }
    return raw;
  }

  resetForm(form: GroupFormGroup, group: GroupFormGroupInput): void {
    const groupRawValue = { ...this.getFormDefaults(), ...group };
    form.reset({
      ...groupRawValue,
      id: { value: groupRawValue.id, disabled: true },
      eventSource: groupRawValue.eventSource ?? 'MEET_UP',
    } as any);
  }

  private getFormDefaults(): GroupFormDefaults & Pick<IGroup, 'eventSource' | 'meetup_group_name' | 'eventbriteOrganizerId'> {
    return {
      id: null,
      eventSource: 'MEET_UP',
      meetup_group_name: null,
      eventbriteOrganizerId: null,
    };
  }
}
