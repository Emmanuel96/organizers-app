import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IEvent } from '../event.model';
import { EventService } from '../service/event.service';

@Component({
  standalone: true,
  templateUrl: './event-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class EventDeleteDialogComponent {
  event?: IEvent;

  protected eventService = inject(EventService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.eventService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
