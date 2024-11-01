import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { DurationPipe, FormatMediumDatePipe, FormatMediumDatetimePipe } from 'app/shared/date';
import { IGroup } from '../group.model';

@Component({
  standalone: true,
  selector: 'jhi-group-detail',
  templateUrl: './group-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class GroupDetailComponent {
  group = input<IGroup | null>(null);

  previousState(): void {
    window.history.back();
  }
}
