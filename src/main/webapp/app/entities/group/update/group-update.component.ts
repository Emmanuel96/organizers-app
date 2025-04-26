import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IGroup } from '../group.model';
import { GroupService } from '../service/group.service';
import { GroupFormGroup, GroupFormService } from './group-form.service';

@Component({
  standalone: true,
  selector: 'jhi-group-update',
  templateUrl: './group-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class GroupUpdateComponent implements OnInit {
  isSaving = false;
  group: IGroup | null = null;
  checkingUrl = false;

  protected groupService = inject(GroupService);
  protected groupFormService = inject(GroupFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: GroupFormGroup = this.groupFormService.createGroupFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ group }) => {
      this.group = group;
      if (group) {
        this.updateForm(group);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const group = this.groupFormService.getGroup(this.editForm);
    if (group.id !== null) {
      this.subscribeToSaveResponse(this.groupService.update(group));
    } else {
      this.subscribeToSaveResponse(this.groupService.create(group));
    }
  }

  checkEventSourceUrl(): void {
    const url = this.editForm.get('eventSourceUrl')?.value;
    if (typeof url !== 'string' || !url.trim()) {
      alert('Please enter a valid URL.');
      return;
    }

    this.checkingUrl = true;
    this.groupService
      .checkUrl(url)
      .pipe(finalize(() => (this.checkingUrl = false)))
      .subscribe({
        next: (group: IGroup) => {
          // this.updateForm will reset all controls based on your GroupFormService logic
          this.updateForm(group);
        },
      });
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IGroup>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(group: IGroup): void {
    this.group = group;
    this.groupFormService.resetForm(this.editForm, group);
  }
}
