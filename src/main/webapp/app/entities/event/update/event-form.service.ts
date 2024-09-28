import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IEvent, NewEvent } from '../event.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IEvent for edit and NewEventFormGroupInput for create.
 */
type EventFormGroupInput = IEvent | PartialWithRequiredKeyOf<NewEvent>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IEvent | NewEvent> = Omit<T, 'event_date'> & {
  event_date?: string | null;
};

type EventFormRawValue = FormValueOf<IEvent>;

type NewEventFormRawValue = FormValueOf<NewEvent>;

type EventFormDefaults = Pick<NewEvent, 'id' | 'event_date'>;

type EventFormGroupContent = {
  id: FormControl<EventFormRawValue['id'] | NewEvent['id']>;
  event_date: FormControl<EventFormRawValue['event_date']>;
  event_location: FormControl<EventFormRawValue['event_location']>;
  event_description: FormControl<EventFormRawValue['event_description']>;
  event_group_name: FormControl<EventFormRawValue['event_group_name']>;
};

export type EventFormGroup = FormGroup<EventFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class EventFormService {
  createEventFormGroup(event: EventFormGroupInput = { id: null }): EventFormGroup {
    const eventRawValue = this.convertEventToEventRawValue({
      ...this.getFormDefaults(),
      ...event,
    });
    return new FormGroup<EventFormGroupContent>({
      id: new FormControl(
        { value: eventRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      event_date: new FormControl(eventRawValue.event_date),
      event_location: new FormControl(eventRawValue.event_location),
      event_description: new FormControl(eventRawValue.event_description),
      event_group_name: new FormControl(eventRawValue.event_group_name),
    });
  }

  getEvent(form: EventFormGroup): IEvent | NewEvent {
    return this.convertEventRawValueToEvent(form.getRawValue() as EventFormRawValue | NewEventFormRawValue);
  }

  resetForm(form: EventFormGroup, event: EventFormGroupInput): void {
    const eventRawValue = this.convertEventToEventRawValue({ ...this.getFormDefaults(), ...event });
    form.reset(
      {
        ...eventRawValue,
        id: { value: eventRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): EventFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      event_date: currentTime,
    };
  }

  private convertEventRawValueToEvent(rawEvent: EventFormRawValue | NewEventFormRawValue): IEvent | NewEvent {
    return {
      ...rawEvent,
      event_date: dayjs(rawEvent.event_date, DATE_TIME_FORMAT),
    };
  }

  private convertEventToEventRawValue(
    event: IEvent | (Partial<NewEvent> & EventFormDefaults),
  ): EventFormRawValue | PartialWithRequiredKeyOf<NewEventFormRawValue> {
    return {
      ...event,
      event_date: event.event_date ? event.event_date.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
