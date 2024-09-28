import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IEvent } from '../event.model';
import { EventService } from '../service/event.service';

const eventResolve = (route: ActivatedRouteSnapshot): Observable<null | IEvent> => {
  const id = route.params.id;
  if (id) {
    return inject(EventService)
      .find(id)
      .pipe(
        mergeMap((event: HttpResponse<IEvent>) => {
          if (event.body) {
            return of(event.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default eventResolve;
