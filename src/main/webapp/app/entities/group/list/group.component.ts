import { Component, NgZone, OnInit, inject } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, forkJoin, map, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { DurationPipe, FormatMediumDatePipe, FormatMediumDatetimePipe } from 'app/shared/date';
import { ItemCountComponent } from 'app/shared/pagination';
import { FormsModule } from '@angular/forms';

import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { IGroup } from '../group.model';
import { EntityArrayResponseType, GroupService } from '../service/group.service';
import { GroupDeleteDialogComponent } from '../delete/group-delete-dialog.component';

@Component({
  standalone: true,
  selector: 'jhi-group',
  templateUrl: './group.component.html',
  imports: [
    RouterModule,
    FormsModule,
    SharedModule,
    SortDirective,
    SortByDirective,
    DurationPipe,
    FormatMediumDatetimePipe,
    FormatMediumDatePipe,
    ItemCountComponent,
  ],
})
export class GroupComponent implements OnInit {
  subscription: Subscription | null = null;
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

  trackId = (item: IGroup): number => this.groupService.getGroupIdentifier(item);

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.load()),
      )
      .subscribe();
  }

  delete(group: IGroup): void {
    const modalRef = this.modalService.open(GroupDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.group = group;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(this.page, event);
  }

  navigateToPage(page: number): void {
    this.handleNavigation(page, this.sortState());
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page = +(page ?? 1);
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.groups = dataFromBody;

    // eslint-disable-next-line no-console
    console.info('Groups:', this.groups);
  }

  protected fillComponentAttributesFromResponseBody(data: IGroup[] | null): IGroup[] {
    return data ?? [];
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems = Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER));
  }

  // protected queryBackend(): Observable<EntityArrayResponseType> {
  //   const { page } = this;

  //   this.isLoading = true;
  //   const pageToLoad: number = page;
  //   const queryObject: any = {
  //     page: pageToLoad - 1,
  //     size: this.itemsPerPage,
  //     sort: this.sortService.buildSortParam(this.sortState()),
  //   };
  //   return this.groupService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  // }

  // Adjust the type according to your actual response type
  protected queryBackend(): Observable<EntityArrayResponseType> {
    const { page } = this;
    this.isLoading = true;
    const pageToLoad: number = page;
    const queryObject: any = {
      page: pageToLoad - 1,
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
      tap(() => (this.isLoading = false)),
    );
  }

  // @typescript-eslint/member-ordering
  protected toggleFollow(group: IGroup): void {
    // Toggle the excluded flag
    group.excluded = !group.excluded;
    // Call the service to persist the change
    this.groupService.updateFollowStatus(group.id, group.excluded).subscribe({
      next() {
        // Optionally update UI or show a toast message
      },
      error() {
        // If the update fails, you might want to revert the toggle in the UI.
        group.excluded = !group.excluded;
      },
    });
  }

  protected handleNavigation(page: number, sortState: SortState): void {
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
}
