export interface IGroup {
  excluded?: boolean;
  id: number;
  name?: string | null;
  meetup_group_name?: string | null;
}

export type NewGroup = Omit<IGroup, 'id'> & { id: null };
