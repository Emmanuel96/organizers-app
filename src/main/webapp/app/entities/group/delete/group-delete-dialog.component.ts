import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IGroup } from '../group.model';
import { GroupService } from '../service/group.service';

@Component({
  standalone: true,
  templateUrl: './group-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class GroupDeleteDialogComponent {
  group?: IGroup;

  protected groupService = inject(GroupService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.groupService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
