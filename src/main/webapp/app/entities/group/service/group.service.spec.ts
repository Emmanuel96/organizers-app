import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IGroup } from '../group.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../group.test-samples';

import { GroupService } from './group.service';

const requireRestSample: IGroup = {
  ...sampleWithRequiredData,
};

describe('Group Service', () => {
  let service: GroupService;
  let httpMock: HttpTestingController;
  let expectedResult: IGroup | IGroup[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(GroupService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Group', () => {
      const group = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(group).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Group', () => {
      const group = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(group).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Group', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Group', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Group', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addGroupToCollectionIfMissing', () => {
      it('should add a Group to an empty array', () => {
        const group: IGroup = sampleWithRequiredData;
        expectedResult = service.addGroupToCollectionIfMissing([], group);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(group);
      });

      it('should not add a Group to an array that contains it', () => {
        const group: IGroup = sampleWithRequiredData;
        const groupCollection: IGroup[] = [
          {
            ...group,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addGroupToCollectionIfMissing(groupCollection, group);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Group to an array that doesn't contain it", () => {
        const group: IGroup = sampleWithRequiredData;
        const groupCollection: IGroup[] = [sampleWithPartialData];
        expectedResult = service.addGroupToCollectionIfMissing(groupCollection, group);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(group);
      });

      it('should add only unique Group to an array', () => {
        const groupArray: IGroup[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const groupCollection: IGroup[] = [sampleWithRequiredData];
        expectedResult = service.addGroupToCollectionIfMissing(groupCollection, ...groupArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const group: IGroup = sampleWithRequiredData;
        const group2: IGroup = sampleWithPartialData;
        expectedResult = service.addGroupToCollectionIfMissing([], group, group2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(group);
        expect(expectedResult).toContain(group2);
      });

      it('should accept null and undefined values', () => {
        const group: IGroup = sampleWithRequiredData;
        expectedResult = service.addGroupToCollectionIfMissing([], null, group, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(group);
      });

      it('should return initial array if no Group is added', () => {
        const groupCollection: IGroup[] = [sampleWithRequiredData];
        expectedResult = service.addGroupToCollectionIfMissing(groupCollection, undefined, null);
        expect(expectedResult).toEqual(groupCollection);
      });
    });

    describe('compareGroup', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareGroup(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareGroup(entity1, entity2);
        const compareResult2 = service.compareGroup(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareGroup(entity1, entity2);
        const compareResult2 = service.compareGroup(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareGroup(entity1, entity2);
        const compareResult2 = service.compareGroup(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
