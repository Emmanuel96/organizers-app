import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IGroup, NewGroup } from '../group.model';

export type PartialUpdateGroup = Partial<IGroup> & Pick<IGroup, 'id'>;

export type EntityResponseType = HttpResponse<IGroup>;
export type EntityArrayResponseType = HttpResponse<IGroup[]>;

@Injectable({ providedIn: 'root' })
export class GroupService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/groups');

  create(group: NewGroup): Observable<EntityResponseType> {
    return this.http.post<IGroup>(this.resourceUrl, group, { observe: 'response' });
  }

  update(group: IGroup): Observable<EntityResponseType> {
    return this.http.put<IGroup>(`${this.resourceUrl}/${this.getGroupIdentifier(group)}`, group, { observe: 'response' });
  }

  partialUpdate(group: PartialUpdateGroup): Observable<EntityResponseType> {
    return this.http.patch<IGroup>(`${this.resourceUrl}/${this.getGroupIdentifier(group)}`, group, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IGroup>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IGroup[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getGroupIdentifier(group: Pick<IGroup, 'id'>): number {
    return group.id;
  }

  compareGroup(o1: Pick<IGroup, 'id'> | null, o2: Pick<IGroup, 'id'> | null): boolean {
    return o1 && o2 ? this.getGroupIdentifier(o1) === this.getGroupIdentifier(o2) : o1 === o2;
  }

  addGroupToCollectionIfMissing<Type extends Pick<IGroup, 'id'>>(
    groupCollection: Type[],
    ...groupsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const groups: Type[] = groupsToCheck.filter(isPresent);
    if (groups.length > 0) {
      const groupCollectionIdentifiers = groupCollection.map(groupItem => this.getGroupIdentifier(groupItem));
      const groupsToAdd = groups.filter(groupItem => {
        const groupIdentifier = this.getGroupIdentifier(groupItem);
        if (groupCollectionIdentifiers.includes(groupIdentifier)) {
          return false;
        }
        groupCollectionIdentifiers.push(groupIdentifier);
        return true;
      });
      return [...groupsToAdd, ...groupCollection];
    }
    return groupCollection;
  }
}
