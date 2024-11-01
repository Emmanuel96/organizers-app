import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import GroupResolve from './route/group-routing-resolve.service';

const groupRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/group.component').then(m => m.GroupComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/group-detail.component').then(m => m.GroupDetailComponent),
    resolve: {
      group: GroupResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/group-update.component').then(m => m.GroupUpdateComponent),
    resolve: {
      group: GroupResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/group-update.component').then(m => m.GroupUpdateComponent),
    resolve: {
      group: GroupResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default groupRoute;
