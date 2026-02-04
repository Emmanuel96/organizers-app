import { ChangeDetectorRef, Component, inject, NgZone, NO_ERRORS_SCHEMA, OnInit } from '@angular/core';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { combineLatest, debounceTime, filter, forkJoin, map, Observable, Subject, switchMap, take, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { ItemCountComponent } from 'app/shared/pagination';
import { FormsModule } from '@angular/forms';

import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { IGroup } from '../group.model';
import { EntityArrayResponseType, GroupService } from '../service/group.service';
import { GroupDeleteDialogComponent } from '../delete/group-delete-dialog.component';
import { UnsubscribeHook } from '../../../shared/hooks/unsubscribe.hook';
import { takeUntil } from 'rxjs/operators';

@Component({
  standalone: true,
  selector: 'jhi-group',
  styleUrls: ['./group.component.scss'],
  templateUrl: './group.component.html',
  schemas: [NO_ERRORS_SCHEMA],
  imports: [RouterModule, FormsModule, SharedModule, SortDirective, SortByDirective, ItemCountComponent],
})
export class GroupComponent extends UnsubscribeHook implements OnInit {
  groups?: IGroup[];
  isLoading = false;
  sortState = sortStateSignal({});
  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;

  public router = inject(Router);
  protected groupService = inject(GroupService);
  protected activatedRoute = inject(ActivatedRoute);
  protected sortService = inject(SortService);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);
  protected cd = inject(ChangeDetectorRef);

  protected readonly adminRole: string = 'ROLE_ADMIN';
  private subject$ = new Subject<void>();

  trackId = (item: IGroup): number => this.groupService.getGroupIdentifier(item);

  ngOnInit(): void {
    this.initRefresh();
    this.getInitDataFromRoute();
  }

  refresh = (): void => {
    this.subject$.next();
  };

  delete(group: IGroup): void {
    const modalRef = this.modalService.open(GroupDeleteDialogComponent, { size: 'lg', backdrop: 'static' });

    modalRef.componentInstance.group = group;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        take(1),
      )
      .subscribe(this.refresh);
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(this.page, event);
  }

  navigateToPage(page: number): void {
    this.handleNavigation(page, this.sortState());
  }

  // @typescript-eslint/member-ordering
  protected toggleFollow(group: IGroup): void {
    // Toggle the excluded flag
    group.excluded = !group.excluded;
    // Call the service to persist the change
    this.groupService
      .updateFollowStatus(group.id, group.excluded)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next() {
          // Optionally update UI or show a toast message
        },
        error() {
          // If the update fails, you might want to revert the toggle in the UI.
          group.excluded = !group.excluded;
        },
      });
  }

  private initRefresh(): void {
    this.subject$.pipe(takeUntil(this.destroy$), debounceTime(500), switchMap(this.queryBackend)).subscribe();
  }

  private onResponseSuccess = (response: EntityArrayResponseType): void => {
    this.totalItems = Number(response.headers.get(TOTAL_COUNT_RESPONSE_HEADER));
    this.groups = response.body ?? [];

    // eslint-disable-next-line no-console
    console.info('Groups:', this.groups);
  };

  // Adjust the type according to your actual response type
  private queryBackend = (): Observable<EntityArrayResponseType> => {
    this.isLoading = true;
    this.cd.detectChanges();

    const queryObject: any = {
      page: this.page - 1,
      size: this.itemsPerPage,
      sort: this.sortService.buildSortParam(this.sortState()),
    };

    return forkJoin({
      groupsResponse: this.groupService.query(queryObject),
      excludedGroups: this.groupService.getExcludedGroups(),
    }).pipe(
      map(({ groupsResponse, excludedGroups }) => {
        // Assuming groupsResponse.body is the array of groups
        const excludedIds = new Set<number>();

        if (excludedGroups.body) {
          for (const group of excludedGroups.body) {
            excludedIds.add(group.id);
          }
        }

        groupsResponse.body?.forEach((group: any) => {
          // Add a new property to indicate if the group is excluded
          group.excluded = excludedIds.has(group.id);
        });

        return groupsResponse;
      }),
      tap(() => {
        this.isLoading = false;
        this.cd.detectChanges();
      }),
      tap(this.onResponseSuccess),
    );
  };

  private handleNavigation(page: number, sortState: SortState): void {
    const queryParamsObj = {
      page,
      size: this.itemsPerPage,
      sort: this.sortService.buildSortParam(sortState),
    };

    this.ngZone.run(() => {
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams: queryParamsObj,
      });
    });
  }

  private getInitDataFromRoute(): void {
    combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        takeUntil(this.destroy$),
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
      )
      .subscribe(this.refresh);
  }

  private fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page = +(page ?? 1);
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
  }
}
