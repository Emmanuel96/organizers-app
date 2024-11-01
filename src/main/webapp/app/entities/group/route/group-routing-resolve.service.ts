import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IGroup } from '../group.model';
import { GroupService } from '../service/group.service';

const groupResolve = (route: ActivatedRouteSnapshot): Observable<null | IGroup> => {
  const id = route.params.id;
  if (id) {
    return inject(GroupService)
      .find(id)
      .pipe(
        mergeMap((group: HttpResponse<IGroup>) => {
          if (group.body) {
            return of(group.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default groupResolve;
